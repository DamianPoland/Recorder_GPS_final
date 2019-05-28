package com.wolfmobileapps.recordergps.data;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


@Database(entities = {MapPoint.class}, version = 1, exportSchema = false)
public abstract class MapPoinDatabase extends RoomDatabase {

    public abstract MapPoinDao userDao();


}
