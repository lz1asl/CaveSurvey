/**
 * @author Alessandro Vernassa <speleoalex@gmail.com>
 * @copyright Copyright (c) 2013
 * @license http://opensource.org/licenses/gpl-license.php GNU General Public License
 */



function ops_CalcCoords(lon_x, lat_y, zone)
{
    var lat;
    var lon;
    var x;
    var y;
    var zone;
    if (zone === "")
    {
        Proj4js.defs["TMP"] = "+proj=utm +zone=" + ops_getZoneFromLonLat(lon_x, lat_y) + " +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
    }
    else {
        Proj4js.defs["TMP"] = "+proj=utm +zone=" + zone + " +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
    }

    var utm = new Proj4js.Proj('TMP');
    var geo = new Proj4js.Proj('EPSG:4326');
    if (zone !== "")
    {
        x = parseFloat(lon_x);
        y = parseFloat(lat_y);
        var source_c = new Proj4js.Point(x, y, 0);
        try {
            var out = Proj4js.transform(utm, geo, source_c);      //do the transformation.  x and y are modified in place    
            lat = out.y;
            lon = out.x;
        } catch (e) {
            console.log(e);
        }
    }
    else
    {
        lon = parseFloat(lon_x);
        lat = parseFloat(lat_y);
        var source_c = new Proj4js.Point(lon, lat, 0);
        try {
            var out = Proj4js.transform(geo, utm, source_c);      //do the transformation.  x and y are modified in place    
            y = out.y;
            x = out.x;
            zone = ops_getZoneFromLonLat(lon, lat);
        } catch (e) {
            console.log(e);
        }
    }
    $("#cave_longitude").val(Math.round(lon * 10000000) / 10000000);
    $("#cave_latitude").val(Math.round(lat * 10000000) / 10000000);
    $("#cave_utm_x").val(Math.round(x * 100) / 100);
    $("#cave_utm_y").val(Math.round(y * 100) / 100);
    $("#cave_utm_zone").val(zone);
}
/**
 * 
 * @param {type} lon
 * @param {type} lat
 * @returns {Number}
 */
function ops_getZoneFromLonLat(lon, lat) {
    var long = parseFloat(lon);
    var LongTemp = (long + 180) - parseInt((long + 180) / 360) * 360 - 180;
    var ZoneNumber = parseInt((LongTemp + 180) / 6) + 1;
    // Special zone for South Norway
    if (lat >= 56.0 && lat < 64.0 && LongTemp >= 3.0 && LongTemp < 12.0)
        ZoneNumber = 32;
    // Special zones for Svalbard
    if (lat >= 72.0 && lat < 84.0)
    {
        if (LongTemp >= 0.0 && LongTemp < 9.0)
            ZoneNumber = 31;
        else if (LongTemp >= 9.0 && LongTemp < 21.0)
            ZoneNumber = 33;
        else if (LongTemp >= 21.0 && LongTemp < 33.0)
            ZoneNumber = 35;
        else if (LongTemp >= 33.0 && LongTemp < 42.0)
            ZoneNumber = 37;
    }
    return ZoneNumber;
}
/**
 * salva l'intero progetto
 * @returns {undefined}
 */
function ops_SaveAs()
{
    for (var caveObjId in caveObj)
    {
        if (caveObjId !== "data" && document.getElementById("cave_" + caveObjId) !== undefined)
        {
            eval("caveObj." + caveObjId + "=" + "$('#cave_' + caveObjId).val();");
        }
    }
    caveObj.data = container.handsontable("getData");
    //console.log(caveObj.data);
    var str = JSON.stringify(caveObj);
    //alert(str);
    str = str.replace(/\}\,\{/gi, "},\r\n{");
    str = str.replace(":[{", ":[\r\n{");
    str = str.replace('}],"name"', '}\r\n],\r\n"name"');
    //alert(str);
    var filename = $("#filenameopened").val();
    filename = trim_extension(filename);
    filename = filename + ".ops.txt";
    Download.save(str, filename);
}


/**
 * salva l'intero progetto
 * @returns {undefined}
 */
function ops_SaveToCSV()
{
    var strCSV = _i18n("from", "Aa") + "\t" + _i18n("to", "Aa") + "\t" + _i18n("length", "Aa") + "\t" + _i18n("compass", "Aa") + "\t" + _i18n("clino", "Aa") + "\t" +
            _i18n("left", "Aa") + "\t" +
            _i18n("right", "Aa") + "\t" +
            _i18n("top", "Aa") + "\t" +
            _i18n("bottom", "Aa") + "\t" +
            _i18n("i", "Aa") + "\t" +
            _i18n("note", "Aa");
    strCSV += "\n";

    var data = $('#cells').handsontable('getData');
    var cols = {from: "from", to: "to", len: "len", compass: "compass", clino: "clino", left: "left", right: "right", top: "top", bottom: "bottom", r: "r", note: "note"};
    for (var caveObjId in data)
    {
        var row = data[caveObjId];
        var cells = new Array();
        var sep = "";
        for (var i in cols)
        {
            try {
                cells[i] = row[i].toString();
            } catch (e) {

                cells[i] = "";
            }
            strCSV += sep + '"' + csvencode(cells[i]) + '"';
            sep = "\t";
        }
//        console.log(cells);
        strCSV += "\n";
    }
    var filename = $("#filenameopened").val();
    filename = trim_extension(filename);
    filename = filename + ".ops.csv";
    Download.save(strCSV, filename);
}

