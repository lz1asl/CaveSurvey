/**
 * 
 * DXF Library
 * @author Alessandro Vernassa
 */
/**
 * 
 * @returns {DXFLayer}
 */
function SVGLayer()
{
    this.name = "0";
    this.color = "0";
    this.linetype = "CONTINUOUS";
}
/**
 * 
 * @returns {DXF}
 */
function SVG() {
    this.strSVG = "";
    this.layers = new Array();
    this.ShapeString = new Array();
    /**
     * 
     * @param {type} name
     * @param {type} color
     * @param {type} linetype
     * @returns {undefined}
     */
    this.addLayer = function(name, color, linetype)
    {
        name = (typeof(name) !== 'undefined') ? name : "0";
        color = (typeof(color) !== 'undefined') ? color : "0";
        linetype = (typeof(linetype) !== 'undefined') ? linetype : "CONTINUOUS";
        var tmp = new DXFLayer();
        tmp.name = name;
        tmp.color = color;
        tmp.linetype = linetype;
        this.layers.push(tmp);
        if (this.ShapeString[name] === undefined)
        {
            this.ShapeString[name] = new Array();
        }
    };
    /**
     * 
     * @returns {String}
     */
    this.getString = function()
    {
        var strSVG = "";
        strSVG += this.getHeaderString();
        strSVG += this.getBodyString();
        return strSVG;
    };
    /**
     * 
     * @returns {String}
     */
    this.getHeaderString = function()
    {
        var strSVG = "";
        strSVG += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
        //layers:
        strSVG += "";
        //strSVG += this.getLayersString();
        strSVG += "";
        return strSVG;
    };
    /**
     * 
     * @returns {String}
     */
    this.getBodyString = function() {
        var strSVG = "";
        strSVG += "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">";
        strSVG += "\n<g id=\"layermain\" style=\"display:inline\">";
        for (var i in this.layers)
        {
            strSVG += "\n<g id=\"layer" + i + "\" style=\"display:inline\">";
            strSVG += this.getShapeString(this.layers[i].name);
            strSVG += "\n</g>";
        }
        strSVG += "\n</g>";
        strSVG += "</svg> ";
        return strSVG;
    };
    /**
     * 
     * @returns {String}
     */
    this.getLayersString = function()
    {
        var strSVG = "";
        strSVG += "";
        var count = 1;
        for (var i in this.layers)
        {
            var layer = this.layers[i];
            // strSVG += "LAYER\n 2\n" + layer.name + "\n 70\n 64\n 62\n " + layer.color + "\n 6\n" + layer.linetype + "\n 0\n";
            count++;
        }
        return strSVG;
    };
    /**
     * 
     * @returns {unresolved}
     */
    this.getShapeString = function(layer) {
        return this.ShapeString[layer];
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
        layerName = (typeof(layerName) !== 'undefined') ? layerName : "0";
        color = (typeof(color) !== 'undefined') ? color : "rgb(0,0,0)";

        /*<line x1="0" y1="0" x2="200" y2="200"
         style="stroke:rgb(255,0,0);stroke-width:2"/>*/

        z = (typeof(z) !== undefined) ? z : "0";
        var str = "\n<line x1=\"" + x + "\" y1=\"" + y + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" style=\"stroke:" + color + ";stroke-width:.1\" />";
        if (this.ShapeString[layerName] === undefined)
        {
            this.ShapeString[layerName] = new Array();
        }
        this.ShapeString[layerName] += str;
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
        size = (typeof(size) !== 'undefined') ? size : 1;
        text = (typeof(text) !== 'undefined') ? text : "";
        layerName = (typeof(layerName) !== 'undefined') ? layerName : "0";
        color = (typeof(color) !== 'undefined') ? color : "rgb(0,0,0)";
        var str = "\n<text font-size=\"" + size + "\" fill=\"" + color + "\" x=\"" + x + "\" y=\"" + y + "\" >" + text + "</text>";
        if (this.ShapeString[layerName] === undefined)
        {
            this.ShapeString[layerName] = new Array();
        }
        this.ShapeString[layerName] += str;
        return str;
    };

    this.addCircle = function(x, y, z, radius, layerName, color)
    {
        layerName = (typeof(layerName) !== 'undefined') ? layerName : "0";
        color = (typeof(color) !== 'undefined') ? color : "rgb(0,0,0)";
        z = (typeof(z) !== undefined) ? z : "0";
        var str = "\n<circle cx=\"" + x + "\" cy=\"" + y + "\" r=\"" + radius + "\" style=\"stroke:" + color + ";stroke-width:1\" />";
        if (this.ShapeString[layerName] === undefined)
        {
            this.ShapeString[layerName] = new Array();
        }
        this.ShapeString[layerName] += str;
        return str;
    };
}




