str_trim = function(str) {
    return str.replace(/^\s+|\s+$/g, '');
};
str_ltrim = function(str) {
    return str.replace(/^\s+/, '');
};
str_rtrim = function(str) {
    return str.replace(/\s+$/, '');
};
str_fulltrim = function(str) {
    return str.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g, '').replace(/\s+/g, ' ');
};


function th_GetSection(str, sectionname)
{

    var start = str.indexOf(sectionname);

    var end = str.indexOf("end" + sectionname);
    alert(str);
    if (start === -1 || end === -1)
        return "";
    var ret = str.substring(start, end);
    return ret;

}
function ops_GetCaveObjectByThconfig(str)
{
    alert(th_GetSection(str, "source"));
    alert("finzione in fase di implementazione");
}/*
 test = "153_3 153_4  4.07   88.1 -23.5  0.65 [0.0 1.80] [1.00 5.00] 1.00";
 test2 = test.replace(/\[([a-zA-Z0-9.]*) ([a-zA-Z0-9.]*)\]/g, "$1,$2");
 alert(test + "\n" + test2);*/
/**
 * Esegue il parser di un file therion .th e ritorna un array con le poligonnali
 * @param {string} str
 */
function ops_GetCaveObjectByTherion(str)
{
    var cave = new CaveObject();
    var tmp = new Array();
    var centerlines = ops_TH_GetCenterlines(str);

    for (var ic in centerlines)
    {
        var fields = new Array();
        var txtCenterline = centerlines[ic];
        var lines = txtCenterline.split("\n");
        var inv = false;
        for (var i in lines)
        {
            var values = new Array();
            var cleanline = lines[i];
            while (cleanline.search("  ") != -1)
            {
                cleanline = cleanline.replace("  ", " ");
            }
            cleanline = cleanline.replace(/\[([a-zA-Z0-9.]*) ([a-zA-Z0-9.]*)\]/g, "$1;$2");

            var elements = cleanline.split(" ");
            var tmp_datarow = {
                from: "",
                to: "",
                length: "",
                compass: "",
                clino: "",
                r: "",
                top: "",
                left: "",
                bottom: "",
                right: "",
                note: ""
            };
            switch (elements[0])
            {

                case "extend":
                    if (elements[1] === "right") {
                        inv = false;
                    }
                    if (elements[1] === "left") {
                        inv = true;
                    }
                    break;
                case "#":
                    tmp_datarow['note'] = cleanline.split("#")[1];
                    break;
                case "flags":
                    break;
                case "date":
                    tmp_datarow['note'] = elements[1];
                    break;
                case "team":
                    tmp_datarow['note'] = cleanline.replace("team ", "");
                    break;
                case "data":
                    fields = new Array();
                    if (elements[1] === "normal")
                    {
                        for (var c = 2; elements[c] !== undefined; c++)
                        {
                            if (elements[c] == "length")
                            {
                                elements[c] = "len";
                            }
                            fields[elements[c]] = c;
                        }
                    }
                    // console.log(fields);
                    break;
                case "station":
                    tmp_datarow['from'] = elements[1];

                    break;
                default:
                    tmp_datarow["r"] = (inv !== true) ? "" : "<";
                    if (cleanline.search("#") != -1)
                    {
                        tmp_datarow['note'] = cleanline.split("#")[1];
                    }
                    for (var f in fields)
                    {
                        switch (f)
                        {
                            case "length":
                                tmp_datarow['len'] = elements[fields[f] - 2];
                                break;
                            case "up":
                                tmp_datarow['top'] = elements[fields[f] - 2];
                                break;
                            case "down":
                                tmp_datarow['bottom'] = elements[fields[f] - 2];
                                break;
                            default:
                                tmp_datarow[f] = elements[fields[f] - 2];
                                break;
                        }
                    }

                    break;
            }
            // console.log(tmp_datarow);
            if (tmp_datarow['from'] !== undefined || tmp_datarow['note'] !== undefined)
                tmp.push(tmp_datarow);
        }



    }

    cave.data = tmp;
    // ops_GetVectorsByData();
    //  splx.print_r(cave.data);
    return cave;
    //fix \n ---->
    str = str.replace("\r\n", "\n");
    str = str.replace("\r", "\n");
    //fix \n ----<
    var centerline = str.split("endcenterline");
    centerline = centerline[0];
    centerline = centerline.split("centerline");
    centerline = centerline[1];
    centerline = centerline.split("data normal from to length compass clino");
    centerline = centerline[1];
    str = centerline;
    var lines = str.split("\n");
    var inv = false;
    for (var i in lines)
    {
        var line = lines[i].trim();
        line = line.replace("\n", "");
        line = line.replace("\r", "");
        if (line !== "" /*&& line[0] !== "#"*/)
        {
            var values = line.split(" ");
            if (values[0] === "extend" && values[1] === "right")
            {
                inv = false;
            }
            if (values[0] === "extend" && values[1] === "left")
            {
                inv = true;
            }
            if (values[0] === "station")
            {
                //console.log(values[1]);
                var newval = new Array();
                var from = values[1];
                values[0] = "";
                values[1] = "";
                var note = values.join(" ").trim();
                var tmp_datarow = {
                    from: from,
                    to: "",
                    length: "",
                    compass: "",
                    clino: "",
                    r: "",
                    top: "",
                    left: "",
                    bottom: "",
                    right: "",
                    note: note
                };

                tmp.push(tmp_datarow);
            }
            else
            if (values[0] !== "extend" && values[0] !== "flags" && values[0] !== "date" && values[0] !== "station")
            {

                var tmp_datarow = {
                    from: values[0],
                    to: values[1],
                    length: values[2],
                    compass: values[3],
                    clino: values[4],
                    r: (inv !== true) ? "" : "<",
                    top: "",
                    left: "",
                    bottom: "",
                    right: ""
                };
                tmp.push(tmp_datarow);
            }
        }
    }
    cave.data = tmp;
    // ops_GetVectorsByData();
    //  splx.print_r(cave.data);
    return cave;
}

