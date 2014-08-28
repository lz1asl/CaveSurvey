/**
 * @author Alessandro Vernassa <speleoalex@gmail.com>
 * @copyright Copyright (c) 2013
 * @license http://opensource.org/licenses/gpl-license.php GNU General Public License
 */
/**
 * 
 * @param {type} divID
 * @global shape
 * @returns {ShapeCanvan}
 */
function ShapeCanvan(divID)
{
    this.divID = divID;
    this.zoom = 1;
    /**
     * 
     * @returns {undefined}
     */
    this.initByForm = function() {
        this.xRotation = DegToRad($("#" + this.divID + " input.xrotation").val());
        this.yRotation = DegToRad($("#" + this.divID + " input.yrotation").val());
        this.zRotation = DegToRad($("#" + this.divID + " input.zrotation").val());
        this.xPan = parseInt($("#" + this.divID + " input.xpan").val());
        this.yPan = parseInt($("#" + this.divID + " input.ypan").val());
        this.zoom = parseFloat($("#" + this.divID + " input.zoom").val());
        this.prosp = $("#" + this.divID + " input.prosp").val();
        this.layer_text = parseInt($("#" + this.divID + " input.layer_text").val());
        // this.zoom = -1;
    };
    /**
     * 
     * @param {type} shape
     * @returns {undefined}
     */
    this.render = function(shape) {
        this.initByForm();
        var distance = 200;
        var width = $("#" + this.divID + " canvas").width();
        var height = $("#" + this.divID + " canvas").height();
        distance = parseFloat(width) + parseFloat(height);
        var offset_x_shape = 0;
        var offset_y_shape = 0;
        var width_shape;
        var height_shape;
        var centercanvas_x = width / 2.0;
        var centercanvas_y = height / 2.0;
        var ctx = $("#" + this.divID + " canvas")[0].getContext('2d');
        var x, y, x2, y2;
        ctx.clearRect(0, 0, width, height);
        ctx.globalCompositeOperation = "lighter";
        //--------calcolo della dimensione------------------------------------->
        var max_x = null;
        var max_y = null;
        var min_x = null;
        var min_y = null;
        x = null;
        y = null;
        x2 = null;
        y2 = null;

        if (shape == undefined)
            return;
        for (var i = 0; shape.points[i] !== undefined && shape.points[i].x !== undefined; i++) {
            var p = new Point3D();
            p.x = (shape.points[i].x);
            p.y = (shape.points[i].y);
            p.z = (shape.points[i].z);
            if (p.y2 !== undefined) {
                p.x2 = (shape.points[i].x2);
                p.y2 = (shape.points[i].y2);
                p.z2 = (shape.points[i].z2);
            }
            rotateXYZ(p, this.xRotation, this.yRotation, this.zRotation);
            if (parseInt(this.prosp) === 1)
            {
                x = projection(p.x, p.z, centercanvas_x, width, distance);
                y = projection(p.y, p.z, centercanvas_y, width, distance);
                if (p.y2 !== undefined) {
                    x2 = projection(p.x2, p.z2, centercanvas_x, width, distance);
                    y2 = projection(p.y2, p.z2, centercanvas_y, width, distance);
                }
            }
            else
            {
                x = (p.x);
                y = (p.y);
                if (p.y2 !== undefined) //linea
                {
                    x2 = (p.x2);
                    y2 = (p.y2);
                }
            }
            if (min_x === null)
                min_x = x;
            if (min_y === null)
                min_y = y;
            max_x = Math.max(max_x, x);
            max_y = Math.max(max_y, y);
            min_x = Math.min(min_x, x);
            min_y = Math.min(min_y, y);
            if (p.y2 !== undefined) {
                max_x = Math.max(max_x, x2);
                max_y = Math.max(max_y, y2);
                min_x = Math.min(min_x, x2);
                min_y = Math.min(min_y, y2);
            }
        }
        width_shape = max_x - min_x;
        height_shape = max_y - min_y;
        //--------calcolo della dimensione-------------------------------------<      
        if (this.zoom === -1)
        {
            this.zoom = Math.min((width / width_shape), (height / height_shape));
            $("#" + this.divID + " input.zoom").val(this.zoom);
            $("#" + this.divID + " input.xpan").val(0);
            $("#" + this.divID + " input.ypan").val(0);
            this.xPan = 0;
            this.yPan = 0;
        }
        offset_x_shape = (width_shape / 2) - (0 - min_x);
        offset_y_shape = (height_shape / 2) - (0 - min_y);

        for (var i = 0; shape.points[i] !== undefined && shape.points[i].x !== undefined; i++) {
            var p = new Point3D();
            p.x = (shape.points[i].x);
            p.y = (shape.points[i].y);
            p.z = (shape.points[i].z);
            p.text = shape.points[i].text;
            p.type = shape.points[i].type;
            p.color = shape.points[i].color;
            p.layer = shape.points[i].layer;
            p.radius = shape.points[i].radius;
            //console.log(this.layer_text);
            if (this.layer_text === 0 && p.layer === "stations")
            {
                continue;
            }
            if (shape.points[i].x2 !== undefined)
            {
                p.x2 = (shape.points[i].x2);
                p.y2 = (shape.points[i].y2);
                p.z2 = (shape.points[i].z2);
            }
            rotateXYZ(p, this.xRotation, this.yRotation, this.zRotation);
            if (parseInt(this.prosp) === 1)
            {
                x = projection(p.x, p.z, centercanvas_x, width, distance);
                y = projection(p.y, p.z, centercanvas_y, width, distance);
                if (p.y2 !== undefined) {
                    x2 = projection(p.x2, p.z2, centercanvas_x, width, distance);
                    y2 = projection(p.y2, p.z2, centercanvas_y, width, distance);
                }
            }
            else
            {
                x = (p.x);
                y = (p.y);
                if (p.y2 !== undefined) //linea
                {
                    x2 = (p.x2);
                    y2 = (p.y2);
                }
            }
            x = Math.floor((this.zoom * x) + this.xPan + centercanvas_x - (this.zoom * offset_x_shape));
            y = Math.floor((this.zoom * y) + this.yPan + centercanvas_y - (this.zoom * offset_y_shape));
            x2 = Math.floor((this.zoom * x2) + this.xPan + centercanvas_x - (this.zoom * offset_x_shape));
            y2 = Math.floor((this.zoom * y2) + this.yPan + centercanvas_y - (this.zoom * offset_y_shape));
            switch (p.type)
            {
                case "line":
                    drawLine(ctx, x, y, x2, y2, p.color);
                    break;
                case "text":
                    drawText(ctx, x, y, p.text, p.color);
                    break;
                case "circle":
                    drawCircle(ctx, x, y, this.zoom * p.radius, p.color);
                    break;
                case "3dpoint":
                    drawPointWithGradient(ctx, x, y, 5, p.color, 0.5);
                    break;
            }
        }
        ctx.restore();
        //--------x,y,z ----->
        ctx.fillStyle = "rgb(150,150,150)";
        ctx.fillText("x =" + Math.floor(RadToDeg(this.xRotation)) + "°, y=" + Math.floor(RadToDeg(this.yRotation)) + "°, z=" + Math.floor(RadToDeg(this.zRotation)) + "°" /*+" zoom="+this.zoom*/, 0, 10);
        //--------x,y,z -----<
        //-----scala--------->
        ctx.fillStyle = "#000000";
        ctx.fillText("0", 5, height - 10);
        var max = 50;
        var step = 10;
        if (this.zoom < 1)
        {
            max = 100;        
            step = 10;
        }
        if (this.zoom < 0.1)
        {
            max = 1000;        
            step = 100;
        }
        if (this.zoom > 5)
        {
            max = 10;        
            step = 1;
        }
        drawLine(ctx, 10, height - 5, 10 + Math.floor(max * parseFloat(this.zoom)), height - 5, "#000000");
        for (var i = 0; i <= max; i += step)
        {
            drawLine(ctx, Math.floor(10 + (i * parseFloat(this.zoom))), height - 5, 10 + Math.floor(i * parseFloat(this.zoom)), height - 2, "#000000");
        }
        drawLine(ctx, 10, height - 2, 10 + Math.floor(max * parseFloat(this.zoom)), height - 2, "#000000");

        ctx.fillText(max + "m", Math.floor(max * parseFloat(this.zoom)), height - 10);

        //-----scala---------<
        //ucs ----------------------------->
        var ucs_xoffset = 30;
        var ucs_yoffset = 40;
        var ucs_size = 20;

        var ucspoint = new Point3D();
        //X
        ucspoint.x = 0;
        ucspoint.y = 0;
        ucspoint.z = 0;
        ucspoint.x2 = ucs_size;
        ucspoint.y2 = 0;
        ucspoint.z2 = 0;
        ucspoint.text = "x";
        rotateXYZ(ucspoint, this.xRotation, this.yRotation, this.zRotation);
        ucspoint.x += ucs_xoffset;
        ucspoint.y += ucs_yoffset;
        ucspoint.x2 += ucs_xoffset;
        ucspoint.y2 += ucs_yoffset;
        drawLine(ctx, ucspoint.x, ucspoint.y, ucspoint.x2, ucspoint.y2, "rgba(255,0,0,100)");
        drawText(ctx, ucspoint.x2, ucspoint.y2, ucspoint.text, "rgba(255,0,0,100)");
        //Y
        ucspoint.x = 0;
        ucspoint.y = 0;
        ucspoint.z = 0;
        ucspoint.x2 = 0;
        ucspoint.y2 = 1 - ucs_size;
        ucspoint.z2 = 0;
        ucspoint.text = "y";
        rotateXYZ(ucspoint, this.xRotation, this.yRotation, this.zRotation);
        ucspoint.x += ucs_xoffset;
        ucspoint.y += ucs_yoffset;
        ucspoint.x2 += ucs_xoffset;
        ucspoint.y2 += ucs_yoffset;
        drawLine(ctx, ucspoint.x, ucspoint.y, ucspoint.x2, ucspoint.y2, "rgba(0,255,0,100)");
        drawText(ctx, ucspoint.x2, ucspoint.y2, ucspoint.text, "rgba(0,255,0,100)");
        //Z
        ucspoint.x = 0;
        ucspoint.y = 0;
        ucspoint.z = 0;
        ucspoint.x2 = 0;
        ucspoint.y2 = 0;
        ucspoint.z2 = ucs_size;
        ucspoint.text = "z";
        rotateXYZ(ucspoint, this.xRotation, this.yRotation, this.zRotation);
        ucspoint.x += ucs_xoffset;
        ucspoint.y += ucs_yoffset;
        ucspoint.x2 += ucs_xoffset;
        ucspoint.y2 += ucs_yoffset;
        drawLine(ctx, ucspoint.x, ucspoint.y, ucspoint.x2, ucspoint.y2, "rgba(0,0,255,100)");
        drawText(ctx, ucspoint.x2, ucspoint.y2, ucspoint.text, "rgba(0,0,255,100)");
        //ucs -----------------------------<

    };
    return this;
}


