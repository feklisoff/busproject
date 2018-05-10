package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.*;
import ca.ubc.cs.cpsc210.translink.util.Geometry;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ca.ubc.cs.cpsc210.translink.util.Geometry.gpFromLatLon;
import static ca.ubc.cs.cpsc210.translink.util.Geometry.rectangleContainsPoint;
import static ca.ubc.cs.cpsc210.translink.util.Geometry.rectangleIntersectsLine;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /**
     * overlay used to display bus route legend text on a layer above the map
     */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /**
     * overlays used to plot bus routes
     */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     *
     * @param context the application context
     * @param mapView the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {
        updateToClear();
        if (StopManager.getInstance().getSelected() != null) {
            for (Route r: StopManager.getInstance().getSelected().getRoutes()) {
                int color = busRouteLegendOverlay.add(r.getNumber());
                for (RoutePattern rp : r.getPatterns()) {
                    for (int j = 0; j < rp.getPath().size() - 1; j++) {
                        if (rectangleIntersectsLine(northWest, southEast, rp.getPath().get(j),
                                rp.getPath().get(j + 1))) {
                            setUpLine(color, zoomLevel, retList(rp.getPath().get(j),
                                    rp.getPath().get(j + 1)), new Polyline(context));
                        }
                    }
                }
            }
        }
    }

    private List<GeoPoint> retList(LatLon l1, LatLon l2) {
        List<GeoPoint> gpList = new ArrayList<>();
        GeoPoint g1 = Geometry.gpFromLatLon(l1);
        GeoPoint g2 = Geometry.gpFromLatLon(l2);
        gpList.add(g1);
        gpList.add(g2);
        return gpList;
    }

    private void updateToClear() {
        busRouteLegendOverlay.clear();
        busRouteOverlays.clear();
        updateVisibleArea();
    }

    private void setUpLine(int color, int zoomLevel, List<GeoPoint> geoPointList, Polyline line) {
        line.setPoints(geoPointList);
        line.setWidth(getLineWidth(zoomLevel));
        line.setColor(color);
        busRouteOverlays.add(line);
    }
    /**
     * Gets points for the route
     * @param rp -  the route pattern being used
     * @param geoPointList - list of points to plot
     */

    public void getPoints(RoutePattern rp, List<GeoPoint> geoPointList) {
        for (LatLon p : rp.getPath()) {
            if (rectangleContainsPoint(northWest, southEast, p)) {
                GeoPoint geoPoint = gpFromLatLon(p);
                geoPointList.add(geoPoint);
            }
        }
    }

    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     *
     * @param zoomLevel the zoom level of the map
     * @return width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if (zoomLevel > 14) {
            return 7.0f * BusesAreUs.dpiFactor();
        } else if (zoomLevel > 10) {
            return 5.0f * BusesAreUs.dpiFactor();
        } else {
            return 2.0f * BusesAreUs.dpiFactor();
        }
    }
}
