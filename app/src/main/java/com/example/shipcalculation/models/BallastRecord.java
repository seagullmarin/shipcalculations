package com.example.shipcalculation.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.List;
import androidx.room.TypeConverters; // TypeConverters için import
import com.example.shipcalculation.utils.DateConverter; // Oluşturacağımız DateConverter için import
import java.util.Date; // Date tipi için import
@Entity(tableName = "ballast_table")
@TypeConverters(DateConverter.class) // Burası doğru olmalı
public class BallastRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String ballastTankNo;
    private String ballastTankType;
    private double ballastTankCapacity;
    private double ballastTankSounding;
    private double density;
    private double ballastTankVolume;
    private double ballastTankWeight;
    private Date lastUpdated; // YENİ ALAN: Son güncelleme zamanı

    public BallastRecord(String ballastTankNo, String ballastTankType,double ballastTankCapacity,
                     double ballastTankSounding, double density,
                         double ballastTankVolume, double ballastTankWeight, Date lastUpdated) {
    this.ballastTankNo = ballastTankNo;
    this.ballastTankType = ballastTankType;
    this.ballastTankCapacity = ballastTankCapacity;
    this.ballastTankSounding = ballastTankSounding;
    this.density = density;
    this.ballastTankVolume = ballastTankVolume;
    this.ballastTankWeight = ballastTankWeight;
        this.lastUpdated = lastUpdated; // YENİ ALAN
}
public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getBallastTankNo() {return ballastTankNo;}
    public void setBallastTankNo(String ballastTankNo) {this.ballastTankNo = ballastTankNo;}
    public double getBallastTankCapacity() {return ballastTankCapacity;}
    public void setBallastTankCapacity(double ballastTankCapacity) {this.ballastTankCapacity = ballastTankCapacity;}
    public String getBallastTankType() {return ballastTankType;}
    public void setBallastTankType(String ballastTankType) {this.ballastTankType = ballastTankType;}
    public double getBallastTankSounding() {return ballastTankSounding;}
    public void setBallastTankSounding(double ballastTankSounding) {this.ballastTankSounding = ballastTankSounding;}
    public double getBallastTankVolume() {return ballastTankVolume;}
    public void setBallastTankVolume(double ballastTankVolume) {this.ballastTankVolume = ballastTankVolume;}
    public double getDensity() {return density;}
    public void setDensity(double density) {this.density = density;}
    public double getBallastTankWeight() {return ballastTankWeight;}
    public void setBallastTankWeight(double ballastTankWeight) {this.ballastTankWeight = ballastTankWeight;}
    public Date getLastUpdated() { return lastUpdated; } // YENİ GETTER
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; } // YENİ SETTER
}