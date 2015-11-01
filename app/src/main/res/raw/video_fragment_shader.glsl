//#extension GL_OES_EGL_image_external : require
//
//precision mediump float;
//varying vec2 vTextureCoord;
//uniform samplerExternalOES sTexture;
//
//void main() {
//    vec4 color = texture2D(sTexture, vTextureCoord);
//    gl_FragColor = color;
//}
//
#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES sTexture;
varying vec2 v_TexCoordinate;
void main () {
    vec4 color = texture2D(sTexture, v_TexCoordinate);
    gl_FragColor = color;
}
