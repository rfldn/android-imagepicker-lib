package com.midwatch.pict.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.midwatch.pict.R;

/**
 *
 */
public class ChooseImageSourceDialog extends DialogFragment {

    public static final String TAG = "ChooseImageSource";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.choose_image_source);
        builder.setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ImageCaptureActivityLauncher.takePictureWithCamera(getActivity(), ImageCaptureActivityResultProcessor.REQUESTED_PICTURE_FROM_CAMERA);
            }
        });
        builder.setNeutralButton(R.string.gallery, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ImageCaptureActivityLauncher.pickPictureFromLibrary(getActivity(), ImageCaptureActivityResultProcessor.REQUESTED_IMAGE_FROM_GALLERY);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

}
