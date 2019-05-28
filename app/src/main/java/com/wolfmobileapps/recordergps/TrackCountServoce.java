package com.wolfmobileapps.recordergps;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.wolfmobileapps.recordergps.data.MainMapPoinDatabase;
import com.wolfmobileapps.recordergps.data.MainMapPoint;
import com.wolfmobileapps.recordergps.data.MapPoinDatabase;
import com.wolfmobileapps.recordergps.data.MapPoint;

import java.util.ArrayList;
import java.util.List;

import static com.wolfmobileapps.recordergps.MainActivity.CHANNEL_ID;
import static com.wolfmobileapps.recordergps.SettingsActivity.KEY_FOR_SHARED_PREF_SWITCH;

public class TrackCountServoce extends Service implements LocationListener {

    private static final String TAG = "TrackCountServoce";


    public static final String NAME_OF_SHARED_PREFERNECES = "gpsRedorderSharPref";
    public static final String KEY_FOR_SHARED_PREF_TP_DB_NAME = "key to shared pref tp db name";
    public static final String KEY_FOR_SHARED_PREF_ANIMATE_FIRST_ITEM = "key to shared pref to animate first item";

    private Notification notification;
    private MapPoinDatabase db;
    private LocationManager locationManager;

    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    // konstruktor domyślny
    public TrackCountServoce() {
    }

    // musi być i nic nie robi
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // właczenie servisu
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // ustawienie notification
        setTheNotificationWithTapAction();

        // wystartowanie service
        startForeground(101, notification); //nadać unikalne Id

        //start locationManager i instancja db
        instantionOfLocationListener(this);

        // instancja locationManagera
        instantionOfLocationManager(this);


        return START_STICKY;
    }

    public void instantionOfLocationListener(Context context) {

        // zapisanie nazwy jako long do shared preferences
        long dbName = System.currentTimeMillis();
        shar = context.getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);
        editor = shar.edit(); //wywołany edytor do zmian
        editor.putLong(KEY_FOR_SHARED_PREF_TP_DB_NAME, dbName); // nadanie wartości
        editor.apply(); // musi być na końcu aby zapisać zmiany w shar
        Log.d(TAG, "instantionOfLocationListener check after shared: " + shar.getLong(KEY_FOR_SHARED_PREF_TP_DB_NAME, 9999));

        //instancja bazy danych Room z daną nazwą
        Log.d(TAG, "instantionOfLocationListener dbName:" + dbName);
        db = Room.databaseBuilder(context, MapPoinDatabase.class, "" + dbName)
                .build();
    }

    private void instantionOfLocationManager(Context context){

        // instancja locationManagera
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        // sprawdzenie czy są nadame permissions - musi byc bo błąd pokazuje
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // ustawienie locationMangera żeby słuchał - time i distance zmienia się w zależności od mode - tu Walk Mode
        int minTime = 10000;  //powinno być 10000
        int minDistance = 10; // powinno być 10
        // jeśli jest włączony car mode to zmienia dane
        shar = context.getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_SWITCH,false)){
            minTime = 60000;  //powinno być 60000
            minDistance = 100; // powinno być 100
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //stop location Manager
        stopOfCountingAndSaveInDbs(TrackCountServoce.this);

        // stop of locationManager and updates
        locationManager.removeUpdates(this);
    }

    public void setTheNotificationWithTapAction() {
        // jeśli jest włączony car mode to zmienia dane
        int icon = R.drawable.ic_directions_walk_black_24dp_walk;
        shar = this.getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_SWITCH,false)){
            icon = R.drawable.ic_directions_car_black_24dp_drive_mode;
        }

        // ustawienie pending intent które będzie otwierać MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Recording...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        notification = builder.build();
    }

    //metoda stworzona żeby wpisać do dbMain wynik trasy
    public void stopOfCountingAndSaveInDbs(Context context) {

        // aktualna nazwa db wyciągnięta z shared preferences
        shar = context.getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);
        long dbNameLong = shar.getLong(KEY_FOR_SHARED_PREF_TP_DB_NAME, 1111);
        Log.d(TAG, "stopOfCountingAndSaveInDbs dbNameLong: " + dbNameLong);

        //zmiana w shar na to aby dodane view było animowane
        editor = shar.edit(); //wywołany edytor do zmian
        editor.putBoolean(KEY_FOR_SHARED_PREF_ANIMATE_FIRST_ITEM,true);
        editor.apply();

        // wyliczenie czasu drogi w godzinach
        long timeNow = System.currentTimeMillis();
        long timeOfTrack = timeNow - dbNameLong;
        Log.d(TAG, "stopOfCountingAndSaveInDbs: timeoftrack" + timeOfTrack);

        // przebyty dystans z listy wzięty w metrach
        List<LatLng> listLatLng = getListFromDbOfMapPoins(context, dbNameLong);
        double distance = SphericalUtil.computeLength(listLatLng);
        Log.d(TAG, "distance: " + distance);

        // wyliczenieśredniej prędkości w km/h
        double time = timeOfTrack/(1000); // czas w sekundach
        double speedNotRounded = distance/time*3.6; //speed w km/h
        double speedMultiplayed = speedNotRounded*100;
        double speedRounded = Math.round(speedMultiplayed);
        double speed = speedRounded/100;

        // instancja dbMain
        final MainMapPoinDatabase dbMain = MainMapPoinDatabase.getInstance(context);

        //zapisanie danych do dbMain
        final MainMapPoint mp = new MainMapPoint(dbNameLong,timeOfTrack,distance,speed);
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbMain.userDao().insert(mp);
            }
        }).start();

    }

    public List<LatLng> getListFromDbOfMapPoins(Context context, long nameLong){

        // instancja bazy danych
        MapPoinDatabase dbToMap = Room.databaseBuilder(context, MapPoinDatabase.class, "" + nameLong)
                .allowMainThreadQueries()
                .build();

        //pobranie z bazy listy punktów
        List<MapPoint> listOfMapPoints = dbToMap.userDao().getAll();

        //wpisanie listy z db do listy poliline
        List<LatLng> listLatLng = new ArrayList<>();
        for (MapPoint mapPoint : listOfMapPoints){
            double lat = mapPoint.getLatitudePoint();
            double lng = mapPoint.getLongitudePoint();
            listLatLng.add(new LatLng(lat,lng));
            Log.d(TAG, "lat: " + lat + " lng: " + lng);
        }
        return listLatLng;
    }


    @Override
    public void onLocationChanged(Location location) {

        //przy każdej zmianie czasu lub/i dystansu z locationManagera wywołuje tą metodę i zapisuje w db z aktualnym name
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        final MapPoint mp = new MapPoint(lat,lng);
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.userDao().insertAll(mp);
            }
        }).start();
        Log.d(TAG, "onLocationChanged lat: " + location.getLatitude() + " lng: " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
    }
}
