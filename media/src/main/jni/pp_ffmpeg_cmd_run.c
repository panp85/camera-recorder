#include <jni.h>
//#include <string>

JNIEXPORT jint JNICALL
Java_com_example_pc_ffmpeg_1android_1test_MainActivity_ffmpegRun(
        JNIEnv* env,
        jobject instance,
        jobjectArray commands)
{
    int argc = (*env)->GetArrayLength(env,commands);
    char *argv[argc];
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) (*env)->GetObjectArrayElement(env,commands, i);
        argv[i] = (char *) (*env)->GetStringUTFChars(env,js, 0);
    }
    return jxRun(argc,argv);
    //std::string hello = "Hello from C++";
    //return env->NewStringUTF(hello.c_str());
}
