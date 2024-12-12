package vkx64.upwork.inventory_management.helpers;

import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import vkx64.upwork.inventory_management.database.InventoryItem;

public class CSVParser {
    public static List<InventoryItem> parseCSVFromUri(Context context, Uri uri) {
        List<InventoryItem> items = new ArrayList<>();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // skip header line
                    continue;
                }

                String[] columns = line.split(",");

                // Make sure columns match your CSV format
                InventoryItem item = new InventoryItem();
                item.product_id = Integer.parseInt(columns[0].trim());
                item.product_category = columns[1].trim();
                item.product_name = columns[2].trim();
                item.storage_quantity = Integer.parseInt(columns[3].trim());
                item.selling_quantity = Integer.parseInt(columns[4].trim());
                item.date_created = columns[5].trim();
                item.date_updated = columns[6].trim();

                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }
}
