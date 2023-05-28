package de.ur.mi.android.wetter.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoClass {

    @Insert
    void insertAllData(CardModel model);

    @Query("select * from citycard")
    List<CardModel> getAllData();

}
