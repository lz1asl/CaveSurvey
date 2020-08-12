package com.astoev.cave.survey.activity.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.GalleryUtil;

import static com.astoev.cave.survey.activity.map.MapUtilities.getNextGalleryColor;

public class NewGalleryActivity extends MainMenuActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_gallery);

        try {
            // name
            TextView nameTextView = findViewById(R.id.new_gallery_name);
            nameTextView.setText(GalleryUtil.generateNextGalleryName());

            // color
            long currProjectGalleriesCount = GalleryUtil.getGalleriesCount(Workspace.getCurrentInstance().getActiveProjectId());
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
}
