package com.astoev.cave.survey.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.dto.ProjectConfig;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;

/**
 * Fragment that holds the components for creating or updating a survey project. The Fragment itself
 * will not save or update any data. It will group the UI components and will provide information
 * for the values but the saving/updating in the model should be done by the parent Activity.
 *
 * Created by Jivko on 15-3-6.
 *
 * @author Jivko Mitrev
 */
public class ProjectFragment extends Fragment {

    private static String PROJECT_KEY = "project";

    private final String[] DISTANCE_UNIT = {Option.UNIT_METERS, Option.UNIT_FEET};
    private final String[] DISTANCE_SENSOR = {Option.CODE_SENSOR_NONE, Option.CODE_SENSOR_BLUETOOTH};
    private final String[] AZIMUTH_UNIT = {Option.UNIT_DEGREES, Option.UNIT_GRADS};
    private final String[] AZIMUTH_SENSOR = {Option.CODE_SENSOR_NONE, Option.CODE_SENSOR_INTERNAL, Option.CODE_SENSOR_BLUETOOTH};
    private final String[] SLOPE_UNIT = {Option.UNIT_DEGREES, Option.UNIT_GRADS};
    private final String[] SLOPE_SENSOR = {Option.CODE_SENSOR_NONE, Option.CODE_SENSOR_BLUETOOTH, Option.CODE_SENSOR_INTERNAL};

    /**
     * ProjectConfig to work with. Could be null for new projects
     */
    private ProjectConfig config;

    /**
     * Instance method for ProjectFragment. Will add as an argument ProjectConfig instance
     *
     * @param projectConfigArg - ProjectConfig instance
     * @return created ProjectFragment
     */
    public static ProjectFragment newInstance(ProjectConfig projectConfigArg){
        ProjectFragment projectFragment = new ProjectFragment();
        Bundle args = new Bundle();
        args.putSerializable(PROJECT_KEY, projectConfigArg);
        projectFragment.setArguments(args);
        return projectFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflaterArg, @Nullable ViewGroup containerArg, @Nullable Bundle savedInstanceStateArg) {

        View view = inflaterArg.inflate(R.layout.project_fragment, containerArg, false);

        // check if there is project configuration to use (Edit mode)
        Bundle args = getArguments();
        if (args != null) {
            config = (ProjectConfig) args.getSerializable(PROJECT_KEY);
        }

        // check if build in orientation sensor is available
        boolean hasBuildInOrientationProcessor = OrientationProcessorFactory.canReadOrientation(getActivity());
        int azimuthOptions = R.array.azimuth_read_type_all;
        int slopeOptions = R.array.slope_read_type_all;

        if (!hasBuildInOrientationProcessor) {
            azimuthOptions = R.array.azimuth_read_type_manually;
            slopeOptions = R.array.slope_read_type;
        }

        // populate spinners

        if (config != null){
            // there is a project config. Working in edit mode
            EditText projectNameField = view.findViewById(R.id.new_projectname);
            projectNameField.setText(config.getName());
            projectNameField.setEnabled(false);
            projectNameField.setInputType(EditorInfo.TYPE_NULL);

            prepareSpinner(view, R.id.options_units_distance, R.array.distance_units, DISTANCE_UNIT, config.getDistanceUnits(), false);
            prepareSpinner(view, R.id.options_distance_type, R.array.distance_read_type, DISTANCE_SENSOR, config.getDistanceSensor());

            prepareSpinner(view, R.id.options_units_azimuth, R.array.azimuth_units, AZIMUTH_UNIT, config.getAzimuthUnits(), false);
            prepareSpinner(view, R.id.options_azimuth_type, azimuthOptions, AZIMUTH_SENSOR, config.getAzimuthSensor());

            prepareSpinner(view, R.id.options_units_slope, R.array.slope_units, SLOPE_UNIT, config.getSlopeUnits(), false);
            prepareSpinner(view, R.id.options_slope_type, slopeOptions, SLOPE_SENSOR, config.getSlopeSensor());

        } else {
            // there is no configuration available working in create mode
            // distance
            prepareSpinner(view, R.id.options_units_distance, R.array.distance_units);
            prepareSpinner(view, R.id.options_distance_type, R.array.distance_read_type);

            // azimuth
            prepareSpinner(view, R.id.options_units_azimuth, R.array.azimuth_units);
            prepareSpinner(view, R.id.options_azimuth_type, azimuthOptions);

            // slope
            prepareSpinner(view, R.id.options_units_slope, R.array.slope_units);
            prepareSpinner(view, R.id.options_slope_type, slopeOptions);
        }
        return view;
    }

