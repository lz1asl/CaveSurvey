package com.astoev.cave.survey.activity.dialog;

import static android.R.layout.simple_list_item_1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.UIUtilities;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.LegMetadata;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.StringUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.sql.SQLException;
import java.util.List;

public class LegMetadataDialog extends DialogFragment  {

    private String mTitle;
    private Leg mLeg;


    public LegMetadataDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setIcon(R.drawable.ic_info_white);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.leg_metadata_dialog, null);
        builder.setView(view);

        TextView timestamp = view.findViewById(R.id.meta_date);
        timestamp.setText(StringUtils.dateToDateTimeString(mLeg.getDate()));

        try {
            List<LegMetadata> meta = Workspace.getCurrentInstance().getDBHelper().getLegMetadataDao()
                    .queryForEq(LegMetadata.COLUMN_LEG_ID, mLeg.getId());

            if (CollectionUtils.isNotEmpty(meta)) {

                LegMetadata[] metadataArray = new LegMetadata[meta.size()];
                metadataArray = meta.toArray(metadataArray);
                ArrayAdapter<LegMetadata> projectsAdapter = new ArrayAdapter<>(getActivity(), simple_list_item_1, metadataArray);
                ListView metaContainerContainer = view.findViewById(R.id.metadata_list);
                metaContainerContainer.setAdapter(projectsAdapter);
            }
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to load metadata", e);
            UIUtilities.showNotification(R.string.error);
        }

        return builder.create();
    }


    public void setTitle(String aTitle) {
        mTitle = aTitle;
    }

    public void setLeg(Leg aLeg) {
        mLeg = aLeg;
    }
}
