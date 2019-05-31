package com.wolfmobileapps.recordergps;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static com.wolfmobileapps.recordergps.TrackCountServoce.NAME_OF_SHARED_PREFERNECES;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    public static final String KEY_FOR_SHARED_PREF_SWITCH = "switchMode";

    private Switch switchMode;
    private LinearLayout version;
    private LinearLayout faq;
    private LinearLayout source;
    private LinearLayout privacy;
    private LinearLayout info;

    private TextView textViewVersionName;
    private TextView textViewMode;

    private SharedPreferences shar;
    private SharedPreferences.Editor editor;



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

        //ustawienie górnej nazwy i strzałki do powrotu
        getSupportActionBar().setTitle(getResources().getString(R.string.settings)); //ustawia nazwę na górze
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // ustawia strzałkę

        //ustavienie zazwy wersji z gradle
        textViewVersionName = findViewById(R.id.textViewVersionName);
        textViewVersionName.setText(BuildConfig.VERSION_NAME);

        // instancja shar preferences
        shar = this.getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);

        // ustawienie texstu na textView MODE
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_SWITCH,false)){
            textViewMode.setText(getResources().getString(R.string.drive_mode));
        }else {
            textViewMode.setText(getResources().getString(R.string.walk_mode));
        }

        // właczenie switcha jeśli był wcześniej zapisany w shar jako włączony
        switchMode.setChecked(shar.getBoolean(KEY_FOR_SHARED_PREF_SWITCH,false));

        switchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchMode.isChecked()){

                    //zapisanie do shar włączenia Drive Mode
                    editor = shar.edit();//wywołany edytor do zmian
                    editor.putBoolean(KEY_FOR_SHARED_PREF_SWITCH, true); // nadanie wartości dla włączonego switcha czyli car mode
                    editor.apply(); // musi być na końcu aby zapisać zmiany w shar
                    Toast.makeText(SettingsActivity.this, "Mode Drive ON", Toast.LENGTH_SHORT).show();
                    //ustawienie textu na drive
                    textViewMode.setText(getResources().getString(R.string.drive_mode));
                }else {

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
    }

    // tworzy alert dialog z podanego stringa tutułu i opisu
    private void createAlertDialog (String titule, String alertString){

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(titule);
        if (titule.equals(getResources().getString(R.string.infoApp))){
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
