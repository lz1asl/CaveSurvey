/**
 * 
 */
package com.astoev.cave.survey.service.gps;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Adapter for LocationListener
 * 
 * @author jmitrev
 */
public class LocationListenerAdapter implements LocationListener {

    /**
     * @see android.location.LocationListener#onLocationChanged(android.location.Location)
     */
    @Override
    public void onLocationChanged(Location locationArg) {
    }

    /**
     * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String providerArg, int statusArg, Bundle extrasArg) {
    }

    /**
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String providerArg) {
    }

    /**
     * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String providerArg) {
    }

}
