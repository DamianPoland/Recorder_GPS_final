package com.wolfmobileapps.recordergps.data;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wolfmobileapps.recordergps.MainActivity;
import com.wolfmobileapps.recordergps.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.wolfmobileapps.recordergps.TrackCountServoce.KEY_FOR_SHARED_PREF_ANIMATE_FIRST_ITEM;
import static com.wolfmobileapps.recordergps.TrackCountServoce.NAME_OF_SHARED_PREFERNECES;

// MapPointArrayAsapter służy do wyświeltania danych w MainActivity w listViewOfMapPoints
public class MainMapPoinArrayAdapter extends ArrayAdapter<MainMapPoint> {

    private static final String TAG = "MainMapPoinArrayAdapter";

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        MainMapPoint currentItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_for_map_point_adapter, parent, false);
        }
        TextView text11 = convertView.findViewById(R.id.textViewForAdapter11);
        TextView text12 = convertView.findViewById(R.id.textViewForAdapter12);
        TextView text2 = convertView.findViewById(R.id.textViewForAdapter2);
        TextView text3 = convertView.findViewById(R.id.textViewForAdapter3);
        TextView text4 = convertView.findViewById(R.id.textViewForAdapter4);

        // przekształcenie wziętego z dbMain longa na stringa z datą
        long dbNameLong = currentItem.getDbOfMapName();
        String weekDay = new SimpleDateFormat("EEEE").format(dbNameLong); //dzień tygodnia wzięty
        text11.setText(weekDay);
        String dataNameData = new SimpleDateFormat("dd.MM.yyyy").format(new Date(dbNameLong));
        text12.setText(dataNameData);

        // przekształcenie czasu drogi na czas do odczytu
        long dbTimeLong = currentItem.getTime();
        String dataNameTime;
        long hoursLong = ((dbTimeLong / (1000 * 60 * 60)) % 25);
        String hours = "" + hoursLong;
        if (hoursLong < 10) {
            hours = "0" + hoursLong;
        }
        long minutsLong = ((dbTimeLong / (1000 * 60)) % 60);
        String minuts = "" + minutsLong;
        if (minutsLong < 10) {
            minuts = "0" + minutsLong;
        }
        long secondLong = ((dbTimeLong / 1000) % 60);
        String second = "" + secondLong;
        if (secondLong < 10) {
            second = "0" + secondLong;
        }
        String timeFromResource = getContext().getResources().getString(R.string.time) + " ";
        dataNameTime = timeFromResource + hours + ":" + minuts + ":" + second + " s";
        text2.setText(dataNameTime);

        // przekształcenie na dystans przebyty
        double dystanceDouble = currentItem.getDistance();
        String text2toDislpay;
        String distance = getContext().getResources().getString(R.string.distance) + " ";
        if (dystanceDouble >= 1000) {
            double dystanceHelpCount = (Math.round(dystanceDouble));
            text2toDislpay = distance + dystanceHelpCount / 1000 + " km";
        } else {
            text2toDislpay = distance + Math.round(dystanceDouble) + " m";
        }
        text3.setText(text2toDislpay);

        // przekształcenie na speed
        String speedFromResource = getContext().getResources().getString(R.string.speed) + " ";
        String speed = speedFromResource + currentItem.getSpeed()
                + " km/h";
        text4.setText(speed);

        // ustawienie animacje tylko pierwszego itema i tylko po dodaniu do listy
        SharedPreferences shar = getContext().getSharedPreferences(NAME_OF_SHARED_PREFERNECES, MODE_PRIVATE);
        if (shar.getBoolean(KEY_FOR_SHARED_PREF_ANIMATE_FIRST_ITEM,false) && position==0){
            // animation of view

            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_rotation);
            animation.setDuration(1500);
            convertView.startAnimation(animation);
            SharedPreferences.Editor editor = shar.edit(); //wywołany edytor do zmian
            editor.putBoolean(KEY_FOR_SHARED_PREF_ANIMATE_FIRST_ITEM,false); //zmiana z powrotem aby było false - na true zmienia service podczas dodawania itema
            editor.apply();
        }
        Log.d(TAG, "getView:  end");

        return convertView;
    }

    public MainMapPoinArrayAdapter(Context context, int resource, List<MainMapPoint> objects) {
        super(context, resource, objects);
    }
}
