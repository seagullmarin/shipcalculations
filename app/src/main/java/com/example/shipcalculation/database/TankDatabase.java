package com.example.shipcalculation.database;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters; // Bu import kalsın (Room için)
import androidx.room.migration.Migration; // Migration için import
import androidx.sqlite.db.SupportSQLiteDatabase; // Migration için import

import com.example.shipcalculation.models.FreshWaterRecord;
import com.example.shipcalculation.models.FreshWaterSoundingLog; // Yeni Entity
//import com.example.shipcalculation.models.User;
import com.example.shipcalculation.utils.DateConverter;
import com.example.shipcalculation.utils.DateConverter; // Kendi DateConverter'ınızın importu
import com.example.shipcalculation.models.BallastRecord;

@Database(entities = {BallastRecord.class, FreshWaterRecord.class, FreshWaterSoundingLog.class}, version = 7, exportSchema = false) // versiyon numaranız doğru
@TypeConverters({DateConverter.class}) // BURAYI DÜZELTİN: Sadece kendi DateConverter'ınızı kullanın
public abstract class TankDatabase extends RoomDatabase {

    private static volatile TankDatabase INSTANCE;
    public abstract BallastDao ballastDao();
    public abstract FreshWaterDao freshWaterDao();

    // Örnek Migration (versiyon 1'den 2'ye geçerken yeni tabloyu ekler)
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // FreshWaterSoundingLog tablosunu oluştur
            database.execSQL("CREATE TABLE IF NOT EXISTS `fresh_water_sounding_log_table` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`tankId` INTEGER NOT NULL, " +
                    "`measurementTime` INTEGER, " + // DateConverter Long olarak saklar
                    "`sounding` REAL NOT NULL, " +
                    "`calculatedVolume` REAL NOT NULL, " +
                    "FOREIGN KEY(`tankId`) REFERENCES `fresh_water_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED)");
            // Gerekirse fresh_water_sounding_log_table için index oluştur
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_fresh_water_sounding_log_table_tankId` ON `fresh_water_sounding_log_table` (`tankId`)");
        }
    };
    public static TankDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TankDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TankDatabase.class, "tank_database")
                            .addMigrations(MIGRATION_6_7) // Migration'ı ekleyin
                            // .fallbackToDestructiveMigration() // Sadece geliştirme sırasında, veri kaybına neden olur!

                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
