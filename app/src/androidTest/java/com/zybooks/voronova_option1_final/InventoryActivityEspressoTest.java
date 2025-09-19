package com.zybooks.voronova_option1_final;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso tests without toast matching.
 *
 * For validation cases, we assert that the table row count does NOT increase.
 * This avoids flakiness from transient toast windows on emulators.
 */
@RunWith(AndroidJUnit4.class)
public class InventoryActivityEspressoTest {

    @Rule
    public ActivityScenarioRule<InventoryActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            InventoryActivity.class
                    ).putExtra(MainActivity.EXTRA_USERNAME, "testuser")
            );

    /** Number of rows present before each test (usually 1 header row). */
    private int baseRowCount;

    @Before
    public void captureBaseRowCount() {
        rule.getScenario().onActivity(activity -> {
            TableLayout table = activity.findViewById(R.id.inventoryTable);
            baseRowCount = table.getChildCount();
        });
    }

    // ---------- small utilities ----------

    /** Assert a ViewGroup has exactly expected child count. */
    private static ViewAssertion hasChildCount(int expected) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) throw noViewFoundException;
            if (!(view instanceof ViewGroup)) {
                throw new AssertionError("View is not a ViewGroup: " + view);
            }
            int actual = ((ViewGroup) view).getChildCount();
            if (actual != expected) {
                throw new AssertionError("Expected childCount=" + expected + " but was " + actual);
            }
        };
    }

    // ---------- tests ----------

    /** Happy path: adding an item shows a new row with name and quantity. */
    @Test
    public void addItem_showsRowWithNameAndQty() {
        onView(withId(R.id.itemNameInput)).perform(replaceText("Apples"), closeSoftKeyboard());
        onView(withId(R.id.itemQtyInput)).perform(replaceText("3"), closeSoftKeyboard());
        onView(withId(R.id.addItemButton)).perform(click());

        // Row count increased by 1 and the values are visible.
        onView(withId(R.id.inventoryTable)).check(hasChildCount(baseRowCount + 1));
        onView(withText("Apples")).check(matches(isDisplayed()));
        onView(withText("3")).check(matches(isDisplayed()));
    }

    /** Validation: empty name should NOT add a row. */
    @Test
    public void addItem_emptyName_doesNotAddRow() {
        onView(withId(R.id.itemNameInput)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.itemQtyInput)).perform(replaceText("2"), closeSoftKeyboard());
        onView(withId(R.id.addItemButton)).perform(click());

        onView(withId(R.id.inventoryTable)).check(hasChildCount(baseRowCount));
    }

    /** Validation: empty quantity should NOT add a row. */
    @Test
    public void addItem_emptyQuantity_doesNotAddRow() {
        onView(withId(R.id.itemNameInput)).perform(replaceText("Anything"), closeSoftKeyboard());
        // Ensure the numeric field is truly empty
        onView(withId(R.id.itemQtyInput)).perform(click(), clearText(), closeSoftKeyboard());
        onView(withId(R.id.addItemButton)).perform(click());

        onView(withId(R.id.inventoryTable)).check(hasChildCount(baseRowCount));
    }

    /** Validation: too-large quantity should NOT add a row. */
    @Test
    public void addItem_tooLargeQuantity_doesNotAddRow() {
        onView(withId(R.id.itemNameInput)).perform(replaceText("Big"), closeSoftKeyboard());
        onView(withId(R.id.itemQtyInput)).perform(replaceText("10000000"), closeSoftKeyboard());
        onView(withId(R.id.addItemButton)).perform(click());

        onView(withId(R.id.inventoryTable)).check(hasChildCount(baseRowCount));
    }

    /** Editing: tap quantity, change to 7, confirm, and see the cell update. */
    @Test
    public void updateQuantity_dialogChangesValue() {
        // Seed a row.
        onView(withId(R.id.itemNameInput)).perform(replaceText("Seeds"), closeSoftKeyboard());
        onView(withId(R.id.itemQtyInput)).perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.addItemButton)).perform(click());
        onView(withId(R.id.inventoryTable)).check(hasChildCount(baseRowCount + 1));

        // Open the "Update Quantity" dialog by tapping the "1" cell.
        onView(withText("1")).perform(click());

        // Replace with 7 and confirm.
        onView(withText("Update")).check(matches(isDisplayed()));
        // In AlertDialog with setView(EditText), framework assigns android.R.id.input to the EditText
        onView(withId(android.R.id.input)).perform(replaceText("7"), closeSoftKeyboard());
        onView(withText("Update")).perform(click());

        // Updated value should be visible.
        onView(withText("7")).check(matches(isDisplayed()));
    }

    /** Deletion: add a row, press its "Delete" action, confirm, and row disappears. */
    @Test
    public void deleteItem_removesRow() {
        // Seed a row.
        onView(withId(R.id.itemNameInput)).perform(replaceText("ToDelete"), closeSoftKeyboard());
        onView(withId(R.id.itemQtyInput)).perform(replaceText("4"), closeSoftKeyboard());
        onView(withId(R.id.addItemButton)).perform(click());
        onView(withId(R.id.inventoryTable)).check(hasChildCount(baseRowCount + 1));

        // Click the Delete action in the row that has the "ToDelete" label.
        onView(allOf(withText(R.string.delete_button), hasSibling(withText("ToDelete"))))
                .perform(click());

        // Confirm in the dialog.
        onView(withText(R.string.confirm_delete_title)).check(matches(isDisplayed()));
        onView(withText(R.string.delete_button)).perform(click());

        // Row count is back to base; the name should not be visible anymore.
        onView(withId(R.id.inventoryTable)).check(hasChildCount(baseRowCount));
        onView(withText("ToDelete")).check(matches(not(isDisplayed())));
    }

    /** Navigation: clicking the SMS header opens the SMS screen (by checking a view text). */
    @Test
    public void navigateToSms_screenOpens() {
        onView(withId(R.id.smsButton)).perform(click());
        onView(withText(R.string.notification_settings)).check(matches(isDisplayed()));
    }
}