package com.wolfmobileapps.recordergps;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.List;
import static com.wolfmobileapps.recordergps.MainActivity.NAME_OF_EXTRA_DATA_TO_INTENT_MAP;
import static com.wolfmobileapps.recordergps.SettingsActivity.KEY_FOR_SHARED_PREF_SWITCH;
import static com.wolfmobileapps.recordergps.TrackCountServoce.NAME_OF_SHARED_PREFERNECES;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "MapsActivity";

    // do pokazania aktualnej lokalizacji
    LocationManager locationManager;

    // mapa na której jest wyświrtlane
    private GoogleMap mMap;

    // nazwa Db
    long nameLong;

    // lista z danymi gps
    List<LatLng> listLatLng;

    //marker z aktualną lokalizacją
    Marker curentMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // pobranie extra danych czyli nazwy bazy danych z intent
        Intent intent = getIntent();
        if (intent.hasExtra(NAME_OF_EXTRA_DATA_TO_INTENT_MAP)) {
            nameLong = intent.getLongExtra(NAME_OF_EXTRA_DATA_TO_INTENT_MAP, 0);
        }
        Log.d(TAG, "extra in intent is : " + nameLong);

        // pobiera listę punktów GPS z bazy danych z nazwą nameLong
        TrackCountServoce trackCountServoce = new TrackCountServoce();
        listLatLng = trackCountServoce.getListFromDbOfMapPoins(MapsActivity.this, nameLong);

        //instancja location managera
        instantionOfLocationManager();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // instancja mapy
        mMap = googleMap;

        //zabezpieczeni przed pustą listą
        if (listLatLng.size() == 0) {
            Log.d(TAG, "onMapReady: return!!! list is empty");
            return;
        }

        // pobranie z listy pierwszego i ostaniego miejsca i ustawienie markera z opisem
        LatLng startLatLng = listLatLng.get(0);
        String startName = "start";
        LatLng stopLatLng = listLatLng.get(listLatLng.size() - 1);
        String stopName = "stop";
        mMap.addMarker(new MarkerOptions().position(startLatLng).title(startName)).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(stopLatLng).title(stopName)).showInfoWindow();

        //ustawienie kamery aby było widać początek i koniec polylina z listy
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startLatLng);
        builder.include(stopLatLng);

        // ustawienie kamery wg ostatniego punktu
        int zoom = 15; // większy zoom dla walk mode
        // jeśli jest włączony car mode to zmienia dane
        SharedPreferences shar = this.getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_SWITCH,false)){
            zoom = 8; // mniejszy zoom dla drive mode
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stopLatLng,zoom));

        //ustawienie polyline na mapie
        PolylineOptions polylineOptions = new PolylineOptions().width(5).color(Color.RED).geodesic(true);
        polylineOptions.addAll(listLatLng);
        mMap.addPolyline(polylineOptions);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // stop of locationManager and updates
        locationManager.removeUpdates(this);
    }

    // metody do wyświetlania aktualnej lokalizacji
    @Override
    public void onLocationChanged(Location location) {
        if (curentMarker!=null){
            curentMarker.remove();
        }
        if (mMap==null){
            return;
        }
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        LatLng curentPosition = new LatLng(lat,lng);
        curentMarker = mMap.addMarker(new MarkerOptions().position(curentPosition).title(getResources().getString(R.string.you_are_here)));
        curentMarker.showInfoWindow();
        Log.d(TAG, "onLocationChanged: now");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void instantionOfLocationManager(){
        // instancja locationManagera
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        // sprawdzenie czy są nadame permissions - musi byc bo błąd pokazuje
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // ustawienie locationMangera żeby słuchał - time i distance można zmienić
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
    }
}
