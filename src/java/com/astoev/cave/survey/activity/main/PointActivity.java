package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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

            // down
            EditText down = (EditText) findViewById(R.id.point_down);
            setNotNull(down, mLegEdited.getDown());

            // left
            EditText left = (EditText) findViewById(R.id.point_left);
            setNotNull(left, mLegEdited.getLeft());

            // right
            EditText right = (EditText) findViewById(R.id.point_right);
            setNotNull(right, mLegEdited.getRight());

            // distance
            EditText distance = (EditText) findViewById(R.id.point_distance);
            setNotNull(distance, mLegEdited.getDistance());

            // azimuth
            EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
            setNotNull(azimuth, mLegEdited.getAzimuth());

            // slope
            EditText slope = (EditText) findViewById(R.id.point_slope);
            slope.setText("0");
            setNotNull(slope, mLegEdited.getSlope());

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render point", e);
            UIUtilities.showNotification(this, R.string.error);
        }
    }

    private void setNotNull(EditText aEditText, Float aValue) {
        if (aValue != null) {
            aEditText.setText(StringUtils.floatToLabel(aValue));
        }
    }


    private void saveLeg() {
        try {
            Log.i(Constants.LOG_TAG_UI, "Saving leg");

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

            // distance
            EditText distance = (EditText) findViewById(R.id.point_distance);
            mLegEdited.setDistance(StringUtils.getFromEditTextNotNull(distance));

            // compass
            EditText azimuth = (EditText) findViewById(R.id.point_azimuth);
            mLegEdited.setAzimuth(checkAzimuth(azimuth));

            // slope
            EditText slope = (EditText) findViewById(R.id.point_slope);
            mLegEdited.setSlope(StringUtils.getFromEditTextNotNull(slope));

            // save
            mWorkspace.getDBHelper().getLegDao().update(mLegEdited);
            UIUtilities.showNotification(this, R.string.todo);

            Log.i(Constants.LOG_TAG_UI, "Saved");
        } catch (DataException de) {
            Log.e(Constants.LOG_TAG_UI, "Leg not saved", de);
            String message = getString(R.string.popup_bad_input) + " : " + de.getMessage();
            UIUtilities.showNotification(this, message);
        } catch (Exception e) {
            UIUtilities.showNotification(this, R.string.error);
            Log.e(Constants.LOG_TAG_UI, "Leg not saved", e);
        }
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

    public void noteButton(View view) {
        Intent intent = new Intent(this, NoteActivity.class);
        startActivity(intent);
    }

    public void saveButton(View view) {
        saveLeg();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void drawingButton(View view) {
        Intent intent = new Intent(this, DrawingActivity.class);
        startActivity(intent);
    }

    public void coordinateButton(View view) {
        // TODO location http://www.tutorialforandroid.com/2009/05/permissions-journey-accesscoarselocatio.html
        UIUtilities.showNotification(this, R.string.todo);
    }

    public void deleteButton(View view) {
        // TODO
        UIUtilities.showNotification(this, R.string.todo);
    }

    public void readDistance(View view) {
        // TODO
        UIUtilities.showNotification(this, R.string.todo);
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

}