function ops_TH_GetCenterlines(str)
{
    //------clean file--------------------------------------------------------->
    var strclean = "";
    var ret = new Array();
    var centerlines = new Array();
    str = str.replace("\r\n", "\n");
    str = str.replace("\r", "\n");
    var lines = str.split("\n");
    var sep = "";
    for (var i in lines)
    {
        var tmp = str_fulltrim(lines[i]);
        if (tmp !== "" /*&& tmp[0] !== "#"*/)
        {
            strclean += sep + tmp;
            sep = "\n";
        }
    }

    //------clean file---------------------------------------------------------<

    var tmpCenterlines = strclean.split("endcenterline");
    for (var i in tmpCenterlines)
    {
        var tmpCenterline = tmpCenterlines[i].split("centerline");
        if (tmpCenterline[1] !== undefined)
        {
            var centerline_contents = tmpCenterline[1];
            centerlines.push(centerline_contents);
        }
    }
    return centerlines;
}


//---------------------save---------------------------------------------------->
function ops_SaveToTherion() {
    var unit_length = 1; //m,cm
    var unit_clino = 1; //deg,rad
    var unit_compass = 1; //deg,rad
    var unit_top = 1;
    var unit_bottom = 1;
    var unit_left = 1;
    var unit_right = 1;
    var str = "";
    var name = caveObj.name.replace(/ /g, "_");
    str += "encoding  utf-8\n";
    str += "\nsurvey " + name + " -title \"" + caveObj.name + "\"";
    str += "\n\ncenterline";
    str += "\ncs long-lat";
    if (!isNaN(caveObj.northdeclination) && caveObj.northdeclination !== "")
    {
        str += "\ndeclination " + caveObj.northdeclination + " deg";
    }

    str += "\nfix " + caveObj.startPoint + " " + caveObj.longitude + " " + caveObj.latitude + " " + caveObj.altitude + "\n";

//    str += "\ndata normal from to length compass clino left up down right \n";
    var strheader = "data normal from to length compass clino";
    var oldheader = "";
    var extend = "";
    for (var i in caveObj.data)
    {
        
        var item = caveObj.data[i];
        if ((item['from'] === undefined || item['from'] === null || item['from'] === "") && (item['to'] === null || item['to'] === ""))
        {
            //m
            if (item['len'] === "m")
            {
                unit_length = 1;
            }
            if (item['top'] === "m")
            {
                unit_top = 1;
            }
            if (item['left'] === "m")
            {
                unit_left = 1;
            }
            if (item['right'] === "m")
            {
                unit_right = 1;
            }
            if (item['bottom'] === "m")
            {
                unit_bottom = 1;
            }
            //cm
            if (item['len'] === "cm")
            {
                unit_length = 0.01;
            }
            if (item['top'] === "cm")
            {
                unit_top = 0.01;
            }
            if (item['left'] === "cm")
            {
                unit_left = 0.01;
            }
            if (item['right'] === "cm")
            {
                unit_right = 0.01;
            }
            if (item['bottom'] === "cm")
            {
                unit_bottom = 0.01;
            }
            //dm
            if (item['len'] === "dm")
            {
                unit_length = 0.1;
            }
            if (item['top'] === "dm")
            {
                unit_top = 0.1;
            }
            if (item['left'] === "dm")
            {
                unit_left = 0.1;
            }
            if (item['right'] === "dm")
            {
                unit_right = 0.1;
            }
            if (item['bottom'] === "dm")
            {
                unit_bottom = 0.1;
            }
            //ft 0.3048
            if (item['len'] === "ft")
            {
                unit_length = 0.3048;
            }
            if (item['top'] === "ft")
            {
                unit_top = 0.3048;
            }
            if (item['left'] === "ft")
            {
                unit_left = 0.3048;
            }
            if (item['right'] === "ft")
            {
                unit_right = 0.3048;
            }
            if (item['bottom'] === "ft")
            {
                unit_bottom = 0.3048;
            }
            //dm
            if (item['len'] === "ft")
            {
                unit_length = 0.3048;
            }
            if (item['top'] === "ft")
            {
                unit_top = 0.3048;
            }
            if (item['left'] === "ft")
            {
                unit_left = 0.3048;
            }
            if (item['right'] === "ft")
            {
                unit_right = 0.3048;
            }
            if (item['bottom'] === "ft")
            {
                unit_bottom = 0.3048;
            }

            if (item['clino'] === "rad")
            {
                unit_clino = 180 / Math.PI;
            }
            if (item['compass'] === "rad")
            {
                unit_compass = 180 / Math.PI;
            }
            if (item['clino'] === "deg")
            {
                unit_clino = 1;
            }
            if (item['compass'] === "deg")
            {
                unit_compass = 1;
            }
        }
        if (item['len'] !== undefined && item['len'] !== null && item['from'] !== undefined && item['from'] !== null && item['from'] !== "")
        {
            strheader = "data normal from to length compass clino";
            if (item['top'] !== undefined && item['top'] !== "" && item['top'] !== null)
            {
                strheader += " up";
            }
            if (item['bottom'] !== undefined && item['bottom'] !== "" && item['bottom'] !== null)
            {
                strheader += " down";
            }
            if (item['left'] !== undefined && item['left'] !== "" && item['left'] !== null)
            {
                strheader += " left";
            }
            if (item['right'] !== undefined && item['right'] !== "" && item['right'] !== null)
            {
                strheader += " right";
            }
            if (oldheader !== strheader)
            {
                oldheader = strheader;
                str += "\n";
                str += strheader;
            }
            if (item['r'] !== undefined && (item['r'] === "i" || item['r'] === "<") && extend !== "right")
            {
                extend = "left";
                str += "\nextend left";
            }
            else
            {
                if (extend !== "right")
                {
                    str += "\n\textend right";
                    extend = "right";
                }
            }
            //----lateral------------------------------------------------------>
            var to = item['to'];
            if (to === "")
                to = "-";
            if (item['from'] === to)
            {
                item['from'] = item['from'] + ".0";
            }
            if (!isNaN(item['len']) && !isNaN(item['compass']))
            {
                str += "\n\t" + item['from'] + " " + to + " " + (item['len'] * unit_length);
                str += " " + (item['compass'] * unit_compass) + " " + (item['clino'] * unit_clino);
            }
            //----top--->
            if (item['top'] !== undefined && item['top'] !== "" && item['top'] !== null)
            {
                str += " " + (item['top'] * unit_top);
            }
            //----top---<
            //----bottom--->
            if (item['bottom'] !== undefined && item['bottom'] !== "" && item['bottom'] !== null)
            {
                str += " " + (item['bottom'] * unit_bottom);
            }
            //----bottom---<
            //----left--->
            if (item['left'] !== undefined && item['left'] !== "" && item['left'] !== null)
            {
                str += " " + (item['left'] * unit_left);
            }
            //----left---<
            //----right--->
            if (item['right'] !== undefined && item['right'] !== "" && item['right'] !== null)
            {
                str += " " + (item['right'] * unit_right);
            }
            //----right---<
            //----lateral------------------------------------------------------<
        }
        //splx.print_r(caveObj.data[i]);
        if (item['note'] !== undefined && item['note'] !== "" && item['note'] !== null)
        {
            str += "\n#" + item['note'];
        }
    }


    str += "\nendcenterline";
    str += "\nendsurvey";
    str += "\n";

    var filename = $("#filenameopened").val();
    var tmp = filename.split(".");
    filename = filename.replace(/\\/g, '/').replace(/.*\//, "");
    filename = filename.replace(/(.*)\.(.*?)$/, "$1");
    filename = tmp[0] + ".th";
    //console.log(filename);
    Download.save(str, filename, "text/plain");

    str = "";
    str += "\nencoding  utf-8";
    str += "\nsource " + filename;
    str += "\n";
    str += "\nexport map -proj plan -o " + filename + "-" + _i18n("plan") + ".pdf";
    str += "\n";
    str += "\nexport model  -o " + filename + ".kml";
    str += "\n";
    str += "\nexport model  -o " + filename + ".lox  -enable walls -wall-source all";
    str += "\n";
    str += "\nexport model  -o " + filename + ".dxf";
    str += "\n";
    str += "\n";
    str += "\n";
    str += "\n";
    str += "\n";
    str += "\n";
    str += "\n";
    str += "\n";
    str += "\n";
    str += "\n";
    str += "\n";
    var filename = "thconfig";
    Download.save(str, filename, "text/plain");
}


