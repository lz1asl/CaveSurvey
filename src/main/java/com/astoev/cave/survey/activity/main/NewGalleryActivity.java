package com.astoev.cave.survey.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.util.UIUtilities;
import com.astoev.cave.survey.model.GalleryType;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.GalleryUtil;

import static com.astoev.cave.survey.activity.map.MapUtilities.getNextGalleryColor;
import static com.astoev.cave.survey.model.GalleryType.CLASSIC;
import static com.astoev.cave.survey.model.GalleryType.GEOLOCATION;

public class NewGalleryActivity extends MainMenuActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_gallery);

        try {
            long currProjectGalleriesCount = GalleryUtil.getGalleriesCount(Workspace.getCurrentInstance().getActiveProjectId());

            // name
            TextView nameTextView = findViewById(R.id.new_gallery_name);
            String name;
            if (currProjectGalleriesCount == 0) {
                name = GalleryUtil.getFirstGalleryName();
            } else {
                name = GalleryUtil.generateNextGalleryName();
            }
            nameTextView.setText(name);

            // color
            View colorView = findViewById(R.id.new_gallery_color);
            colorView.setBackgroundColor(getNextGalleryColor((int) currProjectGalleriesCount));

            // types
            Spinner typesSpinner = findViewById(R.id.new_gallery_type);
            final ArrayAdapter<String> typesAdapter =
                    new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            getResources().getStringArray(R.array.gallery_types));
            typesSpinner.setAdapter(typesAdapter);

            // classic gallery by default after the first geolocation
            if (currProjectGalleriesCount > 0) {
                typesSpinner.setSelection(1);
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed to render gallery activity", e);
            UIUtilities.showNotification(R.string.error);
        }

    }

    @Override
    protected boolean showBaseOptionsMenu() {
        return false;
    }

    @Override
    protected String getScreenTitle() {
        return getString(R.string.new_gallery_title);
    }

    public void createButton(View aView) {

        try {
            // create next gallery
            Project project = Workspace.getCurrentInstance().getActiveProject();
            TextView nameTextView = findViewById(R.id.new_gallery_name);

            // TODO get the color from the actual view
            long currProjectGalleriesCount = GalleryUtil.getGalleriesCount(Workspace.getCurrentInstance().getActiveProjectId());
            int color = getNextGalleryColor((int) currProjectGalleriesCount);

            Spinner typesSpinner = findViewById(R.id.new_gallery_type);
            GalleryType type;
            switch (typesSpinner.getSelectedItemPosition()) {
                case 0:
                    type = GEOLOCATION;
                    break;
                case 1:
                    type = CLASSIC;
                    break;
                default:
                    type = CLASSIC;
            }

            GalleryUtil.createGallery(project, nameTextView.getText().toString(), color, type);

            // start editing the first leg
            Intent intent = new Intent(this, PointActivity.class);
            intent.putExtra(Constants.LEG_SELECTED, getWorkspace().getActiveLegId());
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_UI, "Failed create gallery", e);
            UIUtilities.showNotification(R.string.error);
        }
    }
}
