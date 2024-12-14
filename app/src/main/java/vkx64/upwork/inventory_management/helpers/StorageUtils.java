package vkx64.upwork.inventory_management.helpers;

import android.content.ContentResolver;
import android.database.Cursor;

import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
public class StorageUtils {

    // Save image to a specific folder inside internal storage
    public static void saveImageToInternalStorage(ContentResolver contentResolver, Uri imageUri, File storageDirectory) {
        try {
            // Get the original file name from the Uri
            String fileName = getFileName(contentResolver, imageUri);

            // Create a subfolder inside the internal storage directory (e.g., "Images")
            File folder = new File(storageDirectory, "Images");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Create a file in the folder with the same name as the original image
            File internalFile = new File(folder, fileName);

            // Get the input stream from the image Uri
            InputStream inputStream = contentResolver.openInputStream(imageUri);

            // Create an output stream to write data to the internal file
            FileOutputStream outputStream = new FileOutputStream(internalFile);

            // Read and write the data
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Close streams
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get the file name from Uri
    private static String getFileName(ContentResolver contentResolver, Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileName = cursor.getString(nameIndex);
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

}
