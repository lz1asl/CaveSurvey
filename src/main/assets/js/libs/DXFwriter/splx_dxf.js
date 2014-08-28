/**
 * 
 * DXF Library
 * @author Alessandro Vernassa
 */
/**
 * 
 * @returns {DXFLayer}
 */
function DXFLayer()
{
    this.name = "0";
    this.color = "0";
    this.linetype = "CONTINUOUS";
}
/**
 * 
 * @returns {DXF}
 */
function DXF() {
    this.strDXF = "";
    this.layers = new Array();
    this.ShapeString = "";
    this.center_x = 0;
    this.center_y = 0;
    /**
     * 
     * @param {type} x
     * @param {type} y
     * @returns {undefined}
     */
    this.setViewportCenter = function(x, y)
    {
        try {
            this.center_x = x;
            this.center_y = y;
        }
        catch (e) {
            this.center_x = 0;
            this.center_y = 0;
        }
    };

    /**
     * 
     * @param {type} name
     * @param {type} color
     * @param {type} linetype
     * @returns {undefined}
     */
    this.addLayer = function(name, color, linetype)
    {
        name = (typeof (name) !== 'undefined') ? name : "0";
        color = (typeof (color) !== 'undefined') ? color : "0";
        linetype = (typeof (linetype) !== 'undefined') ? linetype : "CONTINUOUS";
        var tmp = new DXFLayer();
        tmp.name = name;
        tmp.color = color;
        tmp.linetype = linetype;
        this.layers.push(tmp);
    };
    /**
     * 
     * @returns {String}
     */
    this.getViewPortString = function()
    {
        //http://www.autodesk.com/techpubs/autocad/acad2000/dxf/vport_dxf_04.htm
        var strDXF = "";
        strDXF += "VPORT";
        strDXF += "\n70";
        strDXF += "\n1";
        strDXF += "\n0";
        strDXF += "\nVPORT";
        strDXF += "\n2";  //Viewport name
        strDXF += "\n*ACTIVE";
        strDXF += "\n70"; //Standard flag values (bit-coded values):
        strDXF += "\n0";
        strDXF += "\n10";  //Lower-left corner of viewport |DXF: X value; APP: 2D point
        strDXF += "\n0.0";
        strDXF += "\n20";  //DXF: Y value of lower-left corner of viewport
        strDXF += "\n0.0";
        strDXF += "\n 11"; //Upper-right corner of viewport |DXF: X value; APP: 2D point
        strDXF += "\n1.0";
        strDXF += "\n 21"; //DXF: Y value of upper-right corner of viewport 
        strDXF += "\n1.0";
        strDXF += "\n 12";//View center point (in DCS)|DXF: X value; APP: 2D point
        strDXF += "\n" + this.center_x; 
        strDXF += "\n 22" ; //DXF: Y value of view center point (in DCS)
        strDXF += "\n" + this.center_y;
        strDXF += "\n 13"; //Snap base point | DXF: X value; APP: 2D point
        strDXF += "\n0.0";
        strDXF += "\n 23"; //DXF: Y value of snap base point
        strDXF += "\n0.0";
        strDXF += "\n 14"; //Snap spacing X and Y
        strDXF += "\n0.5";
        strDXF += "\n 24"; //DXF: Y value of snap spacing X and Y
        strDXF += "\n0.5";
        strDXF += "\n 15"; //Grid spacing X and Y
        strDXF += "\n1.0";
        strDXF += "\n 25"; //DXF: Y value of grid spacing X and Y
        strDXF += "\n1.0";
        strDXF += "\n 16"; //View direction from target point (in WCS) DXF: X value; APP: 3D point
        strDXF += "\n0.0";
        strDXF += "\n 26"; //DXF: Y and Z values of view target point (in WCS)  --->
        strDXF += "\n0.0";
        strDXF += "\n 36";
        strDXF += "\n1.0";
        strDXF += "\n 17";
        strDXF += "\n0.0";
        strDXF += "\n 27";
        strDXF += "\n0.0";
        strDXF += "\n 37"; //DXF: Y and Z values of view target point (in WCS)  ---<
        strDXF += "\n0.0";
        strDXF += "\n 40";  //View height
        strDXF += "\n66.81";
        strDXF += "\n 41";  //Viewport aspect ratio
        strDXF += "\n1.656427758817";
        strDXF += "\n 42"; //Lens length
        strDXF += "\n50.0";
        strDXF += "\n 43"; //Front clipping plane (offset from target point)
        strDXF += "\n0.0";
        strDXF += "\n 44"; //Back clipping plane (offset from target point)
        strDXF += "\n0.0";
        strDXF += "\n 50";  //Snap rotation angle
        strDXF += "\n0.0";
        strDXF += "\n 51"; //View twist angle
        strDXF += "\n0.0";
        strDXF += "\n 71"; //View mode
        strDXF += "\n 16";
        strDXF += "\n 72"; //Circle zoom percent
        strDXF += "\n 50";
        strDXF += "\n 73"; //Fast zoom setting
        strDXF += "\n  1";
        strDXF += "\n 74"; //UCSICON setting
        strDXF += "\n  3";
        strDXF += "\n 75";  //Snap on/off
        strDXF += "\n  0";
        strDXF += "\n 76";  //Grid on/off
        strDXF += "\n  1";
        strDXF += "\n 77";  //Snap style
        strDXF += "\n  0";
        strDXF += "\n 78";  //Snap isopair
        strDXF += "\n  0";
        strDXF += "\n  0";
        strDXF += "\nENDTAB";
        strDXF += "\n  0";
        /* strDXF += "\nTABLE";
         strDXF += "\n  2";
         strDXF += "\nLTYPE";
         strDXF += "\n 70";
         strDXF += "\n     1";
         strDXF += "\n  0";
         strDXF += "\nLTYPE";
         strDXF += "\n  2";
         strDXF += "\nCONTINUOUS";
         strDXF += "\n 70";
         strDXF += "\n     0";
         strDXF += "\n  3";
         strDXF += "\nSolid line";
         strDXF += "\n 72";
         strDXF += "\n    65";
         strDXF += "\n 73";
         strDXF += "\n     0";
         strDXF += "\n 40";
         strDXF += "\n0.0";
         strDXF += "\n  0";
         strDXF += "\nENDTAB";
         strDXF += "\n  0";*/
        return strDXF;
    };
    /**
     * 
     * @returns {String}
     */
    this.getString = function()
    {
        var strDXF = "";
        strDXF += this.getHeaderString();
        strDXF += this.getBodyString();
        return strDXF;
    };
    /**
     * 
     * @returns {String}
     */
    this.getHeaderString = function()
    {
        var strDXF = "";
        strDXF += "0\nSECTION\n  2\nHEADER\n  9\n$ACADVER\n  1\nAC1006\n  0\nENDSEC\n  0\n";
        //layers:
        strDXF += "SECTION\n  2\nTABLES\n  0\nTABLE\n2\n";
        strDXF += this.getViewPortString();
        strDXF += "\nTABLE\n2\n";
        strDXF += this.getLayersString();
        strDXF += "ENDTAB\n 0\nENDSEC\n";
        return strDXF;
    };
    /**
     * 
     * @returns {String}
     */
    this.getBodyString = function() {
        var strDXF = "";
        strDXF += "0\nSECTION\n2\nENTITIES\n0\n";
        strDXF += this.getShapeString();
        strDXF += "ENDSEC\n0\nEOF\n";
        return strDXF;
    };
    /**
     * 
     * @returns {String}
     */
    this.getLayersString = function()
    {
        var strDXF = "";
        strDXF += "LAYER\n  0\n";
        var count = 1;
        for (var i in this.layers)
        {
            var layer = this.layers[i];
            strDXF += "LAYER\n 2\n" + layer.name + "\n 70\n 64\n 62\n " + layer.color + "\n 6\n" + layer.linetype + "\n 0\n";
            count++;
        }
        return strDXF;
    };
    /**
     * 
     * @returns {unresolved}
     */
    this.getShapeString = function() {
        return this.ShapeString;
    };
    /**
     * 
     * @param {type} x
     * @param {type} y
     * @param {type} z
     * @param {type} x2
     * @param {type} y2
     * @param {type} z2
     * @param {type} layerName
     * @param {type} color
     * @returns {String}
     */
    this.addLine = function(x, y, z, x2, y2, z2, layerName, color)
    {
        layerName = (typeof (layerName) !== 'undefined') ? layerName : "0";
        color = (typeof (color) !== 'undefined') ? color : "8";

        z = (typeof (z) !== undefined) ? z : "0";
        var str = "LINE\n" +
                "8" + "\n" +
                layerName + "\n" +
                "10" + "\n" +
                x + "\n" +
                "20" + "\n" +
                y + "\n" +
                "30" + "\n" +
                z + "\n" +
                "11" + "\n" +
                x2 + "\n" +
                "21" + "\n" +
                y2 + "\n" +
                "31" + "\n" +
                z2 + "\n0\n";
        this.ShapeString += str;
        return str;
    };
    /**
     * 
     * @param {type} x
     * @param {type} y
     * @param {type} z
     * @param {type} text
     * @param {type} size
     * @param {type} layerName
     * @param {type} color
     * @returns {String}
     */
    this.addText = function(x, y, z, text, size, layerName, color) {
        size = (typeof (size) !== 'undefined') ? size : 1;
        text = (typeof (text) !== 'undefined') ? text : "";
        layerName = (typeof (layerName) !== 'undefined') ? layerName : "0";
        color = (typeof (color) !== 'undefined') ? color : "8";
        var str = "TEXT\n" +
                "8" + "\n" +
                layerName + "\n" +
                " 10\n" +
                x + "\n" +
                " 20\n" +
                y + "\n" +
                " 30\n" +
                z + "\n" +
                " 40\n" +
                size + "\n" +
                "  1\n" +
                text + "\n" +
                "  0\n";
        this.ShapeString += str;
        return str;
    };

    this.addCircle = function(x, y, z, radius, layerName, color)
    {
        /*CIRCLE
         8
         PUNTI
         10
         3.99
         20
         2.80
         30
         0.00
         40
         0.2
         0*/

        layerName = (typeof (layerName) !== 'undefined') ? layerName : "0";
        color = (typeof (color) !== 'undefined') ? color : "8";

        z = (typeof (z) !== undefined) ? z : "0";
        var str = "CIRCLE\n" +
                "8" + "\n" +
                layerName + "\n" +
                "10" + "\n" +
                x + "\n" +
                "20" + "\n" +
                y + "\n" +
                "30" + "\n" +
                z + "\n" +
                "40" + "\n" +
                radius + "\n" +
                "0\n";
        this.ShapeString += str;
        return str;
    };
}
