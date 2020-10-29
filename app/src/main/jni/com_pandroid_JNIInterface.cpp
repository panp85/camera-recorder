//
// Created by pc on 2020/9/15.
//
#include "com_pandroid_JNIInterface.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
 #include<android/log.h>
 #include <fcntl.h>
//#include <iostream>

#define TAG    "ppt-jni" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型

#ifdef __cplusplus
extern "C" {
#endif
char* jstringToChar(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

JNIEXPORT jstring JNICALL Java_com_pandroid_JNIInterface_setOtg2JNI
  (JNIEnv *env, jobject obj, jstring mode){
    //char* mode_c = jstringToChar(env, mode);
    //printf("mode: %s.\n", mode_c);
    const char* str = env->GetStringUTFChars(mode,0);
    printf("mode: %s.\n", str);
    __android_log_print(ANDROID_LOG_ERROR,TAG ,"mode: %s.\n", str);
    if(str[0] == '0'){
        system("echo 0 > /sys/class/gpio/gpio140/value");
    }
    else{
        system("echo 1 > /sys/class/gpio/gpio140/value");
   }
    return env -> NewStringUTF("ok");
 }

JNIEXPORT jstring JNICALL Java_com_pandroid_JNIInterface_getOtgfromJNI
    (JNIEnv *env, jobject){
    int fd = open("/sys/class/gpio/gpio140/value",O_RDONLY);
    if(fd < 0)
    {
        __android_log_print(ANDROID_LOG_ERROR,TAG ,"open file error:%m\n");
        return env -> NewStringUTF("error");
    }
    char buff[10] = {0};
    read(fd, buff, 10);
    close(fd);
    return env -> NewStringUTF(buff);
}

#ifdef __cplusplus
}
#endif

