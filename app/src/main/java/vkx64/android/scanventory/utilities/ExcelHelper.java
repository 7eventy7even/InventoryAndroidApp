package vkx64.android.scanventory.utilities;

import android.content.Context;
import android.net.Uri;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.DaoItems;
import vkx64.android.scanventory.database.DaoGroups;
import vkx64.android.scanventory.database.TableItems;
import vkx64.android.scanventory.database.TableGroups;

public class ExcelHelper {

    /**
     * Export the database (items and groups) to an Excel file.
     *
     * @param context  Application context
     * @param fileUri  URI of the output file
     */
    public static void exportDatabaseToXlsx(Context context, Uri fileUri) {
        new Thread(() -> {
            try (Workbook workbook = new XSSFWorkbook()) {
                // Create and populate the "Items" sheet
                createItemSheet(context, workbook);

                // Create and populate the "Groups" sheet
                createGroupSheet(context, workbook);

                // Write the workbook to the file
                try (OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri)) {
                    workbook.write(outputStream);
                }

                // Notify success
                notifyOnMainThread(() -> System.out.println("Database exported successfully!"));
            } catch (Exception e) {
                // Notify failure
                notifyOnMainThread(() -> System.err.println("Failed to export file: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Create and populate the "Items" sheet in the workbook.
     */
    private static void createItemSheet(Context context, Workbook workbook) {
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

        // Add item rows
        int rowIndex = 1;
        for (TableItems item : items) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getItem_id());
            row.createCell(1).setCellValue(item.getItem_name());
            row.createCell(2).setCellValue(item.getItem_category());
            row.createCell(3).setCellValue(item.getItem_storage());
            row.createCell(4).setCellValue(item.getItem_selling());
            row.createCell(5).setCellValue(item.getGroup_id() != null ? item.getGroup_id() : "");
            row.createCell(6).setCellValue(item.getItem_created());
            row.createCell(7).setCellValue(item.getItem_updated());
        }
    }

    /**
     * Create and populate the "Groups" sheet in the workbook.
     */
    private static void createGroupSheet(Context context, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Groups");

        // Add header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Group ID");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Parent Group ID");

        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();
        List<TableGroups> groups = daoGroups.getAllGroups();

        // Add group rows
        int rowIndex = 1;
        for (TableGroups group : groups) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(group.getGroup_id());
            row.createCell(1).setCellValue(group.getGroup_name());
            // Replace null with "none" for export
            row.createCell(2).setCellValue(group.getGroup_parent() != null ? group.getGroup_parent() : "none");
        }
    }

    /**
     * Import items and groups from an Excel file into the database.
     *
     * @param context  Application context
     * @param fileUri  URI of the input file
     */
    public static void importXlsxToDatabase(Context context, Uri fileUri) {
        new Thread(() -> {
            try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
                Workbook workbook = new XSSFWorkbook(inputStream);

                // Import groups first to ensure all references are valid
                importGroups(context, workbook);

                // Import items next
                importItems(context, workbook);

                // Notify success
                notifyOnMainThread(() -> System.out.println("Database imported successfully!"));
            } catch (Exception e) {
                // Notify failure
                notifyOnMainThread(() -> System.err.println("Failed to import file: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Import groups from the "Groups" sheet in the workbook.
     */
    private static void importGroups(Context context, Workbook workbook) {
        Sheet sheet = workbook.getSheet("Groups");
        if (sheet == null) return;

        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row

            String groupId = row.getCell(0).getStringCellValue();
            String groupName = row.getCell(1).getStringCellValue();
            String parentGroupId = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;

            // Replace "none" with null for import
            if ("none".equals(parentGroupId)) {
                parentGroupId = null;
            }

            TableGroups group = new TableGroups(groupId, groupName, parentGroupId);
            daoGroups.insertOrUpdateGroup(group);
        }
    }

    /**
     * Import items from the "Items" sheet in the workbook.
     */
    private static void importItems(Context context, Workbook workbook) {
        Sheet sheet = workbook.getSheet("Items");
        if (sheet == null) return;

        DaoItems daoItems = AppClient.getInstance(context).getAppDatabase().daoItems();
        DaoGroups daoGroups = AppClient.getInstance(context).getAppDatabase().daoGroups();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header row

            String itemId = row.getCell(0).getStringCellValue();
            String itemName = row.getCell(1).getStringCellValue();
            String category = row.getCell(2).getStringCellValue();
            int storage = (int) row.getCell(3).getNumericCellValue();
            int selling = (int) row.getCell(4).getNumericCellValue();
            String groupId = row.getCell(5) != null ? row.getCell(5).getStringCellValue() : null;
            String dateCreated = row.getCell(6) != null ? row.getCell(6).getStringCellValue() : null;
            String dateUpdated = row.getCell(7) != null ? row.getCell(7).getStringCellValue() : null;

            // Skip invalid group references
            if (groupId != null && daoGroups.getGroupById(groupId) == null) continue;

            TableItems item = new TableItems(itemId, itemName, category, storage, selling, dateCreated, dateUpdated, groupId);
            daoItems.insertOrUpdateItem(item);
        }
    }

    /**
     * Run a task on the main thread.
     */
    private static void notifyOnMainThread(Runnable runnable) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(runnable);
    }
}
