package vkx64.upwork.inventory_management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import vkx64.upwork.inventory_management.database.AppDatabase;
import vkx64.upwork.inventory_management.database.InventoryItem;
import vkx64.upwork.inventory_management.database.MyDatabase;
import vkx64.upwork.inventory_management.helpers.CSVParser;
import vkx64.upwork.inventory_management.helpers.StorageUtils;

public class MainActivity extends AppCompatActivity {

    private Button btnUploadImages, btnUploadCSV, btnDataTable, btnExportImages, btnExportCsv;
    private AppDatabase db;
    private static final int PICK_IMAGES_REQUEST = 1001;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 2001;
    private static final int REQUEST_CODE_CREATE_DOCUMENT = 3001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        InitializeObjects();
        db = MyDatabase.getDatabase();

    }

    private void InitializeObjects() {
        btnUploadImages = findViewById(R.id.btnUploadImages);
        btnUploadImages.setOnClickListener(v -> OpenImagePicker());

        btnUploadCSV = findViewById(R.id.btnUploadCSV);
        btnUploadCSV.setOnClickListener(v -> OpenCsvPicker());

        btnDataTable = findViewById(R.id.btnDataTable);
        btnDataTable.setOnClickListener( v -> startActivity(new Intent(this, Inventory.class)));

        btnExportImages = findViewById(R.id.btnExportImages);
        btnExportImages.setOnClickListener(v -> { requestZipExport(); });

        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(v -> OpenCsvExportLocation());
    }

    private void OpenImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    private void OpenCsvPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT);
    }

    private void OpenCsvExportLocation() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "exported_data.csv");
        startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT);
    }

    private void parseCsvAndInsert(Uri uri) {
        List<InventoryItem> items = CSVParser.parseCSVFromUri(this, uri);
        db.inventoryDao().deleteAllItems();
        db.inventoryDao().insertAll(items);
    }

    private void requestZipExport() {
        File imagesFolder = new File(getFilesDir(), "Images");
        if (!imagesFolder.exists() || imagesFolder.listFiles() == null || Objects.requireNonNull(imagesFolder.listFiles()).length == 0) {
            Toast.makeText(this, "No images found to export.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, "images_export.zip");
        createFileLauncher.launch(intent);
    }

    private void exportImages(Uri zipUri) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(zipUri);
             ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {

            File imagesFolder = new File(getFilesDir(), "Images");
            File[] imageFiles = imagesFolder.listFiles();

            if (imageFiles != null) {
                for (File file : imageFiles) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        zipOut.putNextEntry(new ZipEntry(file.getName()));

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zipOut.write(buffer, 0, length);
                        }

                        zipOut.closeEntry();
                    }
                }
            }

            Toast.makeText(this, "Images exported successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to export images: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void writeCsvToUri(Uri uri) {
        List<InventoryItem> itemList = MyDatabase.getDatabase().inventoryDao().getAll();

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("product_id,product_category,product_name,storage_quantity,selling_quantity,date_created,date_updated\n");

        for (InventoryItem item : itemList) {
            csvBuilder.append(item.product_id).append(",")
                    .append(item.product_category).append(",")
                    .append(item.product_name).append(",")
                    .append(item.storage_quantity).append(",")
                    .append(item.selling_quantity).append(",")
                    .append(item.date_created).append(",")
                    .append(item.date_updated).append("\n");
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(csvBuilder.toString().getBytes());
                Toast.makeText(this, "CSV exported successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to write file.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error writing CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    File storageDirectory = getFilesDir();
                    StorageUtils.saveImageToInternalStorage(getContentResolver(), imageUri, storageDirectory);
                }
            }
        }

        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                parseCsvAndInsert(uri);
            }
        }

        if (requestCode == REQUEST_CODE_CREATE_DOCUMENT && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                writeCsvToUri(uri);
            }
        }
    }

    private final ActivityResultLauncher<Intent> createFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    exportImages(result.getData().getData());
                }
            }
    );
}