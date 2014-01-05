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
import com.astoev.cave.survey.model.*;
import com.astoev.cave.survey.service.Options;
import com.astoev.cave.survey.service.export.excel.ExcelExport;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.FileStorageUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 2/12/12
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class InfoActivity extends MainMenuActivity {
	
	public static final String MIME_RESOURCE_FOLDER = "resource/folder";
	public static final String MIME_TYPE_ANY = "*/*";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        try {
            // prepare labels
            TextView projectName = (TextView) findViewById(R.id.infoProjectName);
            projectName.setText(getWorkspace().getActiveProject().getName());

            TextView projectCreated = (TextView) findViewById(R.id.infoProjectCreated);
            projectCreated.setText(getWorkspace().getActiveProject().getCreationDateFormatted());

            List<Leg> legs = DaoUtil.getCurrProjectLegs();

            TextView projectNumLegs = (TextView) findViewById(R.id.infoNumLegs);
            projectNumLegs.setText("" + legs.size());

            TextView projectTotalLength = (TextView) findViewById(R.id.infoRawDistance);
            float totalLength = 0;
            int numNotes = 0, numDrawings = 0, numCoordinates = 0, numPhotos = 0;
            for (Leg l : legs) {
                if (l.getDistance() != null) {
                    totalLength += l.getDistance();
                }

                // notes
                Note note = DaoUtil.getActiveLegNote(l);
                if (note != null) {
                    numNotes ++;
                }

                // drawings
                List<Sketch> drawings = getWorkspace().getDBHelper().getSketchDao().queryBuilder().where().eq(Sketch.COLUMN_POINT_ID, l.getFromPoint().getId()).query();
                if (drawings != null && drawings.size() > 0){
                    numDrawings += drawings.size();
                }

                // gps
                List<Location> locations = getWorkspace().getDBHelper().getLocationDao().queryBuilder().where().eq(Location.COLUMN_POINT_ID, l.getFromPoint().getId()).query();
                if (locations != null && locations.size() >0) {
                    numCoordinates += locations.size();
                }

                // photo
                List<Photo>  photos = getWorkspace().getDBHelper().getPhotoDao().queryBuilder().where().eq(Photo.COLUMN_POINT_ID, l.getFromPoint().getId()).query();
                if (photos != null && photos.size() >0) {
                    numPhotos += photos.size();
                }
            }
            String lengthLabel = StringUtils.floatToLabel(totalLength) + " " + Options.getOptionValue(Option.CODE_DISTANCE_UNITS);
            projectTotalLength.setText(lengthLabel);


            TextView projectNumNotes = (TextView) findViewById(R.id.infoNumNotes);

            projectNumNotes.setText(StringUtils.intToLabel(numNotes));
            TextView projectNumDrawings = (TextView) findViewById(R.id.infoNumDrawings);
            projectNumDrawings.setText(StringUtils.intToLabel(numDrawings));
            TextView projectNumCoordinates = (TextView) findViewById(R.id.infoNumCoordinates);
            projectNumCoordinates.setText(StringUtils.intToLabel(numCoordinates));
            TextView projectNumPhotos = (TextView) findViewById(R.id.infoNumPhotos);
            projectNumPhotos.setText(StringUtils.intToLabel(numPhotos));


            ((TextView)findViewById(R.id.infoDistanceIn)).setText(Options.getOptionValue(Option.CODE_DISTANCE_UNITS));
            ((TextView)findViewById(R.id.infoAzimuthIn)).setText(Options.getOptionValue(Option.CODE_AZIMUTH_UNITS));
            ((TextView)findViewById(R.id.infoSlopeIn)).setText(Options.getOptionValue(Option.CODE_SLOPE_UNITS));


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

            UIUtilities.showNotification(this, R.string.export_done, exportPath);
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
	private void onViewFiles(){
    	String projectName = getWorkspace().getActiveProject().getName();
    	
    	Log.i(Constants.LOG_TAG_SERVICE, "View files selected for project:" + projectName);
    	
    	File projectHome = FileStorageUtil.getProjectHome(projectName);
    	
    	if (projectHome != null){
	    	Uri contentUri = Uri.fromFile(projectHome);
	    	
	    	Log.i(Constants.LOG_TAG_SERVICE, "Uri:" + contentUri);
	    	
	        String chooserTitle = getResources().getString(R.string.info_chooser_open_folder);
	        
	        Intent intent = new Intent(Intent.ACTION_VIEW);
	        intent.setData(contentUri); // works wit OI File manager
	        
	        PackageManager pacakgeManager = getPackageManager();
	        
	        // works with OI File manager
	        if (intent.resolveActivity(pacakgeManager) != null){
	        	
	        	Log.i(Constants.LOG_TAG_SERVICE, "ACTION_VIEW resolved");
	        	
	        	startActivity(Intent.createChooser(intent, chooserTitle));
	        	return;
	        }
	        
	        // Works with ES file manager
	        intent.setDataAndType(contentUri, MIME_RESOURCE_FOLDER);
	        if (intent.resolveActivity(pacakgeManager) != null){
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
			case R.id.info_action_export:{
				exportProject();
				return true;
			}
			case R.id.info_action_view_files:{
				onViewFiles();
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}		
	}
}
