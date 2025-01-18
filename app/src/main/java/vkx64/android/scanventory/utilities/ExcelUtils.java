package vkx64.android.scanventory.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.DaoItems;
import vkx64.android.scanventory.database.DaoGroups;
import vkx64.android.scanventory.database.DaoMarkets;
import vkx64.android.scanventory.database.TableItems;
import vkx64.android.scanventory.database.TableGroups;
import vkx64.android.scanventory.database.TableMarkets;

public class ExcelUtils {

    private static final String TAG = "ExcelUtils";

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

    public static void importXlsxToDatabase(Context context, Uri fileUri) {
        // Open the workbook and handle database operations
        new Thread(() -> {
            try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Log.d(TAG, "Workbook opened from file: " + fileUri);

                // Start a single transaction for the entire import process
                AppClient.getInstance(context).getAppDatabase().beginTransaction();

                try {
                    // Import groups first to ensure all references are valid
                    importGroups(context, workbook);

                    // Import items next
                    importItems(context, workbook);

                    // Commit the transaction if everything is valid
                    AppClient.getInstance(context).getAppDatabase().setTransactionSuccessful();
                    notifyOnMainThread(context, "Database imported successfully!", true);

                } catch (Exception e) {
                    Log.e(TAG, "Error during import. Rolling back transaction.", e);
                    notifyOnMainThread(context, "Import failed: " + e.getMessage(), false);
                    throw e; // Rethrow to ensure rollback happens

                } finally {
                    // End the transaction (rollback happens automatically if not set successful)
                    AppClient.getInstance(context).getAppDatabase().endTransaction();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to import file", e);
//                notifyOnMainThread(context, "Failed to import database: " + e.getMessage(), false);
            }
        }).start();
    }

    private static void createGroupSheet(Context context, Workbook workbook) {
        // Constants
        Sheet sheet = workbook.createSheet("Groups");
        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();
        int rowIndex = 1;

        // Header rows
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Group ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Parent Group ID");

        // Fetch all groups from the database
        List<TableGroups> groups = daoGroups.getAllGroups();

        // Create rows
        for (TableGroups group : groups) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(group.getGroup_id());
            row.createCell(1).setCellValue(group.getGroup_name());
            row.createCell(2).setCellValue(group.getGroup_parent() != null ? group.getGroup_parent() : "");
        }

