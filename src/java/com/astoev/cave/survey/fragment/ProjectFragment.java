package com.astoev.cave.survey.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.dto.ProjectConfig;
import com.astoev.cave.survey.model.Option;
import com.astoev.cave.survey.service.orientation.OrientationProcessorFactory;

/**
 * Fragment that holds the components for creating or updating a survey project. The Fragment itself
 * will not save or update any data. It will group the UI components and will provide information
 * for the values but the saving/updating in the model should be done by the parent Activity.
 *
 * Created by jivko on 15-3-6.
 *
 * @author Jivko Mitrev
 */
public class ProjectFragment extends Fragment {

    private final String[] DISTANCE_UNIT = {Option.UNIT_METERS};
    private final String[] DISTANCE_SENSOR = {Option.CODE_SENSOR_NONE, Option.CODE_SENSOR_BLUETOOTH};
    private final String[] AZIMUTH_UNIT = {Option.UNIT_DEGREES, Option.UNIT_GRADS};
    private final String[] AZIMUTH_SENSOR = {Option.CODE_SENSOR_NONE, Option.CODE_SENSOR_INTERNAL, Option.CODE_SENSOR_BLUETOOTH};
    private final String[] SLOPE_UNIT = {Option.UNIT_DEGREES, Option.UNIT_GRADS};
    private final String[] SLOPE_SENSOR = {Option.CODE_SENSOR_NONE, Option.CODE_SENSOR_BLUETOOTH, Option.CODE_SENSOR_INTERNAL};

    @Override
    public View onCreateView(LayoutInflater inflaterArg, @Nullable ViewGroup containerArg, @Nullable Bundle savedInstanceStateArg) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflaterArg.inflate(R.layout.project_fragment, containerArg, false);
        // populate spinners

        // distance
        prepareSpinner(view, R.id.options_units_distance, R.array.distance_units);
        prepareSpinner(view,R.id.options_distance_type, R.array.distance_read_type);

        // azimuth
        prepareSpinner(view,R.id.options_units_azimuth, R.array.azimuth_units);

        // check if build in orientation sensor is available
        boolean hasBuildInOrientationProcessor = OrientationProcessorFactory.canReadOrientation(getActivity());
        int azimuthOptions = R.array.azimuth_read_type_all;
        int slopeOptions = R.array.slope_read_type_all;

        if (!hasBuildInOrientationProcessor){
            azimuthOptions = R.array.azimuth_read_type_manually;
            slopeOptions = R.array.slope_read_type;
        }
        prepareSpinner(view,R.id.options_azimuth_type, azimuthOptions);

        // slope
        prepareSpinner(view,R.id.options_units_slope, R.array.slope_units);
        prepareSpinner(view,R.id.options_slope_type, slopeOptions);

        return view;
    }

    private void prepareSpinner(View view, int aSpinnerId, int aTextArrayId) {
        Spinner spinner = (Spinner) view.findViewById(aSpinnerId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), aTextArrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private int getSpinnerValue(int aSpinnerId) {
        Spinner spinner = (Spinner) getActivity().findViewById(aSpinnerId);
        return spinner.getSelectedItemPosition();
    }

    public ProjectConfig getProjectConfig(){
        ProjectConfig projectConfig = new ProjectConfig();

        EditText projectNameField = (EditText) getActivity().findViewById(R.id.new_projectname);
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
