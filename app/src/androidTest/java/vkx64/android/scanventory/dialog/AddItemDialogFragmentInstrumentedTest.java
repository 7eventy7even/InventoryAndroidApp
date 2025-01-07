package vkx64.android.scanventory.dialog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import vkx64.android.scanventory.MainActivity;
import vkx64.android.scanventory.R;
import vkx64.android.scanventory.database.AppClient;
import vkx64.android.scanventory.database.AppDatabase;
import vkx64.android.scanventory.database.TableItems;

@RunWith(AndroidJUnit4.class)
public class AddItemDialogFragmentInstrumentedTest {

    private AppDatabase testDatabase;
    private AppClient appClient;

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {

        // Create an in-memory database for testing
        Context context = ApplicationProvider.getApplicationContext();
        testDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        // Replace AppClient's database with the test database
        appClient = AppClient.getInstance(context);
        appClient.setAppDatabase(testDatabase);
    }

    @After
    public void tearDown() {
        if (testDatabase != null) {
            testDatabase.close();
        }
    }

    @Test
    public void testAddItemWithValidInputs() {
        // Open the AddItemDialogFragment
        onView(withId(R.id.ibNewItem)).perform(click());

        // Input values into the dialog
        onView(withId(R.id.etItemId)).perform(replaceText("item123"));
        onView(withId(R.id.etItemName)).perform(replaceText("Test Item"));
        onView(withId(R.id.etItemCategory)).perform(replaceText("Electronics"));
        onView(withId(R.id.etItemStorage)).perform(replaceText("10"));
        onView(withId(R.id.etItemSelling)).perform(replaceText("20"));

        // Submit the form
        onView(withId(R.id.btnSubmit)).perform(click());

        // General Tip: Verify the view hierarchy if needed
        // You can use dump() to check if views exist and are visible
        // onView(withId(R.id.btnSubmit)).check(matches(isDisplayed()));

        // Verify the item was added to the database
        activityRule.getScenario().onActivity(activity -> {
            List<TableItems> items = testDatabase.daoItems().getAllItems();
            assertEquals(1, items.size());
            assertEquals("item123", items.get(0).getItem_id());
            assertEquals("Test Item", items.get(0).getItem_name());
            assertEquals("Electronics", items.get(0).getItem_category());
            assertEquals(10, items.get(0).getItem_storage());
            assertEquals(20, items.get(0).getItem_selling());
        });
    }

    @Test
    public void testAddItemWithEmptyCategoryAndNumbers() {
        // Open the AddItemDialogFragment
        onView(withId(R.id.ibNewItem)).perform(click());

        // Input values into the dialog, leaving optional fields empty
        onView(withId(R.id.etItemId)).perform(replaceText("item456"));
        onView(withId(R.id.etItemName)).perform(replaceText("Test Item 2"));
        onView(withId(R.id.etItemCategory)).perform(replaceText(""));
        onView(withId(R.id.etItemStorage)).perform(replaceText(""));
        onView(withId(R.id.etItemSelling)).perform(replaceText(""));

        // Submit the form
        onView(withId(R.id.btnSubmit)).perform(click());

        // Verify the item was added with default values
        activityRule.getScenario().onActivity(activity -> {
            List<TableItems> items = testDatabase.daoItems().getAllItems();
            assertEquals(1, items.size());
            assertEquals("item456", items.get(0).getItem_id());
            assertEquals("Test Item 2", items.get(0).getItem_name());
            assertEquals(null, items.get(0).getItem_category());
            assertEquals(0, items.get(0).getItem_storage());
            assertEquals(0, items.get(0).getItem_selling());
        });
    }

    @Test
    public void testAddItemWithEmptyRequiredFields() {
        // Open the AddItemDialogFragment
        onView(withId(R.id.ibNewItem)).perform(click());

        // Leave required fields empty
        onView(withId(R.id.etItemId)).perform(replaceText(""));
        onView(withId(R.id.etItemName)).perform(replaceText(""));

        // Submit the form
        onView(withId(R.id.btnSubmit)).perform(click());

        // Verify no item was added to the database
        activityRule.getScenario().onActivity(activity -> {
            List<TableItems> items = testDatabase.daoItems().getAllItems();
            assertTrue(items.isEmpty());
        });
    }

    @Test
    public void testAddItemWithSpacesInItemId() {
        // Open the AddItemDialogFragment
        onView(withId(R.id.ibNewItem)).perform(click());

        // Enter spaces in the Item ID field
        onView(withId(R.id.etItemId)).perform(replaceText("item 123"));
        onView(withId(R.id.etItemName)).perform(replaceText("Test Item"));

        // Submit the form
        onView(withId(R.id.btnSubmit)).perform(click());

        // Verify no item was added to the database due to invalid input
        activityRule.getScenario().onActivity(activity -> {
            List<TableItems> items = testDatabase.daoItems().getAllItems();
            assertTrue(items.isEmpty());
        });
    }

    @Test
    public void testAddItemWithMaximumFieldLengths() {
        // Open the AddItemDialogFragment
        onView(withId(R.id.ibNewItem)).perform(click());

        // Enter maximum length values
        String longString = "a".repeat(255); // Assuming 255 is the max length
        onView(withId(R.id.etItemId)).perform(replaceText(longString));
        onView(withId(R.id.etItemName)).perform(replaceText(longString));
        onView(withId(R.id.etItemCategory)).perform(replaceText(longString));
        onView(withId(R.id.etItemStorage)).perform(replaceText("999999"));
        onView(withId(R.id.etItemSelling)).perform(replaceText("999999"));

        // Submit the form
        onView(withId(R.id.btnSubmit)).perform(click());

        // Verify the item was added successfully
        activityRule.getScenario().onActivity(activity -> {
            List<TableItems> items = testDatabase.daoItems().getAllItems();
            assertEquals(1, items.size());
            assertEquals(longString, items.get(0).getItem_id());
            assertEquals(longString, items.get(0).getItem_name());
            assertEquals(longString, items.get(0).getItem_category());
            assertEquals(999999, items.get(0).getItem_storage());
            assertEquals(999999, items.get(0).getItem_selling());
        });
    }
}
