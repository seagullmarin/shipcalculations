package com.example.shipcalculation.viewmodels;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.shipcalculation.database.TankDatabase;
import com.example.shipcalculation.database.BallastDao;
import com.example.shipcalculation.models.BallastRecord;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BallastViewModel extends AndroidViewModel {

    private BallastDao ballastDao;
    private LiveData<List<BallastRecord>> allBallastRecords;
    private LiveData<Double> totalVolume;
    private LiveData<Double> totalWeight;
    private LiveData<Double> totalCapacity;
    private ExecutorService executorService;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public BallastViewModel(@NonNull Application application) {
        super(application);
        TankDatabase database = TankDatabase.getInstance(application);
        ballastDao = database.ballastDao();
        allBallastRecords = ballastDao.getAllBallastRecordsSortedById(); // Veya istediğiniz sıralama
        totalVolume = ballastDao.getTotalVolume();
        totalWeight = ballastDao.getTotalWeight();
        totalCapacity = ballastDao.getTotalCapacity();
        executorService = Executors.newSingleThreadExecutor(); // Basit bir executor
    }

    public void insert(BallastRecord ballastRecord) {
        executorService.execute(() -> ballastDao.insert(ballastRecord));
    }

    public void update(BallastRecord ballastRecord) {
        executorService.execute(() -> ballastDao.update(ballastRecord));
    }

    public void delete(BallastRecord ballastRecord) {
        executorService.execute(() -> ballastDao.delete(ballastRecord));
    }

    public LiveData<List<BallastRecord>> getAllBallastRecords() {
        return allBallastRecords;
    }

    public LiveData<Double> getTotalVolume() {
        return totalVolume;
    }

    public LiveData<Double> getTotalWeight() {
        return totalWeight;
    }
    public LiveData<Double> getTotalCapacity() {
        return totalCapacity;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown(); // ViewModel temizlendiğinde executor'ı kapat
    }
    public LiveData<BallastRecord> getBallastRecordById(int id) {
        return ballastDao.getBallastRecordById(id);
    }
}

