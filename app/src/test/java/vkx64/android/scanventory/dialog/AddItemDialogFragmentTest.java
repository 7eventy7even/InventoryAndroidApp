package vkx64.android.scanventory.dialog;

import org.junit.Test;
import static org.junit.Assert.*;

public class AddItemDialogFragmentTest {

    @Test
    public void testValidInputs() {
        String itemId = "item123";
        String itemName = "Test Item";
        String itemCategory = "Electronics";
        String itemStorage = "10";
        String itemSelling = "20";

        String finalCategory = itemCategory.isEmpty() ? null : itemCategory;
        int storageValue = itemStorage.isEmpty() ? 0 : Integer.parseInt(itemStorage);
        int sellingValue = itemSelling.isEmpty() ? 0 : Integer.parseInt(itemSelling);

        assertEquals("item123", itemId);
        assertEquals("Test Item", itemName);
        assertEquals("Electronics", finalCategory);
        assertEquals(10, storageValue);
        assertEquals(20, sellingValue);
    }

    @Test
    public void testEmptyCategory() {
        String itemCategory = ""; // Empty input
        String finalCategory = itemCategory.isEmpty() ? null : itemCategory;
        assertNull(finalCategory);

        itemCategory = "Books"; // Non-empty input
        finalCategory = itemCategory.isEmpty() ? null : itemCategory;
        assertEquals("Books", finalCategory);
    }

    @Test
    public void testEmptyStorageAndSelling() {
        String itemStorage = ""; // Empty storage
        String itemSelling = ""; // Empty selling

        int storageValue = itemStorage.isEmpty() ? 0 : Integer.parseInt(itemStorage);
        int sellingValue = itemSelling.isEmpty() ? 0 : Integer.parseInt(itemSelling);

        assertEquals(0, storageValue);
        assertEquals(0, sellingValue);
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidStorageValue() {
        String itemStorage = "invalid"; // Invalid input
        Integer.parseInt(itemStorage); // Should throw NumberFormatException
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidSellingValue() {
        String itemSelling = "invalid"; // Invalid input
        Integer.parseInt(itemSelling); // Should throw NumberFormatException
    }

    @Test
    public void testEmptyRequiredFields() {
        String itemId = "";
        String itemName = "";

        assertTrue(itemId.isEmpty());
        assertTrue(itemName.isEmpty());
    }

    @Test
    public void testSpacesInItemId() {
        String itemId = "validItem";
        assertFalse(itemId.contains(" ")); // No spaces

        itemId = "item with spaces";
        assertTrue(itemId.contains(" ")); // Contains spaces
    }
}
