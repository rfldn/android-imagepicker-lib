package com.midwatch.pict.util;

import android.util.Log;

/**
 *
 */
public class Logger {

    private static final String LOG_TAG = "MIDWATCH_PICT";

    public static void e(String message) {
        e(message, null);
    }

    public static void e(Throwable throwable) {
        Log.e(LOG_TAG, throwable.getLocalizedMessage(), throwable);
    }

    public static void e(String message, Throwable throwable) {
        Log.e(LOG_TAG, message, throwable);
    }

    public static void d(String message) {
        Log.d(LOG_TAG, message);
    }

}
