function ops_CaveToKML() {
    var str = "";
    str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    str += "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";
    str += "<Document>\n";
    str += "<name>" + caveObj.name + "</name>\n";
    str += "<visibility>1</visibility>\n";
    str += "  <LookAt>\n";
    str += "    <longitude>" + caveObj.longitude + "</longitude>\n";
    str += "    <latitude>" + caveObj.latitude + "</latitude>\n";
    Proj4js.defs["EPSG:27563"] = "+title=LAMB sud france  +proj=lcc +lat_1=44.1 +lat_0=44.1 +lon_0=0 +k_0=0.999877499 +x_0=600000 +y_0=200000 +a=6378249.2 +b=6356515 +towgs84=-168,-60,320,0,0,0,0 +pm=paris +units=m";
    Proj4js.reportError = function(msg) {
        //alert(msg);
    };
    //-----georeferenziazione-------------------------------------------------->
    //georeferenziazione--->
    var geoOffsets = ops_GetGeoOffsets();
    var delta_x = geoOffsets['x'];
    var delta_y = geoOffsets['y'];
    var delta_z = geoOffsets['z'];
    //georeferenziazione---<
    var source = new Proj4js.Proj('EPSG:4326');
    Proj4js.defs["TMP"] = "+proj=utm +zone=" + ops_getZoneFromLonLat(caveObj.longitude, caveObj.latitude) + " +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
    var dest = new Proj4js.Proj('TMP');
    /*
    
    var pIng = new Proj4js.Point(caveObj.longitude, caveObj.latitude, caveObj.altitude);   //any object will do as long as it has 'x' and 'y' properties
    Proj4js.transform(source, dest, pIng);      //do the transformation.  x and y are modified in place    
    var zIng = caveObj.altitude;
    var entrance = 0;    
    var zoom = 1;
    var shapeLayers = new Array();
    for (var i in shape.points)
    {
        if (shape.points[i].layer !== undefined)
        {
            shapeLayers[shape.points[i].layer] = shape.points[i].layer;
        }
        if (shape.points[i].layer === "entrance")
        {
            entrance = i;
        }
    }
    for (var i in shape.points)
    {
        if (shape.points[i].layer === "georeferenced")
        {
            entrance = i;
        }
    }
    var x, y, z, x2, y2, z2;
    var delta_x = parseFloat(pIng.x) - parseFloat(shape.points[entrance].x);
    var delta_y = parseFloat(pIng.y) + parseFloat(shape.points[entrance].y);
    var delta_z = parseFloat(pIng.z) - parseFloat(shape.points[entrance].z);*/
    //-----georeferenziazione--------------------------------------------------<



    var str_coord = "";
    for (var i = 0; shape.points[i] !== undefined && shape.points[i].x !== undefined; i++) {
        if (shape.points[i].layer !== "grid")
        {
            var p = new Point3D();
            p.x = (shape.points[i].x);
            p.y = 0 - (shape.points[i].y);
            p.z = (shape.points[i].z);
            p.text = shape.points[i].text;
            p.type = shape.points[i].type;
            p.color = shape.points[i].color;
            p.layer = shape.points[i].layer;
            if (shape.points[i].x2 !== undefined)
            {
                p.x2 = (shape.points[i].x2);
                p.y2 = 0 - (shape.points[i].y2);
                p.z2 = (shape.points[i].z2);
            }
            x = p.x + delta_x;
            y = p.y + delta_y;
            z = p.z + delta_z;
            var p1 = new Proj4js.Point(x, y, z);
            Proj4js.transform(dest, source, p1);
            if (p.y2 !== undefined) //linea
            {
                x2 = p.x2 + delta_x;
                y2 = p.y2 + delta_y;
                z2 = parseFloat(p.z2) + parseFloat(delta_z);
                var p2 = new Proj4js.Point(x2, y2, z2);
                Proj4js.transform(dest, source, p2);
            }
            switch (p.type)
            {
                case "line":
                    str_coord += "  <Placemark>\n";
                    str_coord += "    <styleUrl>#route</styleUrl>\n";
                    str_coord += "    <name> " + p.text + "</name>\n";
                    str_coord += "    <LineString>\n";
                    str_coord += "    <altitudeMode>absolute</altitudeMode>\n";
                    str_coord += "      <coordinates>\n";
                    str_coord += " " + p1.x + "," + p1.y + "," + z + " " + p2.x + "," + p2.y + "," + z2;
                    str_coord += "      </coordinates>\n";
                    str_coord += "    </LineString>\n";
                    str_coord += "  </Placemark>\n";
                    break;
                case "text":
                    //         dxfObj.addText(x, y, z, p.text, 0.5, p.layer);
                    break;
                default:
                    //         dxfObj.addText(x, y, z, p.text, 0.5, p.layer);
                    break;
            }
        }
    }
    str += "    <range>10000</range>\n";
    str += "  </LookAt>\n";
    str += "<Style id=\"route\">\n";
    str += "  <icon><href>root://icons/palette-4.png?x=160&amp;y=0&amp;w=32&amp;h=32</href></icon>\n";
    str += "  <LineStyle>\n";
    str += "    <color>FF0000FF</color>\n";
    str += "    <width>2.0</width>\n";
    str += "  </LineStyle>\n";
    str += "</Style>\n";
    str += str_coord;
    // coordinate 
    // str += " 9.9493482,44.0690031 9.9492526,44.0689452 9.9492864,44.0688198 9.9492114,44.0687089 9.9490787,44.0686833 9.9490488,44.0686080 9.9487865,44.0685354 9.9486330,44.0684973 9.9485544,44.0684767\n";
    str += "</Document>\n";
    str += "</kml>";
    str += "";

    //  splx.print_r(str);
    return str;
}