/**
 * @author Alessandro Vernassa <speleoalex@gmail.com>
 * @copyright Copyright (c) 2013
 * @license http://opensource.org/licenses/gpl-license.php GNU General Public License
 */
/**
 * 
 * @returns {Point3D}
 */
function Point3D() {
    this.x = 0;
    this.y = 0;
    this.z = 0;
    this.x2 = undefined;
    this.y2 = undefined;
    this.z2 = undefined;
    this.text = "";
    this.radius = 0.0;
    this.textsize = 0.5;
    this.type = "point";
}
/**
 * 
 * @returns {Shape3D}
 */
function Shape3D() {
    this.points = new Array();
    this.color = "rgb(100,0,255)";
    this.numberOfVertexes = 0;
}
/**
 * 
 * @param {type} point
 * @param {type} rotationX
 * @param {type} rotationY
 * @param {type} rotationZ
 * @returns {undefined}
 */
function rotateXYZ(point, rotationX, rotationY, rotationZ) {
    rotateX(point, rotationX);
    rotateY(point, rotationY);
    rotateZ(point, rotationZ);
}

/**
 * @param {Point3D} point
 * @param {float} radians
 */
function rotateX(point, radians) {
    var y = point.y;
    point.y = (y * Math.cos(radians)) + (point.z * Math.sin(radians) * -1.0);
    point.z = (y * Math.sin(radians)) + (point.z * Math.cos(radians));
    if (point.x2 !== undefined)
    {
        var y2 = point.y2;
        point.y2 = (y2 * Math.cos(radians)) + (point.z2 * Math.sin(radians) * -1.0);
        point.z2 = (y2 * Math.sin(radians)) + (point.z2 * Math.cos(radians));
    }
}

/**
 * @param {Point3D} point
 * @param {float} radians
 */
function rotateY(point, radians) {

    var z = point.z;
    point.z = (z * Math.cos(radians)) + (point.x * Math.sin(radians) * -1.0);
    point.x = (z * Math.sin(radians)) + (point.x * Math.cos(radians));

    if (point.y2 !== undefined)
    {
        //point.type = "line";
        var z2 = point.z2;
        point.z2 = (z2 * Math.cos(radians)) + (point.x2 * Math.sin(radians) * -1.0);
        point.x2 = (z2 * Math.sin(radians)) + (point.x2 * Math.cos(radians));
    }
}
/**
 * @param {Point3D} point
 * @param {float} radians
 */
function rotateZ(point, radians) {

    var x = point.x;
    point.x = (x * Math.cos(radians)) + (point.y * Math.sin(radians) * -1.0);
    point.y = (x * Math.sin(radians)) + (point.y * Math.cos(radians));
    if (point.x2 !== undefined)
    {
        var x2 = point.x2;
        point.x2 = (x2 * Math.cos(radians)) + (point.y2 * Math.sin(radians) * -1.0);
        point.y2 = (x2 * Math.sin(radians)) + (point.y2 * Math.cos(radians));
    }
}

/**
 * @param {Point3D} point
 * @param {float} radians
 */
function _rotateY(point, radians) {
    var x = point.x;
    point.x = (x * Math.cos(radians)) + (point.z * Math.sin(radians) * -1.0);
    point.z = (x * Math.sin(radians)) + (point.z * Math.cos(radians));

    if (point.x2 !== undefined)
    {
        //point.type = "line";
        var x2 = point.x2;
        point.x2 = (x2 * Math.cos(radians)) + (point.z2 * Math.sin(radians) * -1.0);
        point.z2 = (x2 * Math.sin(radians)) + (point.z2 * Math.cos(radians));
    }
}
/**
 * @param {Point3D} point
 * @param {float} radians
 */
function _rotateZ(point, radians) {
    var x = point.x;
    point.x = (x * Math.cos(radians)) + (point.y * Math.sin(radians) * -1.0);
    point.y = (x * Math.sin(radians)) + (point.y * Math.cos(radians));
    if (point.x2 !== undefined)
    {
        var x2 = point.x2;
        point.x2 = (x2 * Math.cos(radians)) + (point.y2 * Math.sin(radians) * -1.0);
        point.y2 = (x2 * Math.sin(radians)) + (point.y2 * Math.cos(radians));
    }
}


/**
 * 
 * @param {type} xy
 * @param {type} z
 * @param {type} xyOffset
 * @param {type} zOffset
 * @param {type} distance
 * @returns {unresolved}
 */
function projection(xy, z, xyOffset, zOffset, distance) {
    return -((distance * xy) / (z - zOffset)) + xyOffset;
}


function PolarToPoint3D(polarPoint) {
    var d = parseFloat(polarPoint['len']);// lunghezza
    var b = DegToRad(180.0 - (parseFloat(polarPoint['compass'])));//azimuth
    var c = DegToRad(parseFloat(polarPoint['clino']));//inclinazione
    var z = d * Math.sin(c);  //# vert.
    var h = d * Math.cos(c); //# horiz.
    var x = h * Math.sin(b); // # east
    var y = h * Math.cos(b); //# north
    var ret = new Point3D();
    ret.x = 0;
    ret.y = 0;
    ret.z = 0;
    ret.x2 = (x);
    ret.y2 = (y);
    ret.z2 = (z);
    ret.type = "line";
    return ret;
}

function Point3DToPolar(x, y, z) {
    var polarPoint = new Array();
    var radius = Math.sqrt((x * x) + (y * y) + (z * z));
    u = Math.atan(y, x) + Math.PI;
    v = Math.acos(z / radius);
    polarPoint['len'] = radius;
    polarPoint['clino'] = u;
    polarPoint['compass'] = v;
    return polarPoint;
}
/**
 * da gradi 0-2PI a gradi 0-360
 * @param {float} radians
 */
function RadToDeg(radians)
{
    return radians * (180 / Math.PI);
}
/**
 * da gradi 0-360 a gradi 0-2PI
 * @param {float} degrees
 */
function DegToRad(degrees)
{
    return degrees * (Math.PI / 180);
}


