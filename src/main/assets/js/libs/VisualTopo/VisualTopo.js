/*
 Version 5.02
 
 Trou ISO12,489.327,4931.100,239,UTM32
 Entree 0
 Couleur 0,0,0
 
 Param Deca Deg Clino Deg 0.0000 Dir,Dir,Dir Inc 0,0,0
 
 0          0                         0.00    0.00    0.00   0.35   0.00   0.00   0.35 N I
 0          1                         5.42  225.00  -32.00   0.50   0.00   3.90   0.00 N I
 1          2                         1.58  352.00  -11.00   0.20   0.20   2.80   0.25 N I
 2          3                         2.27   18.50    2.00   0.35   0.35   1.10   0.40 N I
 3          4                         2.54  340.00   -5.00   0.30   0.30   1.00   0.30 N I
 4          5                         4.38  351.00    1.00      *      *      *      * N I
 5          6                         3.25   12.00    9.00      *      *      *      * N I
 6          7                         3.55  305.50    3.00      *      *      *      * N I
 7          8                         0.80    0.00   90.00      *      *      *      * N I
 8          9                         4.70   28.00  -13.00      *      *      *      * N I
 9          10                        1.85  347.00  -12.00      *      *      *      * N I
 10         11                        2.30   14.50   13.00      *      *      *      * N I
 11         12                        6.54  304.00  -32.00      *      *      *      * N I
 12         13                        3.80  280.00  -19.50      *      *      *      * N I
 13         14                        2.15  337.00  -20.00      *      *      *      * N I
 14         15                        4.20  310.00  -10.00      *      *      *      * N I
 15         16                       10.30  218.00   17.00      *      *      *      * N I
 16         17                        7.22  211.50  -18.00      *      *      *      * N I
 17         18                        4.15  282.00   54.00      *      *      *      * N I
 18         19                        5.00  211.00    5.00      *      *      *      * N I
 19         20                       10.40  213.00    4.00      *      *      *      * N I
 20         21                       13.85  211.00    0.00      *      *      *      * N I
 21         22                        5.50  188.00   18.00      *      *      *      * N I
 22         23                       14.07  208.00    1.00      *      *      *      * N I
 23         24                        3.95  255.00   34.00      *      *      *      * N I
 24         25                       11.82  291.50   -8.00      *      *      *      * N I
 25         27                        7.20  185.00   -2.00      *      *      *      * N I
 26         28                        4.95  235.00   -3.00      *      *      *      * N I
 27         29                        3.90   11.00   12.00      *      *      *      * N I
 28         30                       10.20  359.00    6.00      *      *      *      * N I
 29         31                        9.20  260.50   -1.00      *      *      *      * N I
 30         32                        9.60  328.50   -5.00      *      *      *      * N I
 31         32                       14.05  264.00  -28.50      *      *      *      * N I
 11         11a                       3.80   80.00   18.00      *      *      *      * N I
 11         11b                       6.50   20.00   42.00      *      *      *      * N I
 24         24.1                      4.26  311.00   -5.00      *      *      *      * N I
 24         24.2                      4.75   43.00  -13.00      *      *      *      * N I
 
 [Configuration 5.02]
 
 Visual Topo=0,1,-1,-1,0,0,135,193,1215,839
 Options=1,1
 Calcul=0,1,-1,-1,0,0,61,44,1255,409
 Options=11,1,0,0,1
 ExportDxf=0,100,391,0,0,1,6,7,4,4,3,3,2,7,9
 Colonnes=8.00,8.00,8.00,8.00,8.00,8.00,8.00,8.00,8.00,8.00,8.00,2.00,2.00,4.00,0.38,8.00,8.00,8.00,8.00,8.00,0.38,0.00,0.00
 */

/*
 Param Topof 0.990 Deg Clino Deg 1.0300 Dir,Dir,Dir Inc Std
 
 1_0        1_0              0.0       0.0    0.00    0.00   0.60   0.20      *      * N I
 1_0        1_1          88737.0   88891.0  340.00  -55.00   0.60   0.10   1.00      * N I
 1_1        1_2          88897.0   88994.0  283.00   -5.00   0.90   0.30   0.70      * N I ;Puits de 6 mètres;D:\Programmes\Topo\Exemple1.jpg
 1_2        1_3          89012.0   89645.0  240.00  -85.00   0.50   0.30   1.50      * N I
 1_3        1_4          89861.0   90381.0  330.00  -45.00   0.40   0.50   2.00      * N I
 1_3        1_3a         89654.0   89853.0  163.00   30.00   0.30   0.20   1.50      * N I
 1_4        1_5          90384.0   90640.0  350.00    5.00   3.50   0.50   2.50      * N I
 */

