package com.astoev.cave.survey.activity.map;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ZoomControls;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/23/12
 * Time: 3:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapActivity extends MainMenuActivity implements View.OnTouchListener {

    MapView map;

    public void onCreate(Bundle savedInstanceState) {
    	// do not need the action bar to save some space
        super.onCreate(savedInstanceState, false);
        
        setContentView(R.layout.map);
        map = (MapView) findViewById(R.id.mapSurface);
        map.setOnTouchListener(this);
        map.setWindow(getWindow());

        final ZoomControls zoom = (ZoomControls) findViewById(R.id.mapZoom);
        zoom.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {
                map.zoomIn();
                zoom.setIsZoomOutEnabled(map.canZoomOut());
                zoom.setIsZoomInEnabled(map.canZoomIn());
            }
        });
        zoom.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {
                map.zoomOut();
                zoom.setIsZoomOutEnabled(map.canZoomOut());
                zoom.setIsZoomInEnabled(map.canZoomIn());
            }
        });


    }

    @Override
    public boolean onTouch(View aView, MotionEvent aMotionEvent) {

        if (aMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//            Log.i(Constants.LOG_TAG_UI, "reset initial ");
            map.resetMove(aMotionEvent.getX(), aMotionEvent.getY());
        } else if (aMotionEvent.getAction() == MotionEvent.ACTION_MOVE) {
//            Log.i(Constants.LOG_TAG_UI, "Move on x " + (aMotionEvent.getX())) ;
            map.move(aMotionEvent.getX(), aMotionEvent.getY());
        }

        return true;
    }


}