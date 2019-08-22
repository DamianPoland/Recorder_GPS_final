package com.wolfmobileapps.recordergps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.wolfmobileapps.recordergps.data.MainMapPoinDatabase;
import com.wolfmobileapps.recordergps.data.MainMapPoint;
import com.wolfmobileapps.recordergps.data.MapPoinDatabase;
import com.wolfmobileapps.recordergps.data.MapPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static com.wolfmobileapps.recordergps.TrackCountServoce.NAME_OF_SHARED_PREFERNECES;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    public static final String KEY_FOR_SHARED_PREF_SWITCH = "switchMode";
    public static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 101;
    public static final String KEY_EXPORT_PROGRESS_BAR = "exportKey";
    public static final String KEY_IMPORT_PROGRESS_BAR = "importKey";

    //views
    private Switch switchMode;
    private LinearLayout version;
    private LinearLayout faq;
    private LinearLayout source;
    private LinearLayout privacy;
    private LinearLayout info;
    private TextView textViewVersionName;
    private TextView textViewMode;
    private Button buttonExport;
    private Button buttonImport;
    private ProgressBar progressBarExport;
    private ProgressBar progressBarImport;

    private SharedPreferences shar;
    private SharedPreferences.Editor editor;

    //do import i export
    private MainMapPoinDatabase dbMain;
    private long sizeOfList;
    JSONArray jsonWithAllData = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchMode = findViewById(R.id.switchMode);
        version = findViewById(R.id.version);
        faq = findViewById(R.id.faq);
        source = findViewById(R.id.source);
        privacy = findViewById(R.id.privacy);
        info = findViewById(R.id.info);
        textViewMode = findViewById(R.id.textViewMode);
        buttonExport = findViewById(R.id.buttonExport);
        buttonImport = findViewById(R.id.buttonImport);
        progressBarExport = findViewById(R.id.progressBarExport);
        progressBarImport = findViewById(R.id.progressBarImport);

        //ustawienie górnej nazwy i strzałki do powrotu
        getSupportActionBar().setTitle(getResources().getString(R.string.settings)); //ustawia nazwę na górze
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // ustawia strzałkę

        //ustavienie zazwy wersji z gradle
        textViewVersionName = findViewById(R.id.textViewVersionName);
        textViewVersionName.setText(BuildConfig.VERSION_NAME);

        // instancja shar preferences
        shar = this.getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);

        // ustawienie texstu na textView MODE
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_SWITCH, false)) {
            textViewMode.setText(getResources().getString(R.string.drive_mode));
        } else {
            textViewMode.setText(getResources().getString(R.string.walk_mode));
        }

        // właczenie switcha jeśli był wcześniej zapisany w shar jako włączony
        switchMode.setChecked(shar.getBoolean(KEY_FOR_SHARED_PREF_SWITCH, false));

        // włączenie progress bara jeśli importuje
        if (shar.getBoolean(KEY_IMPORT_PROGRESS_BAR, false)) {
            progressBarImport.setVisibility(View.VISIBLE);
        }

        // włączenie progress bara jeśli exportuje
        if (shar.getBoolean(KEY_EXPORT_PROGRESS_BAR, false)) {
            progressBarExport.setVisibility(View.VISIBLE);
        }


        switchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchMode.isChecked()) {

                    //zapisanie do shar włączenia Drive Mode
                    editor = shar.edit();//wywołany edytor do zmian
                    editor.putBoolean(KEY_FOR_SHARED_PREF_SWITCH, true); // nadanie wartości dla włączonego switcha czyli car mode
                    editor.apply(); // musi być na końcu aby zapisać zmiany w shar
                    Toast.makeText(SettingsActivity.this, "Mode Drive ON", Toast.LENGTH_SHORT).show();
                    //ustawienie textu na drive
                    textViewMode.setText(getResources().getString(R.string.drive_mode));
                } else {

                    //zapisanie do shar wYłączenia Drive Mode
                    editor = shar.edit();//wywołany edytor do zmian
                    editor.putBoolean(KEY_FOR_SHARED_PREF_SWITCH, false); // nadanie wartości dla włączonego switcha czyli car mode
                    editor.apply(); // musi być na końcu aby zapisać zmiany w shar
                    Toast.makeText(SettingsActivity.this, "Mode Drive OFF", Toast.LENGTH_SHORT).show();
                    //ustawienie textu na walk
                    textViewMode.setText(getResources().getString(R.string.walk_mode));
                }
            }
        });

        version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titule = getResources().getString(R.string.version);
                String alertString = BuildConfig.VERSION_NAME;
                createAlertDialog(titule, alertString);
            }
        });

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titule = getResources().getString(R.string.faq);
                String alertString = getResources().getString(R.string.faq_description);
                createAlertDialog(titule, alertString);
            }
        });

        source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titule = getResources().getString(R.string.open_source_licenses);
                String alertString = getResources().getString(R.string.sourceDescription);
                createAlertDialog(titule, alertString);
            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titule = getResources().getString(R.string.privacy_policy);
                String alertString = getResources().getString(R.string.privacy_policy_description);
                createAlertDialog(titule, alertString);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titule = getResources().getString(R.string.infoApp);
                String alertString = getResources().getString(R.string.infoAppDescription);
                createAlertDialog(titule, alertString);
            }
        });

        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sprawdzenie czy są permissions zaakceptowane
                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                    return;
                }

                //wywołuje metode do zrobienia JSona
                seveJSonWithAllData();

            }
        });

        buttonImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sprawdzenie czy są permissions zaakceptowane
                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                    return;
                }


                //zczytanie stringa z pliku
                String allDataFromStorage = readfileFromStorage();

                //metoda do zapisania danych w bazach aplikacji
                if (allDataFromStorage != "") {
                    saveDataInAplication(allDataFromStorage);
                }
            }
        });
    }

    //zczytuje z dysku plik ze stringiem
    private String readfileFromStorage() {

        String result;
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/RecorderGPS/data.txt");

        long length = file.length();
        if (length < 1 || length > Integer.MAX_VALUE) {
            result = "";
            Log.w(TAG, "File is empty or huge: " + file);
            Toast.makeText(this, "File is empty or huge", Toast.LENGTH_SHORT).show();
        } else {
            try (FileReader in = new FileReader(file)) {
                char[] content = new char[(int) length];

                int numRead = in.read(content);
                if (numRead != length) {
                    Log.e(TAG, "Incomplete read of " + file + ". Read chars " + numRead + " of " + length);
                }
                result = new String(content, 0, numRead);
            } catch (Exception ex) {
                Log.e(TAG, "Failure reading " + ex);
                Toast.makeText(this, "\"Failure reading \"", Toast.LENGTH_SHORT).show();
                result = "";
            }
        }
        Log.d(TAG, "readfileFromStorage: " + result);
        return result; //result to string z JSonObject a jak coś poszło nie tak to result =""
    }

    //metoda do zapisania danych w bazach aplikacji pobranych z pliku na dysku
    private void saveDataInAplication(final String allDataFromStorage) {

        //zapisanie do shar włączenia praogres bara import
        editor = shar.edit();
        editor.putBoolean(KEY_IMPORT_PROGRESS_BAR, true);
        editor.apply();

        if (shar.getBoolean(KEY_IMPORT_PROGRESS_BAR, false)) {
            progressBarImport.setVisibility(View.VISIBLE);
        }


        // zapis danyvh
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonObjectWithAllInfo = new JSONArray(allDataFromStorage);
                    for (int i = 0; i < jsonObjectWithAllInfo.length(); i++) {
                        JSONObject currentJson = jsonObjectWithAllInfo.getJSONObject(i); //JsonObject z jedną trasą

                        //wysiągnięcie danyc z JSona z daną trasą i zapisanie danych do dbMain czyli głownej bazy  z podstawowymi informacjami
                        long dbNameLong = currentJson.getLong("dbNameLong");
                        long timeOfTrack = currentJson.getLong("timeOfTrack");
                        double distance = currentJson.getDouble("distance");
                        double speed = currentJson.getDouble("speed");
                        final MainMapPoint mpMain = new MainMapPoint(dbNameLong, timeOfTrack, distance, speed);
                        final MainMapPoinDatabase dbMain = MainMapPoinDatabase.getInstance(SettingsActivity.this);

                        dbMain.userDao().insert(mpMain);
                        Log.d(TAG, "run: insert moMaim");

                        //wysiągnięcie danyc z JSona z daną trasą i zapisanie danych do db z punktami na mapie
                        final MapPoinDatabase db = Room.databaseBuilder(SettingsActivity.this, MapPoinDatabase.class, "" + dbNameLong).build();
                        JSONArray currentAraay = currentJson.getJSONArray("arrayLatLng");
                        for (int j = 0; j < currentAraay.length(); j++) {
                            JSONObject currentJsonFromArray = currentAraay.getJSONObject(j);
                            double lat = currentJsonFromArray.getDouble("lat");
                            double lng = currentJsonFromArray.getDouble("lng");
                            final MapPoint mp = new MapPoint(lat, lng);

                            db.userDao().insertAll(mp);
                            Log.d(TAG, "run: insert All");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //zapisanie do shar wYłączenia praogres bara import
                        editor = shar.edit();
                        editor.putBoolean(KEY_IMPORT_PROGRESS_BAR, false);
                        editor.apply();

                        //wyłączenie progres bara
                        progressBarImport.setVisibility(View.GONE);
                        Toast.makeText(SettingsActivity.this, "Import compleate", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }


    //metoda do zrobienia JSona
    public void seveJSonWithAllData() {

        //instanacja głownej db
        dbMain = MainMapPoinDatabase.getInstance(this);

        // jeśli lista jest pusta to nie pójdzie dalej
        sizeOfList = dbMain.userDao().getAll().size();
        if (sizeOfList == 0) {
            Toast.makeText(this, "List is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        //zapisanie do shar włączenia praogres bara export
        editor = shar.edit();
        editor.putBoolean(KEY_EXPORT_PROGRESS_BAR, true);
        editor.apply();
        if (shar.getBoolean(KEY_EXPORT_PROGRESS_BAR, false)) {
            progressBarExport.setVisibility(View.VISIBLE);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                //pobranie danych z bazy głównej i zapisanie do JSona

                try {
                    jsonWithAllData = new JSONArray();
                    for (int i = 0; i < sizeOfList; i++) {
                        long dbNameLong = dbMain.userDao().getAll().get(i).getDbOfMapName();
                        long timeOfTrack = dbMain.userDao().getAll().get(i).getTime();
                        double distance = dbMain.userDao().getAll().get(i).getDistance();
                        double speed = dbMain.userDao().getAll().get(i).getSpeed();

                        //zapisanie do JSona danych z głownej db czyli dbMain
                        JSONObject currentJSonObject = new JSONObject();
                        currentJSonObject
                                .put("dbNameLong", dbNameLong)
                                .put("timeOfTrack", timeOfTrack)
                                .put("distance", distance)
                                .put("speed", speed);
                        //pobranie danych z bazy z punktami lat i lng i zapisanie do JSona danych z db z lat i lang
                        TrackCountServoce trackCountServoce = new TrackCountServoce();
                        List<LatLng> listLatLng = trackCountServoce.getListFromDbOfMapPoins(getApplicationContext(), dbNameLong);
                        Log.d(TAG, "seveJSonWithAllData: listLatLng_________: " + listLatLng);
                        JSONArray arrayLatLng = new JSONArray();

                        for (int j = 0; j < listLatLng.size(); j++) {
                            double getLat = listLatLng.get(j).latitude;
                            Log.d(TAG, "seveJSonWithAllData: getLat: " + getLat);
                            double getLng = listLatLng.get(j).longitude;
                            Log.d(TAG, "seveJSonWithAllData: getLng: " + getLng);
                            arrayLatLng.put(j, new JSONObject()
                                    .put("lat", getLat)
                                    .put("lng", getLng));
                        }
                        currentJSonObject.put("arrayLatLng", arrayLatLng); // zapisanie tablicy z punktami na mapie
                        jsonWithAllData.put(currentJSonObject); //zapisanie wszystkich danych danej trasy jako obiekt do JSonOBject
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "seveJSonWithAllData: ___________: " + jsonWithAllData);         //log z głównym JSonem ze wszystkimi danymi

                        //zapisuje plik z JSonem na dysku
                        String stringWithAllData = jsonWithAllData.toString();
                        writeFile(stringWithAllData);

                        //zapisanie do shar wYłączenia praogres bara export
                        editor = shar.edit();
                        editor.putBoolean(KEY_EXPORT_PROGRESS_BAR, false);
                        editor.apply();

                        //wyłączenie progres bara
                        progressBarExport.setVisibility(View.GONE);

                    }
                });
            }
        }).start();
    }

    //zapisanie na dysku pliku z jsonem
    public void writeFile(String data) {
        if (isExternalStorageWritable()) {

            // Get the directory for the user's public DCIM directory.
            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/RecorderGPS/");
            if (!path.exists()) { // Make sure the path directory exists.
                path.mkdirs(); // Make it, if it doesn't exit
            }
            final File file = new File(path, "data.txt");
            // Save your stream
            try {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(data);
                myOutWriter.close();
                fOut.flush();
                fOut.close();
                Toast.makeText(this, "Export compleate", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
                Toast.makeText(this, "File write failed: " + e.toString(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    // sprawdzenie czy można zapisuwać dane na dysku
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void checkPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(SettingsActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SettingsActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        } else {
            // Permission has already been granted
        }
    }


    // tworzy alert dialog z podanego stringa tutułu i opisu
    private void createAlertDialog(String titule, String alertString) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(titule);
        if (titule.equals(getResources().getString(R.string.infoApp))) {
            builder.setIcon(R.drawable.wolf_icon);
        }
        builder.setMessage(alertString);
        builder.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do something when click OK
            }
        }).create();
        builder.show();
    }
}
