/**
 * 
 */
package com.astoev.cave.survey.activity.main;

import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.dialog.TurnOnGPSDialogFragment;
import com.astoev.cave.survey.fragment.LocationFragment;
import com.astoev.cave.survey.fragment.UpdatebleLocationFragment;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.service.gps.GPSProcessor;
import com.astoev.cave.survey.service.gps.LocationListenerAdapter;
import com.astoev.cave.survey.util.DaoUtil;

import java.sql.SQLException;

/**
 * Activity that handles capturing GPS location
 * 
 * @author jmitrev
 */
public class GPSActivity extends MainMenuActivity {

    /** Dialog name for enable GPS dialog */
    private static final String GPS_DIALOG = "GPS_DIALOG";
    
    /** Intent key where the parent point is placed*/
    public static final String POINT = "POINT";
    
    /** GPS processor to handle the work with GPS */
    private GPSProcessor gpsProcessor;
    
    /** Flag if GPS enable is already requested */
    private boolean gpsRequested = false;
    
    /** Point owner of the location*/
    private Point parentPoint;
    
    private boolean hasLocation;
    
	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps);
		
        Bundle extras = getIntent().getExtras();
        parentPoint = (Point)extras.get(POINT);
        
		gpsProcessor = getGPSProcessor();
		
		initSavedLocationContainer(parentPoint, this, savedInstanceState);
	}

	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		GPSProcessor gpsProcessor = getGPSProcessor();

		if (!getGPSProcessor().canRead()) {
		    
		    if (!gpsRequested){
		        // one request to turn on gps if disabled on settings page
    		    gpsRequested = true;
    		    TurnOnGPSDialogFragment turnOnGPSDialog = new TurnOnGPSDialogFragment();
    		    turnOnGPSDialog.show(getSupportFragmentManager(), GPS_DIALOG);
		    } 
		    
		    // show GPS disabled layout
		    gpsDisabled();
		}else {
		    
		    waitingForSignal();
		}
		
		gpsProcessor.startListening();
	}

	/**
	 * @see FragmentActivity#onPause()
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
            case R.id.gps_action_save: {
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
        
        UpdatebleLocationFragment fragment = (UpdatebleLocationFragment)getSupportFragmentManager().findFragmentById(R.id.current_location_container);
        Location lastLocation = fragment.getLastLocation();

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
    
    /**
     * Helper method that obtains a GPS processor
     * 
     * @return GPSProcessor with attached listener
     */
    private GPSProcessor getGPSProcessor(){
        if (gpsProcessor == null){
            LocationListenerAdapter listener = new LocationListenerAdapter(){

                @Override
                public void onStatusChanged(String providerArg, int statusArg, Bundle extrasArg) {
                    switch (statusArg) {
                    case LocationProvider.AVAILABLE:
                        Log.d(Constants.LOG_TAG_UI, "onStatusChanged: AVAILABLE :" + statusArg);
                        signalFound();
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        Log.d(Constants.LOG_TAG_UI, "onStatusChanged: TEMPORARILY_UNAVAILABLE :" + statusArg);
                        waitingForSignal();
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        Log.d(Constants.LOG_TAG_UI, "onStatusChanged: OUT_OF_SERVICE :" + statusArg);
                        waitingForSignal();
                        break;
                    default:
                        Log.d(Constants.LOG_TAG_UI, "onStatusChanged: UNKNOWN :" + statusArg);
                    }
                }

                /**
                 * @see com.astoev.cave.survey.service.gps.LocationListenerAdapter#onLocationChanged(android.location.Location)
                 */
                @Override
                public void onLocationChanged(Location locationArg) {
                    if (!hasLocation){
                        // hack for Hero I do not receive the status updates
                        hasLocation = true;
                        signalFound();
                    }
                }
            };
            
            gpsProcessor = new GPSProcessor(this, listener);
        }
        return gpsProcessor;
    }
    
    private void gpsDisabled(){
        findViewById(R.id.no_gps_include).setVisibility(View.VISIBLE);
        findViewById(R.id.no_gps_signal_include).setVisibility(View.GONE);
        findViewById(R.id.current_location_container).setVisibility(View.GONE);
        hasLocation = false;
    }
    
    private void waitingForSignal(){
        findViewById(R.id.no_gps_include).setVisibility(View.GONE);
        findViewById(R.id.no_gps_signal_include).setVisibility(View.VISIBLE);
        findViewById(R.id.current_location_container).setVisibility(View.GONE);
        hasLocation = false;
    }
    
    private void signalFound(){
        findViewById(R.id.no_gps_include).setVisibility(View.GONE);
        findViewById(R.id.no_gps_signal_include).setVisibility(View.GONE);
        findViewById(R.id.current_location_container).setVisibility(View.VISIBLE);
        hasLocation = true;
    }

    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }

    /**
     * Helper method that defines how to initialize saved_location_container fragment with Location
     * 
     * @param parentPoint - parent point
     * @param activity - parent activity
     * @param savedInstanceState - Bundle
     * @return Location if available for the parent point
     */
    public static com.astoev.cave.survey.model.Location initSavedLocationContainer(Point parentPoint, MainMenuActivity activity, Bundle savedInstanceState){
        com.astoev.cave.survey.model.Location currentLocation = null;
        if (parentPoint != null){
            try {
                currentLocation = DaoUtil.getLocationByPoint(parentPoint);
            } catch (SQLException sqle) {
                Log.e(Constants.LOG_TAG_UI, "Unable to load location", sqle);
            }
        }
        if (/*findViewById(R.id.current_location_container) != null &&*/ currentLocation != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return null;
            }
            
            LocationFragment locationFragment = new LocationFragment();
            
            Bundle bundle = new Bundle();
            bundle.putSerializable(LocationFragment.LOCATION_KEY, currentLocation);
            locationFragment.setArguments(bundle);
            
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.saved_location_container, locationFragment);
            
            transaction.commit();
        }
        return currentLocation;
    }
}
