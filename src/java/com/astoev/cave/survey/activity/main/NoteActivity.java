package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.BaseActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Note;
import com.j256.ormlite.misc.TransactionManager;

import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/27/12
 * Time: 3:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class NoteActivity extends BaseActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        TextView title = (TextView) findViewById(R.id.note_title);

        try {

            Leg activeLeg = mWorkspace.getActiveOrFirstLeg();

            title.setText(activeLeg.buildLegDescription(this));

            Note note = Leg.getActiveLegNote(activeLeg, mWorkspace);
            if (note != null) {
                // load note
                TextView noteText = (TextView) findViewById(R.id.note_text);
                noteText.setText(note.getText());
            }

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to load note", e);
            UIUtilities.reportException(this, e);
        }
    }

    public void saveNote(View view) {

        try {

            Log.v(Constants.LOG_TAG_UI, "Saving note");

            final TextView noteText = (TextView) findViewById(R.id.note_text);

            final Leg activeLeg = mWorkspace.getActiveOrFirstLeg();

            TransactionManager.callInTransaction(mWorkspace.getDBHelper().getConnectionSource(),
                    new Callable<Void>() {
                        public Void call() throws Exception {

                            try {
                                Note existingNote = Leg.getActiveLegNote(activeLeg, mWorkspace);

                                if (null != existingNote) {
                                    Log.i(Constants.LOG_TAG_DB, "Existing note found");
                                    // update existing
                                    existingNote.setText(noteText.getText().toString());
                                    mWorkspace.getDBHelper().getNoteDao().update(existingNote);
                                } else {
                                    // create new
                                    existingNote = new Note(noteText.getText().toString());
                                    existingNote.setPoint(activeLeg.getFromPoint());
                                    mWorkspace.getDBHelper().getNoteDao().create(existingNote);
                                }

                                UIUtilities.showNotification(NoteActivity.this, R.string.note_saved);
                                Intent intent = new Intent(NoteActivity.this, MainActivity.class);
                                startActivity(intent);

                                Log.i(Constants.LOG_TAG_DB, "Note stored");
                                return null;
                            } catch (Exception e) {
                                Log.e(Constants.LOG_TAG_DB, "Failed to save note", e);
                                UIUtilities.showNotification(NoteActivity.this, R.string.error);
                                throw e;
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to save note", e);
            UIUtilities.showNotification(NoteActivity.this, R.string.error);
        }
    }
}