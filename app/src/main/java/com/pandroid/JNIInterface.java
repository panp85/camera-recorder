package com.pandroid;

public class JNIInterface {

    static {
        System.loadLibrary("JNISample");
    }
    public native static String setOtg2JNI(String mode);
    public native static String getOtgfromJNI();
}
