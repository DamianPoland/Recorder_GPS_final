package com.wolfmobileapps.recordergps.data;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

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
