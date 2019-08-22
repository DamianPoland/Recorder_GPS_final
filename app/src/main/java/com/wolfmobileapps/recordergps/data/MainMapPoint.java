package com.wolfmobileapps.recordergps.data;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MainMapPoint {
    @PrimaryKey(autoGenerate = true)
    private int idPoint;

    @ColumnInfo
    private long dbOfMapName;

    @ColumnInfo
    private long time;

    @ColumnInfo
    private double distance;

    @ColumnInfo
    private double speed;

    public int getIdPoint() {
        return idPoint;
    }

    public void setIdPoint(int idPoint) {
        this.idPoint = idPoint;
    }

    public long getDbOfMapName() {
        return dbOfMapName;
    }

    public void setDbOfMapName(long dbOfMapName) {
        this.dbOfMapName = dbOfMapName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public MainMapPoint(long dbOfMapName, long time, double distance, double speed) {
        this.dbOfMapName = dbOfMapName;
        this.time = time;
        this.distance = distance;
        this.speed = speed;
    }
}
