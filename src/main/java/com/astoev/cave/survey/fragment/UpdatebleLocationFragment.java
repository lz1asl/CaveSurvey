/**
 * 
 */
package com.astoev.cave.survey.fragment;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.service.gps.GPSProcessor;
import com.astoev.cave.survey.service.gps.LocationListenerAdapter;
import com.astoev.cave.survey.util.LocationUtil;

/**
 * Fragment that shows current location. The Location is updated with current values sent from GPSProcessor
 * 
 * @author jmitrev
 */
public class UpdatebleLocationFragment extends LocationFragment {

    /** GPS processor to handle the work with GPS */
    private GPSProcessor gpsProcessor;
    
    /** Last read location*/
    private Location lastLocation;
    
    /**
     * @see com.astoev.cave.survey.fragment.LocationFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflaterArg, ViewGroup containerArg, Bundle savedInstanceStateArg) {
        View view = super.onCreateView(inflaterArg, containerArg, savedInstanceStateArg);
        
        // update title of the fragment
        TextView title = (TextView)view.findViewById(R.id.location_fragment_title);
        title.setText(getString(R.string.gps_current_location));
        return view;
    }

    /**
     * @see Fragment#onPause()
     */
    @Override
    public void onPause() {
        gpsProcessor.stopListening();
        super.onPause();
    }

    /**
     * @see Fragment#onResume()
     */
    @Override
    public void onResume() {
        getGPSProcessor().startListening();
        super.onResume();
    }

    /**
     * Provides correctly initiated GPSProcessor
     * 
     * @return GPSProcessor
     */
    private GPSProcessor getGPSProcessor(){
        if (gpsProcessor == null){
            LocationListenerAdapter listener = new LocationListenerAdapter(){

                @Override
                public void onLocationChanged(Location locationArg) {
                    
                    // latitude
                    final double latitude = locationArg.getLatitude();
                    String lat = LocationUtil.formatLatitude(latitude);
                    latitudeView.setText(lat);
                    
                    // longitude
                    double longitude = locationArg.getLongitude();
                    String lon = LocationUtil.formatLongitude(longitude);
                    longitudeView.setText(lon);
                    
                    // altitude
                    altitudeView.setText(String.valueOf((int)locationArg.getAltitude()));
                    
                    // accuracy
                    accuracyView.setText(String.valueOf((int)locationArg.getAccuracy()));
                    
                    lastLocation = locationArg;
                }
            };
            
            gpsProcessor = new GPSProcessor(getActivity(), listener);
        }
        return gpsProcessor;
    }

    /**
     * Provides the last read location instance 
     * 
     * @return the lastLocation
     */
    public Location getLastLocation() {
        return lastLocation;
    }
    
}
