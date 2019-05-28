package com.wolfmobileapps.recordergps.data;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MainMapPoinDao {

    @Query("SELECT * FROM mainmappoint") //opis co robi metoda getAll, mainmappoint to nazwa tabelki z Entity
    List<MainMapPoint> getAll(); //metoda abstrakcyjna nazwana getAll(nazwa może być inna), może zwracać List lub Cursor,

    @Query("SELECT * FROM mainmappoint")
    LiveData<List<MainMapPoint>> getAllLiveData(); //metoda zwraca live data

    @Insert
    void insertAll(MainMapPoint... mapPoints);

    @Insert
    void insert(MainMapPoint mainmappoint);

    @Delete
    void delete(MainMapPoint mainmappoint);

    @Query("DELETE FROM mainmappoint")
    void deleteAll();
}
