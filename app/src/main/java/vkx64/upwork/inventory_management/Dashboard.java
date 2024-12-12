package vkx64.upwork.inventory_management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.OutputStream;
import java.util.List;

import vkx64.upwork.inventory_management.database.AppDatabase;
import vkx64.upwork.inventory_management.database.InventoryItem;
import vkx64.upwork.inventory_management.database.MyDatabase;
import vkx64.upwork.inventory_management.helpers.CSVParser;

public class Dashboard extends AppCompatActivity {

    Button btnImportCsv, btnExportCsv;

    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1001;
    private static final int REQUEST_CODE_CREATE_DOCUMENT = 2001;
    private AppDatabase db;
    private InventoryAdapter adapter;
    RecyclerView rvDataTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setObjects();

        db = MyDatabase.getDatabase();
        setupRecyclerView();
    }

    public void initializeViews() {
        btnImportCsv = findViewById(R.id.btnImportCsv);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        rvDataTable = findViewById(R.id.rvDataTable);
    }

    public void setObjects() {
        btnImportCsv.setOnClickListener(v -> {
            openFilePicker();
        });

        btnExportCsv.setOnClickListener(v -> {
            openLocationPicker();
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT);
    }

    private void openLocationPicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "exported_data.csv");
        startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT);
    }

    private void setupRecyclerView() {
        List<InventoryItem> itemList = db.inventoryDao().getAll();
        adapter = new InventoryAdapter(itemList);
        rvDataTable.setAdapter(adapter);
        rvDataTable.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    private void parseCsvAndInsert(Uri uri) {
        List<InventoryItem> items = CSVParser.parseCSVFromUri(this, uri);
        db.inventoryDao().deleteAllItems();
        db.inventoryDao().insertAll(items);
        refreshData();
    }

    private void refreshData() {
        List<InventoryItem> updatedList = db.inventoryDao().getAll();
        adapter = new InventoryAdapter(updatedList);
        rvDataTable.setAdapter(adapter);
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
}