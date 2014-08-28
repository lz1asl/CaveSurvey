/**
 * @author Alessandro Vernassa <speleoalex@gmail.com>
 * @copyright Copyright (c) 2013
 * @license http://opensource.org/licenses/gpl-license.php GNU General Public License
 */
function CaveObject()
{
    this.data = [
        {from: "", to: "", compass: "deg", len: "m", clino: "deg", top: "m", left: "m", right: "m", bottom: "m", r: ""}
    ];
    this.name = "";
    this.altitude = 0.0;
    this.longitude = 0.0;
    this.latitude = 0.0;
    this.startPoint = "0";
    this.geoPoint = "0";
    //calcolate --->
    this.vectors = new Array(); //poligonale reale in coordinate x,y,z
    this.vectorsSections = new Array(); //poligonale appiattita per la sezione x,y,z
    this.northDeclination = 0;
    this.len = 0.0;
    this.depht = 0.0;
    this.positive_depht = 0.0;
    this.negative_depht = 0.0;
    //calcolate ---<
}

function CaveRow()
{

    this.from = "";
    this.to = "";
    this.len = 0;
    this.compass = 0.0;
    this.clino = 0;
    this.left = "";
    this.right = "";
    this.top = "";
    this.bottom = "";
    this.r = "";
}


/**
 * copia i valori sul form
 * @returns {undefined}
 */
function ops_CaveObjToForm()
{
    //$('#cells').handsontable("loadData", caveObj.data);
    // $("#filenameopened").val("new_project");
    $("#cave_utm_x").val("");
    $("#cave_utm_y").val("");
    $("#cave_utm_zone").val("");
    for (var caveObjId in caveObj)
    {
        if (document.getElementById("cave_" + caveObjId) !== undefined)
        {
            $('#cave_' + caveObjId).val(caveObj[caveObjId]);
            //onchange su form, find all elements  "cave_*"
            $('#cave_' + caveObjId).change(function() {
                // console.log(caveObjId);
                eval("caveObj." + caveObjId + "=" + "$('#cave_' + caveObjId).val();");
                //console.log(caveObjId+"startpoint="+caveObj.startPoint);
                //console.log("changed");
                ops_compile();
                documentChanged = true;
            });
        }
    }
    ops_CalcCoords($("#cave_longitude").val(), $("#cave_latitude").val(), "");
}


/**
 * 
 * calcola sviluppo, profondità, estensione
 * 
 * @returns {undefined}
 */
function ops_updateCaveInfo()
{
    //get cave info ----------------------------------------------------------->
    caveObj.len = 0.0;
    caveObj.depht = 0.0;
    caveObj.positive_depht = 0.0;
    caveObj.negative_depht = 0.0;
    var x_min = 0.0, x_max = 0.0, y_min = 0.0, y_max = 0.0, z_min = 0.0, z_max = 0.0;
    var len = 0.0;
    for (var i in caveObj.vectors)
    {
        var currenLine_vector = caveObj.vectors[i];
        if (currenLine_vector['to'] !== "" && currenLine_vector['to'] !== "-")
        {
            len += parseFloat(caveObj.vectors[i]['len']);
        }
        x_min = Math.min(x_min, currenLine_vector['x']);
        y_min = Math.min(y_min, currenLine_vector['y']);
        z_min = Math.min(z_min, currenLine_vector['z']);
        x_min = Math.min(x_min, currenLine_vector['x2']);
        y_min = Math.min(y_min, currenLine_vector['y2']);
        z_min = Math.min(z_min, currenLine_vector['z2']);
        x_max = Math.max(x_max, currenLine_vector['x']);
        y_max = Math.max(y_max, currenLine_vector['y']);
        z_max = Math.max(z_max, currenLine_vector['z']);
        x_max = Math.max(x_max, currenLine_vector['x2']);
        y_max = Math.max(y_max, currenLine_vector['y2']);
        z_max = Math.max(z_max, currenLine_vector['z2']);
    }

    caveObj.len = len;
    caveObj.positive_depht = parseFloat(z_max);
    caveObj.negative_depht = 0 - parseFloat(z_min);
    caveObj.depht = parseFloat(0 - z_min) + parseFloat(z_max);
    //get cave info -----------------------------------------------------------<
    $('.cave_len').val(Math.floor(caveObj.len));
    $('.cave_depht').val(Math.floor(caveObj.depht));
    $('.cave_positive_depht').val(Math.floor(caveObj.positive_depht));
    $('.cave_negative_depht').val(Math.floor(caveObj.negative_depht));

}

