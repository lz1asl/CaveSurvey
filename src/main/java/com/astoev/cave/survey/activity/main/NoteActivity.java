package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.util.UIUtilities;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.util.DaoUtil;
import com.j256.ormlite.misc.TransactionManager;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/27/12
 * Time: 3:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class NoteActivity extends MainMenuActivity {

    private Integer mCurrLeg = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        try {

            Bundle extras = getIntent().getExtras();
            mCurrLeg = extras.getInt(Constants.LEG_SELECTED);
            Leg activeLeg = DaoUtil.getLeg(mCurrLeg);

            if (activeLeg != null) {

                Note note = DaoUtil.getActiveLegNote(activeLeg);
                if (note != null) {
                    // load note if any
                    TextView noteText = findViewById(R.id.note_text);
                    noteText.setText(note.getText());
                }
            } else {
                mCurrLeg = null;
                String mCurrNote = extras.getString(Constants.LEG_NOTE);
                if (mCurrNote != null) {
                    TextView noteText = findViewById(R.id.note_text);
                    noteText.setText(mCurrNote);
                }
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to load note", e);
            UIUtilities.reportException(this, e);
        }
    }

    public void saveNote() {

        try {

            Log.v(Constants.LOG_TAG_UI, "Saving note");

            final TextView noteText = findViewById(R.id.note_text);

            if (mCurrLeg != null) {
                TransactionManager.callInTransaction(getWorkspace().getDBHelper().getConnectionSource(),
                        (Callable<Void>) () -> {
                            try {
                                Leg activeLeg = DaoUtil.getLeg(mCurrLeg);

                                Note existingNote = DaoUtil.getActiveLegNote(activeLeg);

                                if (null != existingNote) {
                                    Log.i(Constants.LOG_TAG_DB, "Existing note found");
                                    // update existing
                                    existingNote.setText(noteText.getText().toString());
                                    getWorkspace().getDBHelper().getNoteDao().update(existingNote);
                                } else {
                                    // create new
                                    existingNote = new Note(noteText.getText().toString());
                                    existingNote.setPoint(activeLeg.getFromPoint());
                                    existingNote.setGalleryId(activeLeg.getGalleryId());
                                    getWorkspace().getDBHelper().getNoteDao().create(existingNote);
                                }

                                UIUtilities.showNotification(R.string.note_saved);
                                Intent data = new Intent();
                                data.putExtra("note", noteText.getText().toString());
                                setResult(RESULT_OK, data);
                                finish();

                                Log.i(Constants.LOG_TAG_DB, "Note stored");
                                return null;
                            } catch (Exception e) {
                                Log.e(Constants.LOG_TAG_DB, "Failed to save note", e);
                                UIUtilities.showNotification(R.string.error);
                                throw e;
                            }
                        });

            } else {
                Log.v(Constants.LOG_TAG_UI, "Return note to point");
                Intent data = new Intent();
                data.putExtra("note", noteText.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to save note", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#getChildsOptionsMenu()
     */
    @Override
    protected int getChildsOptionsMenu() {
        return R.menu.notemenu;
    }

    /**
     * Does not want to show the base menu items
     *
     * @see com.astoev.cave.survey.activity.MainMenuActivity#showBaseOptionsMenu()
     */
    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }

    /**
     * @see com.astoev.cave.survey.activity.MainMenuActivity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Note activity's menu selected - " + item.toString());

        switch (item.getItemId()) {
            case R.id.note_action_save: {
                saveNote();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	/**
	 * @see com.astoev.cave.survey.activity.BaseActivity#getScreenTitle()
	 */
	@Override
	protected String getScreenTitle() {
		StringBuilder builder = new StringBuilder();
		builder.append(getString(R.string.note_title));
		if (mCurrLeg != null){
			try {
				builder.append(DaoUtil.getLeg(mCurrLeg).buildLegDescription());
			} catch (SQLException e) {
				Log.i(Constants.LOG_TAG_UI, "Unable to laod leg:" + mCurrLeg);
			}
		}
		return builder.toString();
	}
    
}