function csvencode(str)
{
    return str.replace(/"/g, '""');
}



/**
 * 
 * @param {type} str
 * @returns {undefined}
 */
function trim_extension(str)
{
    var tmp = str.split(".");
    return tmp[0];
}
/**
 * esporta la poligonale in formato Autodesk DXF mantenendo il 3d
 * @param {type} shape
 * @returns {undefined}
 */
function ops_SaveToDXF(shape) {
    var str = ops_ShapeToDXFGEO3d(shape, 0, 0, 0);
    var filename = $("#filenameopened").val() + "_3d.dxf";
    Download.save(str, filename);
}


/**
 * esporta pianta e sezione in un unico file  in formato Autodesk DXF
 * @returns {undefined}
 */
function ops_SaveToDXFAll() {

    var shapeAll = new Shape3D(); //shape che contiene tutto
    shapeAll.color = "rgb(100,0,255)";
    var xRotation = DegToRad(90);
    var yRotation = DegToRad(-90);
    var zRotation = DegToRad(0);

    var x_min = 0.0;
    var y_min = 0.0;
    var x_max = 0.0;
    var y_max = 0.0;
    var x_max_plan = 0.0;
    var y_max_plan = 0.0;

    var plan_offset_y = 0.0;
    var plan_offset_x = 0.0;
    var section_offset_y = 0.0;
    var section_offset_x = 0.0;
    var tmpPoint;
    //------------------------------pianta min e max -------------------------->
    try {
        x_max = x_min = shape.points[0].x;
        y_max = y_min = shape.points[0].y;
    } catch (e) {
        console.log(e);
    }
    for (var i in shape.points)
    {
        if (shape.points[i].layer === "grid")
            continue;
        //minimo 
        y_min = Math.min(y_min, shape.points[i].y);
        if (shape.points[i].y2 !== undefined)
            y_min = Math.min(y_min, shape.points[i].y2);
        if (shape.points[i].x2 !== undefined)
            x_min = Math.min(x_min, shape.points[i].x2);
        //massimo 
        y_max = Math.max(y_max, shape.points[i].y);
        if (shape.points[i].y2 !== undefined)
            y_max = Math.max(y_max, shape.points[i].y2);
        if (shape.points[i].x2 !== undefined)
            x_max = Math.max(x_max, shape.points[i].x2);
    }
    plan_offset_y = 0 - y_min;
    plan_offset_x = 0 - x_min;
    x_max_plan = x_max + plan_offset_x;
    y_max_plan = y_max + plan_offset_y;
    //------------------------------pianta min e max --------------------------<
    //------------------------------pianta------------------------------------->
    for (var i in shape.points)
    {
        if (shape.points[i].layer === "grid")
            continue;
        shape.points[i].y += plan_offset_y;
        shape.points[i].z = 0;
        shape.points[i].z2 = 0;

        if (shape.points[i].y2 !== undefined)
            shape.points[i].y2 += plan_offset_y;
        shape.points[i].x += plan_offset_x;
        if (shape.points[i].x2 !== undefined)
            shape.points[i].x2 += plan_offset_x;
        shapeAll.points.push(shape.points[i]);
    }
    //------------------------------pianta-------------------------------------<
    //--------------------sezione min e max ----------------------------------->
    try {
        x_max = x_min = shapeSection.points[0].x;
        y_max = y_min = shapeSection.points[0].y;
    } catch (e) {
        console.log(e);
    }
    for (var i in shapeSection.points)
    {
        if (shape.points[i].layer === "grid")
            continue;
        //minimo 
        y_min = Math.min(y_min, shapeSection.points[i].y);
        if (shapeSection.points[i].y2 !== undefined)
            y_min = Math.min(y_min, shapeSection.points[i].y2);
        if (shapeSection.points[i].x2 !== undefined)
            x_min = Math.min(x_min, shapeSection.points[i].x2);
        //massimo 
        y_max = Math.max(y_max, shapeSection.points[i].y);
        if (shapeSection.points[i].y2 !== undefined)
            y_max = Math.max(y_max, shapeSection.points[i].y2);
        if (shapeSection.points[i].x2 !== undefined)
            x_max = Math.max(x_max, shapeSection.points[i].x2);
    }


    section_offset_y = 0 - y_min;
    section_offset_x = 0 - x_min;
    var x_max_section = x_max + section_offset_x;
    var y_max_section = y_max + section_offset_y;

    section_offset_y = section_offset_y + y_max_section;
    //--------------------sezione min e max -----------------------------------<
    //--------------------sezione---------------------------------------------->
    for (var i in shapeSection.points)
    {
        if (shapeSection.points[i].layer === "grid")
            continue;
        tmpPoint = shapeSection.points[i];
        rotateX(tmpPoint, xRotation);
        rotateY(tmpPoint, yRotation);
        rotateZ(tmpPoint, zRotation);
        tmpPoint.y += section_offset_y;
        if (tmpPoint.y2 !== undefined)
            tmpPoint.y2 += section_offset_y;
        tmpPoint.x += section_offset_x;
        if (tmpPoint.x2 !== undefined)
            tmpPoint.x2 += section_offset_x;
        shapeAll.points.push(tmpPoint);
    }
    //--------------------sezione----------------------------------------------<
//console.log(y_min);
    var point = new Point3D();
    point.text = _i18n("plan", "Aa");
    point.type = "text";
    point.x = Math.floor(x_min);
    point.y = -5;
    point.layer = "legenda";
    point.textsize = 2.0;
    shapeAll.points.push(point);
    var point = new Point3D();
    point.text = _i18n("section", "Aa");
    point.type = "text";
    point.x = Math.floor(x_min);
    point.y = section_offset_y - 5;
    point.layer = "legenda";
    point.textsize = 2.0;
    shapeAll.points.push(point);



    var str = ops_ShapeToDXF(shapeAll);
    var filename = $("#filenameopened").val() + "_" + _i18n("plan") + "_" + _i18n("section") + ".dxf";
    Download.save(str, filename);
}


/**
 * 
 * @returns {undefined}
 */
function ops_SaveToSVGAll() {
    var shapeAll = new Shape3D();
    shapeAll.color = "rgb(255,0,0)";
    var xRotation = DegToRad(90);
    var yRotation = DegToRad(-90);
    var zRotation = DegToRad(0);
    var x_min = 0.0;
    var y_min = 0.0;
    var x_max_plan;
    var y_max_plan;
    var x_max = 0.0;
    var y_max = 0.0;
    var plan_offset_y;
    var plan_offset_x;

    var offset_y = 0.0;
    var colors = new Array();
    colors['lateral'] = "rgb(200,200,200)";
    colors['stations'] = "rgb(200,0,200)";
    colors['points'] = "rgb(80,80,80)";
    colors['vector'] = "rgb(255,0,0)";
    var tmpPoint;
    //------------------------------pianta min e max -------------------------->
    try {
        x_max = x_min = shape.points[0].x;
        y_max = y_min = shape.points[0].y;
    } catch (e) {
        console.log(e);
    }
    for (var i in shape.points)
    {
        if (shape.points[i].layer === "grid")
            continue;
        //minimo 
        y_min = Math.min(y_min, shape.points[i].y);
        if (shape.points[i].y2 !== undefined)
            y_min = Math.min(y_min, shape.points[i].y2);
        if (shape.points[i].x2 !== undefined)
            x_min = Math.min(x_min, shape.points[i].x2);
        //massimo 
        y_max = Math.max(y_max, shape.points[i].y);
        if (shape.points[i].y2 !== undefined)
            y_max = Math.max(y_max, shape.points[i].y2);
        if (shape.points[i].x2 !== undefined)
            x_max = Math.max(x_max, shape.points[i].x2);
    }
    plan_offset_y = 0 - y_min;
    plan_offset_x = 0 - x_min;
    x_max_plan = x_max + plan_offset_x;
    y_max_plan = y_max + plan_offset_y;
    //------------------------------pianta min e max --------------------------<

    for (var i in shape.points)
    {
        tmpPoint = shape.points[i];
        //console.log (tmpPoint.layer);
        if (tmpPoint.layer === "grid")
            continue;
        try {
            tmpPoint.color = colors[tmpPoint.layer];
        } catch (e) {
            tmpPoint.color = "rgb(0,0,0)";
        }
        tmpPoint.y += plan_offset_y;
        if (tmpPoint.y2 !== undefined)
            tmpPoint.y2 += plan_offset_y;
        shapeAll.points.push(tmpPoint);

    }

    //aggiunge l'offset per non sovrapporre pianta e sezione  ----------------->
    offset_y = caveObj.depht + 40;
    //aggiunge l'offset per non sovrapporre pianta e sezione  -----------------<
    //sezione
    for (var i in shapeSection.points)
    {
        //console.log (tmpPoint);
        tmpPoint = shapeSection.points[i];
        if (tmpPoint.layer === "grid")
            continue;
        try {
            tmpPoint.color = colors[tmpPoint.layer];
        } catch (e) {
            tmpPoint.color = "rgb(0,0,0)";
        }
        rotateX(tmpPoint, xRotation);
        rotateY(tmpPoint, yRotation);
        rotateZ(tmpPoint, zRotation);
        tmpPoint.y -= offset_y;
        if (tmpPoint.y2 !== undefined)
            tmpPoint.y2 -= offset_y;
        shapeAll.points.push(tmpPoint);
        //minimo 
        y_min = Math.min(y_min, tmpPoint.y);
        if (tmpPoint.y2 !== undefined)
            y_min = Math.min(y_min, tmpPoint.y2);
    }
    var point = new Point3D();
    point.text = _i18n("section", "Aa");
    point.type = "text";
    point.x = Math.floor(x_min);
    point.y = 1 - Math.floor(y_min);
    point.layer = "legenda";
    point.textsize = 2.0;
    shapeAll.points.push(point);
    var point = new Point3D();
    point.text = _i18n("plan", "Aa");
    point.type = "text";
    point.x = Math.floor(x_min);
    point.y = y_min;
    point.layer = "legenda";
    point.textsize = 2.0;
    shapeAll.points.push(point);

    var str = ops_ShapeToSVG(shapeAll, 180, 0, 0);
    var filename = $("#filenameopened").val() + "_" + _i18n("plan") + "_" + _i18n("section") + ".svg";
    Download.save(str, filename);
    ops_compile();

}

/**
 * Salva la pianta in formato Autodesk DXF
 * @param {type} shape
 * @returns {undefined}
 */
function ops_SaveToDXFPlan(shape) {
    var str = ops_ShapeToDXFPlan(shape, 0, 0, 0);
    var filename = $("#filenameopened").val() + "_" + _i18n("plan") + ".dxf";
    Download.save(str, filename);
}

/**
 * Salva la sezione in formato Autodesk DXF
 * @param {type} shape
 * @returns {undefined}
 */
function ops_SaveToDXFSection(shape) {

    var shapeAll = new Shape3D();
    shapeAll.color = "rgb(100,0,255)";
    var xRotation = DegToRad(90);
    var yRotation = DegToRad(-90);
    var zRotation = DegToRad(0);
    var x_min = 0.0;
    var y_min = 0.0;
    var tmpPoint;
    try {
        x_min = shapeSection.points[0].x;
        y_min = shapeSection.points[0].y;
    } catch (e) {
        console.log(e);
    }
    //sezione
    for (var i in shapeSection.points)
    {
        tmpPoint = shapeSection.points[i];
        rotateX(tmpPoint, xRotation);
        rotateY(tmpPoint, yRotation);
        rotateZ(tmpPoint, zRotation);
        shapeAll.points.push(tmpPoint);
        //minimo 
        y_min = Math.min(y_min, tmpPoint.y);
        if (tmpPoint.y2 !== undefined)
            y_min = Math.min(y_min, tmpPoint.y2);
    }
    var point = new Point3D();
    point.text = _i18n("section", "Aa");
    point.type = "text";
    point.x = Math.floor(x_min);
    point.y = Math.floor(y_min) - 10;
    point.layer = "legenda";
    point.textsize = 2.0;
    shapeAll.points.push(point);
    var str = ops_ShapeToDXF(shapeAll);
    var filename = $("#filenameopened").val() + "_" + _i18n("section") + ".dxf";
    Download.save(str, filename);
}

/**
 * Salvataggio in kml
 * @returns {undefined}
 */
function ops_SaveToKML()
{
    var str = ops_CaveToKML();
    var filename = $("#filenameopened").val() + ".kml";
    Download.save(str, filename);
}

/**
 * Crea nuovo progetto
 * @returns {undefined}
 */
function ops_NewFile()
{

    if (documentChanged === false)
    {
        caveObj = new CaveObject();
        ops_CaveObjToForm();
        $('#cells').handsontable("loadData", caveObj.data);
    }
    else
    {
        jConfirm(_i18n("Want to create a new project? Unsaved data will be lost"), "", function(r) {
            if (r) {
                $("#filenameopened").val("new_project");
                caveObj = new CaveObject();
                ops_CaveObjToForm();
                $('#cells').handsontable("loadData", caveObj.data);
            }
        });
    }
}

/**
 * 
 * @param {type} evt
 * @returns {unresolved}
 */
function handleOpenFileSelect(evt) {
    var files = evt.target.files; // FileList object
    if (!files.length) {
        return;
    }
    $("#filenameopened").val(files[0].name);
    var reader = new FileReader();
    reader.onload = function(event)
    {
//import the file
        ops_import(escape(files[0].name), event.target.result, false);
    };
    reader.readAsText(files[0], "UTF-8");
}

/**
 * 
 * @param {type} evt
 * @returns {unresolved}
 */
function handleOpenFileAddSelect(evt) {
    var files = evt.target.files; // FileList object
    if (!files.length) {
        return;
    }
    var reader = new FileReader();
    reader.onload = function(event)
    {
        ops_import(escape(files[0].name), event.target.result, true);
    };
    reader.readAsText(files[0], "UTF-8");
}

/**
 * 
 * @param {type} filename
 * @param {type} str
 * @returns {ops_import.str}
 */
function ops_import(filename, str, append)
{
    var tmpcaveObj = new CaveObject();
    if (str.indexOf("{\"") === 0)
    {
        tmpcaveObj = jQuery.parseJSON(str.replace(/lenght/g, "len"));
    }
    else
    if (filename === "thconfig")
    {
        tmpcaveObj = ops_GetCaveObjectByThconfig(str);
    }
    else
    if (filename.search(/\.th$/) !== -1)
    {
        tmpcaveObj = ops_GetCaveObjectByTherion(str);
    }
    else
    if (filename.search(/\.tro$/) !== -1)
    {
        tmpcaveObj = ops_GetCaveObjectByVisualtopo(str);
    }
    else
    {
        alert(_i18n("I can not import this file", "auto"));
        tmpcaveObj = new CaveObject();
        return;
    }
    if (tmpcaveObj.northdeclination === undefined)
        tmpcaveObj.northdeclination = "-";
    if (append === false)
    {
        caveObj = tmpcaveObj;
        ops_CaveObjToForm();
        $('#cells').handsontable("loadData", caveObj.data);
    }
    else
    {
        //alert("accoda");
        var name = "";
        var prefix = filename.split(".")[0] + "_";
        $.alerts['cancelButton'] = _i18n("no", "Aa");
        $.alerts['okButton'] = _i18n("yes", "Aa");
        jPrompt(_i18n("prefix") + ":", prefix, _i18n("would like to add a prefix to all the points?"), function(r) {
            if (r)
            {
                prefix = r;
            }
            else
            {
                prefix = "";
            }
            var row;
            row = new Object();
            caveObj.data = $('#cells').handsontable("getData");
            caveObj.data.push({"from": null, "to": null, "len": "m", "compass": "deg", "clino": "deg", "top": "m", "left": "m", "right": "m", "bottom": "m", "r": null, "note": filename});
            row.from = "";
            row.to = "";
            row.len = 0;
            row.compass = 0;
            row.clino = 0;
            row.note = _i18n("virtual point of joining");
            caveObj.data.push(row);
            for (var i in tmpcaveObj.data)
            {
                row = new Array();
                row = tmpcaveObj.data[i];
                if (row['from'] !== "" && row['from'] !== "-")
                {
                    row['from'] = prefix + row['from'];
                }
                if (row['to'] !== "" && row['to'] !== "-" && row['to'] !== undefined && row['to'] !== null)
                {
                    row['to'] = prefix + row['to'];
                }
                //console.log(row);
                caveObj.data.push(row);
            }
            $('#cells').handsontable("loadData", caveObj.data);
        });
    }

     ops_compile();
}
function ops_IsEmptyNonZero(v)
{
    if (v === 0)
        return false;
    if (v === "0")
        return false;
    if (v === undefined || v === "" || v === null)
        return true;
    return false;
}

function ops_IsEmpty(v)
{
    if (v === undefined || v === "" || v === 0 || v === "0" || v === 0.0)
        return true;
    return false;
}
/**
 * 
 * @param {type} VectorsPoints
 * @param {type} isSection
 * @returns {Shape3D}
 */
function ops_CreateShapeFromCave(VectorsPoints, isSection)
{
    isSection = (typeof (isSection) !== 'undefined') ? isSection : false;
    var ColorLateral = "rgba(100,100,100,50)";
    var ColorVector = "rgb(250,0,0)";
    var ColorPolygonal = "rgb(250,0,250)";
    var ColorText = "rgba(250,0,250,20)";
    var ColorPoint = "rgb(0,200,0)";
    var ColorGrid = "rgba(128,128,128,90)";
    //splx.print_r("issection="+isSection);
    var listStations = new Array();
    var ShapePoints = new Array();
    var FromList = new Array();
    var x_min = 0.0;
    var y_min = 0.0;
    var z_min = 0.0;
    var x_max = 0.0;
    var y_max = 0.0;
    var z_max = 0.0;
    var pointRadius = 0.2;
    var layerVector = "vector";
    var CurrentColor = ColorVector;
    for (var i in VectorsPoints)
    {

        var currenLine_vector = VectorsPoints[i];
        var p3d = new Point3D();
        p3d.x = parseFloat(currenLine_vector['x']);
        p3d.y = parseFloat(currenLine_vector['y']);
        p3d.z = parseFloat(currenLine_vector['z']);
        p3d.x2 = parseFloat(currenLine_vector['x2']);
        p3d.y2 = parseFloat(currenLine_vector['y2']);
        p3d.z2 = parseFloat(currenLine_vector['z2']);
        x_min = Math.min(x_min, p3d.x);
        y_min = Math.min(y_min, p3d.y);
        z_min = Math.min(z_min, p3d.z);
        x_max = Math.max(x_max, p3d.x);
        y_max = Math.max(y_max, p3d.y);
        z_max = Math.max(z_max, p3d.z);
        x_min = Math.min(x_min, p3d.x2);
        y_min = Math.min(y_min, p3d.y2);
        z_min = Math.min(z_min, p3d.z2);
        x_max = Math.max(x_max, p3d.x2);
        y_max = Math.max(y_max, p3d.y2);
        z_max = Math.max(z_max, p3d.z2);
        //line
        var Shapepoint = p3d;
        Shapepoint.layer = layerVector;
        Shapepoint.type = "line";
        if (currenLine_vector['to'] !== "" && currenLine_vector['to'] !== "-")
        {
            Shapepoint.color = CurrentColor;
            Shapepoint.layer = layerVector;
        }
        else {
            Shapepoint.color = ColorLateral;
            Shapepoint.layer = "lateral";
        }
        Shapepoint.text = currenLine_vector['from'];
        ShapePoints.push(Shapepoint);
        //text
        if (currenLine_vector['from'] !== "")
        {
            if (FromList[currenLine_vector['from']] === undefined)
            {
                FromList[currenLine_vector['from']] = true;
                Shapepoint = new Point3D();
                Shapepoint.type = "text";
                Shapepoint.x = (currenLine_vector['x']) + pointRadius;
                Shapepoint.y = (currenLine_vector['y']) - pointRadius;
                Shapepoint.z = (currenLine_vector['z']);
                Shapepoint.text = currenLine_vector['from'];
                Shapepoint.color = ColorText;
                Shapepoint.layer = "stations";
                ShapePoints.push(Shapepoint);
                Shapepoint = new Point3D();
                Shapepoint.type = "circle";
                Shapepoint.x = (currenLine_vector['x']);
                Shapepoint.y = (currenLine_vector['y']);
                Shapepoint.z = (currenLine_vector['z']);
                Shapepoint.radius = pointRadius;
                Shapepoint.color = ColorPoint;
                Shapepoint.layer = "points";
                ShapePoints.push(Shapepoint);
                listStations[currenLine_vector['from']] = new Point3D();
                listStations[currenLine_vector['from']].x = currenLine_vector['x'];
                listStations[currenLine_vector['from']].y = currenLine_vector['y'];
                listStations[currenLine_vector['from']].z = currenLine_vector['z'];


            }
        }
        if (currenLine_vector['to'] !== "")
        {
            if (FromList[currenLine_vector['to']] === undefined)
            {
                FromList[currenLine_vector['to']] = true;
                Shapepoint = new Point3D();
                Shapepoint.type = "text";
                Shapepoint.x = (currenLine_vector['x2']) + pointRadius;
                Shapepoint.y = (currenLine_vector['y2']) - pointRadius;
                Shapepoint.z = (currenLine_vector['z2']);
                Shapepoint.text = currenLine_vector['to'];
                Shapepoint.color = ColorText;
                Shapepoint.layer = "stations";
                ShapePoints.push(Shapepoint);
                Shapepoint = new Point3D();
                Shapepoint.type = "circle";
                Shapepoint.x = (currenLine_vector['x2']);
                Shapepoint.y = (currenLine_vector['y2']);
                Shapepoint.z = (currenLine_vector['z2']);
                Shapepoint.radius = pointRadius;
                Shapepoint.color = ColorPoint;
                Shapepoint.layer = "points";
                ShapePoints.push(Shapepoint);

                listStations[currenLine_vector['to']] = new Point3D();
                listStations[currenLine_vector['to']].x = currenLine_vector['x2'];
                listStations[currenLine_vector['to']].y = currenLine_vector['y2'];
                listStations[currenLine_vector['to']].z = currenLine_vector['z2'];


            }
        }
        //external geo
        if (caveObj.geoPoint === currenLine_vector['from'])
        {
            //layerVector = "polygonal";
            //CurrentColor=ColorPolygonal;
            //console.log(caveObj.startPoint);
            Shapepoint = new Point3D();
            Shapepoint.type = "3dpoint";
            Shapepoint.x = (currenLine_vector['x']);
            Shapepoint.y = (currenLine_vector['y']);
            Shapepoint.z = (currenLine_vector['z']);
            Shapepoint.color = "rgb(255,0,0)";
            Shapepoint.layer = "georeferenced";
            Shapepoint.text = _i18n("georeferenced");
            if (caveObj.latitude != "" && caveObj.longitude != "")
            {
                Shapepoint.text = _i18n("georeferenced") + " " + caveObj.latitude + "E," + caveObj.longitude + "N";
                Shapepoint.text += " | UTM WGS84 ZONE " + $("#cave_utm_zone").val() + " X:" + $("#cave_utm_x").val() + "Y:" + $("#cave_utm_y").val() + "Z:" + caveObj.altitude;
            }
            ShapePoints.push(Shapepoint);
        }
        //entrance
        if (caveObj.startPoint === currenLine_vector['from'])
        {
            //layerVector = "vector";
            //CurrentColor=ColorVector;
            //console.log(caveObj.startPoint);
            Shapepoint = new Point3D();
            Shapepoint.type = "3dpoint";
            Shapepoint.x = (currenLine_vector['x']);
            Shapepoint.y = (currenLine_vector['y']);
            Shapepoint.z = (currenLine_vector['z']);
            Shapepoint.color = ColorPoint;
            Shapepoint.layer = "entrance";
            Shapepoint.text = _i18n("entrance") + " " + caveObj.name;
            ShapePoints.push(Shapepoint);
        }



        if (PointSelected === currenLine_vector['from'])
        {
            //alert(caveObj.startPoint);
            Shapepoint = new Point3D();
            Shapepoint.type = "3dpoint";
            Shapepoint.x = (currenLine_vector['x']);
            Shapepoint.y = (currenLine_vector['y']);
            Shapepoint.z = (currenLine_vector['z']);
            Shapepoint.color = "rgba(100,100,100,10)";
            ShapePoints.push(Shapepoint);
        }

    }
    //------note--------------------------------------------------------------->
    for (var i in caveObj.data)
    {
        var currenLine_vector = caveObj.data[i];
        if (currenLine_vector['from'] !== "" && currenLine_vector['note'] !== "")
        {
            if (listStations[currenLine_vector['from']] !== "" && listStations[currenLine_vector['from']] !== undefined)
            {

                Shapepoint = new Point3D();
                Shapepoint.type = "text";
                Shapepoint.x = (0.5 + listStations[currenLine_vector['from']].x);
                Shapepoint.y = (-0.5 + listStations[currenLine_vector['from']].y);
                Shapepoint.z = listStations[currenLine_vector['from']].z;
                Shapepoint.color = "rgba(100,100,100,10)";
                Shapepoint.text = currenLine_vector['note'];
                Shapepoint.layer = "note";
                //console.log(Shapepoint);
                ShapePoints.push(Shapepoint);
            }
        }
    }
    //------note---------------------------------------------------------------<

    ///------------------cubo-------------------------------------------------->
    x_min -= 10;
    y_min -= 10;
    z_min -= 10;
    x_max += 10;
    y_max += 10;
    z_max += 10;
    if (isSection === false)
    {
        // quadrato basso
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_min;
        Shapepoint.y = y_min;
        Shapepoint.z = z_min;
        Shapepoint.x2 = x_max;
        Shapepoint.y2 = y_min;
        Shapepoint.z2 = z_min;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_max;
        Shapepoint.y = y_min;
        Shapepoint.z = z_min;
        Shapepoint.x2 = x_max;
        Shapepoint.y2 = y_max;
        Shapepoint.z2 = z_min;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_max;
        Shapepoint.y = y_max;
        Shapepoint.z = z_min;
        Shapepoint.x2 = x_min;
        Shapepoint.y2 = y_max;
        Shapepoint.z2 = z_min;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_min;
        Shapepoint.y = y_max;
        Shapepoint.z = z_min;
        Shapepoint.x2 = x_min;
        Shapepoint.y2 = y_min;
        Shapepoint.z2 = z_min;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        //quadrato alto
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_min;
        Shapepoint.y = y_min;
        Shapepoint.z = z_max;
        Shapepoint.x2 = x_max;
        Shapepoint.y2 = y_min;
        Shapepoint.z2 = z_max;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_max;
        Shapepoint.y = y_min;
        Shapepoint.z = z_max;
        Shapepoint.x2 = x_max;
        Shapepoint.y2 = y_max;
        Shapepoint.z2 = z_max;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_max;
        Shapepoint.y = y_max;
        Shapepoint.z = z_max;
        Shapepoint.x2 = x_min;
        Shapepoint.y2 = y_max;
        Shapepoint.z2 = z_max;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_min;
        Shapepoint.y = y_max;
        Shapepoint.z = z_max;
        Shapepoint.x2 = x_min;
        Shapepoint.y2 = y_min;
        Shapepoint.z2 = z_max;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        //estreme verticali
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_min;
        Shapepoint.y = y_min;
        Shapepoint.z = z_min;
        Shapepoint.x2 = x_min;
        Shapepoint.y2 = y_min;
        Shapepoint.z2 = z_max;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_max;
        Shapepoint.y = y_min;
        Shapepoint.z = z_min;
        Shapepoint.x2 = x_max;
        Shapepoint.y2 = y_min;
        Shapepoint.z2 = z_max;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_min;
        Shapepoint.y = y_max;
        Shapepoint.z = z_min;
        Shapepoint.x2 = x_min;
        Shapepoint.y2 = y_max;
        Shapepoint.z2 = z_max;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
        Shapepoint = new Point3D();
        Shapepoint.color = ColorGrid;
        Shapepoint.type = "line";
        Shapepoint.x = x_max;
        Shapepoint.y = y_max;
        Shapepoint.z = z_min;
        Shapepoint.x2 = x_max;
        Shapepoint.y2 = y_max;
        Shapepoint.z2 = z_max;
        Shapepoint.layer = "grid";
        ShapePoints.push(Shapepoint);
    }
    else
    {



    }
    ///------------------cubo--------------------------------------------------<

    var Shape = new Shape3D();
    Shape.color = "rgb(100,0,255)";
    Shape.points = ShapePoints;
    return Shape;
}


/**
 * 
 * @param {Array} arrayVectors
 */
function ops_DataGroupByFrom(arrayVectors)
{
    var tmp = new Array();
    for (var i in arrayVectors)
    {
        var txt_from = arrayVectors[i]['from'];
        if (tmp[txt_from] === undefined)
        {
            tmp[txt_from] = new Array();
        }
        tmp[txt_from].push(arrayVectors[i]);
    }
    return tmp;
}
/**
 * 
 * @param {type} shape
 * @param {type} xRotation
 * @param {type} yRotation
 * @param {type} zRotation
 * @returns {@exp;dxfObj@call;getString}
 */
function ops_ShapeToDXFPlan(shape, xRotation, yRotation, zRotation) {
    xRotation = DegToRad(xRotation);
    yRotation = DegToRad(yRotation);
    zRotation = DegToRad(zRotation);
    //georeferenziazione--->
    var geoOffsets = ops_GetGeoOffsets();
    var delta_x = geoOffsets['x'];
    var delta_y = geoOffsets['y'];
    //georeferenziazione---<
    var dxfObj = new DXF();
    dxfObj.setViewportCenter(delta_x, delta_y);
    var zoom = 1;
    var shapeLayers = new Array();
    for (var i in shape.points)
    {
        if (shape.points[i].layer !== undefined)
        {
            shapeLayers[shape.points[i].layer] = shape.points[i].layer;
        }
    }
    var countlayer = 1; //color
    for (var i in shapeLayers)
    {
        dxfObj.addLayer(shapeLayers[i], countlayer++, "CONTINUOUS");
    }
    var x, y, z, x2, y2, z2;
    var x_max = 0, y_max = 0, z_max = 0, x_min = 0, y_min = 0, z_min = 0;

    for (var i = 0; shape.points[i] !== undefined && shape.points[i].x !== undefined; i++) {
        var p = new Point3D();
        p.x = (zoom * shape.points[i].x);
        p.y = 0 - (zoom * shape.points[i].y);
        p.z = (zoom * shape.points[i].z);
        p.text = shape.points[i].text;
        p.textsize = shape.points[i].textsize;
        p.type = shape.points[i].type;
        p.color = shape.points[i].color;
        p.layer = shape.points[i].layer;
        p.radius = shape.points[i].radius;
        if (shape.points[i].x2 !== undefined)
        {
            p.x2 = (zoom * shape.points[i].x2);
            p.y2 = 0 - (zoom * shape.points[i].y2);
            p.z2 = (zoom * shape.points[i].z2);
        }
        rotateX(p, xRotation);
        rotateY(p, yRotation);
        rotateZ(p, zRotation);
        x = p.x + delta_x;
        y = p.y + delta_y;
        z = z2 = 0;
        if (p.y2 !== undefined) //linea
        {
            x2 = p.x2 + delta_x;
            y2 = p.y2 + delta_y;
        }
        x_max = Math.max(x_max, x);
        y_max = Math.max(y_max, y);
        z_max = Math.max(z_max, z);
        x_min = Math.min(x_min, x);
        y_min = Math.min(y_min, y);
        z_min = Math.min(z_min, z);
        x_max = Math.max(x_max, x);
        y_max = Math.max(y_max, y2);
        z_max = Math.max(z_max, z2);
        x_min = Math.min(x_min, x2);
        y_min = Math.min(y_min, y2);
        z_min = Math.min(z_min, z2);
        switch (p.type)
        {
            case "line":
                dxfObj.addLine(x, y, z, x2, y2, z2, p.layer);
                break;
            case "text":
                dxfObj.addText(x, y, z, p.text, p.textsize, p.layer);
                break;
            case "circle":
                dxfObj.addCircle(x, y, z, p.radius, p.layer);
                break;
            default:
                dxfObj.addText(x, y, z, p.text, 0.5, p.layer);
                break;
        }
    }

//------scale and title ------>
    var LayerScale = _i18n("scale", "AA");
    var LayerTitle = _i18n("title", "AA");
    dxfObj.addLayer(shapeLayers[i], countlayer++, LayerScale);
    dxfObj.addLayer(shapeLayers[i], countlayer++, LayerTitle);
    y = y_max + 10;
    x = x_min + 1;
    z = 0;
    x2 = x + 50.0;
    y2 = y;
    dxfObj.addLine(x, y, z, x2, y, z2, LayerScale); //orizzontale alta
    dxfObj.addLine(x, y - 2, z, x2, y - 2, z2, LayerScale); //orizzontale bassa
    dxfObj.addLine(x, y - 1, z, x + 10.0, y - 1, z2, LayerScale); //orizzontale bassa corta
    for (var i = 0; i <= 50; i += 10.0)
    {
        dxfObj.addLine(x + i, y, z, x + i, y - 2, z2, LayerScale); // linee verticali a 10 metri
        dxfObj.addText(x + i - 0.3, y - 4, z, i, 1, LayerScale);
    }
    for (var i = 1; i < 10; i++)
    {
        dxfObj.addLine(x + i, y - 1, z, x + i, y - 2, z2, LayerScale); // linee verticali a 1 metro
        //  dxfObj.addText(x + i - 0.3, y - 4, z, i, 1, LayerScale);
    }
    dxfObj.addText(x_min, y_max + 15, z, caveObj.name, 5, _i18n(LayerTitle, "AA"));
    //------scale and title ------<
    return dxfObj.getString();
}
/**
 * 
 * @returns {undefined}
 */
function ops_GetGeoOffsets()
{
    //-----georeferenziazione-------------------------------------------------->
    var source = new Proj4js.Proj('EPSG:4326');
//    var dest = new Proj4js.Proj('EPSG:900913');//spherical
//+proj=utm +zone=32 +ellps=WGS84 +datum=WGS84 +units=m +no_defs 
    Proj4js.defs["EPSG:32632"] = "+proj=utm +zone=32 +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
    var dest = new Proj4js.Proj('EPSG:32632'); //utm 32n
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
    var delta_z = parseFloat(pIng.z) - parseFloat(shape.points[entrance].z);
    //-----georeferenziazione--------------------------------------------------<
    var ret = new Array();
    ret['x'] = delta_x;
    ret['y'] = delta_y;
    ret['z'] = delta_z;
    ret['ref'] = "UTM 32 N";
    ret['txt_ref'] = " X:" + delta_x + "Y:" + delta_y + "Z:" + delta_z;
    return ret;
}

/**
 * 
 * @param {type} shape
 * @param {type} xRotation
 * @param {type} yRotation
 * @param {type} zRotation
 * @returns {@exp;dxfObj@call;getString}
 */
function ops_ShapeToDXFGEO3d(shape, xRotation, yRotation, zRotation) {
    xRotation = DegToRad(xRotation);
    yRotation = DegToRad(yRotation);
    zRotation = DegToRad(zRotation);
    //georeferenziazione--->
    var geoOffsets = ops_GetGeoOffsets();
    var delta_x = geoOffsets['x'];
    var delta_y = geoOffsets['y'];
    var delta_z = geoOffsets['z'];
    //georeferenziazione---<

    var dxfObj = new DXF();
    var zoom = 1;
    var shapeLayers = new Array();
    for (var i in shape.points)
    {
        if (shape.points[i].layer !== undefined)
        {
            shapeLayers[shape.points[i].layer] = shape.points[i].layer;
        }

    }
    var countlayer = 1; //color
    for (var i in shapeLayers)
    {
        dxfObj.addLayer(shapeLayers[i], countlayer++, "CONTINUOUS");
    }
    dxfObj.addLayer(_i18n("scale", "AA"), countlayer++, "CONTINUOUS");
    var x, y, z, x2, y2, z2;
    var x_max = 0, y_max = 0, z_max = 0, x_min = 0, y_min = 0, z_min = 0;
    for (var i = 0; shape.points[i] !== undefined && shape.points[i].x !== undefined; i++) {
        var p = new Point3D();
        p.x = (zoom * shape.points[i].x);
        p.y = 0 - (zoom * shape.points[i].y);
        p.z = (zoom * shape.points[i].z);
        p.radius = shape.points[i].radius;
        p.text = shape.points[i].text;
        p.type = shape.points[i].type;
        p.color = shape.points[i].color;
        p.layer = shape.points[i].layer;
        if (shape.points[i].x2 !== undefined)
        {
            p.x2 = (zoom * shape.points[i].x2);
            p.y2 = 0 - (zoom * shape.points[i].y2);
            p.z2 = (zoom * shape.points[i].z2);
        }
        rotateX(p, xRotation);
        rotateY(p, yRotation);
        rotateZ(p, zRotation);
        x = p.x + delta_x;
        y = p.y + delta_y;
        z = p.z + delta_z;
        if (p.y2 !== undefined) //linea
        {
            x2 = p.x2 + delta_x;
            y2 = p.y2 + delta_y;
            z2 = p.z2 + delta_z;
        }
        x_max = Math.max(x_max, x);
        y_max = Math.max(y_max, y);
        z_max = Math.max(z_max, z);
        x_min = Math.min(x_min, x);
        y_min = Math.min(y_min, y);
        z_min = Math.min(z_min, z);
        x_max = Math.max(x_max, x);
        y_max = Math.max(y_max, y2);
        z_max = Math.max(z_max, z2);
        x_min = Math.min(x_min, x2);
        y_min = Math.min(y_min, y2);
        z_min = Math.min(z_min, z2);
        switch (p.type)
        {
            case "line":
                dxfObj.addLine(x, y, z, x2, y2, z2, p.layer);
                break;
            case "text":
                dxfObj.addText(x, y, z, p.text, p.textsize, p.layer);
                break;
            case "circle":
                dxfObj.addCircle(x, y, z, p.radius, p.layer);
                break;
            default:
                dxfObj.addText(x, y, z, p.text, p.textsize, p.layer);
                break;
        }
    }
    dxfObj.setViewportCenter(delta_x, delta_y);
    return dxfObj.getString();
}

/**
 * 
 * @param {type} shape
 * @returns {String}
 */
function ops_ShapeToDXF(shape) {
    var dxfObj = new DXF();
    var shapeLayers = new Array();
    for (var i in shape.points)
    {
        if (shape.points[i].layer !== undefined)
        {
            shapeLayers[shape.points[i].layer] = shape.points[i].layer;
        }
    }
    var countlayer = 1; //color
    for (var i in shapeLayers)
    {
        dxfObj.addLayer(shapeLayers[i], countlayer++, "CONTINUOUS");
    }
    dxfObj.addLayer(_i18n("scale", "AA"), countlayer++, "CONTINUOUS");
    var x, y, z, x2, y2, z2;
    for (var i = 0; shape.points[i] !== undefined && shape.points[i].x !== undefined; i++) {
        var p = new Point3D();
        p.x = (shape.points[i].x);
        p.y = 0 - (shape.points[i].y);
        p.z = (shape.points[i].z);
        p.radius = shape.points[i].radius;
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
        x = p.x;
        y = p.y;
        z = p.z;
        if (p.y2 !== undefined) //linea
        {
            x2 = p.x2;
            y2 = p.y2;
            z2 = p.z2;
        }
        switch (p.type)
        {
            case "line":
                dxfObj.addLine(x, y, z, x2, y2, z2, p.layer);
                break;
            case "text":
                dxfObj.addText(x, y, z, p.text, p.textsize, p.layer);
                break;
            case "circle":
                dxfObj.addCircle(x, y, z, p.radius, p.layer);
                break;
            default:
                dxfObj.addText(x, y, z, p.text, p.textsize, p.layer);
                break;
        }
    }
    return dxfObj.getString();
}


/**
 * 
 * @param {type} mode
 * @returns {undefined}
 */
function onAppResize(mode)
{
    if (!LayoutOK)
        return;
    //massimizza i canvas ---->
    $('#animation canvas').attr("width", parseInt($('#animation canvas').parent().css("width").replace("px", "")) - 3);
    $('#animation canvas').attr("height", parseInt($('#cavecontainer').height() - $('#caveform').height() - 80));
    $('#cavepreview canvas').attr("width", parseInt($('#cavepreview canvas').parent().css("width").replace("px", "")) - 3);
    $('#cavepreview canvas').attr("height", parseInt($('#cavecontainer').height() - $('#caveform').height() - 80));
    $('#cavesection canvas').attr("width", parseInt($('#cavesection canvas').parent().css("width").replace("px", "")) - 3);
    $('#cavesection canvas').attr("height", parseInt($('#cavecontainer').height() - $('#caveform').height() - 80));
    //per massimizzare l'altezza devo guardare #cavecontainer-#caveform 
    ops_redrawCanvas();
    // alert($('#cavecontainer').height());
    // $("#cells").height($("#cellscontent").height()-1);
    // $("#cells").width($("#cellscontent").width()-1);
    //massimizza i canvas ----<
    $("#loading").attr("style", "display:none");
    if (mode !== "window")
        $("#cells").resize();

//    console.log("resize " + mode);
}
var LayoutOK = true;
/********************************* MAIN ***************************************/
/********************************* MAIN ***************************************/
/********************************* MAIN ***************************************/
// Check for the various File API support.
var isMSIE = /*@cc_on!@*/0;
if (window.File && window.FileReader && window.FileList && window.Blob) {
// Great success! All the File APIs are supported.
    document.getElementById('files').addEventListener('change', handleOpenFileSelect, false);
    document.getElementById('filesadd').addEventListener('change', handleOpenFileAddSelect, false);
} else {
    alert(_i18n('Your browser does not support all the features required to run this program.'));
}
if (isMSIE)
{
    alert(_i18n("Explorer is not supported. Use the browser Chrome or Firefox", ""));
}
var PointSelected = "";
var rotate = 0;
var shape;
var shapeSection;
var documentChanged = false;
var caveObj = new CaveObject();
var animateShape = false;
var canv, canvanimation, canvansection;
//init default cave object --->

caveObj.data = [
    {"from": null, "to": null, "len": "m", "compass": "deg", "clino": "deg", "top": "m", "left": "m", "right": "m", "bottom": "m", "r": null},
    {"from": "26 febbraio 1984", "to": null, "len": null, "compass": null, "clino": null, "top": null, "left": null, "right": null, "bottom": null, "r": null, "note": "S.Zoja, L.Briganti"},
    {"from": "I", "to": "0", "len": 0, "compass": "0.00", "clino": "0.00", "top": "0.8", "left": "0.6", "right": "0.8", "bottom": "1", "r": ""},
    {"from": "0", "to": "1", "len": "6.1", "compass": "230.00", "clino": "0.00", "top": "3", "left": "1", "right": "0", "bottom": "0.5", "r": "", "note": "ingresso"},
    {"from": "1", "to": "2", "len": "8.7", "compass": "169.00", "clino": "0.00", "top": "2.5", "left": "0", "right": "1", "bottom": "0.5", "r": ""},
    {"from": "2", "to": "3", "len": "8.4", "compass": "206.00", "clino": "0.00", "top": "2", "left": "1", "right": "0", "bottom": "0.5", "r": ""},
    {"from": "3", "to": "4", "len": "6.8", "compass": "255.00", "clino": "0.00", "top": "1", "left": "2.5", "right": "0", "bottom": "0.5", "r": ""},
    {"from": "4", "to": "5", "len": "5.3", "compass": "196.00", "clino": "0.00", "top": "1", "left": "0.1", "right": "1.5", "bottom": "0.5", "r": ""},
    {"from": "5", "to": "6", "len": "13.8", "compass": "249.00", "clino": "0.00", "top": "0.5", "left": "0.5", "right": "0.5", "bottom": "0.5", "r": ""},
    {"from": "6", "to": "7", "len": "8", "compass": "251.00", "clino": "0.00", "top": "0.5", "left": "0.5", "right": "3", "bottom": "0.5", "r": ""},
    {"from": "7", "to": "8", "len": "4.1", "compass": "250.00", "clino": "0.00", "top": "0", "left": "1", "right": "1", "bottom": "0.5", "r": ""}
];


// TODO set name etc
caveObj.name = "Grotta Fada";
caveObj.altitude = "125";
caveObj.longitude = "9.949350";
caveObj.latitude = "44.0690";
caveObj.northdeclination = 0;
caveObj.startPoint = "0";
caveObj.geoPoint = "0";

//init default cave object ---<

setInterval(function() {
    if (rotate > 360)
        rotate = 0;
    if (rotate < 0)
        rotate = 360;
    if (shape !== undefined && animateShape === true)
    {
        $("#animation input.yrotation").val(rotate--);
        canvanimation.render(shape);
    }
}
, 100);



customValueRenderer = function(instance, td, row, col, prop, value, cellProperties) {
    Handsontable.TextCell.renderer.apply(this, arguments);
    if (value === undefined || value === null)
    {
        value = "";
    }
    if (col === 0 || col === 1) {

        td.style.fontWeight = 'bold';
        td.style.color = '#537697';
        //td.style.fontSize = '8px';
        return;
    }

    if (col === 3 || col === 4) {
        if (value === "deg" || value === "rad")
        {
            td.style.fontWeight = 'bold';
            td.style.color = '#000000';
            td.style.background = '#f6da19';
            var $td = $(td);
            var $text = $('<div class="htAutocomplete"></div>');
            var $arrow = $('<div class="ArrowUnity">&#x25BC;</div>');
            $arrow.mouseup(function(event) {
                instance.view.wt.getSetting('onCellDblClick');
            });
            Handsontable.TextCell.renderer(instance, $text[0], row, col, prop, value, cellProperties);
            if ($text.html() === '') {
                $text.html('&nbsp;');
            }
            $text.append($arrow);
            $td.empty().append($text);
        }
        else {
            if (isNaN(value))
                td.style.color = "red";
            else
                td.style.color = "";
            td.style.fontWeight = '';
            td.style.background = '';
        }
        return;
    }

    if (col === 2 || col === 5 || col === 6 || col === 7 || col === 8) {
        if (value === "m" || value === "cm" || value === "ft" || value === "dm")
        {
            td.style.fontWeight = 'bold';
            td.style.color = '#000000';
            td.style.background = '#f6da19';
            var $td = $(td);
            var $text = $('<div class="htAutocomplete"></div>');
            var $arrow = $('<div class="ArrowUnity">&#x25BC;</div>');
            $arrow.mouseup(function(event) {
                instance.view.wt.getSetting('onCellDblClick');
            });
            Handsontable.TextCell.renderer(instance, $text[0], row, col, prop, value, cellProperties);
            if ($text.html() === '') {
                $text.html('&nbsp;');
            }
            $text.append($arrow);
            $td.empty().append($text);

        } else {
            try {
                if (value && value.toString().search(";") != -1) {
                    td.style.color = "magenta";
                }
                else
                if (isNaN(value))
                    td.style.color = "red";
                else
                    td.style.color = "";
            } catch (e) {
                td.style.color = "red";

            }
            td.style.fontWeight = '';
            td.style.background = '';
        }
    }


};

var source_length = ["m", "cm", "dm", "ft"];
var source_clino = ["deg", "rad"];

// load CaveSurvey Data

function getQueryParameter ( parameterName ) {
  var queryString = window.top.location.search.substring(1);
  var parameterName = parameterName + "=";
  if ( queryString.length > 0 ) {
    begin = queryString.indexOf ( parameterName );
    if ( begin != -1 ) {
      begin += parameterName.length;
      end = queryString.indexOf ( "&" , begin );
        if ( end == -1 ) {
        end = queryString.length
      }
      return unescape ( queryString.substring ( begin, end ) );
    }
  }
  return "null";
}


//------------------------------ON READY--------------------------------------->
$(document).ready(function() {

    $("body").attr("onselectstart", "return false;");
    $(".xpan").val(0);
    $(".ypan").val(0);
    $(".zpan").val(0);
    $(".xrotation").val(0);
    $(".yrotation").val(0);
    $(".zrotation").val(0);

    $(".xoffset").val(0);
    $(".yoffset").val(0);
    $(".zoom").val(1);
    $("#cavesection .xrotation").val(90);
    $("#cavesection .yrotation").val(-90);
    $("#cavesection .zrotation").val(0);
    $("#animation .xrotation").val(90);

    //----------------- page layout ------------------------------------------->

    $("#loading").attr("style", "display:none");

    //----------------- page layout -------------------------------------------<
    //----------------- grid layout ------------------------------------------->
    container = $("#cells");
    ops_CaveObjToForm();
    container.handsontable({
        data: caveObj.data,
        rowHeaders: false,
        minRows: 5,
        minCols: 11,
        scrollH: 'auto',
        scrollV: 'auto',
        stretchH: 'last',
        minSpareRows: 2,
        autoWrapRow: true,
        nativeScrollbars: false,
        manualColumnResize: true,
        persistentState: true,
        colWidths: [60, 60, 80, 80, 80, 50, 50, 50, 50, 20, 70],
        //ricalcolo dimensioni-->
        width: function() {
            return $("#cells").width();
        },
        height: function() {
            return $("#cells").height();
        },
        //ricalcolo dimensioni--<


        columns: [
            {data: "from"},
            {data: "to"},
            {data: "len"},
            {data: "compass"},
            {data: "clino"},
            {data: "left"},
            {data: "right"},
            {data: "top"},
            {data: "bottom"},
            {data: "r",
                type: "dropdown",
                source: [">", "<"]
            },
            {data: "note"}
        ],
        colHeaders: [_i18n("from", "Aa"), _i18n("to", "Aa"), _i18n("length", "Aa"), _i18n("compass", "Aa"), _i18n("clino"),
            _i18n("left", "Aa"),
            _i18n("right", "Aa"),
            _i18n("top", "Aa"),
            _i18n("bottom", "Aa"),
            _i18n("i", "Aa"),
            _i18n("note", "Aa")],
        contextMenu: {callback: function(key, options) {
                if (key === 'measurement') {
                    //console.log(options);
                    //setTimeout(function() {
                    //timeout is used to make sure the menu collapsed before alert is shown

                    //timeout is needed because Handsontable normally deselects
                    //current cell when you click outside the table
                    var r = container.handsontable('getSelected')[0];
                    container.handsontable("alter", "insert_row", r);
                    caveObj.data = container.handsontable("getData");
                    // container.handsontable.alter("insert_row", r);
                    caveObj.data[r]['len'] = "m";
                    caveObj.data[r]['top'] = "m";
                    caveObj.data[r]['left'] = "m";
                    caveObj.data[r]['right'] = "m";
                    caveObj.data[r]['bottom'] = "m";
                    caveObj.data[r]['compass'] = "deg";
                    caveObj.data[r]['clino'] = "deg";
                    container.handsontable("loadData", caveObj.data);
//                      alert("This is a context menu with default and custom options mixed");
                    // }, 100);
                }
                if (key === 'date_measurement')
                {

                    var r = container.handsontable('getSelected')[0];
                    container.handsontable("alter", "insert_row", r);
                    caveObj.data = container.handsontable("getData");
                    var date = new Date();
                    caveObj.data[r]['from'] = date.getUTCFullYear() + '-' +
                            ('00' + (date.getUTCMonth() + 1)).slice(-2) + '-' +
                            ('00' + date.getUTCDate()).slice(-2) + ' ';
                    container.handsontable("loadData", caveObj.data);
                }
                if (key === 'georeferenced')
                {
                    var r = container.handsontable('getSelected')[0];
                    container.handsontable("alter", "insert_row", r);
                    caveObj.data = container.handsontable("getData");

                    caveObj.data[r]['left'] = "zone";
                    caveObj.data[r]['len'] = "x";
                    caveObj.data[r]['compass'] = "y";
                    caveObj.data[r]['clino'] = "z";

                    container.handsontable("loadData", caveObj.data);
                }

            },
            items: {
                "row_below": {name: _i18n('insert row below', "Aa")},
                "row_above": {name: _i18n('insert row above', "Aa")},
                "remove_row": {name: _i18n('remove row', "Aa")},
                "hsep1": "---------",
                "measurement": {name: _i18n('set units of measurement', "Aa")},
                "date_measurement": {name: _i18n('set date of measurement', "Aa")}/*,
                "georeferenced": {name: _i18n('set georeferenced points', "Aa")}*/
                }


        },
        beforeChange: function(data) {

            for (var i = data.length - 1; i >= 0; i--) {
                try {
                    if (data[i][1] === "len" ||
                            data[i][1] === "clino" ||
                            data[i][1] === "top" ||
                            data[i][1] === "left" ||
                            data[i][1] === "bottom" ||
                            data[i][1] === "right" ||
                            data[i][1] === "compass")
                    {
                        data[i][3] = data[i][3].replace(",", ".");
                    } else if (data[i][1] === "r") {
                        if (data[i][3] === "i") {
                            data[i][3] = ">";
                        }
                        if (data[i][3] !== "" && data[i][3] !== "<") {
                            data[i][3] = "";
                        }
                    }
                } catch (e) {
                    // alert (e);
                }
            }
        },
        cells: function(row, col, prop) {
            var cols = new Array("from", "to", "len", "compass", "clino", "left", "right", "top", "bottom", "r", "note");
            var cellProperties = {};
            //return cellProperties;
            var value = "";
            try {
                value = caveObj.data[row][cols[col]];
            } catch (e) {
                console.log("splx:" + e);
            }
            cellProperties.renderer = customValueRenderer;
            //console.log(value);
            if (col === 2 || col === 5 || col === 6 || col === 7 || col === 8) {
                if (source_length.indexOf(value) !== -1)
                {
                    cellProperties.type = "dropdown";
                    cellProperties.source = source_length;
                    cellProperties.editor = Handsontable.AutocompleteEditor;
                }
                else {
                    cellProperties.type = "text";

                }
            }
            if (col === 3 || col === 4) {
                if (source_clino.indexOf(value) !== -1)
                {
                    cellProperties.type = "dropdown";
                    cellProperties.source = source_clino;
                    cellProperties.editor = Handsontable.AutocompleteEditor;
                }
                else {
                    cellProperties.type = "text";
                }
            }
            if (col === 0) {
                if (value && value.match(/[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]/g))
                {
                    cellProperties.type = "date";
                }
                else {
                    cellProperties.type = "text";
                }
            }
            return cellProperties;
        }
        ,
        afterChange: function(data, source) {
            if (source === 'loadData') {
                //console.log("load data");
            }
            documentChanged = true;
            // console.log("compile on change");
             ops_compile();
        },
        afterSelectionByProp: function(r, p, r2, p2) {
            if (p === "from" || p === "to")
            {
                if (PointSelected !== caveObj.data[r][p])
                {
                    PointSelected = caveObj.data[r][p];
                    //  console.log("compile on select");
                     ops_compile();
                }
            }
            else
                PointSelected = null;
        }

    });
    //----------------- grid layout -------------------------------------------<


    //ops_CaveObjToForm();

    //-----------------------------tabs --------------------------------------->
    try {
        $("#tabs").tabs({
            beforeActivate: function(event, ui) {
                if (ui.newPanel.attr("id") === "tabs-2")
                {
                    animateShape = true;
                }
                else
                    animateShape = false;
                //ops_redrawCanvas();
                setTimeout("onAppResize('tab');", 10);
            }
        });

        $("#menu").jMenu({
            openClick: false,
            ulWidth: '150',
            effects: {
                effectSpeedOpen: 150,
                effectSpeedClose: 150,
                effectTypeOpen: 'slide',
                effectTypeClose: 'hide',
                effectOpen: 'linear',
                effectClose: 'linear'
            },
            TimeBeforeOpening: 100,
            TimeBeforeClosing: 11,
            animatedText: false,
            paddingLeft: 1
        });
    } catch (e) {
        console.log("splx:" + e);
    }
    //-----------------------------tabs ---------------------------------------<    
    //massimizza i canvas ---->
    $('#animation canvas').attr("width", parseInt($('#animation canvas').parent().css("width").replace("px", "")) - 40);
    $('#animation canvas').attr("height", parseInt($('#animation canvas').parent().css("height").replace("px", "")) - 40);
    $('#cavepreview canvas').attr("width", parseInt($('#cavepreview canvas').parent().css("width").replace("px", "")) - 40);
    $('#cavepreview canvas').attr("height", parseInt($('#cavepreview canvas').parent().css("height").replace("px", "")) - 40);
    $('#cavesection canvas').attr("width", parseInt($('#cavesection canvas').parent().css("width").replace("px", "")) - 40);
    $('#cavesection canvas').attr("height", parseInt($('#cavesection canvas').parent().css("height").replace("px", "")) - 40);
    //massimizza i canvas ----<
    //--------------eventi sui canvas------------------------------------------>
    start_x = 0;
    start_y = 0;
    $('canvas')
            .bind('mousedown', function(event) {
                if (event['button'] === 0)
                {
                    start_x = event['clientX'];
                    start_y = event['clientY'];

                }
            });
    $('canvas')
            .bind('mousemove', function(event) {
                if (event['button'] === 0 && start_x !== 0)
                {
                    var offset_x = event['clientX'] - start_x;
                    var offset_y = event['clientY'] - start_y;
                    $(this).parent().children('input.ypan').val(parseInt($(this).parent().children('input.ypan').val()) + (offset_y));
                    $(this).parent().children('input.xpan').val(parseInt($(this).parent().children('input.xpan').val()) + (offset_x));
                    ops_redrawCanvas();
                    start_x = event['clientX'];
                    start_y = event['clientY'];
                }
            });
    $('canvas')
            .bind('mouseup', function(event) {
                start_x = 0;
                start_y = 0;
            });
    $('canvas')
            .bind('mousewheel', function(event, delta) {

                if (delta > 0)
                {
                    var zoom = parseFloat($(this).parent().children('input.zoom').val());
                    var zoom_inc = zoom / 10;
                    $(this).parent().children('input.zoom').val(zoom + zoom_inc);
                }
                else
                {
                    var zoom = parseFloat($(this).parent().children('input.zoom').val());
                    var zoom_inc = zoom / 10;
                    $(this).parent().children('input.zoom').val(zoom - zoom_inc);
                }
                ops_redrawCanvas();
                return false;
            });
    //--------------eventi sui canvas------------------------------------------<
    /*
     setTimeout(function( ) {
     $("#loading").hide("slow");
     }, 1000);
     */
    if (!isNaN($("#cave_longitude").val()) && !isNaN($("#cave_latitude").val()) && $("#cave_latitude").val() !== "" && $("#cave_longitude").val() !== "")

    {
        ops_CalcCoords($("#cave_longitude").val(), $("#cave_latitude").val(), "");
    }
    if (!isNaN($("#cave_utm_zone").val()) && !isNaN($("#cave_utm_x").val()) && !isNaN($("#cave_utm_y").val()) && $("#cave_utm_zone").val() !== "" && $("#cave_utm_x").val() !== "" && $("#cave_utm_y").val() !== "")
    {
        ops_CalcCoords($("#cave_utm_x").val(), $("#cave_utm_y").val(), $("#cave_utm_zone").val());
    }
    ops_validateform();
    setTimeout("onAppResize('init');", 1000);

    var jsonFile = CaveSurveyJSInterface.getProjectFile();
    //alert('got file ' + jsonFile);
    var caveSurveyData = CaveSurveyJSInterface.getProjectData();
    //alert('got data ' + caveSurveyData);

    caveObj.data = jQuery.parseJSON(caveSurveyData);
    $("#filenameopened").val(caveObj.data.name);
    ops_import(jsonFile, caveSurveyData, false);
});

//------------------------------ON READY---------------------------------------<

canv = new ShapeCanvan("cavepreview");
canvanimation = new ShapeCanvan("animation");
canvansection = new ShapeCanvan("cavesection");


TranslateHtml();

//});



var Download = {
    click: function(node) {
        var ev = document.createEvent("MouseEvents");
        ev.initMouseEvent("click", true, false, self, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
        return node.dispatchEvent(ev);
    },
    encode: function(data) {
        return 'data:application/octet-stream;base64,' + btoa(data);
    },
    link: function(data, name) {
        var a = document.createElement('a');
        a.download = name || self.location.pathname.slice(self.location.pathname.lastIndexOf('/') + 1);
        a.href =  data || self.location.href;
        return a;
    },
    save: function(data, name) {

        // instruct the backend about the file name, base64 stream is not properly decoded
        CaveSurveyJSInterface.setCaveSurveyDownloadFileName(name);

        this.click(
                this.link(
                        this.encode(data),
                        name
                        )
                );
    }
};
//check window resize --------------------------------------------------------->
var rtime = new Date(1, 1, 2000, 12, 00, 00);
var timeout = false;
var delta = 500;
$(window).resize(function() {
    rtime = new Date();
    if (timeout === false) {
        timeout = true;
        setTimeout(resizeend, delta);
    }
});
function resizeend() {
    if (new Date() - rtime < delta) {
        setTimeout(resizeend, delta);
    } else {
        timeout = false;
        onAppResize('window');
        //$("#cells").resize();
    }
}
//check window resize ---------------------------------------------------------<

jQuery.browser = {};
(function() {
    jQuery.browser.msie = false;
    jQuery.browser.version = 0;
    if (navigator.userAgent.match(/MSIE ([0-9]+)\./)) {
        jQuery.browser.msie = true;
        jQuery.browser.version = RegExp.$1;
    }
})();
