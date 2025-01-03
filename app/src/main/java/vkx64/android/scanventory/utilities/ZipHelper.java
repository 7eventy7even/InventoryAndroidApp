package vkx64.android.scanventory.utilities;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

    private static final String ITEM_IMAGES_FOLDER = "ItemImages";
    private static final String GROUP_IMAGES_FOLDER = "GroupImages";

    public static void exportImagesToZip(Context context, Uri fileUri) {
        new Thread(() -> {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

                File itemImagesDir = new File(context.getFilesDir(), ITEM_IMAGES_FOLDER);
                File groupImagesDir = new File(context.getFilesDir(), GROUP_IMAGES_FOLDER);

                // Add the ItemImages folder to the zip
                if (itemImagesDir.exists() && itemImagesDir.isDirectory()) {
                    zipDirectory(itemImagesDir, itemImagesDir.getName(), zipOutputStream);
                }

                // Add the GroupImages folder to the zip
                if (groupImagesDir.exists() && groupImagesDir.isDirectory()) {
                    zipDirectory(groupImagesDir, groupImagesDir.getName(), zipOutputStream);
                }

                notifyOnMainThread(() -> System.out.println("Images exported successfully!"));
            } catch (Exception e) {
                notifyOnMainThread(() -> System.err.println("Failed to export images: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    public static void importImagesFromZip(Context context, Uri fileUri) {
        new Thread(() -> {
            try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                 ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    File outputFile = new File(context.getFilesDir(), entry.getName());

                    if (entry.isDirectory()) {
                        if (!outputFile.exists() && !outputFile.mkdirs()) {
                            throw new RuntimeException("Failed to create directory: " + outputFile.getAbsolutePath());
                        }
                    } else {
                        // Ensure parent directories exist
                        File parentDir = outputFile.getParentFile();
                        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                            throw new RuntimeException("Failed to create parent directory: " + parentDir.getAbsolutePath());
                        }

                        // Write file
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = zipInputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                }

                notifyOnMainThread(() -> System.out.println("Images imported successfully!"));
            } catch (Exception e) {
                notifyOnMainThread(() -> System.err.println("Failed to import images: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private static void zipDirectory(File folder, String parentFolder, ZipOutputStream zipOut) throws Exception {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            String zipEntryName = parentFolder + File.separator + file.getName();

            if (file.isDirectory()) {
                // Recursively add sub-directories
                zipDirectory(file, zipEntryName, zipOut);
            } else {
                // Add file
                try (FileInputStream fis = new FileInputStream(file)) {
                    zipOut.putNextEntry(new ZipEntry(zipEntryName));
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, len);
                    }
                    zipOut.closeEntry();
                }
            }
        }
    }

    private static void notifyOnMainThread(Runnable runnable) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(runnable);
    }
}
