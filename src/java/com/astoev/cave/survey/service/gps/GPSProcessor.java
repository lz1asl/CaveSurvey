/**
 * 
 */
package com.astoev.cave.survey.service.gps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Handles the service work for capturing location
 * 
 * @author jmitrev
 */
public class GPSProcessor implements LocationListener {

	private Context context;
	
	private LocationManager locationManager;
	
	private LocationListener listener;
	
	/**
	 * 
	 * @param contextArg
	 */
	public  GPSProcessor(Context contextArg, LocationListener listenerArg){
	    context = contextArg;
	    listener = listenerArg;
	    
	    locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void startListening(){
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	
	public void stopListening(){
	    locationManager.removeUpdates(this);
	}
	
	public boolean canRead(){
	    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

    /**
     * @see android.location.LocationListener#onLocationChanged(android.location.Location)
     */
    @Override
    public void onLocationChanged(Location locationArg) {
        if (listener != null){
            listener.onLocationChanged(locationArg);
        }
    }

    /**
     * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
     */
    @Override
    public void onStatusChanged(String providerArg, int statusArg, Bundle extrasArg) {
        // TODO Auto-generated method stub
    }

    /**
     * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
     */
    @Override
    public void onProviderEnabled(String providerArg) {
        // TODO Auto-generated method stub
    }

    /**
     * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
     */
    @Override
    public void onProviderDisabled(String providerArg) {
        // TODO Auto-generated method stub
    }
}
