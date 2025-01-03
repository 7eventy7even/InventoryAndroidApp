package vkx64.android.scanventory.utilities;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    public static List<String> getImagesForItem(Context context, String itemId) {
        List<String> imagePaths = new ArrayList<>();

        // Locate the "ItemImages" directory in app's internal storage
        File imagesDir = new File(context.getFilesDir(), "ItemImages");

        // Return an empty list if the directory does not exist or is not valid
        if (!imagesDir.exists() || !imagesDir.isDirectory()) {
            return imagePaths;
        }

        // Get all files in the directory
        File[] files = imagesDir.listFiles();

        // Return an empty list if there are no files in the directory
        if (files == null) {
            return imagePaths;
        }

        // Iterate through files and add matching ones to the list
        for (File file : files) {
            if (file.getName().startsWith(itemId + "_")) {
                imagePaths.add(file.getAbsolutePath());
            }
        }

        return imagePaths;
    }

    public static String getGroupImage(Context context, String groupId) {
        // Locate the "GroupImages" directory in app's internal storage
        File groupImagesDir = new File(context.getFilesDir(), "GroupImages");

        // Return null if the directory does not exist or is not valid
        if (!groupImagesDir.exists() || !groupImagesDir.isDirectory()) {
            return null;
        }

        // Construct the expected file name for the group image
        File groupImageFile = new File(groupImagesDir, groupId + ".png");

        // Return the absolute path if the file exists, otherwise return null
        return groupImageFile.exists() ? groupImageFile.getAbsolutePath() : null;
    }
}
