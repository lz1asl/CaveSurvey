package com.astoev.cave.survey.service.imp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProjectData {

    private Map<String, String> options = new HashMap<>();
    private List<LegData> legs = new ArrayList<>();

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> aOptions) {
        options = aOptions;
    }

    public List<LegData> getLegs() {
        return legs;
    }

    public void setLegs(List<LegData> aLegs) {
        legs = aLegs;
    }
}