function ops_redrawCanvas()
{
    //todo capire quale è il canvas da disegnare
    canv.render(shape);
    canvansection.render(shapeSection);
}

/**
 * -prende i dati dalla griglia e dal form e li trasforma nell'oggetto caveObj
 * -prende l'oggetto caveObj e crea gli oggetti shape
 * -aggiorna le anteprime partendo dagli shapes
 * 
 * @returns {undefined}
 */
function ops_compile()
{
    try {
        document.getElementById("processing").style.display = "block";
    } catch (e) {
    }
    setTimeout(function() {
        caveObj.data = $('#cells').handsontable('getData');
        caveObj.name = $("#cave_name").val();
        caveObj.altitude = $("#cave_altitude").val();
        caveObj.longitude = $("#cave_longitude").val();
        caveObj.latitude = $("#cave_latitude").val();
        caveObj.startPoint = $("#cave_startPoint").val();
        caveObj.geoPoint = $("#cave_geoPoint").val();
        caveObj.vectors = ops_GetCaveVectorsFromGridData(caveObj, false);
        caveObj.vectorsSections = ops_GetCaveVectorsFromGridData(caveObj, true);
        // splx.print_r(caveObj.vectorsSections);
        shape = ops_CreateShapeFromCave(caveObj.vectors, false);
        shapeSection = ops_CreateShapeFromCave(caveObj.vectorsSections, true);
        ops_redrawCanvas();
        ops_updateCaveInfo();
        try {
            document.getElementById("processing").style.display = "none";
        } catch (e) {
        }
    }, 1);
}
/**
 * 
 * @returns {undefined}
 */
function ops_autocompile()
{
    if ($("#autocompile").is(':checked'))
    {
        ops_compile();
    }
}
/**
 * 
 * @param {type} arrayVectors
 * @returns {Array}
 */
