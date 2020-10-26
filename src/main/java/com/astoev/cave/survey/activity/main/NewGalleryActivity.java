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
import com.astoev.cave.survey.model.Gallery;
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
            String name;
            if (currProjectGalleriesCount == 0) {
                name = GalleryUtil.getFirstGalleryName();
            } else {
                name = GalleryUtil.generateNextGalleryName();
            }
            TextView nameTextView = findViewById(R.id.new_gallery_name);
            nameTextView.setText(name);

            // color
            View colorView = findViewById(R.id.new_gallery_color);
            colorView.setBackgroundColor(getNextGalleryColor((int) currProjectGalleriesCount));

            // types
            String[] galleryTypes = getResources().getStringArray(R.array.gallery_types);
            if (currProjectGalleriesCount > 0) {
                // only the first gallery can be geolocation
                String[] typesWithoutFirst = new String[galleryTypes.length - 1];
                for (int i=1; i<galleryTypes.length; i++) {
                    typesWithoutFirst[i-1] = galleryTypes[i];
                }
                galleryTypes = typesWithoutFirst;
            }
            Spinner typesSpinner = findViewById(R.id.new_gallery_type);
            final ArrayAdapter<String> typesAdapter =
                    new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            galleryTypes);
            typesSpinner.setAdapter(typesAdapter);

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

            // TODO get the color from the actual view
            long currProjectGalleriesCount = GalleryUtil.getGalleriesCount(Workspace.getCurrentInstance().getActiveProjectId());
            int color = getNextGalleryColor((int) currProjectGalleriesCount);

            String[] galleryTypes = getResources().getStringArray(R.array.gallery_types);
            Spinner typesSpinner = findViewById(R.id.new_gallery_type);
            GalleryType type;
            String selectedGalleyType = (String) typesSpinner.getSelectedItem();
            if (galleryTypes[0].equals(selectedGalleyType)) {
                type = GEOLOCATION;
            } else if (galleryTypes[1].equals(selectedGalleyType)) {
                type = CLASSIC;
            } else {
                type = CLASSIC;
            }

            TextView nameTextView = findViewById(R.id.new_gallery_name);
            Gallery gallery = GalleryUtil.createGallery(project, nameTextView.getText().toString(), color, type);
            Workspace.getCurrentInstance().setActiveGalleryId(gallery.getId());

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
