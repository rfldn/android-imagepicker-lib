package com.midwatch.pict.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.midwatch.pict.computer.ImageProcessor;
import com.midwatch.pict.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class ImageStorer {

    private static final int QUALITY_COMPRESS = 80;
    private static final int QUALITY_MAX = 100;
    private static final String TEMP_FILENAME = "temp_picture.png";

    public static String savePhotoFromCamera(Context context, Intent intent, int width, int height) throws IOException {
        Uri uri = intent.getData();
        // HTC Desire Bug
        if (uri == null && intent.getExtras() != null && intent.getExtras().get("data") instanceof Bitmap) {
            uri = createUriFromPhotoIntentForHtcDesireHD(context, intent);
            Logger.d("The intent is " + intent.getExtras().get("data").getClass().getName());
        }
        if (uri == null) {
            Logger.d("No URI found");
            return null;
        }
        int rotation = ImageProcessor.getOrientation(context, uri);
        String filePath;
        Logger.d("The uri is " + uri.toString());
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            filePath = uri.getPath();
        } else {
            filePath = getFilePathFromDataUri(context, uri);
        }
        Bitmap bitmap = ImageProcessor.decodeFileWithDefaultSize(filePath);
        if (bitmap == null) {
            Logger.e("No bitmap could be read, have you allowed 'android.permission.READ_EXTERNAL_STORAGE' in your manifest?");
            return null;
        }
        if (rotation != 0) {
            bitmap = ImageProcessor.getRotatedBitmap(bitmap, rotation);
        }
        bitmap = ImageProcessor.getScaledCenterCroppedBitmap(bitmap, width, height);
        return writeBitmapAsJpeg(context, bitmap);
    }

    /**
     * Save gallery received data in cache folder as a Bitmap.
     * If the image is too big, it is resized to prevent OutOfMemoryException before saving it.
     */
    public static String saveImageFromGallery(Context context, Intent intent, int width, int height) throws FileNotFoundException {
        Uri selectedImage = intent.getData();
        int rotation = ImageProcessor.getOrientation(context, selectedImage);
        // 1) open image stream to get content necessary size (to prevent OutOfMemory exceptions)
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        int inSampleSize = ImageProcessor.calculateDefaultInSampleSize(imageStream);
        // 2) close input stream (we cannot browse inputStream twice)
        try {
            imageStream.close();
        } catch (IOException e) {
            Logger.e("Image stream was not successfully closed", e);
        }
        // 3) reopen image stream to get bitmap (resize it if necessary)
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        // Decode bitmap with inSampleSize set
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);
        if (rotation != 0) {
            bitmap = ImageProcessor.getRotatedBitmap(bitmap, rotation);
        }
        bitmap = ImageProcessor.getScaledCenterCroppedBitmap(bitmap, width, height);
        return writeBitmapAsPng(context, bitmap);
    }

    /**
     * Write a bitmap as PNG in the cache directory.
     */
    public static String writeBitmapAsPng(Context context, Bitmap bitmap) throws FileNotFoundException {
        return writeBitmap(context, bitmap, Bitmap.CompressFormat.PNG);
    }

    /**
     * Write a bitmap as PNG in the cache directory.
     */
    public static String writeBitmapAsJpeg(Context context, Bitmap bitmap) throws FileNotFoundException {
        return writeBitmap(context, bitmap, Bitmap.CompressFormat.JPEG);
    }

    /**
     * Write a bitmap in the cache directory.
     */
    public static String writeBitmap(Context context, Bitmap bitmap, Bitmap.CompressFormat format) throws FileNotFoundException {
        FileOutputStream fos = null;
        String outputFileName = null;
        if (bitmap == null) {
            return null;
        }
        try {
            File outputDir = context.getCacheDir();
            File outputFile = new File(outputDir, TEMP_FILENAME);
            fos = new FileOutputStream(outputFile);
            bitmap.compress(format, Bitmap.CompressFormat.PNG.equals(format) ? QUALITY_MAX : QUALITY_COMPRESS, fos);
            outputFileName = outputFile.getAbsolutePath();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Logger.e("Could not close FileOutputStream", e);
                }
            }
        }
        return outputFileName;
    }

    public static Bitmap getBitmapFromPath(String path) {
        Bitmap theBitmap = null;
        if (path != null) {
            theBitmap = BitmapFactory.decodeFile(path);
        }
        return theBitmap;
    }

    public static byte[] getPngBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY_MAX, stream);
        return stream.toByteArray();
    }

    private static Uri createUriFromPhotoIntentForHtcDesireHD(Context context, Intent intent) {
        Uri uri = null;
        FileOutputStream fos = null;
        try {
            Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("Photo-", ".jpg", outputDir);
            fos = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_COMPRESS, fos);
            uri = Uri.fromFile(outputFile);
        } catch (IOException e) {
            Logger.e("Error creating temp file for HTC Desire HD", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Logger.e("Error closing temp file for HTC Desire HD", e);
            }
        }
        return uri;
    }

    private static String getFilePathFromDataUri(Context context, Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(projection[0]));
            cursor.close();
        }
        return path;
    }

}