function ops_MakeXYZOffsets(arrayVectors)
{
    var noloop = new Array();
    var poligonali = new Array();

    for (var i = 0; arrayVectors [i] !== undefined; i++)
    {
        if (noloop[i] !== true)
        {
            noloop[i] = true;
            poligonali[i] = (poligonali[i] === undefined) ? new Array() : poligonali[i];
            poligonali[i].push(arrayVectors[i]);
            if (arrayVectors [i]['to'] !== "" && arrayVectors [i]['to'] !== "-")
            {
                var tofind = arrayVectors [i]['to'];
                var x_offset = arrayVectors [i]['dx'];
                var y_offset = arrayVectors [i]['dy'];
                var z_offset = arrayVectors [i]['dz'];
                for (var ii = 0; arrayVectors [ii] !== undefined; ii++)
                {
                    if (noloop[ii] !== true)
                    {
                        //scorro tutta la lista tranne se stesso
                        if (ii !== i && arrayVectors[ii]['from'] === tofind && arrayVectors[ii]['to'] !== "-")
                        {
                            var tmp = arrayVectors[ii];
                            tmp['x'] += x_offset;
                            tmp['y'] += y_offset;
                            tmp['z'] += z_offset;
                            tmp['x2'] += x_offset;
                            tmp['y2'] += y_offset;
                            tmp['z2'] += z_offset;
                            x_offset += parseFloat(tmp['dx']);
                            y_offset += parseFloat(tmp['dy']);
                            z_offset += parseFloat(tmp['dz']);
                            // console.log("i=" + i + " from=" + arrayVectors[ii]['from'] + " to=" + arrayVectors[ii]['to']);
                            poligonali[i].push(tmp);
                            tofind = arrayVectors[ii]['to'];
                            noloop[ii] = true;
                            ii = -1;
                        }
                    }
                }
            }
        }
    }
    //-----metto gli id progressivi ----->
    var tmp = new Array();
    var c = 0;
    for (var i in poligonali)
    {
        tmp[c] = poligonali[i];
        c++;
    }
    poligonali = tmp;
    //-----metto gli id progressivi -----<
    noloop = new Array();
    //ora unisco i tratti di poligonali disconnessi
    var retval = poligonali[0];
    var iret = 0;
    noloop[0] = true;
    //console.log(poligonali);
    if (retval !== undefined)
    {
        while (retval[iret] !== undefined && iret < retval.length)
        {
            for (var i in poligonali)
            {
                if (noloop[i] !== true) //controllo le rimanenti
                {
                    var poligonale_corrente = poligonali[i];
                    for (var id_punto in poligonale_corrente) // scorro i segmenti di ogni poliginale
                    {
                        var segmento = poligonale_corrente[id_punto];
                        if (retval[iret] === undefined || segmento['from'] === undefined)
                            continue;
                        if (retval[iret]['from'] === segmento['from']) // ho trovato una poligonale con un punto in comune
                        {
                            x_offset = parseFloat(retval[iret]['x']);
                            y_offset = parseFloat(retval[iret]['y']);
                            z_offset = parseFloat(retval[iret]['z']);
                            for (var inew in poligonale_corrente) //traslo tutta la poligonale trovata di x,y,z_offset
                            {
                                var segmento_datraslare = poligonale_corrente[inew];
                                segmento_datraslare['x'] += x_offset;
                                segmento_datraslare['y'] += y_offset;
                                segmento_datraslare['z'] += z_offset;
                                segmento_datraslare['x2'] += x_offset;
                                segmento_datraslare['y2'] += y_offset;
                                segmento_datraslare['z2'] += z_offset;
                                retval.push(segmento_datraslare); //aggiungo alla poligonale di ritorno i nuovi segmenti traslati
                            }
                            noloop[i] = true;
                            //   iret = -1;
                            break;
                        }
                        else //--ok
                        if (segmento['to'] !== "-" && segmento['to'] !== "" && retval[iret]['to'] === segmento['to']) // ho trovato una poligonale con un punto in comune
                        {
                            x_offset = parseFloat(retval[iret]['x2']) - segmento['x2'];
                            y_offset = parseFloat(retval[iret]['y2']) - segmento['y2'];
                            z_offset = parseFloat(retval[iret]['z2']) - segmento['z2'];
                            //splx.print_r(poligonale_corrente);
                            for (var inew in poligonale_corrente) //traslo tutta la poligonale trovata di x,y,z_offset
                            {
                                var segmento_datraslare = poligonale_corrente[inew];
                                segmento_datraslare['x'] += x_offset;
                                segmento_datraslare['y'] += y_offset;
                                segmento_datraslare['z'] += z_offset;
                                segmento_datraslare['x2'] += x_offset;
                                segmento_datraslare['y2'] += y_offset;
                                segmento_datraslare['z2'] += z_offset;
                                retval.push(segmento_datraslare); //aggiungo alla poligonale di ritorno i nuovi segmenti traslati
                            }
                            noloop[i] = true;
                            //  iret = -1;
                            break;
                        }/*
                         else //--todo
                         if (segmento['to'] !== "-" && segmento['to'] !== "" && retval[iret]['to'] === segmento['from']) // ho trovato una poligonale con un punto in comune
                         {
                         x_offset = parseFloat(retval[iret]['x2']) - segmento['x'];
                         y_offset = parseFloat(retval[iret]['y2']) - segmento['y'];
                         z_offset = parseFloat(retval[iret]['z2']) - segmento['z'];
                         //splx.print_r(poligonale_corrente);
                         for (var inew in poligonale_corrente) //traslo tutta la poligonale trovata di x,y,z_offset
                         {
                         var segmento_datraslare = poligonale_corrente[inew];
                         segmento_datraslare['x'] += x_offset;
                         segmento_datraslare['y'] += y_offset;
                         segmento_datraslare['z'] += z_offset;
                         segmento_datraslare['x2'] += x_offset;
                         segmento_datraslare['y2'] += y_offset;
                         segmento_datraslare['z2'] += z_offset;
                         retval.push(segmento_datraslare); //aggiungo alla poligonale di ritorno i nuovi segmenti traslati
                         }
                         noloop[i] = true;
                         iret = -1;
                         break;
                         }*/
                        else //--ok
                        if (segmento['to'] !== "-" && segmento['to'] !== "" && retval[iret]['from'] === segmento['to']) // ho trovato una poligonale con un punto in comune
                        {
                            x_offset = parseFloat(retval[iret]['x']) - segmento['x2'];
                            y_offset = parseFloat(retval[iret]['y']) - segmento['y2'];
                            z_offset = parseFloat(retval[iret]['z']) - segmento['z2'];
                            //splx.print_r(poligonale_corrente);
                            for (var inew in poligonale_corrente) //traslo tutta la poligonale trovata di x,y,z_offset
                            {
                                var segmento_datraslare = poligonale_corrente[inew];
                                segmento_datraslare['x'] += x_offset;
                                segmento_datraslare['y'] += y_offset;
                                segmento_datraslare['z'] += z_offset;
                                segmento_datraslare['x2'] += x_offset;
                                segmento_datraslare['y2'] += y_offset;
                                segmento_datraslare['z2'] += z_offset;
                                retval.push(segmento_datraslare); //aggiungo alla poligonale di ritorno i nuovi segmenti traslati
                            }
                            noloop[i] = true;
                            // iret = -1;
                            break;
                        }
                    }
                }
            }
            iret++;
        }
    }
    return retval;

}
/**
 * 
 * @param {type} arrayVectors
 * @returns {Array}
 */