    /**
     * Prepare spinner by supplying corresponding Options values and chosen values. This value is
     * used to determine the selected index in the spinner
     *
     * @param viewArg - parent view
     * @param spinnerIdArg - spinner id
     * @param textArrayIdArg - text array id, used to populate the spinner
     * @param valuesArg - Options values, used to determine the index in the spinner
     * @param valueArg - chosen value
     */
    private void prepareSpinner(View viewArg, int spinnerIdArg, int textArrayIdArg,
                                String[] valuesArg, String valueArg) {
        prepareSpinner(viewArg, spinnerIdArg, textArrayIdArg, valuesArg, valueArg, true);
    }

    /**
     * Prepare spinner by supplying corresponding Options values and chosen values. This value is
     * used to determine the selected index in the spinner. Can set if the spinner is editable or
     * view only
     *
     * @param viewArg - parent view
     * @param spinnerIdArg - spinner id
     * @param textArrayIdArg - text array id, used to populate the spinner
     * @param valuesArg - Options values, used to determine the index in the spinner
     * @param valueArg - chosen value
     * @param editableArg - flag if the spinner is editable
     */
    private void prepareSpinner(View viewArg, int spinnerIdArg, int textArrayIdArg, String[] valuesArg,
                                String valueArg, boolean editableArg) {

        Spinner spinner = prepareSpinner(viewArg, spinnerIdArg, textArrayIdArg);

        int arrayId = 0;
        for (int i = 0; i < valuesArg.length; i++){
            if (valuesArg[i].equals(valueArg)){
                arrayId = i;
                break;
            }
        }

        spinner.setSelection(arrayId);
        spinner.setEnabled(editableArg);
    }

    /**
     * Initiates a spinner with array of values
     *
     * @param viewArg - parent view
     * @param spinnerIdArg - spinner id
     * @param textArrayIdArg - text array id, used to populate the spinner
     * @return Spinner
     */
    private Spinner prepareSpinner(View viewArg, int spinnerIdArg, int textArrayIdArg) {
        Spinner spinner = viewArg.findViewById(spinnerIdArg);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), textArrayIdArg, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }

    /**
     * Reads the value of Spinner
     *
     * @param spinnerIdArg - spinner id
     * @return int selected value
     */
    private int getSpinnerValue(int spinnerIdArg) {
        Spinner spinner = getActivity().findViewById(spinnerIdArg);
        return spinner.getSelectedItemPosition();
    }

    /**
     * Helper method that creates a ProjectConfig based on UI elements in the fragment
     *
     * @return ProjectConfig
     */
    public ProjectConfig getProjectConfig(){
        ProjectConfig projectConfig = new ProjectConfig();

        EditText projectNameField = getActivity().findViewById(R.id.new_projectname);
        final String projectName = projectNameField.getText().toString();

        projectConfig.setName(projectName);

        // project units

        // distance
        int distanceUnitSpinner = getSpinnerValue(R.id.options_units_distance);
        String distanceUnits = DISTANCE_UNIT[distanceUnitSpinner];

        int distanceSensorSpinner = getSpinnerValue(R.id.options_distance_type);
        String distanceSensor = DISTANCE_SENSOR[distanceSensorSpinner];

        // azimuth
        int azimuthUnitSpinner = getSpinnerValue(R.id.options_units_azimuth);
        String azimuthUnits = AZIMUTH_UNIT[azimuthUnitSpinner];

        int azimuthSensorSpinner = getSpinnerValue(R.id.options_azimuth_type);
        String azimuthSensor = AZIMUTH_SENSOR[azimuthSensorSpinner];

        // slope
        int slopeUnitSpinner = getSpinnerValue(R.id.options_units_slope);
        String slopeUnits = SLOPE_UNIT[slopeUnitSpinner];

        int slopeSensorSpinner = getSpinnerValue(R.id.options_slope_type);
        String slopeSensor = SLOPE_SENSOR[slopeSensorSpinner];

        projectConfig.setAzimuthSensor(azimuthSensor);
        projectConfig.setAzimuthUnits(azimuthUnits);
        projectConfig.setDistanceSensor(distanceSensor);
        projectConfig.setDistanceUnits(distanceUnits);
        projectConfig.setSlopeSensor(slopeSensor);
        projectConfig.setSlopeUnits(slopeUnits);

        return projectConfig;
    }
}
