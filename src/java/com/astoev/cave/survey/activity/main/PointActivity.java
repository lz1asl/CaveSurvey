package com.astoev.cave.survey.activity.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.dialog.AzimuthDialog;
import com.astoev.cave.survey.activity.dialog.SlopeDialog;
import com.astoev.cave.survey.activity.dialog.VectorDialog;
import com.astoev.cave.survey.activity.draw.DrawingActivity;
import com.astoev.cave.survey.fragment.LocationFragment;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;
import com.astoev.cave.survey.service.orientation.AzimuthChangedListener;
import com.astoev.cave.survey.service.orientation.SlopeChangedListener;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.misc.TransactionManager;


import org.w3c.dom.Text;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/17/12
 * Time: 1:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class PointActivity extends MainMenuActivity implements AzimuthChangedListener, SlopeChangedListener{

	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQIEST_EDIT_NOTE = 2;
	
	private static final String AZIMUTH_DIALOG = "azimuth_dialog";
	private static final String SLOPE_DIALOG = "slope_dialog";
    private static final String VECTOR_DIALOG = "vector_dialog";
	
    private String mNewNote = null;

    private String currentPhotoPath;
    
    /** Current leg to work with */
    private Leg currentLeg = null;
    
    private BTMeasureResultReceiver receiver = new BTMeasureResultReceiver(new Handler());
    
    private AzimuthDialog azimuthDialog;
    
    private SlopeDialog slopeDialog;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.point);
        mNewNote = null;
        
        // initialize the view with leg data only if the activity is new
        if (savedInstanceState == null){
            loadPointData();
        }

        // if the azimuth is read from build in sensors add onClickListener to show azimuth dialog
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
        
        // if the slope is read from build in sensors add onClickListener to show slope dialog
        if (Option.CODE_SENSOR_INTERNAL.equals(Options.getOptionValue(Option.CODE_SLOPE_SENSOR))){
            Log.i(Constants.LOG_TAG_UI, "Will register onClickListener for Slope");
            EditText slope = (EditText) findViewById(R.id.point_slope);
            slope.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View viewArg) {
                    readSlope(viewArg);
                }
            });
        }
        
        Leg legEdited = getCurrentLeg();
        if (legEdited != null){
            GPSActivity.initSavedLocationContainer(legEdited.getFromPoint(), this, savedInstanceState);
        }

        loadLegVectors(legEdited);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        receiver.resetMeasureExpectations();
        
        //check if location is added and returned back to this activity
        Fragment fragment = this.getSupportFragmentManager().findFragmentById(R.id.saved_location_container);
        if (!(fragment instanceof LocationFragment)){
            Leg legEdited = getCurrentLeg();
            GPSActivity.initSavedLocationContainer(legEdited.getFromPoint(), this, null);
        }

        loadLegVectors(getCurrentLeg());
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
		if (slopeDialog != null){
		    slopeDialog.cancelDialog();
		    slopeDialog.dismiss();
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
            valid = valid && UIUtilities.validateNumber(distance, true);

            final EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
            valid = valid && UIUtilities.validateNumber(azimuth, true) && UIUtilities.checkAzimuth(azimuth);

            final EditText slope = (EditText) findViewById(R.id.point_slope);
            valid = valid && UIUtilities.validateNumber(slope, false) && UIUtilities.checkSlope(slope);

            final EditText up = (EditText) findViewById(R.id.point_up);
            valid = valid && UIUtilities.validateNumber(up, false);

            final EditText down = (EditText) findViewById(R.id.point_down);
            valid = valid && UIUtilities.validateNumber(down, false);

            final EditText left = (EditText) findViewById(R.id.point_left);
            valid = valid && UIUtilities.validateNumber(left, false);

            final EditText right = (EditText) findViewById(R.id.point_right);
            valid = valid && UIUtilities.validateNumber(right, false);

            if (!valid) {
                return false;
            }

            Log.i(Constants.LOG_TAG_UI, "Saving leg");

            TransactionManager.callInTransaction(getWorkspace().getDBHelper().getConnectionSource(),
                    new Callable<Integer>() {
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

                            getWorkspace().setActiveLeg(legEdited);

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

    private void vectorButton() {
        VectorDialog dialog = new VectorDialog();
        dialog.setLeg(getCurrentLeg());
        dialog.setCancelable(true);
        dialog.show(getSupportFragmentManager(), VECTOR_DIALOG);
    }

    public void deleteButton() {
        try {
            Leg legEdited = getCurrentLeg();
            boolean deleted = DaoUtil.deleteLeg(legEdited);
            if (deleted){
                UIUtilities.showNotification(R.string.action_deleted);
                onBackPressed();
            } 
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to delete point", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void triggerBluetoothMeasure(Constants.Measures aMeasure) {
        // register listeners & send command
        BluetoothService.sendReadMeasureCommand(receiver, aMeasure);
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
    
    public void readSlope(View view){
        slopeDialog = new SlopeDialog();
        slopeDialog.show(getSupportFragmentManager(), SLOPE_DIALOG);
    }

    public void photoButton() {
        // picture http://www.tutorialforandroid.com/2010/10/take-picture-in-android-with.html
    	// https://developer.android.com/training/camera/photobasics.html

    	File photoFile = null;
		try {
			String projectName = getWorkspace().getActiveProject().getName();
			Leg workingLeg = getCurrentLeg();
            Point pointFrom = workingLeg.getFromPoint();
            DaoUtil.refreshPoint(pointFrom);

			// create file where to capture the image
            String galleryName = PointUtil.getGalleryNameForFromPoint(pointFrom, workingLeg.getGalleryId());
			String filePrefix = FileStorageUtil.getFilePrefixForPicture(pointFrom, galleryName);
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
        finish();
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
            case R.id.point_action_add_vector : {
                vectorButton();
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

        // allow vectors for saved legs
        if (currentLeg != null && !currentLeg.isNew()) {
            MenuItem photoMenuItem = menu.findItem(R.id.point_action_add_vector);
            photoMenuItem.setVisible(true);
        }

        try {
            if (Leg.canDelete(getCurrentLeg())) {
                MenuItem deleteMenuOption = menu.findItem(R.id.point_action_delete);
                deleteMenuOption.setVisible(true);
                return flag;
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to update menu", e);
            UIUtilities.showNotification(R.string.error);
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
                    currGalleryId = null;
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
            protected void onReceiveResult(int aResultCode, Bundle aResultData) {

                switch (aResultCode) {
                    case Activity.RESULT_OK:

                        float aMeasure = aResultData.getFloat(Constants.MEASURE_VALUE_KEY);
                        Constants.Measures type = Constants.Measures.valueOf(aResultData.getString(Constants.MEASURE_TARGET_KEY));
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
                    break;

                    default:
                        UIUtilities.showNotification(aResultData.getString("error"));
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
	 * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
	 */
	@Override
	public void onAzimuthChanged(float newValueArg) {
        final EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
		azimuth.setText(String.valueOf(newValueArg));
	}

    /**
     * @see com.astoev.cave.survey.service.orientation.SlopeChangedListener#onSlopeChanged(float)
     */
    @Override
    public void onSlopeChanged(float newValueArg) {
        final EditText slope = (EditText)findViewById(R.id.point_slope);
        slope.setText(String.valueOf(newValueArg));
    }

    private void loadLegVectors(Leg aLegEdited) {
        try {
            TableLayout vectorsTable = (TableLayout) findViewById(R.id.point_vectors_table);

            // data
            List<Vector> vectorsList = DaoUtil.getLegVectors(aLegEdited);
            if (vectorsList != null && vectorsList.size() > 0) {

                // remove old data
                vectorsTable.removeAllViews();

                // set headers
                TableRow header = new TableRow(this);
                TextView counterHeader = new TextView(this);
                counterHeader.setText(getString(R.string.point_vectors_counter));
                header.addView(counterHeader);
                TextView distanceHeader = new TextView(this);
                distanceHeader.setText(getString(R.string.distance));
                header.addView(distanceHeader);
                TextView azimuthHeader = new TextView(this);
                azimuthHeader.setText(getString(R.string.azimuth));
                header.addView(azimuthHeader);
                TextView slopeHeader = new TextView(this);
                slopeHeader.setText(getString(R.string.slope));
                header.addView(slopeHeader);
                vectorsTable.addView(header);

                // populate data
                int index = 1;
                for(final Vector v: vectorsList) {
                    TableRow row = new TableRow(this);
                    TextView id = new TextView(this);
                    id.setText(String.valueOf(index));
                    id.setGravity(Gravity.CENTER);
                    row.addView(id);

                    TextView distance = new TextView(this);
                    distance.setText(StringUtils.floatToLabel(v.getDistance()));
                    distance.setGravity(Gravity.CENTER);
                    row.addView(distance);

                    TextView azimuth = new TextView(this);
                    azimuth.setText(StringUtils.floatToLabel(v.getAzimuth()));
                    azimuth.setGravity(Gravity.CENTER);
                    row.addView(azimuth);

                    TextView angle = new TextView(this);
                    angle.setText(StringUtils.floatToLabel(v.getSlope()));
                    angle.setGravity(Gravity.CENTER);
                    row.addView(angle);

                    Button deleteButton = new Button(this);
                    deleteButton.setText("-");
                    final int finalIndex = index;
                    deleteButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View aView) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PointActivity.this);
                            dialogBuilder.setMessage(getString(R.string.point_vectors_delete, finalIndex))
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Log.i(Constants.LOG_TAG_UI, "Delete vector");
                                            try {
                                                DaoUtil.deleteVector(v);
                                                UIUtilities.showNotification(R.string.action_deleted);
                                                loadLegVectors(getCurrentLeg());
                                            } catch (Exception e) {
                                                Log.e(Constants.LOG_TAG_UI, "Failed to delete vector", e);
                                                UIUtilities.showNotification(R.string.error);
                                            }
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alert = dialogBuilder.create();
                            alert.show();

                        }
                    });
                    row.addView(deleteButton);

                    vectorsTable.addView(row);
                    index++;
                }

                vectorsTable.setVisibility(View.VISIBLE);
            } else {
                vectorsTable.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to load vectors", e);
            UIUtilities.showNotification(R.string.error);
        }
    }
}
