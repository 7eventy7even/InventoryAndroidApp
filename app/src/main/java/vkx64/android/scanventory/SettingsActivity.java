package vkx64.android.scanventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.ClearDataDao;
import vkx64.android.scanventory.utilities.ExcelUtils;
import vkx64.android.scanventory.utilities.ZipHelper;

public class SettingsActivity extends AppCompatActivity {

    private static final int IMPORT_XLSX_REQUEST_CODE = 1;
    private static final int EXPORT_XLSX_REQUEST_CODE = 2;

    private static final int IMPORT_ZIP_REQUEST_CODE = 3;
    private static final int EXPORT_ZIP_REQUEST_CODE = 4;

    private ImageButton ibLeftButton, ibClearData1, ibClearData2, ibXLSXUpload, ibXLSXDownload, ibImageUpload, ibImageDownload;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
    }

    private void initializeViews() {
        // Xlsx Upload
        ibXLSXUpload = findViewById(R.id.ibXLSXUpload);
        ibXLSXUpload.setOnClickListener(v -> openFilePickerForImport(IMPORT_XLSX_REQUEST_CODE));

        // Xlsx Download
        ibXLSXDownload = findViewById(R.id.ibXLSXDownload);
        ibXLSXDownload.setOnClickListener(v -> openFilePickerForExport(EXPORT_XLSX_REQUEST_CODE));

        // Image Import
        ibImageUpload = findViewById(R.id.ibImageUpload);
        ibImageUpload.setOnClickListener(v -> openFilePickerForImport(IMPORT_ZIP_REQUEST_CODE));

        // Image Export
        ibImageDownload = findViewById(R.id.ibImageDownload);
        ibImageDownload.setOnClickListener(v -> openFilePickerForExport(EXPORT_ZIP_REQUEST_CODE));

        // Header Left Button
        ibLeftButton = findViewById(R.id.ibLeftButton);
        ibLeftButton.setOnClickListener(v -> finish());

        // Clear Image
        ibClearData2 = findViewById(R.id.ibClearData2);
        ibClearData2.setOnClickListener(v -> deleteImages());

        // Clear Database
        ibClearData1 = findViewById(R.id.ibClearData1);
        ibClearData1.setOnClickListener(v -> clearDatabaseTables());
    }

    private void clearDatabaseTables() {
        AppClient appClient = AppClient.getInstance(this);
        ClearDataDao clearDataDao = appClient.getAppDatabase().clearDataDao();

        // Run the clearing process on a background thread
        executor.execute(() -> {
            clearDataDao.clearOrderItems();
            clearDataDao.clearOrders();
            clearDataDao.clearItems();
            clearDataDao.clearGroups();
            clearDataDao.resetAutoIncrement();

            runOnUiThread(() -> Toast.makeText(this, "Database cleared successfully!", Toast.LENGTH_SHORT).show());
        });
    }

    private void deleteImages() {
        // Delete all images in the "GroupImages" directory
        File groupImagesDir = new File(getFilesDir(), "GroupImages");
        deleteDirectoryContents(groupImagesDir);

        // Delete all images in the "ItemImages" directory
        File itemImagesDir = new File(getFilesDir(), "ItemImages");
        deleteDirectoryContents(itemImagesDir);
    }

    private void deleteDirectoryContents(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    deleteDirectoryContents(file);
                }
            }
        }
    }

    private void openFilePickerForImport(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (requestCode == IMPORT_XLSX_REQUEST_CODE) {
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if (requestCode == IMPORT_ZIP_REQUEST_CODE) {
            intent.setType("application/zip");
        }
        startActivityForResult(intent, requestCode);
    }

    private void openFilePickerForExport(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (requestCode == EXPORT_XLSX_REQUEST_CODE) {
            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.putExtra(Intent.EXTRA_TITLE, "export.xlsx");
        } else if (requestCode == EXPORT_ZIP_REQUEST_CODE) {
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_TITLE, "images.zip");
        }

        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of the file picker
        if (resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (requestCode == IMPORT_XLSX_REQUEST_CODE && fileUri != null) {
                // Import data from the selected Excel file
                ExcelUtils.importXlsxToDatabase(this, fileUri);
            } else if (requestCode == EXPORT_XLSX_REQUEST_CODE && fileUri != null) {
                // Export data to the selected location
                ExcelUtils.exportDatabaseToXlsx(this, fileUri);
            } else if (requestCode == IMPORT_ZIP_REQUEST_CODE && fileUri != null) {
                // Import images from the selected ZIP file
                ZipHelper.importImagesFromZip(this, fileUri);
            } else if (requestCode == EXPORT_ZIP_REQUEST_CODE && fileUri != null) {
                // Export images to the selected ZIP file
                ZipHelper.exportImagesToZip(this, fileUri);
            }
        }

    }
}