function ops_MakeXYZOffsets_old(arrayVectors)
{
    //console.log("xy");
    var tofind = "";
    var retvectors = new Array();
    var tratti = new Array();
    var contatratti = 0;
    for (var i in arrayVectors)
    {
        if (arrayVectors[i]['from'] !== "" && arrayVectors[i]['from'] !== "-")
        {
            tofind = arrayVectors[i]['from'];

            var ii = 0;
            var noloopTo = new Array();
            for (ii = 0; arrayVectors [ii] !== undefined; ii++)
            {
                //scorro tutta la lista tranne se stesso
                if (ii !== i && arrayVectors[ii]['to'] === tofind && arrayVectors[ii]['to'] !== "-")
                {
                    arrayVectors[i]['x'] += (arrayVectors[ii]['dx']);
                    arrayVectors[i]['x2'] += arrayVectors[ii]['dx'];
                    arrayVectors[i]['y'] += arrayVectors[ii]['dy'];
                    arrayVectors[i]['y2'] += arrayVectors[ii]['dy'];
                    arrayVectors[i]['z'] += arrayVectors[ii]['dz'];
                    arrayVectors[i]['z2'] += arrayVectors[ii]['dz'];
                    //evita i loop infiniti
                    if (noloopTo[arrayVectors[ii]['from']] !== true)
                    {
                        noloopTo[arrayVectors[ii]['from']] = true;
                        tofind = arrayVectors[ii]['from'];
                        ii = -1;
                    }
                }
            }
        }
        retvectors.push(arrayVectors[i]);
    }
//    splx.print_r(tratti);
    return retvectors;

}



/**
 * -legge la tabella riempe arrayVectors con coordinate
 * polari e xyz con unità di misura deg e metri
 * -richiama ops_MakeXYZOffsets per aggiungere gli offsets
 * 
 * 
 * @param {type} Cave_OBJ
 * @param {type} isSection
 * @returns {array} 
 */
