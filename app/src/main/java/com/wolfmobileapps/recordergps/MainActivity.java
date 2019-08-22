package com.wolfmobileapps.recordergps;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.wolfmobileapps.recordergps.data.MainMapPoinArrayAdapter;
import com.wolfmobileapps.recordergps.data.MainMapPoinDatabase;
import com.wolfmobileapps.recordergps.data.MainMapPoint;
import com.wolfmobileapps.recordergps.data.MapPoinDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

import static com.wolfmobileapps.recordergps.SettingsActivity.KEY_FOR_SHARED_PREF_SWITCH;
import static com.wolfmobileapps.recordergps.TrackCountServoce.NAME_OF_SHARED_PREFERNECES;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // stała do permissions
    public static final int PERMISSION_ALL = 101;
    // nazwa głównej db
    public static final String MAIN_DATABASE_NAME = "main_database_name";
    //stała do wysyłanie intent do map
    public static final String NAME_OF_EXTRA_DATA_TO_INTENT_MAP = "extra data to map intent";
    // stała do notification chanel
    public static final String CHANNEL_ID = "Track counter";
    // key do start/stop wyświetlania
    public static final String KEY_FOR_SHARED_PREF_TO_VISIBILITY = "key to shared pref to visibility";
    // dos prawdzenia google plat\y service
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // stałe do reklam
    public static final String APP_ID = "ca-app-pub-1490567689734833~4589409756"; //zapisana w manifeście w metadata
    public static final String ADD_INTERSTITIAL_ID = "ca-app-pub-1490567689734833/7064884493"; //w start activity wpisane (testowe to: "ca-app-pub-3940256099942544/1033173712")
    public static final String ADD_BANNER_ID = "ca-app-pub-1490567689734833/5447075710"; //w XML wpisane (testowe to "ca-app-pub-3940256099942544/6300978111")


    private Button buttonStart;
    private Button buttonStop;
    private ListView listViewOfMapPoints;
    private TextView textViewRecording;
    private ProgressBar progressBar;
    private TextView textViewDescriptionIfListIsEmpty;
    private GifImageView gifImageView;
    private TextView textViewMode;
    private TextView textViewOpeningGoogleMaps;

    private MainMapPoinArrayAdapter adapter;

    // głowna baza danych
    private MainMapPoinDatabase dbMain;

    private SharedPreferences shar;
    private SharedPreferences.Editor editor;
    private String[] permissions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //zapytanie o permissions do GPS
        permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
        }

        // metoda tworząca notificationChanel
        createNotificationChannel();

        //instancja Views
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        listViewOfMapPoints = findViewById(R.id.listViewOfMapPoints);
        textViewRecording = findViewById(R.id.textViewRecording);
        progressBar = findViewById(R.id.progressBar);
        textViewDescriptionIfListIsEmpty = findViewById(R.id.textViewDescriptionIfListIsEmpty);
        gifImageView = findViewById(R.id.gif);
        textViewMode = findViewById(R.id.textViewMode);
        textViewOpeningGoogleMaps = findViewById(R.id.textViewOpeningGoogleMaps);

        // animation przycisków start i stip przy włączeniu aplikacji
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_right_to_left);
        buttonStop.startAnimation(animation);
        animation = AnimationUtils.loadAnimation(this, R.anim.anim_left_to_right);
        buttonStart.startAnimation(animation);

        //instanacja głownej db
        dbMain = MainMapPoinDatabase.getInstance(this);

        // instancja shar
        shar = this.getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);

        // ustawienie visibility jeśli jest w trakcie liczenia
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_SWITCH, false)) {
            //ustawienie gifa i textu jeśli jest car Moode
            gifImageView.setImageResource(R.drawable.gif_car_drive_right_go);
            textViewMode.setText(getResources().getString(R.string.drive_mode));
        } else {
            //ustawienie gifa i textu jeśli jest walk Moode
            gifImageView.setImageResource(R.drawable.mario_run);
            textViewMode.setText(getResources().getString(R.string.walk_mode));
        }

        //ustawienie visibility jesli jest w trakcie rejestrowania
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_TO_VISIBILITY, false)) {
            textViewRecording.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            gifImageView.setVisibility(View.VISIBLE);
        }

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //zapytanie o permision - jeśli nie ma to nic nie robi
                if (!hasPermissions(MainActivity.this, permissions)) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.permission_not_accepted), Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_ALL);
                    return;
                }
                // sprawdzenie czy GPS jest włączony
                if (!checkEnabledGPS()) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.disabled_gps), Toast.LENGTH_SHORT).show();
                    return;
                }

                // start service
                Intent intent = new Intent(MainActivity.this, TrackCountServoce.class);
                startService(intent);

                // wiev i progresBar że rejestruje
                textViewRecording.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                gifImageView.setVisibility(View.VISIBLE);
                editor = shar.edit(); //wywołany edytor do zmian
                editor.putBoolean(KEY_FOR_SHARED_PREF_TO_VISIBILITY, true); // nadanie wartości
                editor.apply(); // musi być na końcu aby zapisać zmiany w shar
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //zatrzymanie servisu
                Intent intent = new Intent(MainActivity.this, TrackCountServoce.class);
                stopService(intent);

                //view i orogressBar że przestał rejestrować
                textViewRecording.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                gifImageView.setVisibility(View.GONE);
                editor = shar.edit(); //wywołany edytor do zmian
                editor.putBoolean(KEY_FOR_SHARED_PREF_TO_VISIBILITY, false); // nadanie wartości
                editor.apply(); // musi być na końcu aby zapisać zmiany w shar
            }
        });

        // kliknięcie elentów z list view otwiera mapę
        listViewOfMapPoints.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //sprawdzeni czy google play sservice jest dostepny na telefonie
                if (!checkGooglePlayServices()) {
                    Toast.makeText(MainActivity.this, "Google Play service not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // jeśli widać recording to trzeba pousuwać żeby pokazć text view z info please wait opening google maps
                textViewRecording.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                gifImageView.setVisibility(View.GONE);

                // pokazanie text view z info please wait opening google maps
                textViewOpeningGoogleMaps.setVisibility(View.VISIBLE);

                //pobranie nazwy danego child z db
                MainMapPoint mp = adapter.getItem(position);
                long nameLong = mp.getDbOfMapName(); // zmiana nazwy na longa

                // otwarcie mapy i dodanie do intent nazwy db danego child
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra(NAME_OF_EXTRA_DATA_TO_INTENT_MAP, nameLong);
                startActivity(intent);
                Log.d(TAG, "onItemClick: ??");
            }
        });

        //rejestracja listenera dla context Menu aby mozna było usunąć item z listViewOfMapPoints
        registerForContextMenu(listViewOfMapPoints);


        // live data do tego aby uaktualniał adapter po jakiejkolwiek zmianie w DbMain
        final LiveData<List<MainMapPoint>> listMainLiveData = dbMain.userDao().getAllLiveData();
        listMainLiveData.observe(this, new Observer<List<MainMapPoint>>() {
            @Override
            public void onChanged(@Nullable List<MainMapPoint> mainMapPoints) {
                Collections.reverse(mainMapPoints); // reverse list before show in list view
                adapter = new MainMapPoinArrayAdapter(MainActivity.this, 0, mainMapPoints);
                listViewOfMapPoints.setAdapter(adapter);

                //właczenie opisu, że nic nie zarejestrowano jeśli lista jest pusta a jeśli nie jest to wyłączenie
                if (mainMapPoints.size() == 0) {
                    textViewDescriptionIfListIsEmpty.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onCreate: list==0" + listViewOfMapPoints.getFirstVisiblePosition());
                } else {
                    textViewDescriptionIfListIsEmpty.setVisibility(View.GONE);
                }
            }
        });

        // do reklam -banner
        AdView mAdView;
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // jeśli się wraca z MapsActivity to trzeba ukryć
        textViewOpeningGoogleMaps.setVisibility(View.GONE);

        //ustawienie visibility jesli jest w trakcie rejestrowania
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_TO_VISIBILITY, false)) {
            textViewRecording.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            gifImageView.setVisibility(View.VISIBLE);
        }
    }

    // obie metody są po to aby mozna było usunąć item z listViewOfMapPoints po długim klikniięciu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_for_list_view_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_item_context_delete:

                int positionOnList = info.position; //pozycja na liście listViewOfMapPoints
                final MainMapPoint mp = adapter.getItem(positionOnList);
                long nameLong = mp.getDbOfMapName(); // zmiana nazwy na longa

                //usunięcie wpisu bazy z nameLong z głownej bazy danych i całkowite usunięcie bazy danych z danym nameLong
                final MapPoinDatabase dbToDelete = Room.databaseBuilder(MainActivity.this, MapPoinDatabase.class, "" + nameLong)
                        .build();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dbMain.userDao().delete(mp); // usunięcie wpisu
                        dbToDelete.userDao().deleteAll(); // usunięcie całej bazy która byłą wpisana
                    }
                }).start();
                return true;
        }
        return super.onContextItemSelected(item);
    }


    // odpalenie górnego menu w Main Activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetingsMain:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);//nie usuwać bo up button nie działa
    }

    // notification chanel
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name); //chanel name
            String description = getString(R.string.channel_description); //chanel description
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //metoda do sprawdzenia czy są nadane permisssions
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // sprawdzenie czy GPS jest właczony
    private boolean checkEnabledGPS() {
        LocationManager manager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("TAG", "Gps already enabled");
            return false;

        }
        return true;
    }

    //sprawdzenie czy jest w telefonie GooglePlayService
    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
