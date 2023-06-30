package com.taxieasyua.back4app.ui.open_map;


import android.content.Context;
import android.graphics.Region;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;

import com.taxieasyua.back4app.R;

import org.json.JSONException;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

public class MarkerOverlay extends Overlay {

    public static Marker marker;

    public MarkerOverlay(Context context) {
        super(context);
        marker = null;
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {
        if (marker != null) {
            mapView.getOverlays().remove(marker);
        }


        OpenStreetMapActivity.endPoint = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
        String target = OpenStreetMapActivity.epm;
        OpenStreetMapActivity.setMarker(OpenStreetMapActivity.endPoint.getLatitude(), OpenStreetMapActivity.endPoint.getLongitude(), target);
        try {
            OpenStreetMapActivity.dialogMarkers();
        } catch (MalformedURLException | JSONException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return true;
    }


}

