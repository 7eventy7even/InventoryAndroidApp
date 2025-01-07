package vkx64.android.scanventory.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.DaoItems;
import vkx64.android.scanventory.database.DaoGroups;
import vkx64.android.scanventory.database.TableItems;
import vkx64.android.scanventory.database.TableGroups;

public class ExcelHelper {

    private static final String TAG = "ExcelHelper";

    public static void exportDatabaseToXlsx(Context context, Uri fileUri) {
        new Thread(() -> {
            try (Workbook workbook = new XSSFWorkbook()) {
                Log.d(TAG, "Starting export process");

                // Create and populate the "Items" sheet
                createItemSheet(context, workbook);
                Log.d(TAG, "Items sheet created successfully");

                // Create and populate the "Groups" sheet
                createGroupSheet(context, workbook);
                Log.d(TAG, "Groups sheet created successfully");

                // Write the workbook to the file
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri)) {
                    workbook.write(outputStream);
                    Log.d(TAG, "Workbook written to file: " + fileUri);
                }

                notifyOnMainThread(context, "Database exported successfully!", true);
            } catch (Exception e) {
                Log.e(TAG, "Failed to export file", e);
                notifyOnMainThread(context, "Failed to export database: " + e.getMessage(), false);
            }
        }).start();
    }

    private static void createItemSheet(Context context, Workbook workbook) {
        Log.d(TAG, "Creating Items sheet");
        Sheet sheet = workbook.createSheet("Items");

        // Add header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Item ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Category");
        header.createCell(3).setCellValue("Storage");
        header.createCell(4).setCellValue("Selling");
        header.createCell(5).setCellValue("Group ID");
        header.createCell(6).setCellValue("Date Created");
        header.createCell(7).setCellValue("Date Updated");

        DaoItems daoItems = AppClient.getInstance(context).getAppDatabase().daoItems();
        List<TableItems> items = daoItems.getAllItems();
        Log.d(TAG, "Fetched " + items.size() + " items from database");

        // Add item rows
        int rowIndex = 1;
        for (TableItems item : items) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getItem_id());
            row.createCell(1).setCellValue(item.getItem_name());
            row.createCell(2).setCellValue(item.getItem_category() != null ? item.getItem_category() : "");
            row.createCell(3).setCellValue(item.getItem_storage() != 0 ? item.getItem_storage() : 0);
            row.createCell(4).setCellValue(item.getItem_selling() != 0 ? item.getItem_selling() : 0);
            row.createCell(5).setCellValue(item.getGroup_id() != null ? item.getGroup_id() : "");
            row.createCell(6).setCellValue(item.getItem_created() != null ? item.getItem_created() : "");
            row.createCell(7).setCellValue(item.getItem_updated() != null ? item.getItem_updated() : "");
        }
        Log.d(TAG, "Items sheet populated successfully");
    }

    private static void createGroupSheet(Context context, Workbook workbook) {
        Log.d(TAG, "Creating Groups sheet");
        Sheet sheet = workbook.createSheet("Groups");

        // Add header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Group ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Parent Group ID");

        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();
        List<TableGroups> groups = daoGroups.getAllGroups();
        Log.d(TAG, "Fetched " + groups.size() + " groups from database");

        // Add group rows
        int rowIndex = 1;

        for (TableGroups group : groups) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(group.getGroup_id());
            row.createCell(1).setCellValue(group.getGroup_name());
            row.createCell(2).setCellValue(group.getGroup_parent() != null ? group.getGroup_parent() : "");
        }
        Log.d(TAG, "Groups sheet populated successfully");
    }

    public static void importXlsxToDatabase(Context context, Uri fileUri) {
        new Thread(() -> {
            try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Log.d(TAG, "Workbook opened from file: " + fileUri);

                // Import groups first to ensure all references are valid
                importGroups(context, workbook);

                // Import items next
                importItems(context, workbook);

                notifyOnMainThread(context, "Database imported successfully!", true);
            } catch (Exception e) {
                Log.e(TAG, "Failed to import file", e);
                notifyOnMainThread(context, "Failed to import database: " + e.getMessage(), false);
            }
        }).start();
    }

    private static void importGroups(Context context, Workbook workbook) {
        Log.d(TAG, "Importing Groups sheet");

        Sheet sheet = workbook.getSheet("Groups");

        if (sheet == null) {
            Log.e(TAG, "Groups sheet not found");
            return;
        }

        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();

        int importedCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row

            String groupId = getCellValueAsString(row.getCell(0));
            String groupName = getCellValueAsString(row.getCell(1));
            String parentGroupId = getCellValueAsString(row.getCell(2));

            if (groupId == null || groupName == null) {
                Log.w(TAG, "Skipping invalid group row: Missing ID or Name");
                continue;
            }

            TableGroups group = new TableGroups(groupId, groupName, parentGroupId);
            daoGroups.insertOrUpdateGroup(group);
            importedCount++;
        }
        Log.d(TAG, "Imported " + importedCount + " groups");
    }

    private static void importItems(Context context, Workbook workbook) {
        Log.d(TAG, "Importing Items sheet");

        Sheet sheet = workbook.getSheet("Items");
        if (sheet == null) {
            Log.e(TAG, "Items sheet not found");
            return;
        }

        DaoItems daoItems = AppClient.getInstance(context).getAppDatabase().daoItems();
        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();

        int importedCount = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm");
        String currentDateTime = dateFormat.format(new java.util.Date());

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            // Read and convert cell values
            String itemId = getCellValueAsString(row.getCell(0));
            String itemName = getCellValueAsString(row.getCell(1));
            String category = getCellValueAsString(row.getCell(2));
            Integer storage = getCellValueAsInteger(row.getCell(3));
            Integer selling = getCellValueAsInteger(row.getCell(4));
            String groupId = getCellValueAsString(row.getCell(5));
            String dateCreated = getCellValueAsString(row.getCell(6));
            String dateUpdated = getCellValueAsString(row.getCell(7));

            // Validate and convert groupId
            if (groupId != null && daoGroups.getGroupById(groupId) == null) {
                Log.w(TAG, "Invalid group ID for item '" + itemId + "': " + groupId + ". Setting to null.");
                groupId = null;
            }

            if (itemId == null || itemName == null) {
                Log.w(TAG, "Skipping invalid item row: Missing ID or Name");
                continue;
            }

            TableItems item = new TableItems(
                    itemId,
                    itemName,
                    category != null ? category : "Unknown",
                    storage != null ? storage : 0,
                    selling != null ? selling : 0,
                    dateCreated != null ? dateCreated : currentDateTime,
                    dateUpdated != null ? dateUpdated : currentDateTime,
                    groupId
            );

            daoItems.insertOrUpdateItem(item);
            importedCount++;
        }

        Log.d(TAG, "Imported " + importedCount + " items");
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private static Integer getCellValueAsInteger(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();

        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid number format: " + cell.getStringCellValue(), e);
            }
        }

        return null;
    }

    private static void notifyOnMainThread(Context context, String message, boolean isSuccess) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(() -> Toast.makeText(context, message, isSuccess ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show());
    }
}