function ops_GetCaveVectorsFromGridData(Cave_OBJ, isSection)
{
    isSection = (typeof (isSection) !== 'undefined') ? isSection : false;

    var northDeclinationDefault = Cave_OBJ.northdeclination;
    var northDeclination;
    var data = Cave_OBJ.data;
    //splx.print_r(data);
    var counter = 0;
    var arrayVectors;
    var tmpVector = new Point3D();
    var unit_length = 1; //m,cm
    var unit_clino = 1; //deg,rad
    var unit_compass = 1; //deg,rad
    var unit_top = 1;
    var unit_bottom = 1;
    var unit_left = 1;
    var unit_right = 1;
    if (isNaN(northDeclinationDefault) || northDeclinationDefault === null || northDeclinationDefault === "")
        northDeclinationDefault = 0;
    northDeclination = northDeclinationDefault;
    arrayVectors = new Array();
    for (var idrow in data)
    {
        //declination
        var row = data[idrow];
        if (
                ops_IsEmptyNonZero(row['len']) &&
                ops_IsEmptyNonZero(row['clino']) &&
                ops_IsEmptyNonZero(row['bottom']) &&
                ops_IsEmptyNonZero(row['right']) &&
                ops_IsEmptyNonZero(row['left']) &&
                ops_IsEmptyNonZero(row['top']) &&
                ops_IsEmptyNonZero(row['to']) &&
                !ops_IsEmptyNonZero(row['compass']) &&
                !isNaN(row['compass'])

                )
        {
            // alert(row['compass']);
            //console.log(row);
            northDeclination = row['compass'];
        }
        //units
        else
        if ((row['from'] === null || row['from'] === "") && (row['to'] === null || row['to'] === ""))
        {
            //m
            if (row['len'] === "m")
            {
                unit_length = 1;
            }
            if (row['top'] === "m")
            {
                unit_top = 1;
            }
            if (row['left'] === "m")
            {
                unit_left = 1;
            }
            if (row['right'] === "m")
            {
                unit_right = 1;
            }
            if (row['bottom'] === "m")
            {
                unit_bottom = 1;
            }
            //cm
            if (row['len'] === "cm")
            {
                unit_length = 0.01;
            }
            if (row['top'] === "cm")
            {
                unit_top = 0.01;
            }
            if (row['left'] === "cm")
            {
                unit_left = 0.01;
            }
            if (row['right'] === "cm")
            {
                unit_right = 0.01;
            }
            if (row['bottom'] === "cm")
            {
                unit_bottom = 0.01;
            }
            //dm
            if (row['len'] === "dm")
            {
                unit_length = 0.1;
            }
            if (row['top'] === "dm")
            {
                unit_top = 0.1;
            }
            if (row['left'] === "dm")
            {
                unit_left = 0.1;
            }
            if (row['right'] === "dm")
            {
                unit_right = 0.1;
            }
            if (row['bottom'] === "dm")
            {
                unit_bottom = 0.1;
            }
            //ft 0.3048
            if (row['len'] === "ft")
            {
                unit_length = 0.3048;
            }
            if (row['top'] === "ft")
            {
                unit_top = 0.3048;
            }
            if (row['left'] === "ft")
            {
                unit_left = 0.3048;
            }
            if (row['right'] === "ft")
            {
                unit_right = 0.3048;
            }
            if (row['bottom'] === "ft")
            {
                unit_bottom = 0.3048;
            }
            //dm
            if (row['len'] === "ft")
            {
                unit_length = 0.3048;
            }
            if (row['top'] === "ft")
            {
                unit_top = 0.3048;
            }
            if (row['left'] === "ft")
            {
                unit_left = 0.3048;
            }
            if (row['right'] === "ft")
            {
                unit_right = 0.3048;
            }
            if (row['bottom'] === "ft")
            {
                unit_bottom = 0.3048;
            }
            if (row['clino'] === "rad")
            {
                unit_clino = 180 / Math.PI;
            }
            if (row['compass'] === "rad")
            {
                unit_compass = 180 / Math.PI;
            }
            if (row['clino'] === "deg")
            {
                unit_clino = 1;
            }
            if (row['compass'] === "deg")
            {
                unit_compass = 1;
            }

        }

        else if (row['from'] !== null && row['from'] !== "" && !isNaN(row['len']) && row['len'] !== "" && row['len'] !== null)
        {
            arrayVectors[counter] = new Array();
            arrayVectors[counter]['from'] = row['from'];
            arrayVectors[counter]['to'] = row['to'];
            if (isSection === true)
            {
                arrayVectors[counter]['compass'] = 0.0;
                if ((row['r']) === "i" || (row['r']) === "<") //todo gestione inverti su sezione
                {
                    arrayVectors[counter]['compass'] = -180.0;
                }
            } else
            {
                arrayVectors[counter]['compass'] = parseFloat(row['compass']);
            }
            arrayVectors[counter]['len'] = (row['len']) * unit_length;
            arrayVectors[counter]['clino'] = (row['clino']) * unit_clino;
            if (isSection === false)
                arrayVectors[counter]['compass'] = ((row['compass']) * unit_compass) + parseFloat(northDeclination);

            arrayVectors[counter]['left'] = (row['left']) * unit_left;
            arrayVectors[counter]['right'] = (row['right']) * unit_right;
            arrayVectors[counter]['top'] = (row['top']) * unit_top;
            arrayVectors[counter]['bottom'] = (row['bottom']) * unit_bottom;
            arrayVectors[counter]['note'] = row['note'];
            arrayVectors[counter]['r'] = row['r'];
            tmpVector = PolarToPoint3D(arrayVectors[counter]);

            arrayVectors[counter]['dx'] = parseFloat(tmpVector.x2);
            arrayVectors[counter]['dy'] = parseFloat(tmpVector.y2);
            arrayVectors[counter]['dz'] = parseFloat(tmpVector.z2);
            arrayVectors[counter]['x'] = 0;
            arrayVectors[counter]['y'] = 0;
            arrayVectors[counter]['z'] = 0;
            arrayVectors[counter]['x2'] = arrayVectors[counter]['dx'];
            arrayVectors[counter]['y2'] = arrayVectors[counter]['dy'];
            arrayVectors[counter]['z2'] = arrayVectors[counter]['dz'];
            var counterFrom = counter;
            counter++;
            // splx.print_r(row);
            if (arrayVectors[counterFrom]['to'] !== "" && arrayVectors[counterFrom]['to'] !== "-")
            {
                //top
                if (row['top'] !== undefined && row['top'] !== null) {
                    var toplist = row['top'].split(";");
                    for (var itop in toplist) {
                        if (!isNaN(toplist[itop])) {
                            var key = "";
                            if (itop == 0 && toplist[1] !== undefined)
                            {
                                key = "from";
                            }
                            else
                            {
                                key = "to";
                            }
                            arrayVectors[counter] = new Array();
                            arrayVectors[counter]['from'] = arrayVectors[counterFrom][key];
                            arrayVectors[counter]['to'] = "-";
                            arrayVectors[counter]['len'] = (toplist[itop]) * unit_top;
                            arrayVectors[counter]['compass'] = arrayVectors[counterFrom]['compass'];
                            if (isSection === true)
                            {
                                arrayVectors[counter]['compass'] = 0;
                            }
                            arrayVectors[counter]['clino'] = 90;
                            tmpVector = PolarToPoint3D(arrayVectors[counter]);
                            arrayVectors[counter]['dx'] = parseFloat(tmpVector.x2);
                            arrayVectors[counter]['dy'] = parseFloat(tmpVector.y2);
                            arrayVectors[counter]['dz'] = parseFloat(tmpVector.z2);
                            arrayVectors[counter]['x'] = 0;
                            arrayVectors[counter]['y'] = 0;
                            arrayVectors[counter]['z'] = 0;
                            arrayVectors[counter]['x2'] = arrayVectors[counter]['dx'];
                            arrayVectors[counter]['y2'] = arrayVectors[counter]['dy'];
                            arrayVectors[counter]['z2'] = arrayVectors[counter]['dz'];
                            counter++;
                        }
                    }
                }
                //bottom
                if (row['bottom'] !== undefined && row['bottom'] !== null) {
                    var bottomlist = row['bottom'].split(";");
                    for (var ibottom in bottomlist) {
                        if (!isNaN(bottomlist[ibottom])) {
                            var key = "";
                            if (ibottom == 0 && bottomlist[1] !== undefined)
                            {
                                key = "from";
                            }
                            else
                            {
                                key = "to";
                            }
                            arrayVectors[counter] = new Array();
                            arrayVectors[counter]['from'] = arrayVectors[counterFrom][key];
                            arrayVectors[counter]['to'] = "-";
                            arrayVectors[counter]['len'] = (bottomlist[ibottom]) * unit_bottom;
                            arrayVectors[counter]['compass'] = arrayVectors[counterFrom]['compass'];
                            if (isSection === true)
                            {
                                arrayVectors[counter]['compass'] = 0;
                            }
                            arrayVectors[counter]['clino'] = -90;
                            tmpVector = PolarToPoint3D(arrayVectors[counter]);
                            arrayVectors[counter]['dx'] = parseFloat(tmpVector.x2);
                            arrayVectors[counter]['dy'] = parseFloat(tmpVector.y2);
                            arrayVectors[counter]['dz'] = parseFloat(tmpVector.z2);
                            arrayVectors[counter]['x'] = 0;
                            arrayVectors[counter]['y'] = 0;
                            arrayVectors[counter]['z'] = 0;
                            arrayVectors[counter]['x2'] = arrayVectors[counter]['dx'];
                            arrayVectors[counter]['y2'] = arrayVectors[counter]['dy'];
                            arrayVectors[counter]['z2'] = arrayVectors[counter]['dz'];
                            counter++;
                        }
                    }
                }
                if (isSection === false)
                {
                    //left
                    if (row['left'] !== undefined && row['left'] !== null) {
                        var leftlist = row['left'].split(";");
                        for (var ileft in leftlist) {
                            if (!isNaN(leftlist[ileft])) {
                                var key = "";
                                if (ileft == 0 && leftlist[1] !== undefined)
                                {
                                    key = "from";
                                }
                                else
                                {
                                    key = "to";
                                }
                                arrayVectors[counter] = new Array();
                                arrayVectors[counter]['from'] = arrayVectors[counterFrom][key];
                                arrayVectors[counter]['to'] = "-";
                                arrayVectors[counter]['len'] = (leftlist[ileft]) * unit_left;
                                arrayVectors[counter]['compass'] = parseFloat(arrayVectors[counterFrom]['compass']) - 90;
                                arrayVectors[counter]['clino'] = 0;
                                tmpVector = PolarToPoint3D(arrayVectors[counter]);
                                arrayVectors[counter]['dx'] = parseFloat(tmpVector.x2);
                                arrayVectors[counter]['dy'] = parseFloat(tmpVector.y2);
                                arrayVectors[counter]['dz'] = parseFloat(tmpVector.z2);
                                arrayVectors[counter]['x'] = 0;
                                arrayVectors[counter]['y'] = 0;
                                arrayVectors[counter]['z'] = 0;
                                arrayVectors[counter]['x2'] = arrayVectors[counter]['dx'];
                                arrayVectors[counter]['y2'] = arrayVectors[counter]['dy'];
                                arrayVectors[counter]['z2'] = arrayVectors[counter]['dz'];
                                counter++;
                            }
                        }
                    }
                    //right
                    if (row['right'] !== undefined && row['right'] !== null) {
                        var rightlist = row['right'].split(";");
                        for (var iright in rightlist) {
                            if (!isNaN(rightlist[iright])) {
                                var key = "";
                                if (iright == 0 && rightlist[1] !== undefined)
                                {
                                    key = "from";
                                }
                                else
                                {
                                    key = "to";
                                }
                                arrayVectors[counter] = new Array();
                                arrayVectors[counter]['from'] = arrayVectors[counterFrom][key];
                                arrayVectors[counter]['to'] = "-";
                                arrayVectors[counter]['len'] = (rightlist[iright]) * unit_right;
                                arrayVectors[counter]['compass'] = parseFloat(arrayVectors[counterFrom]['compass']) + 90;
                                arrayVectors[counter]['clino'] = 0;
                                tmpVector = PolarToPoint3D(arrayVectors[counter]);
                                arrayVectors[counter]['dx'] = parseFloat(tmpVector.x2);
                                arrayVectors[counter]['dy'] = parseFloat(tmpVector.y2);
                                arrayVectors[counter]['dz'] = parseFloat(tmpVector.z2);
                                arrayVectors[counter]['x'] = 0;
                                arrayVectors[counter]['y'] = 0;
                                arrayVectors[counter]['z'] = 0;
                                arrayVectors[counter]['x2'] = arrayVectors[counter]['dx'];
                                arrayVectors[counter]['y2'] = arrayVectors[counter]['dy'];
                                arrayVectors[counter]['z2'] = arrayVectors[counter]['dz'];
                                counter++;
                            }
                        }
                    }
                }
            }
        }
    }
    //splx.print_r(arrayVectors);
    arrayVectors = ops_MakeXYZOffsets(arrayVectors);
    //splx.print_r(arrayVectors);

    return arrayVectors;
}
