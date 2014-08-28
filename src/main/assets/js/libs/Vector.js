function Vector(x, y, z)
{
    this.x = (typeof(x) !== 'undefined') ? parseFloat(x) : 0.0;
    this.y = (typeof(y) !== 'undefined') ? parseFloat(y) : 0.0;
    this.z = (typeof(z) !== 'undefined') ? parseFloat(z) : 0.0;
    this.Set = function(a)
    {
        this.x = a.x;
        this.y = a.y;
        this.z = a.z;
    };
    this.length = function()
    {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    };
    this.MaxDiff = function(b)
    {
        var dx = Math.abs(this.x - b.x);
        var dy = Math.abs(this.y - b.y);
        var dz = Math.abs(this.z - b.z);
        if (dx < dy) {
            dx = dy;
        }
        if (dx < dz) {
            dx = dz;
        }
        return dx;
    };
}
