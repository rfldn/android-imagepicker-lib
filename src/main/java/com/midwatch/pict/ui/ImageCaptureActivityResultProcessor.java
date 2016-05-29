package com.midwatch.pict.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.midwatch.pict.computer.ImageProcessor;
import com.midwatch.pict.storage.ImageStorer;
import com.midwatch.pict.util.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 */
public class ImageCaptureActivityResultProcessor {

    public static final int REQUESTED_IMAGE_FROM_GALLERY = 7398;
    public static final int REQUESTED_PICTURE_FROM_CAMERA = 7399;

    public static String processImageCaptureResult(Context context, int requestCode, int resultCode, Intent data) {
        String path = null;
        if (resultCode == Activity.RESULT_OK) {
            try {
                if (requestCode == REQUESTED_IMAGE_FROM_GALLERY) {
                    path = ImageStorer.saveImageFromGallery(context, data, ImageProcessor.PICTO_IMAGE_MAX_SIZE, ImageProcessor.PICTO_IMAGE_MAX_SIZE);
                } else if (requestCode == REQUESTED_PICTURE_FROM_CAMERA) {
                    path = ImageStorer.savePhotoFromCamera(context, data, ImageProcessor.PICTO_IMAGE_MAX_SIZE, ImageProcessor.PICTO_IMAGE_MAX_SIZE);
                }
            } catch (IOException e) {
                Logger.e(e);
            }
        }
        return path;
    }

}
