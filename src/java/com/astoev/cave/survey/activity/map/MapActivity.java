package com.astoev.cave.survey.activity.map;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ZoomControls;

import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.MainMenuActivity;
import com.astoev.cave.survey.activity.draw.DrawingActivity;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/23/12
 * Time: 3:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapActivity extends MainMenuActivity implements View.OnTouchListener {

    private MapView map;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map);
        map = (MapView) findViewById(R.id.mapSurface);
        map.setOnTouchListener(this);

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
            map.resetMove(aMotionEvent.getX(), aMotionEvent.getY());
        } else if (aMotionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            map.move(aMotionEvent.getX(), aMotionEvent.getY());
        }

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        map = (MapView) findViewById(R.id.mapSurface);
        map.setOnTouchListener(this);
    }

    public void annotateMap(View aView) {
        Intent intent = new Intent(this, DrawingActivity.class);
        intent.putExtra(DrawingActivity.SKETCH_BASE, map.getPngDump());
        intent.putExtra(DrawingActivity.MAP_FLAG, true);
        startActivity(intent);
    }
}