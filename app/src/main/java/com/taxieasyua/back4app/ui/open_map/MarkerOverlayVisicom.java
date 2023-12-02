package com.taxieasyua.back4app.ui.open_map;


import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.net.MalformedURLException;

public class MarkerOverlayVisicom extends Overlay {
    Marker marker;
    public MarkerOverlayVisicom(Context context) {
        super(context);
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {


    // Удаление старого маркера
        if(marker != null) {
            mapView.getOverlays().remove(marker);
            mapView.invalidate();
            marker = null;
        }
        mapView.invalidate();
        OpenStreetMapVisicomActivity.m = null;

        GeoPoint endPoint = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
        OpenStreetMapVisicomActivity.endPoint = endPoint;

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                OpenStreetMapVisicomActivity.dialogMarkers(OpenStreetMapVisicomActivity.fragmentManager, OpenStreetMapVisicomActivity.map.getContext());
            }
        } catch (MalformedURLException | JSONException | InterruptedException e) {
            Log.d("TAG", "onCreate:" + new RuntimeException(e));
        }

        return true;
    }


}

