package data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

@Database(entities = {Plant.class}, version = 1)
public abstract class PlantRoomDatabase extends RoomDatabase {

    public abstract PlantDao plantDao();

    private static PlantRoomDatabase INSTANCE;

    public static PlantRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PlantRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PlantRoomDatabase.class, "plant_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final PlantDao mDao;

        PopulateDbAsync(PlantRoomDatabase db) {
            mDao = db.plantDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // TODO: remove the deleteAll code
            mDao.deleteAll();
            Plant plant = new Plant("Yucca", 109293305, 10,"archblob/moisture");
            mDao.insert(plant);
            plant = new Plant("Banana Tree", 893248829, 93,"archblob/moisture");
            mDao.insert(plant);
            plant = new Plant("Tulips", 293048239, 27,"archblob/moisture");
            mDao.insert(plant);
            Log.v("PlantRoomDatabase", "Added dummy plants to the database");
            return null;
        }
    }
}

