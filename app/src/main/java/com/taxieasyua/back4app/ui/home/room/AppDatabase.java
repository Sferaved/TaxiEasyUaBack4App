package com.taxieasyua.back4app.ui.home.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RouteCost.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RouteCostDao routeCostDao();
}

