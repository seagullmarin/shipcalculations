package com.example.shipcalculation.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao; // @Dao ek açıklamasını import et
import androidx.room.Delete; // @Delete ek açıklamasını import et
import androidx.room.Insert;
import androidx.room.OnConflictStrategy; // OnConflictStrategy için import
import androidx.room.Query;
import androidx.room.Update;

import com.example.shipcalculation.models.FreshWaterRecord;
import com.example.shipcalculation.models.FreshWaterSoundingLog; // Yeni import

import java.util.Date; // Date için import
import java.util.List;

@Dao // Hata 1: @Dao ek açıklaması eklendi
public interface FreshWaterDao {

    // Standart INSERT metodu. Çakışma durumunda veriyi değiştirir.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FreshWaterRecord freshWaterRecord);

    // Standart UPDATE metodu.
    @Update
    void update(FreshWaterRecord freshWaterRecord);

    // Standart DELETE metodu.
    @Delete
    void delete(FreshWaterRecord freshWaterRecord);

    // Eğer sadece ID ile silmek istiyorsanız:
    @Query("DELETE FROM fresh_water_table WHERE id = :freshWaterId")
    void deleteById(int freshWaterId);

    // Tüm kayıtları ID'ye göre artan sırada getirir.
    @Query("SELECT * FROM fresh_water_table ORDER BY id ASC")
    LiveData<List<FreshWaterRecord>> getAllFreshWaterRecordsSortedById();

    // Tüm kayıtları Tank Numarasına göre artan sırada getirir.
    @Query("SELECT * FROM fresh_water_table ORDER BY freshWaterTankNo ASC")
    LiveData<List<FreshWaterRecord>> getAllFreshWaterRecordsSortedByTankNo();
    @Query("SELECT * FROM fresh_water_table WHERE id = :id LIMIT 1")
    LiveData<FreshWaterRecord> getFreshWaterRecordById(int id);

    @Query("SELECT SUM(freshWaterTankVolume) FROM fresh_water_table")
    LiveData<Double> getTotalWeight();
    @Query("SELECT SUM(freshWaterTankCapacity) FROM fresh_water_table")
    LiveData<Double> getTotalCapacity();

    @Query("SELECT SUM(freshWaterTankVolume) FROM fresh_water_table")
    LiveData<Double> getTotalVolume(); // Bu, ana tankların mevcut (son) hacimlerinin toplamı olur

    // --- YENİ: FreshWaterSoundingLog Metotları ---
    @Insert
    void insertSoundingLog(FreshWaterSoundingLog log);

    @Query("SELECT * FROM fresh_water_sounding_log_table WHERE tankId = :tankId ORDER BY measurementTime DESC")
    LiveData<List<FreshWaterSoundingLog>> getSoundingLogsForTank(int tankId);

    @Query("SELECT * FROM fresh_water_sounding_log_table ORDER BY measurementTime DESC")
    LiveData<List<FreshWaterSoundingLog>> getAllSoundingLogs();

    @Query("SELECT * FROM fresh_water_sounding_log_table WHERE tankId = :tankId ORDER BY measurementTime DESC LIMIT 1")
    LiveData<FreshWaterSoundingLog> getLatestSoundingLogForTank(int tankId);

    // Belirli bir tankın belirli bir tarih aralığındaki loglarını getirme
    @Query("SELECT * FROM fresh_water_sounding_log_table WHERE tankId = :tankId AND measurementTime BETWEEN :startDate AND :endDate ORDER BY measurementTime ASC")
    LiveData<List<FreshWaterSoundingLog>> getSoundingLogsForTankByDateRange(int tankId, Date startDate, Date endDate);

    @Delete
    void deleteSoundingLog(FreshWaterSoundingLog log);

    @Query("DELETE FROM fresh_water_sounding_log_table WHERE tankId = :tankId")
    void deleteAllSoundingLogsForTank(int tankId); // Bir tanka ait tüm logları silmek için
    @Query("SELECT * FROM fresh_water_sounding_log_table WHERE tankId = :tankId ORDER BY measurementTime DESC LIMIT :limit")
    LiveData<List<FreshWaterSoundingLog>> getRecentSoundingLogsForTank(int tankId, int limit);
    @Query("SELECT * FROM fresh_water_sounding_log_table WHERE tankId = :tankId AND measurementTime >= :startDate ORDER BY measurementTime DESC")
    LiveData<List<FreshWaterSoundingLog>> getSoundingLogsForTankSince(int tankId, Date startDate);



}

