package com.taxieasyua.back4app.ui.open_map;


import android.content.Context;
import android.graphics.Color;
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

public class MarkerOverlay extends Overlay {
    Marker marker;
    public MarkerOverlay(Context context) {
        super(context);
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {

        OpenStreetMapActivity.progressBar.setVisibility(View.VISIBLE);
    // Удаление старого маркера
        if(marker != null) {
            mapView.getOverlays().remove(marker);
            mapView.invalidate();
            marker = null;
        }
        mapView.invalidate();
        OpenStreetMapActivity.m = null;

        GeoPoint endPoint = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
        OpenStreetMapActivity.endPoint = endPoint;

        String title = OpenStreetMapActivity.epm;


        marker = new Marker(OpenStreetMapActivity.map);
        marker.setPosition(new GeoPoint(endPoint.getLatitude(), endPoint.getLongitude()));
        marker.setTextLabelBackgroundColor(
                Color.TRANSPARENT
        );
        marker.setTextLabelForegroundColor(
                Color.RED
        );
        marker.setTextLabelFontSize(40);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setTitle(title);
        OpenStreetMapActivity.map.getOverlays().add(marker);
        OpenStreetMapActivity.map.invalidate();

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                OpenStreetMapActivity.dialogMarkers(OpenStreetMapActivity.fragmentManager, OpenStreetMapActivity.map.getContext());
            }
        } catch (MalformedURLException | JSONException | InterruptedException e) {
            Log.d("TAG", "onCreate:" + new RuntimeException(e));
        }

        return true;
    }


}

