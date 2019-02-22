package com.astoev.cave.survey.activity.dialog;

/**
 * Interface that should be implemented from tha activity that will handle the selected options from
 * AddNewDialog dialog
 *
 * @author Jivko Mitrev
 */
public interface AddNewSelectedHandler {

    public enum NewItem {
        LEG, BRANCH, MIDDLE_POINT, TRIANGLE_GALLERY, TRIANGLE;
    }

    void addNewSelected(NewItem itemArg);
}
