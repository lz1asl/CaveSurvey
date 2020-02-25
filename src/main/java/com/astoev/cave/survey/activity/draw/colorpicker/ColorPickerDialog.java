package com.astoev.cave.survey.activity.draw.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.astoev.cave.survey.R;

public class ColorPickerDialog extends Dialog {

    private ColorChangedListener mListener;
    private int mInitialColor;

    public ColorPickerDialog(Context context,
                             ColorChangedListener listener,
                             int initialColor) {
        super(context);

        mListener = listener;
        mInitialColor = initialColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.sketch_pick_color);
        setContentView(R.layout.color_picker);

        ColorPickerView view = (ColorPickerView) findViewById(R.id.colorPicker);
        view.setInitialColor(mInitialColor);
        view.setListener(new ColorChangedListener() {
            public void colorChanged(int color) {
                mListener.colorChanged(color);
                dismiss();
            }
        });

    }
}