function ops_GetCaveObjectByVisualtopo(str)
{
    var lastpoint = "";


    //Proj4js.defs["WGS84"] = "+proj=latlong +ellps=WGS84 +datum=WGS84 +no_defs";//wgs84
    Proj4js.defs["UTM32"] = "+proj=utm +zone=32 +ellps=WGS84 +datum=WGS84 +no_defs";//wgs84
    Proj4js.defs["UTM33"] = "+proj=utm +zone=33 +ellps=WGS84 +datum=WGS84 +no_defs";//wgs84
    Proj4js.defs["UTM32E"] = "+proj=utm +zone=32 +ellps=intl +towgs84=-87,-98,-121,0,0,0,0 +units=m +no_defs"; //utm ed50
    Proj4js.defs["UTM33E"] = "+proj=utm +zone=33 +ellps=intl +towgs84=-87,-98,-121,0,0,0,0 +units=m +no_defs";//utm ed50


    var cave = new CaveObject();
    var tmp = new Array();
    var ii = 0;
    var readok = false;
    var data = new Array();
    //fix \n ---->
    str = str.replace("\r\n", "\n");
    str = str.replace("\r", "\n");
    //fix \n ----<
    var rows = str.split("\n");
    var UClino = "";
    var ULenght = "";
    var UCompass = "";
    var isTopofilo = false;
    for (var i in rows)
    {
        var row = rows[i];
        row = row.replace("\n", "");
        row = row.replace("\r", "");
        var tmp_r = row.split(" ");


        if (tmp_r[0] === "Entree")
        {
            cave.startPoint = tmp_r[1];
            cave.geoPoint = tmp_r[1];
        }

        else

        if (tmp_r[0] === "Trou")
        {
            var CaveParams = row.split(",");
            cave.name = CaveParams[0].replace(/^Trou /, "");
            var source = new Proj4js.Proj('EPSG:4326');
            var dest = new Proj4js.Proj('EPSG:4326');
            cave.altitude = parseFloat(CaveParams[3]);
           
            //wgs84
            if (CaveParams[4] === "UTM32")
            {
                source = new Proj4js.Proj("UTM32");//wgs84 UTM 32T  su pamb
            }
            else if (CaveParams[4] === "UTM33")
            {
                source = new Proj4js.Proj("UTM33");//wgs84 UTM 33T su pamb
            }
            //ED50
            else if (CaveParams[4] === "UTM32E")
            {
                //alert("UTM32E");
                source = new Proj4js.Proj("UTM32E");//UTM 32N  su pamb
            }
            else if (CaveParams[4] === "UTM33E")
            {
                source = new Proj4js.Proj("UTM33E");//UTM 33N su pamb
            }
            else
            {
                source = new Proj4js.Proj("UTM32");//wgs84 UTM 32T  su pamb
            }


            var pIng = new Proj4js.Point(parseFloat(CaveParams[1]) * 1000.0, parseFloat(CaveParams[2]) * 1000, parseFloat(cave.altitude));   //any object will do as long as it has 'x' and 'y' properties
            //console.log(pIng);
            Proj4js.transform(source, dest, pIng);      //do the transformation.  x and y are modified in place   
            //console.log(pIng);
            // alert(dest.x);
            //splx.print_r(pIng);
            // splx.print_r(pIng);
            cave.longitude = pIng.x;
            cave.latitude = pIng.y;


            //splx.print_r(cave);
        }

        if (tmp_r[0] === "Param")
        {
            //splx.print_r(tmp_r);
            if (tmp_r[1] === "Topof")
            {
                isTopofilo = true;
            }
            else if (tmp_r[1] === "Deca")
            {
                isTopofilo = false;
            }

            readok = false;
            UClino = "";
            ULenght = "";
            UCompass = "";
            UDecl = "";
            //Param Deca Deg Clino Deg 0.0000 Dir,Dir,Dir Inc 0,0,0
            if (tmp_r[1] === "Deca")
            {
                ULenght = "Deca";
            }
            if (tmp_r[2] === "Deg")
            {
                UCompass = "Deca";
                UDecl = tmp_r[3];
            }
            if (tmp_r[4] === "Dir,Dir,Dir")
            {

            }
            readok = true;
            continue;
        }

        if (readok)
        {
            var from = 0;
            var to = 0;
            var len = 0;
            var compass = 0;
            var clino = 0;
            var top = 0;
            var left = 0;
            var right = 0;
            var bottom = 0;
            var r = 0;


            while (row.search("  ") !== -1) {
                row = row.replace("  ", " ");
            }
            var tmp_r = row.split(" ");
            //splx.print_r(tmp_r);
            if (tmp_r[8] !== undefined)
            {
                from = (tmp_r[0] === "*") ? lastpoint : tmp_r[0];
                to = tmp_r[1];
                if (isTopofilo)
                {
                    len = (parseFloat(tmp_r[3]) - parseFloat(tmp_r[2])) / 100.0;
                    compass = tmp_r[4];
                    clino = tmp_r[5];
                    left = (tmp_r[6] === "*") ? "" : tmp_r[6];
                    right = (tmp_r[7] === "*") ? "" : tmp_r[7];
                    top = (tmp_r[8] === "*") ? "" : tmp_r[8];
                    bottom = (tmp_r[9] === "*") ? "" : tmp_r[9];
                    r = (tmp_r[10] === "N") ? "" : "<";
                }
                else
                {
                    len = parseFloat(tmp_r[2]);
                    compass = tmp_r[3];
                    clino = tmp_r[4];
                    left = (tmp_r[5] === "*") ? "" : tmp_r[5];
                    right = (tmp_r[6] === "*") ? "" : tmp_r[6];
                    top = (tmp_r[7] === "*") ? "" : tmp_r[7];
                    bottom = (tmp_r[8] === "*") ? "" : tmp_r[8];
                    r = (tmp_r[9] === "N") ? "" : "<";
                }
                tmp_datarow = {
                    from: from,
                    to: to,
                    len: len,
                    compass: compass,
                    clino: clino,
                    top: top,
                    left: left,
                    right: right,
                    bottom: bottom,
                    r: r
                };

                data.push(tmp_datarow);
                lastpoint = tmp_datarow.to;

            }
        }
    }
    cave.data = data;
    console.log(cave);

    return cave;
}