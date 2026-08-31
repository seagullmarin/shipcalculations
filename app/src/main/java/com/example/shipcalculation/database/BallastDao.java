package com.example.shipcalculation.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao; // @Dao ek açıklamasını import et
import androidx.room.Delete; // @Delete ek açıklamasını import et
import androidx.room.Insert;
import androidx.room.OnConflictStrategy; // OnConflictStrategy için import
import androidx.room.Query;
import androidx.room.Update;

import com.example.shipcalculation.models.BallastRecord;

import java.util.List;

@Dao // Hata 1: @Dao ek açıklaması eklendi
public interface BallastDao {

    // Standart INSERT metodu. Çakışma durumunda veriyi değiştirir.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BallastRecord ballastRecord); // Hata 2: @Insert eklendi, isim standartlaştırıldı

    // Standart UPDATE metodu.
    @Update
    void update(BallastRecord ballastRecord); // Hata 2: @Update eklendi, isim standartlaştırıldı

    // Standart DELETE metodu.
    @Delete
    void delete(BallastRecord ballastRecord); // Hata 2: @Delete eklendi, isim standartlaştırıldı

    // Belirli bir ID'ye sahip kaydı silmek için özel bir Query de kullanılabilir,
    // ancak @Delete(entity = BallastRecord.class) ile nesneyi göndermek daha yaygındır.
    // Eğer sadece ID ile silmek istiyorsanız:
    @Query("DELETE FROM ballast_table WHERE id = :ballastId")
    void deleteById(int ballastId); // deleteBallastRecord yerine deleteById veya benzeri bir isim

    // Tüm kayıtları ID'ye göre artan sırada getirir.
    // getAllBallastRecords() ve getAllBallastRecordsSortedByIdAsc() aynı işi yapıyor gibi. Birini seçin.
    @Query("SELECT * FROM ballast_table ORDER BY id ASC")
    LiveData<List<BallastRecord>> getAllBallastRecordsSortedById();

    // Tüm kayıtları Tank Numarasına göre artan sırada getirir.
    @Query("SELECT * FROM ballast_table ORDER BY ballastTankNo ASC")
    LiveData<List<BallastRecord>> getAllBallastRecordsSortedByTankNo(); // getAllBallastObservable yerine daha açıklayıcı bir isim

    // Belirli bir ID'ye sahip tek bir kaydı getirir.
    // getBallastByIdAsLiveData ve getBallastRecordById aynı işi yapıyor. Birini seçin.
    @Query("SELECT * FROM ballast_table WHERE id = :id LIMIT 1")
    LiveData<BallastRecord> getBallastRecordById(int id);

    // Toplam hacmi getirir.
    @Query("SELECT SUM(ballastTankVolume) FROM ballast_table")
    LiveData<Double> getTotalVolume();

    // Toplam ağırlığı getirir (BallastRecord'da ballastTankWeight alanı olduğunu varsayarak).
    @Query("SELECT SUM(ballastTankWeight) FROM ballast_table")
    LiveData<Double> getTotalWeight();
    @Query("SELECT SUM(ballastTankCapacity) FROM ballast_table")
    LiveData<Double> getTotalCapacity();

}

