package com.astoev.cave.survey.service.export.svg;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.map.MapUtilities;

import org.xmlpull.v1.XmlSerializer;

/**
 * Created by astoev on 3/8/16.
 */
public class SVGCanvas extends Canvas {

    private XmlSerializer svg;

    public SVGCanvas(XmlSerializer aSerializer) {
        svg = aSerializer;
    }

    @Override
    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {

        try {
            svg.startTag("", "line");
            svg.attribute("", "x1", String.valueOf(startX));
            svg.attribute("", "x2", String.valueOf(stopX));
            svg.attribute("", "y1", String.valueOf(startY));
            svg.attribute("", "y2", String.valueOf(stopX));

            StringBuilder style = new StringBuilder();
            style.append("stroke:").append(MapUtilities.intToColor(paint.getColor())).append(";stroke-width:").append((int) paint.getStrokeWidth());
            svg.attribute("", "style", style.toString());

            svg.endTag("", "line");

        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "SVG export failed", e);
        }
    }

    @Override
    public void drawText(String text, float x, float y, Paint paint) {
        try {
            svg.startTag("", "text");
            svg.attribute("", "x", String.valueOf(x));
            svg.attribute("", "y", String.valueOf(y));
            svg.attribute("", "fill", MapUtilities.intToColor(paint.getColor()));
            svg.text(text);
            svg.endTag("", "text");
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "SVG export failed", e);
        }
    }

    @Override
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        try {
            svg.startTag("", "circle");
            svg.attribute("", "cx", String.valueOf(cx));
            svg.attribute("", "cy", String.valueOf(cy));
            svg.attribute("", "r", String.valueOf((int) radius));
            svg.attribute("", "stroke", MapUtilities.intToColor(paint.getColor()));

            svg.endTag("", "circle");
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG_SERVICE, "SVG export failed", e);
        }
    }
}
