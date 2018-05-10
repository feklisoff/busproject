package ca.ubc.cs.cpsc210.translink.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.R;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.HashMap;
import java.util.Map;

import static ca.ubc.cs.cpsc210.translink.util.Geometry.gpFromLatLon;
import static ca.ubc.cs.cpsc210.translink.util.Geometry.rectangleContainsPoint;

// A plotter for bus stop locations
public class BusStopPlotter extends MapViewOverlay {
    /**
     * clusterer
     */
    private RadiusMarkerClusterer stopClusterer;
    /**
     * maps each stop to corresponding marker on map
     */
    private Map<Stop, Marker> stopMarkerMap = new HashMap<>();
    /**
     * marker for stop that is nearest to user (null if no such stop)
     */
    private Marker nearestStnMarker;
    private Activity activity;
    private StopInfoWindow stopInfoWindow;

    /**
     * Constructor
     *
     * @param activity the application context
     * @param mapView  the map view on which buses are to be plotted
     */
    public BusStopPlotter(Activity activity, MapView mapView) {
        super(activity.getApplicationContext(), mapView);
        this.activity = activity;
        nearestStnMarker = null;
        stopInfoWindow = new StopInfoWindow((StopSelectionListener) activity, mapView);
        newStopClusterer();
    }

    public RadiusMarkerClusterer getStopClusterer() {
        return stopClusterer;
    }

    /**
     * Mark all visible stops in stop manager onto map.
     */
    public void markStops(Location currentLocation) {
        Drawable stopIconDrawable = activity.getResources().getDrawable(R.drawable.stop_icon);
        newStopClusterer();
        clearMarkers();
        updateVisibleArea();
        for (Stop s: StopManager.getInstance()) {
            if (rectangleContainsPoint(northWest, southEast, s.getLocn())) {
                if (nearestStnMarker != null && s.equals(nearestStnMarker.getRelatedObject())) {
                    stopClusterer.add(nearestStnMarker);
                } else {
                    Marker m = new Marker(mapView);
                    String title = s.getName() + Integer.toString(s.getNumber());
                    setUpMarker(m, stopIconDrawable, title, s);
                }
            }
        }
    }

    private void setUpMarker(Marker m, Drawable stopIconDrawable, String title, Stop s) {
        GeoPoint posn = gpFromLatLon(s.getLocn());
        m.setPosition(posn);
        m.setRelatedObject(s);
        m.setIcon(stopIconDrawable);
        m.setTitle(title);
        m.setInfoWindow(stopInfoWindow);
        stopClusterer.add(m);
        setMarker(s, m);
    }


    /**
     * Create a new stop cluster object used to group stops that are close by to reduce screen clutter
     */
    private void newStopClusterer() {
        stopClusterer = new RadiusMarkerClusterer(activity);
        stopClusterer.getTextPaint().setTextSize(20.0F * BusesAreUs.dpiFactor());
        int zoom = mapView == null ? 16 : mapView.getZoomLevel();
        if (zoom == 0) {
            zoom = MapDisplayFragment.DEFAULT_ZOOM;
        }
        int radius = 1000 / zoom;

        stopClusterer.setRadius(radius);
        Drawable clusterIconD = activity.getResources().getDrawable(R.drawable.stop_cluster);
        Bitmap clusterIcon = ((BitmapDrawable) clusterIconD).getBitmap();
        stopClusterer.setIcon(clusterIcon);
    }

    /**
     * Update marker of nearest stop (called when user's location has changed).  If nearest is null,
     * no stop is marked as the nearest stop.
     *
     * @param nearest stop nearest to user's location (null if no stop within StopManager.RADIUS metres)
     */
    public void updateMarkerOfNearest(Stop nearest) {
        Drawable stopIconDrawable = activity.getResources().getDrawable(R.drawable.stop_icon);
        Drawable closestStopIconDrawable = activity.getResources().getDrawable(R.drawable.closest_stop_icon);
        if (nearest != null) {
            GeoPoint p = gpFromLatLon(nearest.getLocn());
            String title = nearest.getNumber() + " " + nearest.getName();
            nearestStnMarker = new Marker(mapView);
            setUpNearestMarker(p,nearest,closestStopIconDrawable, title);
            //mapView.getOverlays().add(nearestStnMarker);
        }
    }

    private void setUpNearestMarker(GeoPoint p, Stop nearest, Drawable closestStopIconDrawable, String title) {
        nearestStnMarker.setTitle(title);
        nearestStnMarker.setPosition(p);
        nearestStnMarker.setIcon(closestStopIconDrawable);
        nearestStnMarker.setRelatedObject(nearest);
        stopClusterer.add(nearestStnMarker);
        //setMarker(nearest, nearestStnMarker);
    }

    /**
     * Manage mapping from stops to markers using a map from stops to markers.
     * The mapping in the other direction is done using the Marker.setRelatedObject() and
     * Marker.getRelatedObject() methods.
     */
    private Marker getMarker(Stop stop) {
        return stopMarkerMap.get(stop);
    }

    private void setMarker(Stop stop, Marker marker) {
        stopMarkerMap.put(stop, marker);
    }

    private void clearMarker(Stop stop) {
        stopMarkerMap.remove(stop);
    }

    private void clearMarkers() {
        stopMarkerMap.clear();
    }
}
