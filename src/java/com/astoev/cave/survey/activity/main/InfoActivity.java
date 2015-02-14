package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.openstopo.WebViewActivity;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.service.export.json.OpensTopoJsonExport;
import com.astoev.cave.survey.service.orientation.MagneticOrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationDeprecatedProcessor;
import com.astoev.cave.survey.service.orientation.OrientationProcessor;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;
import com.astoev.cave.survey.service.orientation.RotationOrientationProcessor;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.ProjectInfo;
import com.astoev.cave.survey.util.StringUtils;

import org.apache.commons.codec.net.URLCodec;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/12/12
 * Time: 11:37 AM
 *
 * @author astoev
 * @author jmitrev
 */
public class InfoActivity extends MainMenuActivity {

    public static final String MIME_RESOURCE_FOLDER = "resource/folder";
    public static final String MIME_TYPE_ANY = "*/*";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        try {

            ProjectInfo projectInfo = DaoUtil.getProjectInfo();
            // prepare labels
            TextView projectName = (TextView) findViewById(R.id.infoProjectName);
            projectName.setText(projectInfo.getName());

            TextView projectCreated = (TextView) findViewById(R.id.infoProjectCreated);
            projectCreated.setText(projectInfo.getCreationDate());

            TextView projectNumGalleries = (TextView) findViewById(R.id.infoNumGalleries);
            projectNumGalleries.setText(String.valueOf(projectInfo.getGalleries()));

            TextView projectNumLegs = (TextView) findViewById(R.id.infoNumLegs);
            projectNumLegs.setText(String.valueOf(projectInfo.getLegs()));

            TextView projectTotalLength = (TextView) findViewById(R.id.infoRawDistance);

            String lengthLabel = StringUtils.floatToLabel(projectInfo.getLength()) + " " + Options.getOptionValue(Option.CODE_DISTANCE_UNITS);
            projectTotalLength.setText(lengthLabel);

            TextView projectNumNotes = (TextView) findViewById(R.id.infoNumNotes);
            projectNumNotes.setText(StringUtils.intToLabel(projectInfo.getNotes()));

            TextView projectNumDrawings = (TextView) findViewById(R.id.infoNumDrawings);
            projectNumDrawings.setText(StringUtils.intToLabel(projectInfo.getSketches()));

            TextView projectNumCoordinates = (TextView) findViewById(R.id.infoNumCoordinates);
            projectNumCoordinates.setText(StringUtils.intToLabel(projectInfo.getLocations()));

            TextView projectNumPhotos = (TextView) findViewById(R.id.infoNumPhotos);
            projectNumPhotos.setText(StringUtils.intToLabel(projectInfo.getPhotos()));

            CharSequence distanceUnitsLabel = StringUtils.extractDynamicResource(
                    getResources(), StringUtils.RESOURCE_PREFIX_UNITS + Options.getOptionValue(Option.CODE_DISTANCE_UNITS));
            ((TextView) findViewById(R.id.infoDistanceIn)).setText(distanceUnitsLabel);
            CharSequence azimuthUnitsLabel = StringUtils.extractDynamicResource(
                    getResources(), StringUtils.RESOURCE_PREFIX_UNITS + Options.getOptionValue(Option.CODE_AZIMUTH_UNITS));
            ((TextView) findViewById(R.id.infoAzimuthIn)).setText(azimuthUnitsLabel);
            CharSequence slopeUnitsLabel = StringUtils.extractDynamicResource(
                    getResources(), StringUtils.RESOURCE_PREFIX_UNITS + Options.getOptionValue(Option.CODE_SLOPE_UNITS));
            ((TextView) findViewById(R.id.infoSlopeIn)).setText(slopeUnitsLabel);

            // set the value for azimuth build in processor
            if (Option.CODE_SENSOR_INTERNAL.equals(Options.getOption(Option.CODE_AZIMUTH_SENSOR).getValue())) {
                TextView azimuthSensor = (TextView) findViewById(R.id.info_azimuth_sensor);
                OrientationProcessor orientationProcessor = OrientationProcessorFactory.getOrientationProcessor(this, null);
                if (orientationProcessor.canReadOrientation()) {
                    if (orientationProcessor instanceof RotationOrientationProcessor) {
                        azimuthSensor.setText(getString(R.string.rotation_sensor));
                    } else if (orientationProcessor instanceof MagneticOrientationProcessor) {
                        azimuthSensor.setText(getString(R.string.magnetic_sensor));
                    } else if (orientationProcessor instanceof OrientationDeprecatedProcessor) {
                        azimuthSensor.setText(getString(R.string.orientation_sensor));
                    }
                }
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render info activity", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void exportProject() {
        try {
            Log.i(Constants.LOG_TAG_SERVICE, "Start excel export");

            // export legs

            ExcelExport export = new ExcelExport(this);
            String exportPath = export.runExport(getWorkspace().getActiveProject());
            if (StringUtils.isEmpty(exportPath)) {
                UIUtilities.showNotification(this, R.string.export_io_error, exportPath);
            } else {
                UIUtilities.showNotification(this, R.string.export_done, exportPath);
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to export project", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    /**
     * Handles click on Files button. Tries to find an activity to show project's folder by sending
     * ACTION_VIEW intent. Sequentially tries to find a compatible activity. First looks for activity to just
     * handle a file path. Second tries to for mime-type "resource/folder" and finally executes a search for
     * mime-type "*.*" and leaves the user to select a matching application.
     */
    private void onViewFiles() {
        String projectName = getWorkspace().getActiveProject().getName();

        Log.i(Constants.LOG_TAG_SERVICE, "View files selected for project:" + projectName);

        File projectHome = FileStorageUtil.getProjectHome(projectName);

        if (projectHome != null) {
            Uri contentUri = Uri.fromFile(projectHome);

            Log.i(Constants.LOG_TAG_SERVICE, "Uri:" + contentUri);

            String chooserTitle = getResources().getString(R.string.info_chooser_open_folder);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(contentUri); // works wit OI File manager

            PackageManager pacakgeManager = getPackageManager();

            // works with OI File manager
            if (intent.resolveActivity(pacakgeManager) != null) {

                Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW resolved");

                startActivity(Intent.createChooser(intent, chooserTitle));
                return;
            }

            // Works with ES file manager
            intent.setDataAndType(contentUri, MIME_RESOURCE_FOLDER);
            if (intent.resolveActivity(pacakgeManager) != null) {
                Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW with resource/folder resolved");

                startActivity(Intent.createChooser(intent, chooserTitle));
                return;
            }

            // brute force to choose a file manager
            Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW with */* resolved");
            intent.setDataAndType(contentUri, MIME_TYPE_ANY);
            startActivity(Intent.createChooser(intent, chooserTitle));
            return;

        } else {
            Log.e(Constants.LOG_TAG_SERVICE, "No project folder for project:" + projectName);
            UIUtilities.showNotification(R.string.io_error);
        }
    }//end of onViewFiles

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
     */
    @Override
    protected int getChildsOptionsMenu() {
        return R.menu.infomenu;
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Info activity's menu selected - " + item.toString());

        switch (item.getItemId()) {
            case R.id.info_action_export: {
                exportProject();
                return true;
            }
            case R.id.info_action_openstopo: {


                try {
                    // export
                    Log.i(Constants.LOG_TAG_SERVICE, "Start json export");
                    OpensTopoJsonExport export = new OpensTopoJsonExport(this);
                    String exportPath = export.runExport(getWorkspace().getActiveProject());
                    if (StringUtils.isEmpty(exportPath)) {
                        UIUtilities.showNotification(this, R.string.export_io_error, exportPath);
                    } else {
                        // load ui
                        Log.i(Constants.LOG_TAG_SERVICE, "exported to " + exportPath);
                        Intent intent = new Intent(InfoActivity.this, WebViewActivity.class);
                        intent.putExtra("path", new URLCodec().encode(exportPath));
                        startActivity(intent);
                    }

                    return true;

                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed to export project", e);
                    UIUtilities.showNotification(R.string.error);
                }


            }
            case R.id.info_action_view_files: {
                onViewFiles();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
