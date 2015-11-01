//uniform mat4 uMVPMatrix;
//uniform mat4 uSTMatrix;
//attribute vec4 aPosition;
//attribute vec4 aTexCoords;
//varying vec2 vTextureCoord;
//
//void main() {
//    vTextureCoord = (uSTMatrix * aTexCoords).xy;
//    gl_Position = uMVPMatrix * aPosition;
//}

//
attribute vec4 vPosition;
attribute vec4 vTexCoordinate;
uniform mat4 uSTMatrix;
uniform mat4 uMVPMatrix;
varying vec2 v_TexCoordinate;
void main() {
   v_TexCoordinate = (uSTMatrix * vTexCoordinate).xy;
   gl_Position = uMVPMatrix * vPosition;
}