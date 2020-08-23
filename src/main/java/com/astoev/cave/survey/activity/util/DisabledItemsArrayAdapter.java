package com.astoev.cave.survey.activity.util;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * ArrayAdapter that allows items to be disabled.
 * https://stackoverflow.com/questions/2662358/how-to-disable-items-in-a-list-view/2662775
 */
public class DisabledItemsArrayAdapter extends ArrayAdapter {

    private Set<Integer> disabledItems = new HashSet<>();


    public DisabledItemsArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public DisabledItemsArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public DisabledItemsArrayAdapter(@NonNull Context context, int resource, @NonNull Object[] objects) {
        super(context, resource, objects);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void disableItem(int aPosition) {
        disabledItems.add(aPosition);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return disabledItems.isEmpty();
    }

    @Override
    public boolean isEnabled(int position) {
        return !disabledItems.contains(position);
    }
}
