/**
 * 
 */
package com.astoev.cave.survey.activity.main;

import java.util.zip.Inflater;

import com.astoev.cave.survey.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * @author jmitrev
 */
public class LocationFragment extends Fragment {

    public static final String LOCATION_KEY = "LOCATION";
    
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView altitudeView;
    private TextView accuracyView;

    
    /**
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflaterArg,
            ViewGroup containerArg, Bundle savedInstanceStateArg) {

        View view = inflaterArg.inflate(R.layout.location_fragment, containerArg, false);
        
        latitudeView  = (TextView)view.findViewById(R.id.gps_latitude);
        longitudeView = (TextView)view.findViewById(R.id.gps_longitude);
        altitudeView  = (TextView)view.findViewById(R.id.gps_altitude);
        accuracyView  = (TextView)view.findViewById(R.id.gps_accuracy);
        
        Bundle arguments = getArguments();
        if (arguments != null && arguments.get(LOCATION_KEY) != null){
//            latitudeView
        }
        
        return view;

    }

    
    
}
