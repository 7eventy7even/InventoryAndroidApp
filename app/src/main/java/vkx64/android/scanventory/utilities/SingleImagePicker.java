package vkx64.android.scanventory.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SingleImagePicker {

    public static final int PICK_IMAGE_REQUEST = 1;

    public static void openImageSelector(@NonNull Object context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else if (context instanceof Fragment) {
            ((Fragment) context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            throw new IllegalArgumentException("Context must be an Activity or Fragment");
        }
    }

    public static Uri handleImagePickerResult(int requestCode, int resultCode, @Nullable Intent data, Context context) {
        //* Handle image picker result *//

        if (requestCode != PICK_IMAGE_REQUEST || resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(context, "No image selected.", Toast.LENGTH_SHORT).show();
            return null;
        }

        return data.getData();
    }

    public static void displayImage(Uri imageUri, ImageView imageView, Context context) {
        // Display selected image in the ImageView
        if (imageUri == null) {
            Toast.makeText(context, "No image to display.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Convert the URI to a Bitmap and set it to the ImageView
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            bitmap = correctImageOrientation(context, imageUri, bitmap);
            imageView.setImageBitmap(bitmap); // Set the image on the ImageView
        } catch (IOException e) {
            Toast.makeText(context, "Failed to display image.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void saveImageToInternalStorage(Uri imageUri, String folderName, String fileName, Context context) {
        //* Save image to internal storage *//

        if (imageUri == null) {
            Toast.makeText(context, "No image to save. Please select one first.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File folder = new File(context.getFilesDir(), folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, fileName);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            Log.d("ImagePicker", "Image saved successfully to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("ImagePicker", "Failed to save image.", e);
        }
    }

    public static Bitmap correctImageOrientation(Context context, Uri imageUri, Bitmap bitmap) {
        //* Correct image orientation *//

        try {
            // Open the image file and read its EXIF metadata
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(input);

            // Get the orientation from the EXIF metadata
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            // Determine the rotation angle based on the orientation
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return rotateBitmap(bitmap, 90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return rotateBitmap(bitmap, 180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return rotateBitmap(bitmap, 270);
            }

            // If no rotation is needed, return the original bitmap
            return bitmap;
        } catch (IOException e) {
            // If an error occurs, return the original bitmap
            return bitmap;
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        //* Rotate bitmap *//
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        // Create a new rotated bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
