package com.astoev.cave.survey.activity.main;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static com.astoev.cave.survey.util.FileStorageUtil.JPG_FILE_EXTENSION;
import static com.astoev.cave.survey.util.FileStorageUtil.MIME_TYPE_JPG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.dialog.ConfirmDeleteDialog;
import com.astoev.cave.survey.activity.dialog.ConfirmationDialog;
import com.astoev.cave.survey.activity.dialog.ConfirmationOperation;
import com.astoev.cave.survey.activity.dialog.DeleteHandler;
import com.astoev.cave.survey.activity.dialog.GpsTypeDialog;
import com.astoev.cave.survey.activity.dialog.GpsTypeHandler;
import com.astoev.cave.survey.activity.dialog.VectorDialog;
import com.astoev.cave.survey.activity.draw.DrawingActivity;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.bluetooth.BTMeasureResultReceiver;
import com.astoev.cave.survey.service.bluetooth.BTResultAware;
import com.astoev.cave.survey.service.bluetooth.util.MeasurementsUtil;
import com.astoev.cave.survey.service.orientation.AzimuthChangedListener;
import com.astoev.cave.survey.service.orientation.SlopeChangedListener;
import com.astoev.cave.survey.util.ConfigUtil;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.GalleryUtil;
import com.astoev.cave.survey.util.PermissionUtil;
import com.astoev.cave.survey.util.PointUtil;
import com.astoev.cave.survey.util.StringUtils;
import com.j256.ormlite.misc.TransactionManager;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/17/12
 * Time: 1:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class PointActivity extends MainMenuActivity implements AzimuthChangedListener, SlopeChangedListener, BTResultAware, View.OnTouchListener, DeleteHandler, GpsTypeHandler {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQIEST_EDIT_NOTE = 2;

    private static final String VECTOR_DIALOG = "vector_dialog";
    private static final String STATE_PHOTO_PATH = "STATE_PHOTO_PATH";

    private static final int PERM_REQ_CODE_CAMERA = 101;
    private static final int PERM_REQ_CODE_GPS = 102;

    private String mNewNote = null;

    private DocumentFile mCurrentPhotoFile;

    /**
     * Current leg to work with
     */
    private Leg mCurrentLeg = null;

    private BTMeasureResultReceiver mReceiver = new BTMeasureResultReceiver(this);

    // swipe detection variables
    private float x1, x2;
    private static int MIN_SWIPE_DISTANCE;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.point);
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        MIN_SWIPE_DISTANCE = (int) (250 * getApplicationContext().getResources().getDisplayMetrics().density);
        mNewNote = null;

        // initialize the view with leg data only if the activity is new
        if (savedInstanceState == null) {
            loadPointData();
        }

        // handle double click for reading built-in azimuth and slope
        EditText azimuth = findViewById(R.id.point_azimuth);
        EditText slope = findViewById(R.id.point_slope);
        MeasurementsUtil.bindSensorsAwareFields(azimuth, slope, getSupportFragmentManager());

        Leg legEdited = getCurrentLeg();
        if (legEdited != null) {
            GPSActivity.initSavedLocationContainer(legEdited.getFromPoint(), this, savedInstanceState);
        }

        loadLegPhotos(legEdited);
        loadLegSketches(legEdited);
        loadLegVectors(legEdited);

        // make swipe work
        View view = findViewById(R.id.point_main_view);
        view.setOnTouchListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPhotoFile != null) {
            outState.putString(STATE_PHOTO_PATH, mCurrentPhotoFile.getUri().toString());
            // save photo path if available
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPhotoFile = DocumentFile.fromTreeUri(ConfigUtil.getContext(), Uri.parse(savedInstanceState.getString(STATE_PHOTO_PATH)));
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver.resetMeasureExpectations();

        // we need to reload the location if edited
        Leg legEdited = getCurrentLeg();
        GPSActivity.initSavedLocationContainer(legEdited.getFromPoint(), this, null);

        loadLegSketches(getCurrentLeg());
        loadLegVectors(getCurrentLeg());
    }

    /**
     * @see FragmentActivity#onPause()
     */
    @Override
    protected void onPause() {
        MeasurementsUtil.closeDialogs();
        mReceiver.resetMeasureExpectations();
        super.onPause();
    }

    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }

    /**
     * Shows the current leg as activity title
     *
     * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
     */
    @Override
    protected String getScreenTitle() {
        try {
            return getCurrentLeg().buildLegDescription(true);
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
            EditText up = findViewById(R.id.point_up);
            StringUtils.setNotNull(up, legEdited.getTop());
            mReceiver.bindBTMeasures(up, Constants.Measures.up, false, null);

            // down
            EditText down = findViewById(R.id.point_down);
            StringUtils.setNotNull(down, legEdited.getDown());
            mReceiver.bindBTMeasures(down, Constants.Measures.down, false, null);

            // left
            EditText left = findViewById(R.id.point_left);
            StringUtils.setNotNull(left, legEdited.getLeft());
            mReceiver.bindBTMeasures(left, Constants.Measures.left, false, null);

            // right
            EditText right = findViewById(R.id.point_right);
            StringUtils.setNotNull(right, legEdited.getRight());
            mReceiver.bindBTMeasures(right, Constants.Measures.right, false, null);

            // distance
            EditText distance = findViewById(R.id.point_distance);
            StringUtils.setNotNull(distance, legEdited.getDistance());
            mReceiver.bindBTMeasures(distance, Constants.Measures.distance, false, new Constants.Measures[]{Constants.Measures.angle, Constants.Measures.slope});
            disableIfMiddle(legEdited, distance);

            // azimuth
            EditText azimuth = findViewById(R.id.point_azimuth);
            StringUtils.setNotNull(azimuth, legEdited.getAzimuth());
            mReceiver.bindBTMeasures(azimuth, Constants.Measures.angle, false, new Constants.Measures[]{Constants.Measures.distance, Constants.Measures.slope});
            disableIfMiddle(legEdited, azimuth);

            // slope
            EditText slope = findViewById(R.id.point_slope);
            slope.setText("0");
            StringUtils.setNotNull(slope, legEdited.getSlope());
            mReceiver.bindBTMeasures(slope, Constants.Measures.slope, false, new Constants.Measures[]{Constants.Measures.angle, Constants.Measures.distance});
            disableIfMiddle(legEdited, slope);

            if (!legEdited.isMiddle()) {
                // fill note_text with its value
                Note note = DaoUtil.getActiveLegNote(legEdited);
                TextView textView = findViewById(R.id.point_note_text);
                if (note != null && note.getText() != null) {
                    textView.setText(note.getText());
                    textView.setClickable(true);
                } else if (mNewNote != null) {
                    textView.setText(mNewNote);
                    textView.setClickable(true);
                }
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render point", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void disableIfMiddle(Leg aCurrentLeg, EditText anEditText) {
        anEditText.setEnabled(!aCurrentLeg.isMiddle());
    }

    private boolean saveLeg() {
        try {

            // start validation
            final EditText distance = findViewById(R.id.point_distance);
            boolean valid =  UIUtilities.validateNumber(distance, true);

            final EditText azimuth = findViewById(R.id.point_azimuth);
            valid = valid && UIUtilities.validateNumber(azimuth, true) && UIUtilities.checkAzimuth(azimuth);

            final EditText slope = findViewById(R.id.point_slope);
            valid = valid && UIUtilities.validateNumber(slope, false) && UIUtilities.checkSlope(slope);

            final EditText up = findViewById(R.id.point_up);
            valid = valid && UIUtilities.validateNumber(up, false);

            final EditText down = findViewById(R.id.point_down);
            valid = valid && UIUtilities.validateNumber(down, false);

            final EditText left = findViewById(R.id.point_left);
            valid = valid && UIUtilities.validateNumber(left, false);

            final EditText right = findViewById(R.id.point_right);
            valid = valid && UIUtilities.validateNumber(right, false);

            if (!valid) {
                return false;
            }

            Log.i(Constants.LOG_TAG_UI, "Saving leg");

            TransactionManager.callInTransaction(getWorkspace().getDBHelper().getConnectionSource(),
                    () -> {

                        Leg legEdited = getCurrentLeg();

                        if (getIntent().getBooleanExtra(Constants.GALLERY_NEW, false)) {
                            Gallery newGallery = GalleryUtil.createGallery(false);
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
                            note.setGalleryId(legEdited.getGalleryId());
                            getWorkspace().getDBHelper().getNoteDao().create(note);
                        }

                        if (legEdited.isMiddle()) {
                            getWorkspace().setActiveLeg(DaoUtil.getLegByToPoint(legEdited.getToPoint()));
                        } else {
                            getWorkspace().setActiveLeg(legEdited);
                        }

                        Log.i(Constants.LOG_TAG_UI, "Saved");
                        UIUtilities.showNotification(R.string.action_saved);
                        return 0;
                    }
            );
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
            finish();
        }
    }

    public void drawingButton() {
        Intent intent = new Intent(this, DrawingActivity.class);
        startActivity(intent);
    }

    /**
     * Called once the GPS buton is selected from the menu. Will show the dialog for manual/auto GPS
     */
    public void gpsButton() {

        Log.i(Constants.LOG_TAG_UI, "Adding GPS Type dialog");
        GpsTypeDialog gpsTypeDialog = new GpsTypeDialog();
        gpsTypeDialog.show(getSupportFragmentManager(), GpsTypeDialog.GPS_TYPE_DIALOG);
    }

    private void vectorButton() {
        if (saveLeg()) {
            VectorDialog dialog = new VectorDialog(getSupportFragmentManager());
            Bundle bundle = new Bundle();
            Leg leg = getCurrentLeg();
            bundle.putSerializable(VectorDialog.LEG, leg);
            dialog.setCancelable(true);
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), VECTOR_DIALOG);
        }
    }

    public void deleteButton() {
        try {
            Leg legEdited = getCurrentLeg();
            boolean deleted = DaoUtil.deleteLeg(legEdited);
            if (deleted) {
                UIUtilities.showNotification(R.string.action_deleted);

                // ensure active leg present
                getWorkspace().setActiveLeg(getWorkspace().getLastLeg());

                onBackPressed();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to delete point", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    public void photoButton() {
        // picture http://www.tutorialforandroid.com/2010/10/take-picture-in-android-with.html
        // https://developer.android.com/training/camera/photobasics.html

        if (!PermissionUtil.requestPermission(CAMERA, this, PERM_REQ_CODE_CAMERA)) {
            return;
        }

        DocumentFile photoFile;
        try {
            String projectName = getWorkspace().getActiveProject().getName();
            Leg workingLeg = getCurrentLeg();
            Point pointFrom = workingLeg.getFromPoint();
            DaoUtil.refreshPoint(pointFrom);

            // create file where to capture the image
            String galleryName = PointUtil.getGalleryNameForFromPoint(pointFrom, workingLeg.getGalleryId());
            String filePrefix = FileStorageUtil.getFilePrefixForPicture(pointFrom, galleryName);
            photoFile = FileStorageUtil.createPictureFile(this, projectName, filePrefix, JPG_FILE_EXTENSION, MIME_TYPE_JPG, true);

        } catch (SQLException e) {
            UIUtilities.showNotification(R.string.error);
            return;
        } catch (Exception e) {
            UIUtilities.showNotification(R.string.export_io_error);
            Log.e(Constants.LOG_TAG_UI, "Failed to write to SD card", e);
            return;
        }

        // call capture image
        if (photoFile != null) {

            Log.i(Constants.LOG_TAG_SERVICE, "Going to capture image in: " + photoFile.getUri());
            mCurrentPhotoFile = photoFile;
            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile.getUri());
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // photo is captured
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent aData) {

        super.onActivityResult(requestCode, resultCode, aData);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: {
                    Log.i(Constants.LOG_TAG_SERVICE, "Got image");
                    try {

                        // check if the photo path is available
                        if (mCurrentPhotoFile == null) {
                            UIUtilities.showNotification(R.string.export_io_error);
                            Log.e(Constants.LOG_TAG_UI, "Photo file url is not available");
                            break;
                        }

                        // check if the file really exists
                        if (!mCurrentPhotoFile.exists()) {
                            UIUtilities.showNotification(R.string.export_io_error);
                            Log.e(Constants.LOG_TAG_UI, "Photo file not available:" + mCurrentPhotoFile);
                            break;
                        }

                        // broadcast that the file is added
                        FileStorageUtil.notifyPictureAddedToGallery(this, mCurrentPhotoFile);

                        Log.i(Constants.LOG_TAG_SERVICE, "Image captured in: " + mCurrentPhotoFile);
                        Photo photo = new Photo();
                        photo.setFSPath(mCurrentPhotoFile.getName());

                        Leg legEdited = getCurrentLeg();
                        Point currPoint = DaoUtil.getPoint(legEdited.getFromPoint().getId());
                        photo.setPoint(currPoint);
                        photo.setGalleryId(legEdited.getGalleryId());

                        getWorkspace().getDBHelper().getPhotoDao().create(photo);
                        Log.i(Constants.LOG_TAG_SERVICE, "Image stored");
                        loadLegPhotos(mCurrentLeg);

                    } catch (SQLException e) {
                        Log.e(Constants.LOG_TAG_UI, "Picture object not saved", e);
                        UIUtilities.showNotification(R.string.error);
                    }
                }
                break;
                case REQIEST_EDIT_NOTE:
                    mNewNote = aData.getStringExtra("note");
                    TextView textView = findViewById(R.id.point_note_text);
                    textView.setText(mNewNote);
                    textView.setClickable(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Leg currLeg = getCurrentLeg();
        if (!currLeg.isNew() && currLeg.isMiddle()) {
            try {
                getWorkspace().setActiveLeg(DaoUtil.getLegByToPoint(currLeg.getToPoint()));
            } catch (SQLException e) {
                Log.e(Constants.LOG_TAG_UI, "Failed to locate parent leg", e);
                UIUtilities.showNotification(R.string.error);
            }
        }
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
            case R.id.point_action_save:
                saveButton();
                return true;
            case R.id.point_action_note:
                noteButton(null);
                return true;
            case R.id.point_action_draw:
                drawingButton();
                return true;
            case R.id.point_action_gps:
                gpsButton();
                return true;
            case R.id.point_action_photo:
                photoButton();
                return true;
            case R.id.point_action_add_vector:
                vectorButton();
                return true;
            case R.id.point_action_delete:
                deleteButton();
                return true;
            case R.id.point_action_reverse:
                confirmReverseLeg();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // need to call super to prepare menu
        boolean flag = super.onPrepareOptionsMenu(menu);

        Leg currLeg = getCurrentLeg();

        if (currLeg.isMiddle()) {
            MenuItem noteMenuItem = menu.findItem(R.id.point_action_note);
            noteMenuItem.setVisible(false);

            MenuItem drawingMenuItem = menu.findItem(R.id.point_action_draw);
            drawingMenuItem.setVisible(false);

            MenuItem gpsMenuItem = menu.findItem(R.id.point_action_gps);
            gpsMenuItem.setVisible(false);

            MenuItem vectorsMenuItem = menu.findItem(R.id.point_action_add_vector);
            vectorsMenuItem.setVisible(false);

            MenuItem reverseMenuItem = menu.findItem(R.id.point_action_reverse);
            reverseMenuItem.setVisible(false);
        }

        // check if the device has a camera
        PackageManager packageManager = getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) || currLeg.isMiddle()) {
            // if there is no camera remove the photo button
            MenuItem photoMenuItem = menu.findItem(R.id.point_action_photo);
            photoMenuItem.setVisible(false);
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
    private Leg getCurrentLeg() {
        if (mCurrentLeg == null) {
            Bundle extras = getIntent().getExtras();
            try {
                if (extras != null) {
                    int currentLegSelectedId = extras.getInt(Constants.LEG_SELECTED);

                    if (currentLegSelectedId > 0) {
                        mCurrentLeg = DaoUtil.getLeg(currentLegSelectedId);
                        Log.i(Constants.LOG_TAG_UI, "PointView for leg with id: " + currentLegSelectedId);
                        return mCurrentLeg;
                    }
                }

                Log.i(Constants.LOG_TAG_UI, "Create new leg");
                Integer currGalleryId = getWorkspace().getActiveGalleryId();

                // another leg, starting from the latest in the gallery
                boolean newGalleryFlag = extras != null && extras.getBoolean(Constants.GALLERY_NEW, false);
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
                mCurrentLeg = new Leg(newFrom, newTo, getWorkspace().getActiveProject(), currGalleryId);
            } catch (SQLException sqle) {
                throw new RuntimeException(sqle);
            }
        }
        return mCurrentLeg;
    }

    /**
     * @see com.astoev.cave.survey.service.orientation.AzimuthChangedListener#onAzimuthChanged(float)
     */
    @Override
    public void onAzimuthChanged(float newValueArg) {
        final EditText azimuth = findViewById(R.id.point_azimuth);
        azimuth.setText(String.valueOf(newValueArg));
    }

    /**
     * @see com.astoev.cave.survey.service.orientation.SlopeChangedListener#onSlopeChanged(float)
     */
    @Override
    public void onSlopeChanged(float newValueArg) {
        final EditText slope = findViewById(R.id.point_slope);
        slope.setText(String.valueOf(newValueArg));
    }

    public int loadLegVectors(Leg aLegEdited) {

        if (aLegEdited.isNew()) {
            // no vectors anyway
            return 0;
        }

        if (aLegEdited.isMiddle()) {
            // no need to proceed
            return 0;
        }

        try {
            TableLayout vectorsTable = findViewById(R.id.point_vectors_table);

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
                for (final Vector v : vectorsList) {
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

                    final int finalIndex = index;
                    final Vector finalVector = v;

                    row.setOnLongClickListener(v1 -> {

                        // instantiate delete dialog and pass the vector
                        String message = getString(R.string.point_vectors_delete, finalIndex);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ConfirmDeleteDialog.ELEMENT, finalVector);
                        bundle.putString(ConfirmDeleteDialog.MESSAGE, message);

                        ConfirmDeleteDialog deleteVecotrDialog = new ConfirmDeleteDialog();
                        deleteVecotrDialog.setArguments(bundle);
                        deleteVecotrDialog.show(getSupportFragmentManager(), ConfirmDeleteDialog.DELETE_VECTOR_DIALOG);
                        return true;
                    });

                    vectorsTable.addView(row);
                    index++;
                }

                vectorsTable.setVisibility(View.VISIBLE);
            } else {
                vectorsTable.setVisibility(View.INVISIBLE);
            }
            return vectorsList.size();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to load vectors", e);
            UIUtilities.showNotification(R.string.error);
            return 0;
        }
    }

    public void loadLegPhotos(Leg aLegEdited) {

        if (aLegEdited.isNew()) {
            // no photos anyway
            return;
        }

        if (aLegEdited.isMiddle()) {
            // no need to proceed
            return;
        }

        try {
            TableLayout photosTable = findViewById(R.id.point_photos_table);

            // data
            List<Photo> photosList = DaoUtil.getAllPhotosByPointAndGallery(
                    aLegEdited.getFromPoint().getId(), aLegEdited.getGalleryId());
            if (photosList != null && photosList.size() > 0) {

                // remove old data
                photosTable.removeAllViews();

                // set headers
                TableRow header = new TableRow(this);
                TextView counterHeader = new TextView(this);
                counterHeader.setText(getString(R.string.point_photos_header));
                header.addView(counterHeader);
                photosTable.addView(header);

                // populate data
                int index = 1;
                for (final Photo photo : photosList) {
                    TableRow row = new TableRow(this);
                    TextView id = new TextView(this);
                    String rowLabel = getString(R.string.point_photo_label, String.valueOf(index));
                    id.setText(rowLabel);
                    id.setGravity(Gravity.CENTER);
                    row.addView(id);

                    row.setOnClickListener(v -> {
                        // show the picture
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri fileUri =  FileStorageUtil.toFullRelativePath(photo.getFSPath()).getUri();
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(fileUri, "image/*");
                        startActivity(intent);
                    });

                    photosTable.addView(row);
                    index++;
                }

                photosTable.setVisibility(View.VISIBLE);
            } else {
                photosTable.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to load photos", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    public void loadLegSketches(Leg aLegEdited) {

        if (aLegEdited.isNew()) {
            // no sketches anyway
            return;
        }

        if (aLegEdited.isMiddle()) {
            // no need to proceed
            return;
        }

        try {
            TableLayout sketchesTable = findViewById(R.id.point_sketches_table);

            // data
            List<Sketch> sketchesList = DaoUtil.getAllSketchesByPoint(aLegEdited.getFromPoint());
            if (sketchesList != null && sketchesList.size() > 0) {

                // remove old data
                sketchesTable.removeAllViews();

                // set headers
                TableRow header = new TableRow(this);
                TextView counterHeader = new TextView(this);
                counterHeader.setText(getString(R.string.point_sketches_header));
                header.addView(counterHeader);
                sketchesTable.addView(header);

                // populate data
                int index = 1;
                for (final Sketch sketch : sketchesList) {
                    TableRow row = new TableRow(this);
                    TextView id = new TextView(this);
                    String rowLabel = getString(R.string.point_sketch_label, String.valueOf(index));
                    id.setText(rowLabel);
                    id.setGravity(Gravity.CENTER);
                    row.addView(id);

                    row.setOnClickListener(v -> {
                        // show the sketch
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri fileUri =  FileStorageUtil.toFullRelativePath(sketch.getFSPath()).getUri();
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(fileUri, "image/*");
                        startActivity(intent);
                    });

                    sketchesTable.addView(row);
                    index++;
                }

                sketchesTable.setVisibility(View.VISIBLE);
            } else {
                sketchesTable.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to load sketches", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    @Override
    public void onReceiveMeasures(Constants.Measures aMeasureTarget, float aMeasureValue) {
        switch (aMeasureTarget) {
            case distance:
                Log.i(Constants.LOG_TAG_UI, "Got distance " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.point_distance);
                break;

            case angle:
                Log.i(Constants.LOG_TAG_UI, "Got angle " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.point_azimuth);
                break;

            case slope:
                Log.i(Constants.LOG_TAG_UI, "Got slope " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.point_slope);
                break;

            case up:
                Log.i(Constants.LOG_TAG_UI, "Got up " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.point_up);
                break;

            case down:
                Log.i(Constants.LOG_TAG_UI, "Got down " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.point_down);
                break;

            case left:
                Log.i(Constants.LOG_TAG_UI, "Got left " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.point_left);
                break;

            case right:
                Log.i(Constants.LOG_TAG_UI, "Got right " + aMeasureValue);
                populateMeasure(aMeasureValue, R.id.point_right);
                break;

            default:
                Log.i(Constants.LOG_TAG_UI, "Ignore type " + aMeasureTarget);
        }
    }

    private void populateMeasure(float aMeasure, int anEditTextId) {
        EditText field = findViewById(anEditTextId);
        StringUtils.setNotNull(field, aMeasure);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // detect swipes to switch the current point
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_SWIPE_DISTANCE) {
                    try {
                        Leg currLeg = getCurrentLeg();
                        if (!currLeg.isNew()) {
                            Leg nextPoint;
                            if (deltaX > 0) {
                                Log.i(Constants.LOG_TAG_UI, "swipe right");
                                nextPoint = DaoUtil.getGalleryPrevLeg(currLeg);
                            } else {
                                Log.i(Constants.LOG_TAG_UI, "swipe left");
                                nextPoint = DaoUtil.getGalleryNextLeg(getCurrentLeg());
                            }
                            swipeTo(nextPoint);
                        }
                    } catch (Exception e) {
                        Log.e(Constants.LOG_TAG_UI, "Failed to swipe", e);
                        UIUtilities.showNotification(R.string.error);
                    }
                }
                break;
        }

        // propagate back to allow scrolls etc
        return super.onTouchEvent(event);
    }

    private void swipeTo(Leg aNextLeg) {
        if (aNextLeg != null) {
            Intent intent = new Intent(PointActivity.this, PointActivity.class);
            intent.putExtra(Constants.LEG_SELECTED, aNextLeg.getId());
            getWorkspace().setActiveLeg(aNextLeg);
            startActivity(intent);
            finish();
        } else {
            UIUtilities.showNotification(R.string.point_swipe_not_possible);
        }
    }

    /**
     * Executed when menu button for reverse leg is selected. It shows confirmation dialog to
     * confirm the operation of reversing.
     */
    private void confirmReverseLeg() {
        try {

            // validate
            final EditText azimuth = findViewById(R.id.point_azimuth);
            final EditText slope = findViewById(R.id.point_slope);

            if (UIUtilities.validateNumber(azimuth, true) && UIUtilities.checkAzimuth(azimuth)
                    && UIUtilities.validateNumber(slope, false) && UIUtilities.checkSlope(slope) ) {

                // build and show confirmation dialog
                String message = getString(R.string.main_reverse_message, mCurrentLeg.buildLegDescription());
                Bundle bundle = new Bundle();
                bundle.putSerializable(ConfirmationDialog.OPERATION, ConfirmationOperation.REVERSE_LEG);
                bundle.putString(ConfirmationDialog.MESSAGE, message);
                bundle.putString(ConfirmationDialog.TITLE, getString(R.string.title_warning));

                ConfirmationDialog confirmationDialog = new ConfirmationDialog();
                confirmationDialog.setArguments(bundle);
                confirmationDialog.show(getSupportFragmentManager(), ConfirmationDialog.CONFIRM_DIALOG);

            } else {
                // TODO Alexander, what if not valid? Never handled before
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to build reverse dialog", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    @Override
    public boolean confirmOperation(ConfirmationOperation operationArg) {
        if (ConfirmationOperation.REVERSE_LEG.equals(operationArg)){
            reverseLeg();
            return true;
        } else {
            return super.confirmOperation(operationArg);
        }
    }

    /**
     * Reverses the leg after confirmation from the confirmation dialog
     */
    private void reverseLeg() {
        Log.i(Constants.LOG_TAG_UI, "Reverse leg");
        // validate
        final EditText azimuth = findViewById(R.id.point_azimuth);
        final EditText slope = findViewById(R.id.point_slope);

        // if values are present update them in the UI only, they will be persisted on "save"
        try {
            // calculate in degrees
            Float currAzimuth = MapUtilities.getAzimuthInDegrees(StringUtils.getFromEditTextNotNull(azimuth));
            if (currAzimuth != null) {
                Float reversedAzimuth = MapUtilities.add90Degrees(MapUtilities.add90Degrees(currAzimuth));

                // back to grads if needed
                String currAzimuthMeasure = Options.getOptionValue(Option.CODE_AZIMUTH_UNITS);
                if (Option.UNIT_GRADS.equals(currAzimuthMeasure)) {
                    reversedAzimuth = MapUtilities.degreesToGrads(reversedAzimuth);
                }
                populateMeasure(reversedAzimuth, R.id.point_azimuth);
            }
            Float currSlope = StringUtils.getFromEditTextNotNull(slope);
            if (currSlope != null && currSlope != 0) {
                populateMeasure(-currSlope, R.id.point_slope);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to reverse leg", e);
            UIUtilities.showNotification(R.string.error);
        }
    }


    @Override
    public void delete(Serializable vectorArg) {
        Log.i(Constants.LOG_TAG_UI, "Delete vector");
        try {
            if (vectorArg != null && vectorArg instanceof Vector ) {
                DaoUtil.deleteVector((Vector) vectorArg);
                UIUtilities.showNotification(R.string.action_deleted);
                loadLegVectors(getCurrentLeg());
            } else {
                String vectorClass = vectorArg != null ? vectorArg.getClass().getName() : null;
                Log.e(Constants.LOG_TAG_UI, "Failed to delete vector. Passed instance not a Vector but:" + vectorClass);
                UIUtilities.showNotification(R.string.error);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to delete vector", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    /**
     * Called back once the auto/manual option for GPS is selected.
     *
     * @param gpsTypeArg - type of option manual GPS entry, or automatic using GPS service
     */
    @Override
    public void gpsTypeSelected(GpsTypeDialog.GPSType gpsTypeArg) {

        // dismiss the dialog
        Fragment prev = getSupportFragmentManager().findFragmentByTag(GpsTypeDialog.GPS_TYPE_DIALOG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }

        Point parentPoint = getCurrentLeg().getFromPoint();
        if (GpsTypeDialog.GPSType.AUTO.equals(gpsTypeArg)) {

            if (!PermissionUtil.requestPermission(ACCESS_FINE_LOCATION, this, PERM_REQ_CODE_GPS)) {
                return;
            }

            Intent intent = new Intent(this, GPSActivity.class);
            intent.putExtra(GPSActivity.POINT, parentPoint);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, GPSManualActivity.class);
            intent.putExtra(GPSActivity.POINT, parentPoint);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERM_REQ_CODE_CAMERA:
                if (PermissionUtil.isGranted(permissions, grantResults)) {
                    photoButton();
                }
                return;

            case PERM_REQ_CODE_GPS:
                if (PermissionUtil.isGranted(permissions, grantResults)) {
                    Intent intent = new Intent(this, GPSActivity.class);
                    Point parentPoint = getCurrentLeg().getFromPoint();
                    intent.putExtra(GPSActivity.POINT, parentPoint);
                    startActivity(intent);
                }
                return;

            default:
                Log.i(Constants.LOG_TAG_SERVICE, "Ignore request " + requestCode);
        }
    }
}
