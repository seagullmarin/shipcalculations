package com.example.shipcalculation.viewmodels;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.shipcalculation.database.FreshWaterDao;
import com.example.shipcalculation.database.TankDatabase;
import com.example.shipcalculation.models.FreshWaterRecord;
import com.example.shipcalculation.models.FreshWaterSoundingLog; // Yeni import
import java.util.Date; // Date için import
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FreshWaterViewModel extends AndroidViewModel {

    private FreshWaterDao freshWaterDao;
    private LiveData<List<FreshWaterRecord>> allFreshWaterRecords;
    private LiveData<Double> totalVolume;
    private LiveData<Double> totalCapacity;
    private ExecutorService executorService;

    //@RequiresApi(api = Build.VERSION_CODES.O)
    public FreshWaterViewModel(@NonNull Application application) {
        super(application);
        TankDatabase database = TankDatabase.getInstance(application);
        freshWaterDao = database.freshWaterDao();
       // allFreshWaterRecords = freshWaterDao.getAllFreshWaterRecordsSortedById(); // Veya istediğiniz sıralama
        allFreshWaterRecords = freshWaterDao.getAllFreshWaterRecordsSortedByTankNo(); // Veya ID'ye göre sıralı

        totalVolume = freshWaterDao.getTotalWeight();
        totalCapacity = freshWaterDao.getTotalCapacity();
        executorService = Executors.newSingleThreadExecutor(); // Basit bir executor
    }

    public void insert(FreshWaterRecord freshWaterRecord) {
        executorService.execute(() -> freshWaterDao.insert(freshWaterRecord));
    }

    public void update(FreshWaterRecord freshWaterRecord) {
        executorService.execute(() -> freshWaterDao.update(freshWaterRecord));
    }

    public void delete(FreshWaterRecord freshWaterRecord) {
        executorService.execute(() -> freshWaterDao.delete(freshWaterRecord));
    }
    public void deleteLog(FreshWaterSoundingLog log) {
        // Room işlemleri ana thread dışında yapılmalı
        Executors.newSingleThreadExecutor().execute(() -> {
            freshWaterDao.deleteSoundingLog(log);
        });
    }
    public LiveData<List<FreshWaterRecord>> getAllFreshWaterRecords() {
        return allFreshWaterRecords;
    }
    public LiveData<Double> getTotalVolume() {
        return totalVolume;
    }
    public LiveData<Double> getTotalCapacity() {
        return totalCapacity;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown(); // ViewModel temizlendiğinde executor'ı kapat
    }
    public LiveData<FreshWaterRecord> getFreshWaterRecordById(int id) {
        return freshWaterDao.getFreshWaterRecordById(id);
    }

    public void deleteById(int id) {
        executorService.execute(() -> {
            freshWaterDao.deleteAllSoundingLogsForTank(id); // Önce ilişkili logları sil
            freshWaterDao.deleteById(id);                   // Sonra ana kaydı sil
        });
    }

    // --- YENİ: FreshWaterSoundingLog için metotlar ---
    public void insertSoundingLog(FreshWaterSoundingLog log) {
        executorService.execute(() -> {
            freshWaterDao.insertSoundingLog(log);
            // İSTEĞE BAĞLI: Yeni log eklendiğinde ana FreshWaterRecord'un hacmini güncelle
            // Bu, getTotalVolume() sorgusunun her zaman en güncel toplamı vermesini sağlar.
            FreshWaterRecord parentTank = freshWaterDao.getFreshWaterRecordById(log.getTankId()).getValue(); // Bu LiveData, doğrudan değer almayı zorlaştırır.
            // DAO'da LiveData olmayan bir getById metodu eklemek daha iyi olabilir.
            // Şimdilik bu kısmı atlayabilir veya farklı bir yaklaşımla (örn: Repository Pattern) çözebilirsiniz.
            // VEYA: Log ekleme aktivitesinde, logu kaydettikten sonra ana tankı da güncelleyebilirsiniz.
        });
    }

    public LiveData<List<FreshWaterSoundingLog>> getSoundingLogsForTank(int tankId) {
        return freshWaterDao.getSoundingLogsForTank(tankId);
    }

    public LiveData<FreshWaterSoundingLog> getLatestSoundingLogForTank(int tankId) {
        return freshWaterDao.getLatestSoundingLogForTank(tankId);
    }

    public LiveData<List<FreshWaterSoundingLog>> getSoundingLogsForTankByDateRange(int tankId, Date startDate, Date endDate) {
        return freshWaterDao.getSoundingLogsForTankByDateRange(tankId, startDate, endDate);
    }

   // public void deleteSoundingLog(FreshWaterSoundingLog log) {
       // executorService.execute(() -> freshWaterDao.deleteSoundingLog(log));
   // }
    // ... (mevcut metotlar) ...

    public LiveData<List<FreshWaterSoundingLog>> getRecentSoundingLogsForTank(int tankId, int limit) {
        // Bu metot için DAO'da @Query("SELECT * FROM fresh_water_sounding_log_table WHERE tankId = :tankId ORDER BY measurementTime DESC LIMIT :limit")
        // şeklinde bir sorgu olmalı.
        return freshWaterDao.getRecentSoundingLogsForTank(tankId, limit); // DAO'da bu metot tanımlı olmalı
    }

    public LiveData<List<FreshWaterSoundingLog>> getSoundingLogsForTankSince(int tankId, Date startDate) {
        // Bu metot için DAO'da @Query("SELECT * FROM fresh_water_sounding_log_table WHERE tankId = :tankId AND measurementTime >= :startDate ORDER BY measurementTime DESC")
        // şeklinde bir sorgu olmalı.
        return freshWaterDao.getSoundingLogsForTankSince(tankId, startDate); // DAO'da bu metot tanımlı olmalı
    }
    public void deleteSoundingLog(FreshWaterSoundingLog log) {
        executorService.execute(() -> {
            freshWaterDao.deleteSoundingLog(log); // FreshWaterDao'da bu metodun olması lazım
        });
    }

}


