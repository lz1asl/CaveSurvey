package com.astoev.cave.survey.activity.main;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.sql.SQLException;

/**
 * Handles manual entering of GPS location.
 *
 * @author Jivko Mitrev
 */
public class GPSManualActivity extends MainMenuActivity {

    private static final int NORTH_INDEX = 0;
    private static final int SOUTH_INDEX = 1;
    private static final int EAST_INDEX = 0;
    private static final int WEST_INDEX = 1;

    /** Point owner of the location*/
    private Point parentPoint;

    protected EditText latitudeView;
    protected EditText longitudeView;
    protected EditText altitudeView;
    protected EditText accuracyView;

    /**
     * @see com.astoev.cave.survey.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpsmanual);

        Bundle extras = getIntent().getExtras();
        parentPoint = (Point)extras.get(GPSActivity.POINT);

        com.astoev.cave.survey.model.Location location = null;
        if (parentPoint != null){
            try {
                location = DaoUtil.getLocationByPoint(parentPoint);
            } catch (SQLException sqle) {
                Log.e(Constants.LOG_TAG_UI, "Unable to load location", sqle);
            }
        }

        latitudeView = (EditText)findViewById(R.id.gps_manual_latitude);
        longitudeView = (EditText)findViewById(R.id.gps_manual_longitude);
        altitudeView = (EditText)findViewById(R.id.gps_manual_altitude);
        accuracyView = (EditText)findViewById(R.id.gps_manual_accuracy);

        int latDirection = NORTH_INDEX;// North by default
        int lonDirection = EAST_INDEX; // East by default

        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();

            // check if negative that represents South
            if (lat < 0) {
                latDirection = SOUTH_INDEX;
                lat = -lat;
            }

            // check if negative that represents West
            if (lon < 0) {
                lonDirection = WEST_INDEX;
                lon = -lon;
            }

            latitudeView.setText(String.valueOf(lat));
            longitudeView.setText(String.valueOf(lon));
            altitudeView.setText(String.valueOf(location.getAltitude()));
            accuracyView.setText(String.valueOf(location.getAccuracy()));


        }

        // apply initial values for the spinners
        prepareSpinner(R.id.gps_manual_latitude_type, latDirection);
        prepareSpinner(R.id.gps_manual_longitude_type, lonDirection);
    }

    /**
     * Prepares the spinner by applying the initial value.
     *
     * @param spinnerIdArg - spinner id to update
     * @param selectionArg - initial value selected
     * @return Spinner instance
     */
    private Spinner prepareSpinner(int spinnerIdArg, Integer selectionArg) {
        Spinner spinner = (Spinner) findViewById(spinnerIdArg);

        if (selectionArg != null) {
            spinner.setSelection(selectionArg);
        }
        return spinner;
    }

    /**
     * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
     */
    @Override
    protected String getScreenTitle() {
        return getString(R.string.gps_title);
    }

    /**
     * Using standard GPS menu
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
        Log.i(Constants.LOG_TAG_UI, "GPS manual activity's menu selected - " + itemArg.toString());

        switch (itemArg.getItemId()) {
            case R.id.gps_action_save: {
                saveLocation();
            }
            default:
                return super.onOptionsItemSelected(itemArg);
        }
    }

    /**
     * Action method to save the gps location. Reads the UI components and generates the model
     * instance
     */
    private void saveLocation(){

        boolean valid =  UIUtilities.validateNumber(latitudeView, true);
        valid = UIUtilities.validateNumber(longitudeView, true) && valid;
        valid = UIUtilities.validateNumber(altitudeView, true) && valid;

        if (valid) {

            int latDirection = (int)((Spinner)findViewById(R.id.gps_manual_latitude_type)).getSelectedItemId();
            int lonDirection = (int)((Spinner)findViewById(R.id.gps_manual_longitude_type)).getSelectedItemId();

            com.astoev.cave.survey.model.Location location = new com.astoev.cave.survey.model.Location();

            double lat = Double.parseDouble(latitudeView.getText().toString());
            // apply N/S before save
            if (latDirection == SOUTH_INDEX) {
                lat = -lat;
            }
            double lon = Double.parseDouble(longitudeView.getText().toString());

            // apply E/W before save
            if (lonDirection == WEST_INDEX) {
                lon = -lon;
            }
            int altitude = Integer.parseInt(altitudeView.getText().toString());

            int accuracy = 0;
            if (!StringUtils.isEmpty(accuracyView)) {
                accuracy = Integer.parseInt(accuracyView.getText().toString());
            }

            location.setLatitude(lat);
            location.setLongitude(lon);
            location.setAltitude(altitude);
            location.setAccuracy(accuracy);

            try {
                DaoUtil.saveLocationToPoint(parentPoint, location);
            } catch (SQLException sqle) {
                UIUtilities.showNotification(R.string.gps_db_error);
                Log.e(Constants.LOG_TAG_SERVICE, "Unable to save location for point:" + parentPoint, sqle);
            }
            finish();
        }
    }
}
