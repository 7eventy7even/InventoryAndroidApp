package vkx64.android.scanventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vkx64.android.scanventory.utilities.ExcelHelper;
import vkx64.android.scanventory.utilities.ZipHelper;

public class SettingsActivity extends AppCompatActivity {

    private static final int IMPORT_XLSX_REQUEST_CODE = 1;
    private static final int EXPORT_XLSX_REQUEST_CODE = 2;

    private static final int IMPORT_ZIP_REQUEST_CODE = 3;
    private static final int EXPORT_ZIP_REQUEST_CODE = 4;

    private ImageButton ibLeftButton;


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

        // Set up click listeners for Excel import/export buttons
        findViewById(R.id.ibXLSXUpload).setOnClickListener(v -> openFilePickerForImport(IMPORT_XLSX_REQUEST_CODE));
        findViewById(R.id.ibXLSXDownload).setOnClickListener(v -> openFilePickerForExport(EXPORT_XLSX_REQUEST_CODE));

        // Set up click listeners for image import/export buttons
        findViewById(R.id.ibImageUpload).setOnClickListener(v -> openFilePickerForImport(IMPORT_ZIP_REQUEST_CODE));
        findViewById(R.id.ibImageDownload).setOnClickListener(v -> openFilePickerForExport(EXPORT_ZIP_REQUEST_CODE));

        // Set up the left button to finish the activity
        ibLeftButton = findViewById(R.id.ibLeftButton);
        ibLeftButton.setOnClickListener(v -> finish());
    }

    /**
     * Open a file picker to select an Excel file for importing data into the database.
     */
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

    /**
     * Open a file picker to select a location for exporting database data to an Excel file.
     */
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
                ExcelHelper.importXlsxToDatabase(this, fileUri);
            } else if (requestCode == EXPORT_XLSX_REQUEST_CODE && fileUri != null) {
                // Export data to the selected location
                ExcelHelper.exportDatabaseToXlsx(this, fileUri);
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