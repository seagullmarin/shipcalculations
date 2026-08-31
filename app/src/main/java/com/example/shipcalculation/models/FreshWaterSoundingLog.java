package com.example.shipcalculation.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.shipcalculation.utils.DateConverter;
import java.util.Date;

@Entity(tableName = "fresh_water_sounding_log_table",
        foreignKeys = @ForeignKey(entity = FreshWaterRecord.class,
                parentColumns = "id",
                childColumns = "tankId",
                onDelete = ForeignKey.CASCADE), // Eğer ana tank silinirse logları da sil
        indices = {@Index("tankId")}) // tankId üzerinde index sorguları hızlandırır
@TypeConverters(DateConverter.class)
public class FreshWaterSoundingLog {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int tankId; // Hangi FreshWaterRecord'a ait olduğunu belirtir (Foreign Key)

    private Date measurementTime; // Ölçümün yapıldığı tarih ve saat

    private double sounding; // Alınan sounding değeri

    private double calculatedVolume; // Sounding'e göre hesaplanan hacim

    // Yapıcı (Constructor)
    public FreshWaterSoundingLog(int tankId, Date measurementTime, double sounding, double calculatedVolume) {
        this.tankId = tankId;
        this.measurementTime = measurementTime;
        this.sounding = sounding;
        this.calculatedVolume = calculatedVolume;
    }

    // Getter ve Setter metotları
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTankId() {
        return tankId;
    }

    public void setTankId(int tankId) {
        this.tankId = tankId;
    }

    public Date getMeasurementTime() {
        return measurementTime;
    }

    public void setMeasurementTime(Date measurementTime) {
        this.measurementTime = measurementTime;
    }

    public double getSounding() {
        return sounding;
    }

    public void setSounding(double sounding) {
        this.sounding = sounding;
    }

    public double getCalculatedVolume() {
        return calculatedVolume;
    }

    public void setCalculatedVolume(double calculatedVolume) {
        this.calculatedVolume = calculatedVolume;
    }
}
