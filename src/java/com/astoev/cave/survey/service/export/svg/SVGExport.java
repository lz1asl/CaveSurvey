package com.astoev.cave.survey.service.export.svg;

import android.util.Log;
import android.util.Xml;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.activity.map.MapUtilities;
import com.astoev.cave.survey.activity.map.MapView;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.model.SketchElement;
import com.astoev.cave.survey.model.SketchPoint;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.util.ConfigUtil;

import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by astoev on 3/7/16.
 */
public class SVGExport {

    public static InputStream mapDrawingToSVG(Sketch aSketch, boolean isHorizontal) throws SQLException, IOException {

        Log.i(Constants.LOG_TAG_SERVICE, "Exporting sketch: " + aSketch.getId());

        // prepare xml serialization
        XmlSerializer svg = Xml.newSerializer();
        StringWriter buff = new StringWriter();
        svg.setOutput(buff);
        svg.startDocument("UTF-8", true);

        // headers
        svg.startTag("", "svg");
        svg.attribute("", "xmlns", "http://www.w3.org/2000/svg");
        svg.attribute("", "version", "1.1");

        Collection<SketchElement> elements = Workspace.getCurrentInstance().getDBHelper().getSketchElementDao().queryForEq(SketchElement.COLUMN_SKETCH_ID, aSketch.getId());
        if (elements != null) {

            // define dimensions
            float maxX = 0;
            float maxY = 0;

            for (SketchElement e : elements) {
                StringBuilder points = new StringBuilder();
                for (SketchPoint p : e.getPoints()) {
                    maxX = Math.max(maxX, p.getX());
                    maxY = Math.max(maxY, p.getY());
                }
            }

            svg.attribute("", "width", String.valueOf(Math.ceil(maxX)));
            svg.attribute("", "height", String.valueOf(Math.ceil(maxY)));

            // actual elements
            svg.startTag("", "g");
            svg.attribute("", "id", "drawings");
            for (SketchElement e : elements) {

                StringBuilder points = new StringBuilder();
                for (SketchPoint p: e.getPoints()) {
                    points.append(p.getX());
                    points.append(",");
                    points.append(p.getY());
                    points.append(" ");

                }

                StringBuilder style = new StringBuilder();

                switch (e.getType()) {

                    case THICK:

                        svg.startTag("", "polyline");
                        svg.attribute("", "points", points.toString());
                        style.append("fill:none;stroke:").append(MapUtilities.intToColor(e.getColor()))
                                .append(";stroke-width:").append(e.getSize())
                                .append(";stroke-linejoin:round")
                                .append(";stroke-linecap:round");
                        svg.attribute("", "style", style.toString());
                        svg.endTag("", "polyline");
                        break;

                    case FILL:

                        svg.startTag("", "polygon");
                        svg.attribute("", "points", points.toString());
                        style.append("fill:").append(MapUtilities.intToColor(e.getColor()))
                                .append(";stroke-linejoin:bevel");
                        svg.attribute("", "style", style.toString());
                        svg.endTag("", "polygon");
                        break;


                    case DASH:

                        svg.startTag("", "polyline");
                        svg.attribute("", "points", points.toString());
                        style.append("fill:none;stroke:").append(MapUtilities.intToColor(e.getColor()))
                                .append(";stroke-width:").append(e.getSize())
                                .append(";stroke-dasharray:").append(2 * e.getSize()).append(",").append( 4 * e.getSize());
                        svg.attribute("", "style", style.toString());
                        svg.endTag("", "polyline");
                        break;

                    default: throw new RuntimeException("Type not supported" + e.getType());
                }

            }
            svg.endTag("", "g");

            // the actual line - will use the same code for drawing but will intercept the actual content into svg
            svg.startTag("", "g");
            svg.attribute("", "id", "line");
            MapView map = new MapView(ConfigUtil.getContext(), null);
            map.setHorizontalPlan(isHorizontal);
            map.setAnnotateMap(false);
            map.scale(20);
            map.move(200, 200);
            SVGCanvas svgCanvas = new SVGCanvas(svg);
            map.draw(svgCanvas);
            svg.endTag("", "g");
        }

        svg.endTag("", "svg");
        svg.endDocument();

        return new ByteArrayInputStream(buff.toString().getBytes());
    }
}
