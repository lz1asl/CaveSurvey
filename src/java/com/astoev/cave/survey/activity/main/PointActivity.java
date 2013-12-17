package com.astoev.cave.survey.activity.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.draw.DrawingActivity;
import com.astoev.cave.survey.exception.DataException;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import com.astoev.cave.survey.util.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/17/12
 * Time: 1:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class PointActivity extends MainMenuActivity {

    private Leg mLegEdited;
    private ResultReceiver receiver = new ResultReceiver(new Handler()) {

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            float aMeasure = resultData.getFloat("result");
            Constants.Measures type = Constants.Measures.valueOf(resultData.getString("type"));
            switch (type) {
                case distance:
                    Log.i(Constants.LOG_TAG_UI, "Got distance " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_distance);
                    break;

                case angle:
                    Log.i(Constants.LOG_TAG_UI, "Got angle " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_azimuth);
                    break;

                case slope:
                    Log.i(Constants.LOG_TAG_UI, "Got slope " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_slope);
                    break;

                case up:
                    Log.i(Constants.LOG_TAG_UI, "Got up " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_up);
                    break;

                case down:
                    Log.i(Constants.LOG_TAG_UI, "Got down " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_down);
                    break;

                case left:
                    Log.i(Constants.LOG_TAG_UI, "Got left " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_left);
                    break;

                case right:
                    Log.i(Constants.LOG_TAG_UI, "Got right " + aMeasure);
                    populateMeasure(aMeasure, R.id.point_right);
                    break;

                default:
                    Log.i(Constants.LOG_TAG_UI, "Ignore type " + type);
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.point);

        loadPointData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadPointData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPointData();
    }

    private void loadPointData() {
        Log.i(Constants.LOG_TAG_UI, "Initialize point view");

        try {
            Bundle extras = getIntent().getExtras();
            int legId = extras.getInt(Constants.LEG_SELECTED);
            mLegEdited = (Leg) mWorkspace.getDBHelper().getLegDao().queryForId(legId);

            // label
            TextView leg = (TextView) findViewById(R.id.point_curr_leg);
            leg.setText(mLegEdited.buildLegDescription(this));

            // up
            EditText up = (EditText) findViewById(R.id.point_up);
            setNotNull(up, mLegEdited.getTop());
            addOnClickListener(up, Constants.Measures.up);

            // down
            EditText down = (EditText) findViewById(R.id.point_down);
            setNotNull(down, mLegEdited.getDown());
            addOnClickListener(down, Constants.Measures.down);

            // left
            EditText left = (EditText) findViewById(R.id.point_left);
            setNotNull(left, mLegEdited.getLeft());
            addOnClickListener(left, Constants.Measures.left);

            // right
            EditText right = (EditText) findViewById(R.id.point_right);
            setNotNull(right, mLegEdited.getRight());
            addOnClickListener(right, Constants.Measures.right);

            // distance
            EditText distance = (EditText) findViewById(R.id.point_distance);
            setNotNull(distance, mLegEdited.getDistance());
            addOnClickListener(distance, Constants.Measures.distance);

            // azimuth
            EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
            setNotNull(azimuth, mLegEdited.getAzimuth());
            addOnClickListener(azimuth, Constants.Measures.angle);

            // slope
            EditText slope = (EditText) findViewById(R.id.point_slope);
            slope.setText("0");
            setNotNull(slope, mLegEdited.getSlope());
            addOnClickListener(slope, Constants.Measures.slope);

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render point", e);
            UIUtilities.showNotification(this, R.string.error);
        }
    }

    private void addOnClickListener(EditText text, final Constants.Measures aMeasure) {

        if (BluetoothService.isBluetoothSupported()) {

            if (!ensureDeviceSelected()) {
                return;
            }

            switch (aMeasure) {
                case distance:
                case up:
                case down:
                case left:
                case right:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_DISTANCE_SENSOR))) {
                        return;
                    }
                    break;

                case angle:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_AZIMUTH_SENSOR))) {
                        return;
                    }
                    break;

                case slope:
                    if (!Option.CODE_SENSOR_BLUETOOTH.equals(Options.getOptionValue(Option.CODE_SLOPE_SENSOR))) {
                        return;
                    }
                    break;
            }
        }

        // supported for the measure, add the listener
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerBluetoothMeasure(aMeasure);
            }
        });
    }

    private void setNotNull(EditText aEditText, Float aValue) {
        if (aValue != null) {
            aEditText.setText(StringUtils.floatToLabel(aValue));
        } else {
            aEditText.setText("");
        }
    }

    private boolean saveLeg() {
        try {
            Log.i(Constants.LOG_TAG_UI, "Saving leg");
            // distance
            EditText distance = (EditText) findViewById(R.id.point_distance);
            if (distance.getText().toString().trim().equals("")) {
                distance.setError(getString(R.string.required));
                return false;
            }
            mLegEdited.setDistance(StringUtils.getFromEditTextNotNull(distance));

            // compass
            EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
            if (azimuth.getText().toString().trim().equals("")) {
                azimuth.setError(getString(R.string.required));
                return false;
            }
            mLegEdited.setAzimuth(checkAzimuth(azimuth));

            // slope
            EditText slope = (EditText) findViewById(R.id.point_slope);
            mLegEdited.setSlope(StringUtils.getFromEditTextNotNull(slope));

            // up
            EditText up = (EditText) findViewById(R.id.point_up);
            mLegEdited.setTop(StringUtils.getFromEditTextNotNull(up));

            // down
            EditText down = (EditText) findViewById(R.id.point_down);
            mLegEdited.setDown(StringUtils.getFromEditTextNotNull(down));

            // left
            EditText left = (EditText) findViewById(R.id.point_left);
            mLegEdited.setLeft(StringUtils.getFromEditTextNotNull(left));

            // right
            EditText right = (EditText) findViewById(R.id.point_right);
            mLegEdited.setRight(StringUtils.getFromEditTextNotNull(right));

            // save
            mWorkspace.getDBHelper().getLegDao().update(mLegEdited);
            UIUtilities.showNotification(this, R.string.todo);

            Log.i(Constants.LOG_TAG_UI, "Saved");
            return true;
        } catch (DataException de) {
            Log.e(Constants.LOG_TAG_UI, "Leg not saved", de);
            String message = getString(R.string.popup_bad_input) + " : " + de.getMessage();
            UIUtilities.showNotification(this, message);
        } catch (Exception e) {
            UIUtilities.showNotification(this, R.string.error);
            Log.e(Constants.LOG_TAG_UI, "Leg not saved", e);
        }
        return false;
    }

    private Float checkAzimuth(EditText aEditText) throws DataException {
        Float azimuth = StringUtils.getFromEditTextNotNull(aEditText);
        if (null != azimuth) {
            if (azimuth.floatValue() < 0) {
                throw new DataException(getString(R.string.main_table_header_azimuth));
            }

            String currAzimuthMeasure = Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);
            int maxValue;
            if (Option.UNIT_DEGREES.equals(currAzimuthMeasure)) {
                maxValue = Option.MAX_VALUE_DEGREES;
            } else { // Option.UNIT_GRADS
                maxValue = Option.MAX_VALUE_GRADS;
            }
            if (azimuth.floatValue() > maxValue) {
                throw new DataException(getString(R.string.main_table_header_azimuth));
            }
        }

        return azimuth;
    }

    //TODO remove View attribute when migrated to ActionBar
    public void noteButton(View view) {
        Intent intent = new Intent(this, NoteActivity.class);
        startActivity(intent);
    }

    //TODO remove View attribute when migrated to ActionBar
    public void saveButton(View view) {
        if (saveLeg()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    // TODO remove View attribute when migrated to ActionBar
    public void drawingButton(View view) {
        Intent intent = new Intent(this, DrawingActivity.class);
        startActivity(intent);
    }

    // TODO remove View attribute when migrated to ActionBar
    public void coordinateButton(View view) {
        // TODO location http://www.tutorialforandroid.com/2009/05/permissions-journey-accesscoarselocatio.html
        UIUtilities.showNotification(this, R.string.todo);
    }

    // TODO remove View attribute when migrated to ActionBar
    public void deleteButton(View view) {
        // TODO
        UIUtilities.showNotification(this, R.string.todo);
    }

    private void triggerBluetoothMeasure(Constants.Measures aMeasure) {
        // register listeners & send command
        BluetoothService.sendReadDistanceCommand(receiver, aMeasure);
        Log.i(Constants.LOG_TAG_UI, "Command sent for " + aMeasure);
    }

    private void populateMeasure(float aMeasure, int anEditTextId) {
        EditText up = (EditText) findViewById(anEditTextId);
        setNotNull(up, aMeasure);
    }

    private boolean ensureDeviceSelected() {
        if (BluetoothService.isDeviceSelected()) {
            return true;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.bt_not_selected)
                .setCancelable(false)
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(PointActivity.this, BTActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = dialogBuilder.create();
        alert.show();
        return false;
    }

    public void readAzimuth(View view) {
        Intent intent = new Intent(this, ReadAzimuthActivity.class);
        startActivityForResult(intent, 1);
        EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
        azimuth.setText(intent.getStringExtra("Azimuth"));

    }

    public void readSlope(View view) {
        // TODO
        UIUtilities.showNotification(this, R.string.todo);
    }

    // TODO remove View attribute when migrated to ActionBar
    public void photoButton(View view) {
        // picture http://www.tutorialforandroid.com/2010/10/take-picture-in-android-with.html

        final File path = new File(Environment.getExternalStorageDirectory(), "CaveSurvey");
        if (!path.exists()) {
            path.mkdir();
        }

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(path, "photo.tmp")));
        startActivityForResult(intent, 1);

    }

    // photo is captured
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Log.i(Constants.LOG_TAG_SERVICE, "Got image");
                    final File file = new File(new File(Environment.getExternalStorageDirectory(), "CaveSurvey"), "photo.tmp");
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(file);
                        Photo photo = new Photo();
                        photo.setPictureBytes(IOUtils.toByteArray(in));

                        Point currPoint = (Point) mWorkspace.getDBHelper().getPointDao().queryForId(mLegEdited.getFromPoint().getId());
                        photo.setPoint(currPoint);

                        mWorkspace.getDBHelper().getPhotoDao().create(photo);

                        Log.i(Constants.LOG_TAG_SERVICE, "Image stored");
                    } catch (Exception e) {
                        Log.e(Constants.LOG_TAG_UI, "Picture not saved", e);
                        UIUtilities.showNotification(this, R.string.error);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
	 */
	@Override
	protected int getChildsOptionsMenu() {
		return R.menu.pointmenu;
	}

	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(Constants.LOG_TAG_UI, "Point activity's menu selected - " + item.toString());
		
		switch (item.getItemId()) {
			case R.id.point_action_save:{
				saveButton(null);
				return true;
			}
			case R.id.point_action_note:{
				noteButton(null);
				return true;
			}
			case R.id.point_action_draw : {
				drawingButton(null);
				return true;
			}
			case R.id.point_action_gps : {
				coordinateButton(null);
				return true;
			}
			case R.id.point_action_photo : {
				photoButton(null);
				return true;
			}
			case R.id.point_action_delete : {
				deleteButton(null);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}	
	}

}
