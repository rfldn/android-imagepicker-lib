package com.midwatch.pict.computer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.midwatch.pict.exception.RotationException;
import com.midwatch.pict.util.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public final class ImageProcessor {

    public static final int PICTO_IMAGE_MAX_SIZE = 512;

    private static final int ROTATE_90 = 90;
    private static final int ROTATE_180 = 180;
    private static final int ROTATE_270 = 270;

    private static final int EXIF_REVERSE = 3;
    private static final int EXIF_REVERSE_FLIP = 4;
    private static final int EXIF_LEFT_FLIP = 5;
    private static final int EXIF_LEFT = 6;
    private static final int EXIF_RIGHT_FLIP = 7;
    private static final int EXIF_RIGHT = 8;

    /**
     * @see <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html">Displaying bitmaps</a>
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        return calculateInSampleSize(reqWidth, reqHeight, height, width, inSampleSize);
    }

    public static int calculateDefaultInSampleSize(InputStream imageInputStream) {
        return calculateInSampleSize(imageInputStream, PICTO_IMAGE_MAX_SIZE, PICTO_IMAGE_MAX_SIZE);
    }

    /**
     * @see <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html">Displaying bitmaps</a>
     */
    public static int calculateInSampleSize(InputStream imageInputStream, int reqWidth, int reqHeight) {

        // Decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(imageInputStream, null, options);

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        return calculateInSampleSize(reqWidth, reqHeight, height, width, inSampleSize);
    }

    private static int calculateInSampleSize(int imageMaxWidth, int imageMaxHeight, final int height, final int width, int inSampleSize) {
        if (height > imageMaxHeight || width > imageMaxWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) imageMaxHeight);
            final int widthRatio = Math.round((float) width / (float) imageMaxWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeFileWithDefaultSize(String filePath) throws IOException {
        return decodeFileWithSize(filePath, PICTO_IMAGE_MAX_SIZE);
    }

    public static Bitmap decodeFileWithSize(String filePath, int maxSize) throws IOException {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream(filePath);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        int scale = 1;
        if (o.outHeight > maxSize || o.outWidth > maxSize) {
            scale = (int)Math.pow(2, (int) Math.ceil(Math.log(maxSize /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(filePath);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();

        return b;
    }

    public static Bitmap getScaledCenterCroppedBitmap(Bitmap source, int newWidth, int newHeight) {
        if (source == null) {
            return null;
        }
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new, scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public static Bitmap getRoundCroppedBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        float diameter = Math.min(bitmap.getHeight(), bitmap.getWidth());
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, diameter / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap getRotatedBitmap(Bitmap bitmap, int degree) {
        if (bitmap == null) {
            return null;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public static int getOrientation(Context context, Uri photoUri) {
        int rotation;
        try {
            rotation = getStandardOrientation(context, photoUri);
        } catch (RotationException e) {
            rotation = getOrientationUsingExif(photoUri);
        }
        return rotation;
    }

    private static int getStandardOrientation(Context context, Uri photoUri) throws RotationException {
        Cursor cursor = context.getContentResolver().query(photoUri, new String[] {MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);
        if (cursor == null) {
            throw new RotationException();
        }
        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }

    private static int getOrientationUsingExif(Uri selectedPictureUri) {
        // extract EXIF data if any (rotation issues)
        int orientation = 0;
        try {
            ExifInterface exif = new ExifInterface(selectedPictureUri.getPath());
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        } catch (IOException e) {
            Logger.e(e.getLocalizedMessage(), e);
        }

        switch (orientation) {
            case EXIF_REVERSE:
            case EXIF_REVERSE_FLIP:
                return ROTATE_180;

            case EXIF_LEFT:
            case EXIF_LEFT_FLIP:
                return ROTATE_90;

            case EXIF_RIGHT:
            case EXIF_RIGHT_FLIP:
                return ROTATE_270;

            default:
                return 0;
        }
    }

}
