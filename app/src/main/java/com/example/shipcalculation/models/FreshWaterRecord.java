package com.example.shipcalculation.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.List;
import androidx.room.TypeConverters; // TypeConverters için import
import com.example.shipcalculation.utils.DateConverter; // Oluşturacağımız DateConverter için import
import java.util.Date; // Date tipi için import
@Entity(tableName = "fresh_water_table")
@TypeConverters(DateConverter.class) // Burası doğru olmalı
public class FreshWaterRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String freshWaterTankNo;
    private String freshWaterTankType;
    private double freshWaterTankCapacity;
    private double freshWaterTankSounding;
    private double freshWaterTankVolume;
    private Date lastUpdated; // YENİ ALAN: Son güncelleme zamanı

    public FreshWaterRecord(String freshWaterTankNo, String freshWaterTankType,double freshWaterTankCapacity,
                         double freshWaterTankSounding,double freshWaterTankVolume, Date lastUpdated) {
        this.freshWaterTankNo = freshWaterTankNo;
        this.freshWaterTankType = freshWaterTankType;
        this.freshWaterTankCapacity = freshWaterTankCapacity;
        this.freshWaterTankSounding = freshWaterTankSounding;
        this.freshWaterTankVolume = freshWaterTankVolume;
        this.lastUpdated = lastUpdated; // YENİ ALAN
    }
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getFreshWaterTankNo() {return freshWaterTankNo;}
    public void setFreshWaterTankNo(String freshWaterTankNo) {this.freshWaterTankNo = freshWaterTankNo;}
    public double getFreshWaterTankCapacity() {return freshWaterTankCapacity;}
    public void setFreshWaterTankCapacity(double freshWaterTankCapacity) {this.freshWaterTankCapacity = freshWaterTankCapacity;}
    public String getFreshWaterTankType() {return freshWaterTankType;}
    public void setFreshWaterTankType(String freshWaterTankType) {this.freshWaterTankType = freshWaterTankType;}
    public double getFreshWaterTankSounding() {return freshWaterTankSounding;}
    public void setFreshWaterTankSounding(double freshWaterTankSounding) {this.freshWaterTankSounding = freshWaterTankSounding;}

    public double getFreshWaterTankVolume() {return freshWaterTankVolume;}
    public void setFreshWaterTankVolume(double freshWaterTankVolume) {this.freshWaterTankVolume = freshWaterTankVolume;}
    public Date getLastUpdated() { return lastUpdated; } // YENİ GETTER
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; } // YENİ SETTER
}