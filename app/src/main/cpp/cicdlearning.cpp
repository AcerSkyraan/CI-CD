#include <jni.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("cicdlearning");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("cicdlearning")
//      }
//    }
extern "C"
JNIEXPORT jstring JNICALL
Java_com_alpha_cicdlearning_MainActivity_00024Companion_gyuhj(JNIEnv *env, jobject thiz) {
    return (*env).NewStringUTF("");
}