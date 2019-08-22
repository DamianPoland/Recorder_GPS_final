package com.wolfmobileapps.recordergps.data;


import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {MapPoint.class}, version = 1, exportSchema = false)
public abstract class MapPoinDatabase extends RoomDatabase {

    public abstract MapPoinDao userDao();


}
