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

/*
 
 
 public Vector TurnX( float s, float c )
 {
 return new Vector( x, c*y - s*z, c*z + s*y );
 }
 
 public Vector TurnY( float s, float c )
 {
 return new Vector( c*x + s*z, y, c*z - s*x );
 }
 
 public Vector TurnZ( float s, float c )
 {
 return new Vector( c*x - s*y, c*y + s*x, z );
 }
 
 
 public void Normalized( )
 {
 float n = 1.0f / Length();
 x *= n;
 y *= n;
 z *= n;
 }
 
 public float MaxDiff( Vector b )
 {
 float dx = (float)Math.abs( x - b.x );
 float dy = (float)Math.abs( y - b.y );
 float dz = (float)Math.abs( z - b.z );
 if ( dx < dy ) { dx = dy; }
 if ( dx < dz ) { dx = dz; }
 return dx;
 }
 
 public void copy( Vector b ) // copy assignment
 {
 x = b.x;
 y = b.y;
 z = b.z;
 }
 
 public void add( Vector b ) 
 {
 x += b.x;
 y += b.y;
 z += b.z;
 }
 
 public Vector plus( Vector b ) 
 {
 return new Vector( x+b.x, y+b.y, z+b.z );
 }
 
 public Vector minus( Vector b ) 
 {
 return new Vector( x-b.x, y-b.y, z-b.z );
 }
 
 // MULTIPLICATION: this * b
 public Vector mult( float b )
 {
 return new Vector(x*b, y*b, z*b );
 }
 
 // DOT PRODUCT: this * b
 public float dot( Vector b )
 {
 return x*b.x + y*b.y + z*b.z;
 }
 
 // CROSS PRODUCT: this % b
 public Vector cross( Vector b )
 {
 return new Vector( y*b.z - z*b.y, z*b.x - x*b.z, x*b.y - y*b.x );
 }
 
 }
 */