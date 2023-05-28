package de.ur.mi.android.wetter.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Not used!
 **/
@Database(entities = {CardModel.class}, version = 1)
public abstract class DatabaseClass extends RoomDatabase {

    private static DatabaseClass instance;

    public abstract DaoClass getDao();

    DatabaseClass getDatabase(final Context context) {
        if (instance == null) {
            synchronized (DatabaseClass.class) {
                instance = Room.databaseBuilder(context, DatabaseClass.class, "DATABASE").build();
            }
        }
        return instance;
    }
}
