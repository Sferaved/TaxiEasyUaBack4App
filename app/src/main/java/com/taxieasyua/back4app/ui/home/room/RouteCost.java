package com.taxieasyua.back4app.ui.home.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity
public class RouteCost {
    @PrimaryKey
    public int routeId;
    public String from;
    public String fromNumber;
    public String to;
    public String toNumber;
    public String text_view_cost;
    // Другие поля, связанные с стоимостью маршрута
}

