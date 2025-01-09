package vkx64.android.scanventory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vkx64.android.scanventory.adapter.GalleryAdapter;
import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.TableItems;
import vkx64.android.scanventory.utilities.FileHelper;
import vkx64.android.scanventory.utilities.SingleImagePicker;

public class ItemDetailsActivity extends AppCompatActivity {

    private EditText etItemId, etItemName, etItemCategory, etItemStorage, etItemSelling;
    RecyclerView rvGallery;
    private MaterialButton btnSubmit;
    private ImageButton ibLeftButton;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String itemId;
    private boolean isDataChanged = false;

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

        initializeViews();

        itemId = getIntent().getStringExtra("item_id");
        if (isInvalidItemId(itemId)) {
            Toast.makeText(this, "Invalid item ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch item details asynchronously
        fetchItemDetails(itemId);
        loadGallery();
    }

    /**
     * Initialize views and listeners.
     */
    private void initializeViews() {
        etItemId = findViewById(R.id.etItemId);
        etItemName = findViewById(R.id.etItemName);
        etItemCategory = findViewById(R.id.etItemCategory);
        etItemStorage = findViewById(R.id.etItemStorage);
        etItemSelling = findViewById(R.id.etItemSelling);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setEnabled(false);
        btnSubmit.setOnClickListener(v -> saveItemDetails());

        addTextChangeListener(etItemName);
        addTextChangeListener(etItemCategory);
        addTextChangeListener(etItemStorage);
        addTextChangeListener(etItemSelling);

        rvGallery = findViewById(R.id.rvGallery);
        rvGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        ibLeftButton = findViewById(R.id.ibLeftButton);
        ibLeftButton.setOnClickListener(v -> finish());
    }

    private void loadGallery() {
        executor.execute(() -> {
            // Fetch primary image
            String primaryImagePath = FileHelper.getPrimaryImageForItem(this, itemId);

            // Fetch additional images
            List<String> additionalImagePaths = FileHelper.getImagesForItem(this, itemId);

            // Create a combined list
            List<String> allImagePaths = new ArrayList<>();

            // Add primary image first if it exists
            if (primaryImagePath != null) {
                allImagePaths.add(primaryImagePath);
            }

            // Add all additional images
            allImagePaths.addAll(additionalImagePaths);

            // Update the gallery on the main thread
            runOnUiThread(() -> {
                GalleryAdapter adapter = new GalleryAdapter(
                        this,
                        allImagePaths,
                        this::openImagePicker, // Handle add image
                        this::onImageClick      // Handle image click
                );
                rvGallery.setAdapter(adapter);
            });
        });
    }

    // Handle image clicks
    private void onImageClick(String imagePath) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage(imagePath))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Handle the deletion of the image
    private void deleteImage(String imagePath) {
        File file = new File(imagePath);
        if (file.exists() && file.delete()) {
            Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
            loadGallery(); // Reload the gallery after deletion
        } else {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isInvalidItemId(String itemId) {
        return itemId == null || itemId.isEmpty();
    }

    private void populateItemDetails(TableItems item) {
        isDataChanged = false; // Prevent TextWatcher from enabling the button

        etItemId.setText(item.getItem_id());
        etItemName.setText(item.getItem_name());
        etItemCategory.setText(item.getItem_category());
        etItemStorage.setText(String.valueOf(item.getItem_storage()));
        etItemSelling.setText(String.valueOf(item.getItem_selling()));

        btnSubmit.setEnabled(false); // Ensure button stays disabled
        btnSubmit.setBackgroundTintList(getResources().getColorStateList(R.color.hints));
    }

    private void fetchItemDetails(String itemId) {
        executor.execute(() -> {
            // Retrieve the item from the database
            TableItems item = AppClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .daoItems()
                    .getItemById(itemId);

            // Handle the result on the main thread
            runOnUiThread(() -> {
                if (item == null) {
                    Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                } else {
                    populateItemDetails(item);
                }
            });
        });
    }

    private void addTextChangeListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isDataChanged) {
                    // Allow changes to be detected after initial population
                    isDataChanged = true;
                }
                // Enable the button and set it to green
                btnSubmit.setEnabled(true);
                btnSubmit.setBackgroundTintList(getResources().getColorStateList(R.color.green));
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
    }

    private void openImagePicker() {
        SingleImagePicker.openImageSelector(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImageUri = SingleImagePicker.handleImagePickerResult(requestCode, resultCode, data, this);
        if (selectedImageUri != null) {
            saveImage(selectedImageUri);
            loadGallery();
        }
    }

    private void saveImage(Uri imageUri) {
        String folderName = "ItemImages";
        String baseFileName = itemId;
        File folder = new File(getFilesDir(), folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Check for existing files starting with itemId
        File[] existingFiles = folder.listFiles((dir, name) -> name.startsWith(baseFileName) &&
                (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")));

        String finalFileName;

        if (existingFiles != null && existingFiles.length > 0) {
            // If any file with itemId exists, append timestamp
            String extension = ".png"; // Default extension
            // Optionally, derive the extension from the selected image
            finalFileName = baseFileName + "_" + System.currentTimeMillis() + extension;
        } else {
            // Use the first extension (e.g., png) or derive from imageUri
            String extension = ".png"; // Default extension
            finalFileName = baseFileName + extension;
        }

        SingleImagePicker.saveImageToInternalStorage(imageUri, folderName, finalFileName, this);

        Toast.makeText(this, "Image saved as " + finalFileName, Toast.LENGTH_SHORT).show();
        loadGallery();
    }

    private void saveItemDetails() {
        // Retrieve input values from EditTexts
        String itemName = etItemName.getText().toString().trim();
        String itemCategory = etItemCategory.getText().toString().trim();
        String storageValue = etItemStorage.getText().toString().trim();
        String sellingValue = etItemSelling.getText().toString().trim();

        String categoryToSave = itemCategory.isEmpty() ? null : itemCategory;

        // Convert storage and selling values to integers, default to 0 if empty
        int itemStorage = storageValue.isEmpty() ? 0 : Integer.parseInt(storageValue);
        int itemSelling = sellingValue.isEmpty() ? 0 : Integer.parseInt(sellingValue);

        // Validate inputs
        if (itemName.isEmpty()) {
            Toast.makeText(this, "Name Cannot be Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            // Fetch the existing item from the database
            TableItems existingItem = AppClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .daoItems()
                    .getItemById(itemId);

            if (existingItem == null) {
                // If the item does not exist, notify the user
                runOnUiThread(() -> Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show());
                return;
            }

            // Update the fields that have changed
            existingItem.setItem_name(itemName);
            existingItem.setItem_category(categoryToSave);
            existingItem.setItem_storage(itemStorage);
            existingItem.setItem_selling(itemSelling);

            // Update the item in the database
            AppClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .daoItems()
                    .updateItem(existingItem);

            // Notify the user on the main thread
            runOnUiThread(() -> {
                isDataChanged = false; // Reset change flag
                btnSubmit.setEnabled(false); // Disable the button again
                btnSubmit.setBackgroundTintList(getResources().getColorStateList(R.color.hints));
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void updateButtonState(boolean isEnabled) {
        btnSubmit.setEnabled(isEnabled);
        if (isEnabled) {
            btnSubmit.setBackgroundTintList(getResources().getColorStateList(R.color.green)); // Green when enabled
        } else {
            btnSubmit.setBackgroundTintList(getResources().getColorStateList(R.color.hints)); // Gray when disabled
        }
    }

    // Shutdown the executor when the activity is destroyed to avoid leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}