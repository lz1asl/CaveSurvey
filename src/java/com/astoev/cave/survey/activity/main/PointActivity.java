package com.astoev.cave.survey.activity.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.draw.DrawingActivity;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.azimuth.AzimuthChangedListener;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.misc.TransactionManager;


import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/17/12
 * Time: 1:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class PointActivity extends MainMenuActivity implements AzimuthChangedListener{

	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQIEST_EDIT_NOTE = 2;
	
	private static final String AZIMUTH_DIALOG = "azimuth_dialog";
	
    private String mNewNote = null;

    private String currentPhotoPath;
    
    /** Current leg to work with */
    private Leg currentLeg = null;
    
    private BTMeasureResultReceiver receiver = new BTMeasureResultReceiver(new Handler());
    
    private AzimuthDialog azimuthDialog;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.point);
        mNewNote = null;

        if (Option.CODE_SENSOR_INTERNAL.equals(Options.getOptionValue(Option.CODE_AZIMUTH_SENSOR))){
        	Log.i(Constants.LOG_TAG_UI, "Will register onClickListener for Azimuth");
        	EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
        	azimuth.setOnClickListener(new OnClickListener(){

				/**
				 * @see android.view.View.OnClickListener#onClick(android.view.View)
				 */
				@Override
				public void onClick(View v) {
					readAzimuth(v);
				}
        	});
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadPointData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        receiver.resetMeasureExpectations();
        loadPointData();
    }
    
    /**
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		if (azimuthDialog != null){
			azimuthDialog.cancelDialog();
			azimuthDialog.dismiss();
		}
		super.onPause();
	}

	/**
     * Shows the current leg as activity title
     * 
	 * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
	 */
	@Override
	protected String getScreenTitle() {
        try {
			StringBuilder builder = new StringBuilder(getString(R.string.leg));
			builder.append(getCurrentLeg().buildLegDescription(true));
			
			return builder.toString();
		} catch (SQLException e) {
			Log.e(Constants.LOG_TAG_UI, "Failed to create activity's name", e);
		}
        return null;
	}

	private void loadPointData() {
        Log.i(Constants.LOG_TAG_UI, "Loading point data");

        try {

            Leg legEdited = getCurrentLeg();

            // label
            TextView leg = (TextView) findViewById(R.id.point_curr_leg);
            leg.setText(legEdited.buildLegDescription());

            // up
            EditText up = (EditText) findViewById(R.id.point_up);
            setNotNull(up, legEdited.getTop());
            bindBTMeasures(up, Constants.Measures.up);

            // down
            EditText down = (EditText) findViewById(R.id.point_down);
            setNotNull(down, legEdited.getDown());
            bindBTMeasures(down, Constants.Measures.down);

            // left
            EditText left = (EditText) findViewById(R.id.point_left);
            setNotNull(left, legEdited.getLeft());
            bindBTMeasures(left, Constants.Measures.left);

            // right
            EditText right = (EditText) findViewById(R.id.point_right);
            setNotNull(right, legEdited.getRight());
            bindBTMeasures(right, Constants.Measures.right);

            // distance
            EditText distance = (EditText) findViewById(R.id.point_distance);
            setNotNull(distance, legEdited.getDistance());
            bindBTMeasures(distance, Constants.Measures.distance);

            // azimuth
            EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
            setNotNull(azimuth, legEdited.getAzimuth());
            bindBTMeasures(azimuth, Constants.Measures.angle);

            // slope
            EditText slope = (EditText) findViewById(R.id.point_slope);
            slope.setText("0");
            setNotNull(slope, legEdited.getSlope());
            bindBTMeasures(slope, Constants.Measures.slope);

            // fill note_text with its value
            Note note = DaoUtil.getActiveLegNote(legEdited);
            TextView textView = (TextView) findViewById(R.id.point_note_text);
            if (note != null && note.getText() != null) {
                textView.setText(note.getText());
                textView.setClickable(true);
            } else if (mNewNote != null) {
                textView.setText(mNewNote);
                textView.setClickable(true);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render point", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void bindBTMeasures(EditText text, final Constants.Measures aMeasure) {

        if (BluetoothService.isBluetoothSupported()) {

            if (!ensureDeviceSelected(false)) {
                Log.i(Constants.LOG_TAG_UI, "No device");
                return;
            }

            Log.i(Constants.LOG_TAG_UI, "Register field? " + aMeasure);
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

            // supported for the measure, add the listener
            if (StringUtils.isEmpty(text)) {
                // no current falue, just moving focus to the cell requests measure from BT
                Log.i(Constants.LOG_TAG_UI, "Add BT focus listener");
                text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            Log.i(Constants.LOG_TAG_UI, "Send read command");
                            receiver.awaitMeasure(aMeasure);
                            triggerBluetoothMeasure(aMeasure);
                        } else {
                            receiver.ignoreMeasure(aMeasure);
                        }
                    }
                });
            } else {
                // trigger BT read only if you tap twice
                Log.i(Constants.LOG_TAG_UI, "Add BT click listener");
                text.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(Constants.LOG_TAG_UI, "Send read command");
                        receiver.awaitMeasure(aMeasure);
                        triggerBluetoothMeasure(aMeasure);
                    }
                });
            }
        }
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

            // start validation
            boolean valid = true;
            final EditText distance = (EditText) findViewById(R.id.point_distance);
            valid = valid && validateNumber(distance, true);

            final EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
            valid = valid && validateNumber(azimuth, true) && checkAzimuth(azimuth);

            final EditText slope = (EditText) findViewById(R.id.point_slope);
            valid = valid && validateNumber(slope, false) && checkSlope(slope);

            final EditText up = (EditText) findViewById(R.id.point_up);
            valid = valid && validateNumber(up, false);

            final EditText down = (EditText) findViewById(R.id.point_down);
            valid = valid && validateNumber(down, false);

            final EditText left = (EditText) findViewById(R.id.point_left);
            valid = valid && validateNumber(left, false);

            final EditText right = (EditText) findViewById(R.id.point_right);
            valid = valid && validateNumber(right, false);

            if (!valid) {
                return false;
            }

            Log.i(Constants.LOG_TAG_UI, "Saving leg");

            TransactionManager.callInTransaction(getWorkspace().getDBHelper().getConnectionSource(),
                    new Callable() {
                        public Integer call() throws Exception {

                        	Leg legEdited = getCurrentLeg();

                            if (getIntent().getBooleanExtra(Constants.GALLERY_NEW, false)) {
                                Gallery newGallery = DaoUtil.createGallery(false);
                                legEdited.setGalleryId(newGallery.getId());
                            }

                            if (legEdited.isNew()) {
                            	getWorkspace().getDBHelper().getPointDao().create(legEdited.getToPoint());
                                getWorkspace().getDBHelper().getLegDao().create(legEdited);
                            }

                            // update model
                            legEdited.setDistance(StringUtils.getFromEditTextNotNull(distance));
                            legEdited.setAzimuth(StringUtils.getFromEditTextNotNull(azimuth));
                            legEdited.setSlope(StringUtils.getFromEditTextNotNull(slope));
                            legEdited.setTop(StringUtils.getFromEditTextNotNull(up));
                            legEdited.setDown(StringUtils.getFromEditTextNotNull(down));
                            legEdited.setLeft(StringUtils.getFromEditTextNotNull(left));
                            legEdited.setRight(StringUtils.getFromEditTextNotNull(right));

                            // save leg
                            getWorkspace().getDBHelper().getLegDao().update(legEdited);

                            if (mNewNote != null) {
                                // create new note
                                Note note = new Note(mNewNote);
                                note.setPoint(legEdited.getFromPoint());
                                getWorkspace().getDBHelper().getNoteDao().create(note);
                            }

                            getWorkspace().setActiveLegId(legEdited.getId());

                            Log.i(Constants.LOG_TAG_UI, "Saved");
                            UIUtilities.showNotification(R.string.action_saved);
                            return 0;
                        }
                    });
            return true;
        } catch (Exception e) {
            UIUtilities.showNotification(R.string.error);
            Log.e(Constants.LOG_TAG_UI, "Leg not saved", e);
        }
        return false;
    }

    private boolean validateNumber(EditText aEditField, boolean isRequired) {
        if (StringUtils.isEmpty(aEditField)) {
            if (isRequired) {
                aEditField.setError(aEditField.getContext().getString(R.string.required));
                return false;
            }
            return true;
        } else {
            try {
                Float.parseFloat(aEditField.getText().toString().trim());
                return true;
            } catch (NumberFormatException nfe) {
                aEditField.setError(aEditField.getContext().getString(R.string.invalid));
                return false;
            }
        }
    }

    private boolean checkAzimuth(EditText aEditText) {
        Float azimuth = StringUtils.getFromEditTextNotNull(aEditText);
        if (null != azimuth) {
            if (azimuth.floatValue() < 0) {
                aEditText.setError(aEditText.getContext().getString(R.string.invalid));
                return false;
            }

            String currAzimuthMeasure = Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);
            int maxValue;
            if (Option.UNIT_DEGREES.equals(currAzimuthMeasure)) {
                maxValue = Option.MAX_VALUE_AZIMUTH_DEGREES;
            } else { // Option.UNIT_GRADS
                maxValue = Option.MAX_VALUE_AZIMUTH_GRADS;
            }
            if (azimuth.floatValue() > maxValue) {
                aEditText.setError(aEditText.getContext().getString(R.string.invalid));
                return false;
            }
        }

        return true;
    }

    private boolean checkSlope(EditText aEditText) {
        Float slope = StringUtils.getFromEditTextNotNull(aEditText);
        if (null != slope) {
            String currSlopeMeasure = Options.getOptionValue(Option.CODE_SLOPE_UNITS);
            int maxValue, minValue;
            if (Option.UNIT_DEGREES.equals(currSlopeMeasure)) {
                maxValue = Option.MAX_VALUE_SLOPE_DEGREES;
                minValue = Option.MIN_VALUE_SLOPE_DEGREES;
            } else { // Option.UNIT_GRADS
                maxValue = Option.MAX_VALUE_SLOPE_GRADS;
                minValue = Option.MIN_VALUE_SLOPE_GRADS;
            }
            if (slope.floatValue() > maxValue || slope.floatValue() < minValue) {
                aEditText.setError(aEditText.getContext().getString(R.string.invalid));
                return false;
            }
        }
        return true;
    }

    public void noteButton(View aView) {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(Constants.LEG_SELECTED, getCurrentLeg().getId());
        intent.putExtra(Constants.LEG_NOTE, mNewNote);
        startActivityForResult(intent, REQIEST_EDIT_NOTE);
    }

    public void saveButton() {
        if (saveLeg()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void drawingButton() {
        Intent intent = new Intent(this, DrawingActivity.class);
        startActivity(intent);
    }

    public void gpsButton() {
        Point parentPoint = getCurrentLeg().getFromPoint();
    	Intent intent = new Intent(this, GPSActivity.class);
    	intent.putExtra(GPSActivity.POINT, parentPoint);
    	startActivity(intent);
    }

    public void deleteButton() {
        try {
            TransactionManager.callInTransaction(getWorkspace().getDBHelper().getConnectionSource(),
                    new Callable() {
                        public Object call() throws Exception {
                            Log.i(Constants.LOG_TAG_UI, "Delete " + getWorkspace().getActiveLegId());

                            Leg legEdited = getCurrentLeg();

                            Note note = DaoUtil.getActiveLegNote(legEdited);
                            if (note != null) {
                                getWorkspace().getDBHelper().getNoteDao().delete(note);
                            }

                            getWorkspace().getDBHelper().getLegDao().delete(legEdited);
                            getWorkspace().getDBHelper().getPointDao().delete(legEdited.getToPoint());

                            getWorkspace().setActiveLegId(getWorkspace().getLastLeg().getId());

                            UIUtilities.showNotification(R.string.action_deleted);
                            onBackPressed();
                            return null;
                        }
                    });
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to delete point", e);
            UIUtilities.showNotification(R.string.error);
        }
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

    private boolean ensureDeviceSelected(boolean showBTOptions) {
        if (BluetoothService.isDeviceSelected()) {
            return true;
        }

        if (showBTOptions) {
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
        }
        return false;
    }

    public void readAzimuth(View view) {
		azimuthDialog = new AzimuthDialog();
		azimuthDialog.show(getSupportFragmentManager(), AZIMUTH_DIALOG);
    }

    public void readSlope(View view) {
        // TODO
        UIUtilities.showNotification(R.string.todo);
    }

    public void photoButton() {
        // picture http://www.tutorialforandroid.com/2010/10/take-picture-in-android-with.html
    	// https://developer.android.com/training/camera/photobasics.html

    	File photoFile = null;
		try {
			//TODO fix this. Leg.getPoint should load the whole point!
			String projectName = getWorkspace().getActiveProject().getName();
			Leg workingLeg = getCurrentLeg();
			int fromPointId = workingLeg.getFromPoint().getId();
			Point pointFrom = DaoUtil.getPoint(fromPointId);
			
			// create file where to capture the image
			String filePrefix = FileStorageUtil.getFilePrefixForPicture(pointFrom);
			photoFile = FileStorageUtil.createPictureFile(this, projectName, filePrefix, FileStorageUtil.JPG_FILE_EXTENSION);
			
		} catch (SQLException e) {
			UIUtilities.showNotification(R.string.error);
			return;
		} catch (Exception e) {
			UIUtilities.showNotification(R.string.export_io_error);
			return;
		}
		
		// call capture image
		if (photoFile != null){
			
			currentPhotoPath = photoFile.getAbsolutePath();
			
			Log.i(Constants.LOG_TAG_SERVICE, "Going to capture image in: " + photoFile.getAbsolutePath());
	        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
	        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
		} 
    }

    // photo is captured
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent aData) {
    	
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                {
                	Log.i(Constants.LOG_TAG_SERVICE, "Got image");
    				try {
    					
    					// check if the file really exists
    				    if (!FileStorageUtil.isFileExists(currentPhotoPath)){
    				    	UIUtilities.showNotification(R.string.export_io_error);
    				    	break;
    				    }
    				    
    					File pictureFile = new File(currentPhotoPath);
    					
    					// broadcast that the file is added 
			        	FileStorageUtil.notifyPictureAddedToGalery(this, pictureFile);
    					
    					Log.i(Constants.LOG_TAG_SERVICE, "Image captured in: " + currentPhotoPath);
    					Photo photo = new Photo();
    					photo.setFSPath(currentPhotoPath);

    					Leg legEdited = getCurrentLeg();
    					Point currPoint = DaoUtil.getPoint(legEdited.getFromPoint().getId());
    					photo.setPoint(currPoint);

    					getWorkspace().getDBHelper().getPhotoDao().create(photo);
    					Log.i(Constants.LOG_TAG_SERVICE, "Image stored");
    					
    				} catch (SQLException e) {
    					Log.e(Constants.LOG_TAG_UI, "Picture object not saved", e);
    					UIUtilities.showNotification(R.string.error);
    				}
                }
                    break;
                case REQIEST_EDIT_NOTE:
                    mNewNote = aData.getStringExtra("note");
                    TextView textView = (TextView) findViewById(R.id.point_note_text);
                    textView.setText(mNewNote);
                    textView.setClickable(true);
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
            case R.id.point_action_save: {
                saveButton();
                return true;
            }
            case R.id.point_action_note: {
                noteButton(null);
                return true;
            }
            case R.id.point_action_draw: {
                drawingButton();
                return true;
            }
            case R.id.point_action_gps: {
                gpsButton();
                return true;
            }
            case R.id.point_action_photo: {
                photoButton();
                return true;
            }
            case R.id.point_action_delete: {
                deleteButton();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // need to call super to prepare menu
        boolean flag =  super.onPrepareOptionsMenu(menu);
        
        // check if the device has a camera
        PackageManager packageManager = getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
        	// if there is no camera remove the photo button
        	MenuItem photoMenuItem = menu.findItem(R.id.point_action_photo);
        	photoMenuItem.setVisible(false);
        }

        // enable deletion if already saved
        Leg legEdited = getCurrentLeg();
        if (!legEdited.isNew()) {
            try {
                Leg lastLeg = getWorkspace().getLastLeg();
                if (lastLeg.getId().equals(legEdited.getId())) {
                    // and only last leg for now
                    MenuItem deleteMenuOption = menu.findItem(R.id.point_action_delete);
                    deleteMenuOption.setEnabled(true);
                    return flag;
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG_UI, "Failed to update menu", e);
                UIUtilities.showNotification(R.string.error);
            }
        }

        // delete disabled by default
        MenuItem deleteMenuOption = menu.findItem(R.id.point_action_delete);
        deleteMenuOption.setEnabled(false);
        return flag;
    }
    
    /**
     * Helper method to build the current leg. If the leg is new will create from and to points. The id of the
     * new leg will always be null. If the leg is currently edited it is obtained from the workspace.
     * 
     * @return Leg instance
     */
    private Leg getCurrentLeg(){
        if (currentLeg == null){
        	Bundle extras = getIntent().getExtras();
            try {
				if (extras != null) {
				    int currentLegSelectedId = extras.getInt(Constants.LEG_SELECTED);

                    if (currentLegSelectedId > 0) {
				        currentLeg = DaoUtil.getLeg(currentLegSelectedId);
				        Log.i(Constants.LOG_TAG_UI, "PointView for leg with id: " + currentLegSelectedId);
                        return currentLeg;
                    }
				}

                Log.i(Constants.LOG_TAG_UI, "Create new leg");
                Integer currGalleryId = getWorkspace().getActiveGalleryId();

                // another leg, starting from the latest in the gallery
                boolean newGalleryFlag = extras.getBoolean(Constants.GALLERY_NEW, false);
                Point newFrom, newTo;
                if (newGalleryFlag) {
                    newFrom = getWorkspace().getActiveLeg().getFromPoint();
                    newTo = PointUtil.createSecondPoint();
                } else {
                    newFrom = getWorkspace().getLastGalleryPoint(currGalleryId);
                    newTo = PointUtil.generateNextPoint(currGalleryId);
                }



                Log.i(Constants.LOG_TAG_UI, "PointView for new point");
                currentLeg = new Leg(newFrom, newTo, getWorkspace().getActiveProject(), currGalleryId);
			} catch (SQLException sqle) {
				throw new RuntimeException(sqle);
			}
        }
        return currentLeg;
    }

    private class BTMeasureResultReceiver extends ResultReceiver {
            private Set<Constants.Measures> expectedMeasures = new HashSet<Constants.Measures>();

        public BTMeasureResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                float aMeasure = resultData.getFloat("result");
                Constants.Measures type = Constants.Measures.valueOf(resultData.getString("type"));
                if (!expectsMeasure(type)) {
                    Log.i(Constants.LOG_TAG_SERVICE, "Unexpected measure " + type + " : " + aMeasure);
                    return;
                }

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

        public boolean expectsMeasure(Constants.Measures aMeasure) {
            return expectedMeasures.contains(aMeasure);
        }

        public void awaitMeasure(Constants.Measures aMeasure) {
            expectedMeasures.add(aMeasure);
        }

        public void ignoreMeasure(Constants.Measures aMeasure) {
            expectedMeasures.remove(aMeasure);
        }

        public void resetMeasureExpectations() {
            expectedMeasures.clear();
        }
    }

	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthChangedListener#onAzimuthChanged(float)
	 */
	@Override
	public void onAzimuthChanged(float newValueArg) {
        final EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
		azimuth.setText(String.valueOf(newValueArg));
	}

	/**
	 * @see com.astoev.cave.survey.service.azimuth.AzimuthChangedListener#onAccuracyChanged(int)
	 */
	@Override
	public void onAccuracyChanged(int accuracyArg) {
	}
    
}
