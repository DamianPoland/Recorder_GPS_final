package com.wolfmobileapps.recordergps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";

    // do reklam
    private InterstitialAd mInterstitialAd;
    private boolean shouldLoadAds; // żeby reklamy nie pokazywały się po wyłaczeniu aplikacji - tylko do intestitialAds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // do reklam
        MobileAds.initialize(this); //inicjalizacja reklam potrzebna tylko raz na całą aplikację
        mInterstitialAd = new InterstitialAd(this); // instancja danej reklamy
        mInterstitialAd.setAdUnitId("ca-app-pub-1490567689734833/7064884493"); //wpisać ID danej reklamy czyli identyfikator jednostki reklamowej wzięty z AdMOB
        mInterstitialAd.loadAd(new AdRequest.Builder().build()); // ładuje reklamę to chwile potrwa więc od razu może nie pokazać bo nie będzie załadowana dlatego trzeba dodać listenera jak niżej
        mInterstitialAd.setAdListener(new AdListener() {// dodaje listenera do pokazywania reklam jak np się załaduje reklama i mozna ustawić też inne rzeczy że się wyświetla ale są bez sensu
            @Override
            public void onAdLoaded() {
                if (shouldLoadAds) { // żeby reklamy nie pokazywały się po wyłaczeniu aplikacji - tylko do intestitialAds patrz niżej
                    mInterstitialAd.show(); //pokazuje reklamę
                } else {
                    startActivity(new Intent(StartActivity.this, MainActivity.class)); //ładuje activity
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) { //jeśli error jest 3 to nie ma zasobów reklamowych
                Log.d(TAG, "onAdFailedToLoad: __________ errorCode: " + errorCode);
                startActivity(new Intent(StartActivity.this, MainActivity.class)); //ładuje activity jeśli nie ma reklamy
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() { //jeśli error to 3 to nie ma zasobów reklamowych
                shouldLoadAds = false; // żeby reklamy nie pokazywały się po wyłaczeniu aplikacji - tylko do intestitialAds
            }

            @Override
            public void onAdClosed() {
                startActivity(new Intent(StartActivity.this, MainActivity.class)); //ładuje activity
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        shouldLoadAds = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        shouldLoadAds = true;
    }
}
