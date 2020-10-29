LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_LDLIBS :=-llog
LOCAL_MODULE := JNISample
LOCAL_SRC_FILES := com_pandroid_JNIInterface.cpp

include $(BUILD_SHARED_LIBRARY)