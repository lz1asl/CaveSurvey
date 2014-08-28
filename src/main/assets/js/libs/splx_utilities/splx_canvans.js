
/**
 * 
 * @param {type} ctx
 * @param {type} x
 * @param {type} y
 * @param {type} size
 * @param {type} color
 */
function drawPoint(ctx, x, y, size, color) {
    ctx.save();
    ctx.beginPath();
    ctx.fillStyle = color;
    ctx.arc(x, y, size, 0, 2 * Math.PI, true);
    ctx.fill();
    ctx.restore();
}
/**
 * 
 * @param {type} ctx
 * @param {type} x
 * @param {type} y
 * @param {type} x2
 * @param {type} y2
 * @param {type} color
 * @returns {undefined}
 */
function drawLine(ctx, x, y, x2, y2, color) {
    if (ctx === undefined || x === undefined || y === undefined || x2 === undefined || y2 === undefined)
        return;
    ctx.save();
    ctx.beginPath();
    ctx.strokeStyle = color;
    ctx.moveTo(x, y);
    ctx.lineTo(x2, y2);
    ctx.stroke();
    ctx.restore();
}


function drawText(ctx, x, y, text, color) {
    color = (typeof(color) !== 'undefined') ? color : "#000000";
    text = (typeof(text) !== 'undefined') ? text : "";
    if (ctx === undefined || x === undefined || y === undefined || text === undefined || text === null || text.toString() === "")
        return;

    try {
        ctx.save();
        ctx.beginPath();
        ctx.fillStyle = color;
        ctx.fillText(text, x, y);
        ctx.fill();
        ctx.restore();
    }
    catch (e) {
    }
}


function drawCircle(ctx, x, y, radius, color) {
    color = (typeof(color) !== 'undefined') ? color : "#000000";
    radius = (typeof(radius) !== 'undefined') ? radius : 1;
    if (ctx === undefined || x === undefined || y === undefined || radius === undefined || radius === null )
        return;
    ctx.save();
    ctx.beginPath();
    ctx.fillStyle = color;
    ctx.strokeStyle = color;
    ctx.arc(x, y, radius, 0, 2.0 * Math.PI);
    ctx.stroke();
//    ctx.fill();
    ctx.restore();
}
/**
 * 
 * @param {type} ctx
 * @param {type} x
 * @param {type} y
 * @param {type} size
 * @param {type} color
 * @param {type} gradient
 * @returns {undefined}
 */
function drawPointWithGradient(ctx, x, y, size, color, gradient) {
    var reflection;
    reflection = size / 4;
    ctx.save();
    ctx.translate(x, y);
    var radgrad = ctx.createRadialGradient(-reflection, -reflection, reflection, 0, 0, size);
    radgrad.addColorStop(0, '#FFFFFF');
    radgrad.addColorStop(gradient, color);
    radgrad.addColorStop(1, 'rgba(1,159,98,0)');
    ctx.fillStyle = radgrad;
    ctx.fillRect(-size, -size, size * 2, size * 2);
    ctx.restore();

}