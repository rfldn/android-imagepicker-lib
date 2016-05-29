package com.midwatch.pict.util;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 *
 */
public final class DeviceUtils {

    public static boolean hasCameraFeature(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}
