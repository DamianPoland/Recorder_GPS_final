package com.wolfmobileapps.recordergps.data;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity
public class MapPoint {


    @PrimaryKey(autoGenerate = true)
    private int idPoint;

    @ColumnInfo
    private double latitudePoint;

    @ColumnInfo
    private double longitudePoint;

    public int getIdPoint() {
        return idPoint;
    }

    public void setIdPoint(int idPoint) {
        this.idPoint = idPoint;
    }

    public double getLatitudePoint() {
        return latitudePoint;
    }

    public void setLatitudePoint(double latitudePoint) {
        this.latitudePoint = latitudePoint;
    }

    public double getLongitudePoint() {
        return longitudePoint;
    }

    public void setLongitudePoint(double longitudePoint) {
        this.longitudePoint = longitudePoint;
    }

    public MapPoint(double latitudePoint, double longitudePoint) {
        this.latitudePoint = latitudePoint;
        this.longitudePoint = longitudePoint;
    }
}


