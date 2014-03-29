package com.astoev.cave.survey.activity.map.opengl;

//import android.opengl.GLSurfaceView;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.MotionEvent;

import android.view.View;

import com.astoev.cave.survey.activity.BaseActivity;

//import android.widget.ZoomControls;
//import com.astoev.cave.survey.Constants;
//import com.astoev.cave.survey.R;
//import com.astoev.cave.survey.activity.UIUtilities;

/**
 * Created with IntelliJ IDEA.
 * User: astoev
 * Date: 2/23/13
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 *
 * @see //www.jayway.com/2009/12/03/opengl-es-tutorial-for-android-part-i/
 */
public abstract class Map3DActivity extends BaseActivity implements View.OnTouchListener {

  /*  public void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.map3d);

            final OpenGLRenderer renderer = new OpenGLRenderer();
            GLSurfaceView view = (GLSurfaceView) findViewById(R.id.map3dSurface);
            view.setRenderer(renderer);


            final ZoomControls zoom = (ZoomControls) findViewById(R.id.map3DZoom);

            zoom.setOnZoomInClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View aView) {
                    renderer.zoomIn();
//                zoom.setIsZoomOutEnabled(map.canZoomOut());
//                zoom.setIsZoomInEnabled(map.canZoomIn());
                }
            });
            zoom.setOnZoomOutClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View aView) {
                    renderer.zoomOut();
//                zoom.setIsZoomOutEnabled(map.canZoomOut());
//                zoom.setIsZoomInEnabled(map.canZoomIn());
                }
            });
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to render 3d", e);
            UIUtilities.showNotification(R.string.error);
        }
    }

    @Override
    public boolean onTouch(View aView, MotionEvent aMotionEvent) {

        if (aMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            // TODO end move
        } else if (aMotionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            // TODO perform translation
        }

        return true;
    }*/

}