        Log.d(TAG, "Groups sheet populated successfully");
    }

    private static void importGroups(Context context, Workbook workbook) throws Exception {
        // Constants
        Sheet sheet = workbook.getSheet("Groups");
        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();
        int importedCount = 0;

        // Check if Sheet Exists
        if (sheet == null) {
            throw new Exception("Group Sheet not Found");
        }

        // Iterate through rows
        for (Row row : sheet) {

            // Skip header row
            if (row.getRowNum() == 0) {
                continue;
            }

            // Read and convert cell values
            String groupId = getCellValueAsString(row.getCell(0));
            String groupName = getCellValueAsString(row.getCell(1));
            String parentGroupId = getCellValueAsString(row.getCell(2));

            // Validation: Skip invalid rows
            if (groupId == null || groupName == null) {
                throw new Exception("Invalid Group Row: Missing ID or Name");
            }

            // Data Constructor
            TableGroups group = new TableGroups(
                    groupId,
                    groupName,
                    parentGroupId
            );

            // Insert or update group
            daoGroups.insertOrUpdateGroup(group);
            importedCount++;
        }

        Log.d(TAG, "Imported " + importedCount + " groups");
    }

    private static void createItemSheet(Context context, Workbook workbook) {
        // Constants
        Sheet sheet = workbook.createSheet("Items");
        DaoItems daoItems = AppClient.getInstance(context).getAppDatabase().daoItems();
        DaoMarkets daoMarkets = AppClient.getInstance(context).getAppDatabase().daoMarkets();
        int rowIndex = 1;

        // Fetch all items from the database
        List<TableItems> items = daoItems.getAllItems();

        // Fetch all unique market names
        List<String> allMarkets = daoMarkets.getAllUniqueMarkets(); // A DAO method that retrieves all distinct market names

        // Header Rows
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Item ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Category");
        header.createCell(3).setCellValue("Storage");
        header.createCell(4).setCellValue("Group ID");
        header.createCell(5).setCellValue("Date Created");
        header.createCell(6).setCellValue("Date Updated");

        // Add a header for each unique market with the "Selling:" prefix
        int marketStartIndex = 7;
        for (int i = 0; i < allMarkets.size(); i++) {
            header.createCell(marketStartIndex + i).setCellValue("Selling: " + allMarkets.get(i));
        }

        // Create rows for items
        for (TableItems item : items) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getItem_id());
            row.createCell(1).setCellValue(item.getItem_name());
            row.createCell(2).setCellValue(item.getItem_category() != null ? item.getItem_category() : "");
            row.createCell(3).setCellValue(item.getItem_storage() != 0 ? item.getItem_storage() : 0);
            row.createCell(4).setCellValue(item.getGroup_id() != null ? item.getGroup_id() : "");
            row.createCell(5).setCellValue(item.getItem_created() != null ? item.getItem_created() : "");
            row.createCell(6).setCellValue(item.getItem_updated() != null ? item.getItem_updated() : "");

            // Fetch associated markets for the item
            List<TableMarkets> markets = daoMarkets.getMarketsByItemId(item.getItem_id());

            // Map markets to their columns
            for (TableMarkets market : markets) {
                for (int i = 0; i < allMarkets.size(); i++) {
                    if (market.getMarket_name().equals(allMarkets.get(i))) {
                        row.createCell(marketStartIndex + i).setCellValue(market.getMarket_quantity());
                    }
                }
            }
        }

        Log.d(TAG, "Items sheet populated successfully with market columns.");
    }

    private static void importItems(Context context, Workbook workbook) throws Exception {
        // Constants
        Sheet sheet = workbook.getSheet("Items");
        DaoItems daoItems = AppClient.getInstance(context).getAppDatabase().daoItems();
        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();
        DaoMarkets daoMarkets = AppClient.getInstance(context).getAppDatabase().daoMarkets();

        // Check if Sheet Exists
        if (sheet == null) {
            throw new Exception("Item Sheet not Found");
        }

        int importedCount = 0;

        // Iterate through rows
        for (Row row : sheet) {

            // Skip header row
            if (row.getRowNum() == 0) {
                continue;
            }

            // Read and Convert Cell Values
            String itemId = getCellValueAsString(row.getCell(0));
            String itemName = getCellValueAsString(row.getCell(1));
            String category = getCellValueAsString(row.getCell(2));
            Integer storage = getCellValueAsInteger(row.getCell(3));
            String groupId = getCellValueAsString(row.getCell(4));
            String dateCreated = getCellValueAsString(row.getCell(5));
            String dateUpdated = getCellValueAsString(row.getCell(6));

            // Validation: Skip invalid rows
            if (itemId == null || itemName == null) {
                throw new Exception("Invalid Item Row: Missing ID or Name");
            }

            // Validate Existence and Convert GroupId
            if (groupId != null && daoGroups.getGroupById(groupId) == null) {
                throw new Exception("Invalid Group ID for Item '" + itemId + "': " + groupId);
            }

            // Data Constructor
            TableItems item = new TableItems(
                    itemId,
                    itemName,
                    category != null ? category : "Unknown",
                    storage != null ? storage : 0,
                    dateCreated != null ? dateCreated : DateUtils.getCurrentDateTime(),
                    dateUpdated != null ? dateUpdated : DateUtils.getCurrentDateTime(),
                    groupId
            );

            // Insert or Update Item
            daoItems.insertOrUpdateItem(item);
            importedCount++;

            // Process Selling: columns (markets)
            for (int i = 7; i < row.getLastCellNum(); i++) {
                String columnName = sheet.getRow(0).getCell(i).getStringCellValue();
                if (columnName.startsWith("Selling: ")) {
                    String marketName = columnName.substring(9); // Extract market name
                    Integer marketQuantity = getCellValueAsInteger(row.getCell(i));

                    // Validation: Check if marketQuantity exceeds storage
                    if (marketQuantity != null && storage != null && marketQuantity > storage) {
                        throw new Exception("Market quantity (" + marketQuantity + ") exceeds storage (" + storage + ") for Item '" + itemId + "'");
                    }

                    // Insert market data into the TableMarkets database
                    TableMarkets tableMarket = new TableMarkets(
                            marketQuantity != null ? marketQuantity : 0,
                            marketName,
                            itemId
                    );

                    daoMarkets.insertOrUpdateMarket(tableMarket);
                }
            }
        }

        Log.d(TAG, "Imported " + importedCount + " items");
    }


    private static String getCellValueAsString(Cell cell) {
        // Return null if the cell is either null or blank
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        // Return null if the cell is empty
        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
            return null;
        }

        // Convert cell value to string or null
        switch (cell.getCellType()) {
            case _NONE:
                return null;
            case BLANK:
                return null;
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
        // Return null if the cell is either null or blank
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        // Return the cell's numeric value as an integer if the cell type is numeric.
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }

        // Convert cell value to int or null
        switch (cell.getCellType()) {
            case _NONE:
                return null;
            case BLANK:
                return null;
            case STRING:
                return Integer.parseInt(cell.getStringCellValue().trim());
            case BOOLEAN:
                return null;
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            default:
                return null;
        }
    }

    private static void notifyOnMainThread(Context context, String message, boolean isSuccess) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(() -> Toast.makeText(context, message, isSuccess ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show());
    }
}
