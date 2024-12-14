package vkx64.upwork.inventory_management;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import vkx64.upwork.inventory_management.database.AppDatabase;
import vkx64.upwork.inventory_management.database.InventoryItem;
import vkx64.upwork.inventory_management.database.MyDatabase;

public class ItemDetails extends AppCompatActivity {

    EditText etProductID, etProductName, etProductCategory, etSellingQuantity, etStorageQuantity, etDateAdded, etLastModified;
    Button btnSaveChanges;

    private int IntentproductId;
    private AppDatabase db;
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve the Intent and check for extras
        if (getIntent() != null && getIntent().hasExtra("product_id")) {
            IntentproductId = getIntent().getIntExtra("product_id", -1);

            // Display the product_id in a TextView or use it in your logic
            Toast.makeText(this, "Product ID: " + IntentproductId, Toast.LENGTH_SHORT).show();
        } else {
            // Handle missing extras or invalid Intent
            Toast.makeText(this, "No Product ID Passed", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the required data is missing
        }

        InitializeObjects();
        FetchValues();
        setupListeners();
    }

    private void InitializeObjects() {
        etProductID = findViewById(R.id.etProductID);
        etProductName = findViewById(R.id.etProductName);
        etProductCategory = findViewById(R.id.etProductCategory);
        etSellingQuantity = findViewById(R.id.etSellingQuantity);
        etStorageQuantity = findViewById(R.id.etStorageQuantity);
        etDateAdded = findViewById(R.id.etDateAdded);
        etLastModified = findViewById(R.id.etLastModified);

        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnSaveChanges.setEnabled(false);

        db = MyDatabase.getDatabase();
    }

    private void FetchValues() {
        InventoryItem item = db.inventoryDao().findById(IntentproductId);

        if (item != null) {
            etProductID.setText(String.valueOf(item.product_id));
            etProductName.setText(item.product_name);
            etProductCategory.setText(item.product_category);
            etSellingQuantity.setText(String.valueOf(item.selling_quantity));
            etStorageQuantity.setText(String.valueOf(item.storage_quantity));
            etDateAdded.setText(item.date_created);
            etLastModified.setText(item.date_updated);
        }
    }

    private void setupListeners() {
        // Add a TextWatcher to monitor changes in the EditText fields
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isChanged = true;
                btnSaveChanges.setEnabled(true); // Enable the save button
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Attach the TextWatcher to relevant EditText fields
        etProductName.addTextChangedListener(textWatcher);
        etProductCategory.addTextChangedListener(textWatcher);
        etSellingQuantity.addTextChangedListener(textWatcher);
        etStorageQuantity.addTextChangedListener(textWatcher);

        // Optionally, use OnFocusChangeListener for focus-loss-based saving
        etProductName.setOnFocusChangeListener(this::saveOnFocusLoss);
        etProductCategory.setOnFocusChangeListener(this::saveOnFocusLoss);
        etSellingQuantity.setOnFocusChangeListener(this::saveOnFocusLoss);
        etStorageQuantity.setOnFocusChangeListener(this::saveOnFocusLoss);
    }

    private void saveOnFocusLoss(View v, boolean hasFocus) {
        if (!hasFocus && isChanged) {
            saveChanges();
        }
    }

    private void saveChanges() {
        InventoryItem item = db.inventoryDao().findById(IntentproductId);
        if (item != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String formattedDate = dateFormat.format(new Date());

            item.product_name = etProductName.getText().toString();
            item.product_category = etProductCategory.getText().toString();
            item.selling_quantity = Integer.parseInt(etSellingQuantity.getText().toString());
            item.storage_quantity = Integer.parseInt(etStorageQuantity.getText().toString());
            item.date_updated = formattedDate;

            // Update the item in the database
            db.inventoryDao().updateItem(item);

            Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();

            isChanged = false; // Reset the change tracker
            btnSaveChanges.setEnabled(false); // Disable the save button
        }
    }
}