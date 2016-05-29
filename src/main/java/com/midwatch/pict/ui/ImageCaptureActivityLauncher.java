package com.midwatch.pict.ui;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Toast;

import com.midwatch.pict.R;
import com.midwatch.pict.util.DeviceUtils;

/**
 *
 */
public class ImageCaptureActivityLauncher {

    private static final String MIME_TYPE_IMAGE = "image/*";

    /**
     * Display gallery app. Picked image will be sent back and retrieved through 'onActivityResult' method
     */
    public static void takePictureWithCamera(Activity activity, int requestCode) {
        if (!DeviceUtils.hasCameraFeature(activity)) {
            Toast.makeText(activity, activity.getString(R.string.no_camera_available), Toast.LENGTH_SHORT).show();
        } else {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activity.startActivityForResult(takePhotoIntent, requestCode);
        }
    }

    /**
     * Display gallery app. Picked image will be sent back and retrieved through 'onActivityResult' method
     */
    public static void pickPictureFromLibrary(Activity activity, int requestCode) {
        Intent chooseFromLibraryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        chooseFromLibraryIntent.setType(MIME_TYPE_IMAGE);
        activity.startActivityForResult(chooseFromLibraryIntent, requestCode);
    }

}
