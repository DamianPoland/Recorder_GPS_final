package com.wolfmobileapps.recordergps.data;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import static com.wolfmobileapps.recordergps.MainActivity.MAIN_DATABASE_NAME;


@Database(entities = {MainMapPoint.class}, version = 1, exportSchema = false)
public abstract class MainMapPoinDatabase extends RoomDatabase {

    public abstract MainMapPoinDao userDao();

    // to potrzebne jest do LiveData jeśli instancja ma być z kilku miejśc
    private static MainMapPoinDatabase instance;

    public static MainMapPoinDatabase getInstance(Context context){
        if (instance==null){
            instance = Room.databaseBuilder(context, MainMapPoinDatabase.class, MAIN_DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
