package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.activity.dialog.AddNewDialog;
import com.astoev.cave.survey.activity.dialog.AddNewSelectedHandler;
import com.astoev.cave.survey.activity.dialog.ChangeLegDialog;
import com.astoev.cave.survey.activity.dialog.MiddlePointDialog;
import com.astoev.cave.survey.activity.map.MapActivity;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.DaoUtil;
import com.astoev.cave.survey.util.StringUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * User: astoev
 * Date: 1/23/12
 * Time: 3:04 PM
 *
 * @author Alexander Stoev
 * @author Zhivko Mitrev
 */
public class MainActivity extends MainMenuActivity implements AddNewSelectedHandler{

    private SparseIntArray mGalleryColors = new SparseIntArray();
    private SparseArray<String> mGalleryNames = new SparseArray<>();
    
    private static boolean isDebug = false;
    
    private String sketchPrefix;
    private String notePrefix;
    private String photoPrefix;
    private String locationPrefix;
    private String vectorsPrefix;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sketchPrefix   = getString(R.string.table_sketch_prefix);
        notePrefix     = getString(R.string.table_note_prefix);
        photoPrefix    = getString(R.string.table_photo_prefix);
        locationPrefix = getString(R.string.table_location_prefix);
        vectorsPrefix = getString(R.string.table_vector_prefix);
    }

    private void drawTable() {
        try {
            Leg activeLeg = getWorkspace().getActiveLeg();

            if (activeLeg == null) {
                activeLeg = getWorkspace().getLastLeg();
                getWorkspace().setActiveLeg(activeLeg);
            }

            mGalleryColors.clear();
            mGalleryNames.clear();

            // prepare labels
            TextView activeLegName = (TextView) findViewById(R.id.mainActiveLeg);
            activeLegName.setText(activeLeg.buildLegDescription());

            TableLayout table = (TableLayout) findViewById(R.id.mainTable);

            // prepare grid
            table.removeAllViews();

            List<Leg> legs = DaoUtil.getCurrProjectLegs(true);

            boolean currentLeg;
            Integer lastGalleryId = null, prevGalleryId;

            for (final Leg l : legs) {
                TableRow row = new TableRow(this);
                LayoutParams params = new TableLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
                row.setLayoutParams(params);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View aView) {
                        Intent intent = new Intent(MainActivity.this, PointActivity.class);
                        intent.putExtra(Constants.LEG_SELECTED, l.getId());
                        getWorkspace().setActiveLeg(l);
                        startActivity(intent);
                    }
                });

                currentLeg = getWorkspace().getActiveLegId().equals(l.getId());
                if (currentLeg) {
                    row.setBackgroundColor(Color.parseColor("#202020"));
                }

                if (mGalleryColors.get(l.getGalleryId(), Constants.NOT_FOUND) == Constants.NOT_FOUND) {
                    Gallery gallery = DaoUtil.getGallery(l.getGalleryId());
                    mGalleryColors.put(l.getGalleryId(), MapUtilities.getNextGalleryColor(mGalleryColors.size()));
                    mGalleryNames.put(l.getGalleryId(), gallery.getName());
                }

                Point fromPoint = l.getFromPoint();
                DaoUtil.refreshPoint(fromPoint);
                String fromPointString = fromPoint.getName();

                if (lastGalleryId == null) {
                    lastGalleryId = l.getGalleryId();
                }

                if (l.getGalleryId().equals(lastGalleryId)) {
                    fromPointString =  mGalleryNames.get(l.getGalleryId()) + fromPointString;
                    prevGalleryId = l.getGalleryId();
                } else {
                    prevGalleryId = DaoUtil.getLegByToPointId(l.getFromPoint().getId()).getGalleryId();
                    fromPointString =  mGalleryNames.get(prevGalleryId) + fromPointString;
                }

                
                Point toPoint = l.getToPoint();
                DaoUtil.refreshPoint(toPoint);
                String toPointString = mGalleryNames.get(l.getGalleryId()) + toPoint.getName();

                lastGalleryId = l.getGalleryId();
                
                if (isDebug){
                	fromPointString = fromPointString +"(" + fromPoint.getId() + ")";
                	toPointString = toPointString + "("+toPoint.getId()+")";
                }

                if (l.isMiddle()) {
                    row.addView(createTextView("", currentLeg, false, mGalleryColors.get(prevGalleryId)));
                    row.addView(createTextView("", currentLeg, false, mGalleryColors.get(l.getGalleryId())));
                    row.addView(createTextView(Constants.MIDDLE_POINT_DELIMITER + StringUtils.floatToLabel(l.getMiddlePointDistance()), currentLeg, true));
                } else {
                    row.addView(createTextView(fromPointString, currentLeg, false, mGalleryColors.get(prevGalleryId)));
                    row.addView(createTextView(toPointString, currentLeg, false, mGalleryColors.get(l.getGalleryId())));
                    row.addView(createTextView(l.getDistance(), currentLeg, true));
                    row.addView(createTextView(l.getAzimuth(), currentLeg, true));
                    row.addView(createTextView(l.getSlope(), currentLeg, true));
                }

                StringBuilder moreText = new StringBuilder();
                
                //TODO Debug
                if (isDebug){
                	moreText.append(l.getGalleryId()).append(" ");
                }

                if (!l.isMiddle()) {
                    Sketch sketch = DaoUtil.getScetchByLeg(l);
                    if (sketch != null){
                        moreText.append(sketchPrefix);
                    }
                    Note note = DaoUtil.getActiveLegNote(l);
                    if (note != null){
                        moreText.append(notePrefix);
                    }
                    Photo photo = DaoUtil.getPhotoByLeg(l);
                    if (photo != null) {
                        moreText.append(photoPrefix);
                    }
                    Location location = DaoUtil.getLocationByPoint(fromPoint);
                    if (location != null){
                        moreText.append(locationPrefix);
                    }
                    if (DaoUtil.hasVectorsByLeg(l)) {
                        moreText.append(vectorsPrefix);
                    }
                }

                // reset the text appearance to small
                TextView view = createTextView(moreText.toString(), currentLeg, true);
                view.setTextAppearance(this, android.R.style.TextAppearance_Small);
                row.addView(view);
                table.addView(row, params);
            }
            table.invalidate();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render main activity", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private TextView createTextView(Float aMeasure, boolean isCurrentLeg, boolean allowEditing) {
        return createTextView(StringUtils.floatToLabel(aMeasure), isCurrentLeg, allowEditing);
    }
    
    private TextView createTextView(String aText, boolean isCurrentLeg, boolean allowEditing) {
        TextView edit = new TextView(this);
        edit.setLines(1);
        if (aText != null) {
            edit.setText(aText);
        }
        edit.setGravity(Gravity.CENTER);

        if (!isCurrentLeg || !allowEditing) {
            edit.setEnabled(false);
        }

        //set text appearance as medium
        edit.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        return edit;
    }
    
    private TextView createTextView(String aText, boolean isCurrentLeg, boolean allowEditing, int aColor) {
    	TextView edit = createTextView(aText, isCurrentLeg, allowEditing);
    	edit.setTextColor(aColor);
    	return edit;
    }

    public void addButtonClick() {

        Log.i(Constants.LOG_TAG_UI, "Adding");
        AddNewDialog addNewDialog = new AddNewDialog();
        addNewDialog.show(getSupportFragmentManager(), AddNewDialog.ADD_NEW_DIALOG);
    }

    /**
     * Handles the selected element for AddNewDialog
     *
     * @param itemArg item selected
     */
    public void addNewSelected(int itemArg){

        // dismiss the dialog
        Fragment prev = getSupportFragmentManager().findFragmentByTag(AddNewDialog.ADD_NEW_DIALOG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }

        try {
            if (0 == itemArg) {
                // next leg
                addLeg(false);
            } else if (1 == itemArg) {
                Leg prevLeg = DaoUtil.getLegByToPointId(Workspace.getCurrentInstance().getActiveLeg().getFromPoint().getId());
                if (prevLeg == null) {
                    // not supported
                    UIUtilities.showNotification(R.string.gallery_after_first_point);
                } else {
                    // next gallery
                    addLeg(true);
                }
            } else if (2 == itemArg) {
                requestLengthAndAddMiddle();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Error adding", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    private void requestLengthAndAddMiddle() throws SQLException {
        new MiddlePointDialog().show(getSupportFragmentManager(), "middle_point_dialog");
    }

    private void addLeg(final boolean isDeviation) throws SQLException {
        Log.i(Constants.LOG_TAG_UI, "Creating leg");

        Intent intent = new Intent(MainActivity.this, PointActivity.class);
        intent.putExtra(Constants.GALLERY_NEW, isDeviation);

        startActivity(intent);
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        drawTable();
    }

    public void plotButton() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

   /* public void plot3dButton() {
        Intent intent = new Intent(this, Map3DActivity.class);
        startActivity(intent);
    }*/

    public void infoButton() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
    }

    public void changeButton() {

        Log.i(Constants.LOG_TAG_UI, "Change active leg");

        // show choose leg dialog where to choose the active leg
        ChangeLegDialog changeLedDialog = new ChangeLegDialog();
        changeLedDialog.show(getSupportFragmentManager(), ChangeLegDialog.CHANGE_LEG);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
	 */
	@Override
	protected int getChildsOptionsMenu() {
		return R.menu.mainmenu;
	}

	/**
	 * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Log.i(Constants.LOG_TAG_UI, "Main activity's menu selected - " + item.toString());
		
		switch (item.getItemId()) {
			case R.id.main_action_add:{
				addButtonClick();
				return true;
			}
			case R.id.main_action_select : {
				changeButton();
				return true;
			}
			case R.id.main_action_map :{
				plotButton();
				return true;
			}
			case R.id.main_action_info : {
				infoButton();
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Add project's name as a title
	 * 
	 * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
	 */
	@Override
	protected String getScreenTitle() {
    	// set the name of the chosen project as title in the action bar
        Project activeProject = getWorkspace().getActiveProject();
        if (activeProject != null){
        	return activeProject.getName();
        }
        return null;
	}
    
}