/**
 * 
 * @param {type} shape
 * @param {type} xRotation
 * @param {type} yRotation
 * @param {type} zRotation
 * @returns {@exp;svgObj@call;getString}
 */
function ops_ShapeToSVG(shape, xRotation, yRotation, zRotation) {
    xRotation = DegToRad(xRotation);
    yRotation = DegToRad(yRotation);
    zRotation = DegToRad(zRotation);

    var svgObj = new SVG();
    var zoom = 4.0; //10 significA 1 metro = 10px
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
        svgObj.addLayer(shapeLayers[i], countlayer++, "CONTINUOUS");
    }
    var x, y, z, x2, y2, z2;
    var x_max = 0, y_max = 0, z_max = 0, x_min = 0, y_min = 0, z_min = 0;
    //----------------------metto tutto positivo ----------------------------->
    var delta_x = 0;
    var delta_y = 0;
    for (var i = 0; shape.points[i] !== undefined && shape.points[i].x !== undefined; i++) {
        x = shape.points[i].x;
        y = shape.points[i].y;
        if (i === 0)
        {
            x_max = x;
            y_max = y;
            z_max = z;
            x_min = x;
            y_min = y;
            z_min = z;
        }

        if (shape.points[i].y2 !== undefined) //linea
        {
            x2 = shape.points[i].x2;
            y2 = shape.points[i].y2;
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
    }

    delta_x = Math.round(0 - x_min * zoom) + 10;
    delta_y = Math.round(y_max * zoom) + 10;

    console.log(y_min);
    console.log(x_min);

    //----------------------metto tutto positivo -----------------------------<
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
        switch (p.type)
        {
            case "line":
                svgObj.addLine(x, y, z, x2, y2, z2, p.layer, p.color);
                break;
            case "text":
                svgObj.addText(x, y, z, p.text, p.textsize * zoom, p.layer, p.color);
                break;
            case "circle":
                svgObj.addCircle(x, y, z, p.radius * zoom, p.layer, p.color);
                break;
            default:
                //svgObj.addText(x, y, z, p.text, 0.5*zoom, p.layer);
                break;
        }
    }

    //------scale and title ------>
    var LayerScale = _i18n("scale", "AA");
    var LayerTitle = _i18n("title", "AA");
    svgObj.addLayer(shapeLayers[i], countlayer++, LayerScale);
    svgObj.addLayer(shapeLayers[i], countlayer++, LayerTitle);
    y = delta_y + zoom * 10;
    x = delta_x + zoom * 1;
    z = 0;
    x2 = zoom * (x + 50.0);
    y2 = zoom * y;
    svgObj.addLine(x, y, z, x2, y, z2, LayerScale); //orizzontale alta
    svgObj.addLine(x, y - 2, z, x2, y - 2, z2, LayerScale); //orizzontale bassa
    svgObj.addLine(x, y - 1, z, x + 10.0, y - 1, z2, LayerScale); //orizzontale bassa corta

    for (var ii = 0; ii <= 50; ii += 10.0)
    {
        var i = ii * zoom;

        svgObj.addLine(x + i, y, z, x + i, y - 2, z2, LayerScale); // linee verticali a 10 metri
        svgObj.addText(x + i - 0.3, y - 4, z, i, 1, LayerScale);
    }
    for (var ii = 1; ii < 10; ii++)
    {
        var i = ii * zoom;
        svgObj.addLine(x + i, y - 1, z, x + i, y - 2, z2, LayerScale); // linee verticali a 1 metro
        //  svgObj.addText(x + i - 0.3, y - 4, z, i, 1, LayerScale);
    }
    svgObj.addText(delta_x, delta_y + zoom * 15, z, caveObj.name, zoom * 5, _i18n(LayerTitle, "AA"));
    //------scale and title ------<
    return svgObj.getString();
}

