/**
 * 
 */
package com.astoev.cave.survey.activity.main;

import java.sql.SQLException;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.dialog.TurnOnGPSDialogFragment;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.service.gps.GPSProcessor;
import com.astoev.cave.survey.service.gps.LocationListenerAdapter;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.LocationUtil;

/**
 * Activity that handles capturing GPS location
 * 
 * @author jmitrev
 */
public class GPSActivity extends MainMenuActivity {

//    private static final String N = "N";
//    private static final String S = "S";
//    private static final String E = "E";
//    private static final String W = "W";
    
    /** Dialog name for enable GPS dialog */
    private static final String GPS_DIALOG = "GPS_DIALOG";
    
    /** Intent key where the parent point is placed*/
    public static final String POINT = "POINT";
    
    /** GPS processor to handle the work with GPS */
    private GPSProcessor gpsProcessor;
    
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView altitudeView;
    private TextView accuracyView;
    
//    /** Template for formatting latitude and longitude */
//    private String latLonTemplate = "%f\u00B0%s";

    /** Flag if GPS enable is already requested */
    private boolean gpsRequested = false;
    
    /** Current location from the location listener*/
    private Location lastLocation;
    
    /** Point owner of the location*/
    private Point parentPoint;
    
	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps);
		
        Bundle extras = getIntent().getExtras();
        parentPoint = (Point)extras.get(POINT);

		latitudeView  = (TextView)findViewById(R.id.gps_latitude);
		longitudeView = (TextView)findViewById(R.id.gps_longitude);
		altitudeView  = (TextView)findViewById(R.id.gps_altitude);
		accuracyView  = (TextView)findViewById(R.id.gps_accuracy);
		
		LocationListenerAdapter listener = new LocationListenerAdapter(){

            @Override
            public void onLocationChanged(Location locationArg) {
                
                // latitude
                final double latitude = locationArg.getLatitude();
                String lat = LocationUtil.formatLatitude(latitude);
//                if (latitude > 0){
//                    lat = String.format(latLonTemplate, latitude, N);
//                    
//                } else {
//                    lat = String.format(latLonTemplate, -latitude, S);
//                }
                latitudeView.setText(lat);
                
                // longitude
                double longitude = locationArg.getLongitude();
                String lon = LocationUtil.formatLongitude(longitude);
//                if (longitude > 0){
//                    lon = String.format(latLonTemplate, longitude, E);
//                } else {
//                    lon = String.format(latLonTemplate, -longitude, W);
//                }
                longitudeView.setText(lon);
                
                // altitude
                altitudeView.setText(String.valueOf((int)locationArg.getAltitude()));
                
                // accuracy
                accuracyView.setText(String.valueOf((int)locationArg.getAccuracy()));
                
                lastLocation = locationArg;
            }
		};
		
		gpsProcessor = new GPSProcessor(this, listener);
	}

	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		//TODO implement with dialog!
		if (!gpsProcessor.canRead()){
		    
		    if (!gpsRequested){
		        // one request to turn on gps if disabled on settings page
    		    gpsRequested = true;
    		    TurnOnGPSDialogFragment turnOnGPSDialog = new TurnOnGPSDialogFragment();
    		    turnOnGPSDialog.show(getSupportFragmentManager(), GPS_DIALOG);
		    } 
		    
		    // show GPS disabled layout
	        findViewById(R.id.no_gps_include).setVisibility(View.VISIBLE);
	        findViewById(R.id.gps_located).setVisibility(View.GONE);
		    
//		} 
//		else if (lastLocation != null){
//		    // TODO enable waiting layout
//	          findViewById(R.id.no_gps_include).setVisibility(View.INVISIBLE);
//	          findViewById(R.id.gps_located).setVisibility(View.VISIBLE);
		}else {
		    //TODO enable main Layout
            findViewById(R.id.no_gps_include).setVisibility(View.GONE);
            findViewById(R.id.gps_located).setVisibility(View.VISIBLE);
		}
		
		gpsProcessor.startListening();
	}

	/**
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		gpsProcessor.stopListening();
	}

	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
	 */
	@Override
	protected String getScreenTitle() {
		return getString(R.string.gps_title);
	}
	
    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
     */
    @Override
    protected int getChildsOptionsMenu() {
        return R.menu.gpsmenu;
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem itemArg) {
        Log.i(Constants.LOG_TAG_UI, "GPS activity's menu selected - " + itemArg.toString());

        switch (itemArg.getItemId()) {
            case R.id.note_action_save: {
                saveLocation();
            }
            default:
                return super.onOptionsItemSelected(itemArg);
        }
    }
    
    /**
     * Action method to save the gps location
     */
    private void saveLocation(){
        
        if (lastLocation != null){
            try {
                DaoUtil.saveLocationToPoint(parentPoint, lastLocation);
            } catch (SQLException sqle) {
                UIUtilities.showNotification(R.string.gps_db_error);
                Log.e(Constants.LOG_TAG_SERVICE, "Unable to save location for point:" + parentPoint, sqle);
            }
            finish();
        } else {
            UIUtilities.showNotification(R.string.gps_error_no_location);
        }
    }
}
