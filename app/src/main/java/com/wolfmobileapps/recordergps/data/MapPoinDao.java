package com.wolfmobileapps.recordergps.data;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MapPoinDao {

    @Query("SELECT * FROM mappoint") //opis co robi metoda getAll, mappoint to nazwa tabelki z Entity
    List<MapPoint> getAll(); //metoda abstrakcyjna nazwana getAll(nazwa może być inna), może zwracać List lub Cursor,

    @Insert
    void insertAll(MapPoint... mapPoints);

    @Insert
    void insert(MapPoint mapPoint);

    @Delete
    void delete(MapPoint mapPoint);

    @Query("DELETE FROM mappoint")
    void deleteAll();

}
