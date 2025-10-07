package com.zybooks.voronova_option1_final;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class InventoryAlgorithmsTest {

    // Helper to build items (your InventoryItem ctor is (String name, int quantity, String username))
    private static InventoryItem item(String name, int qty) {
        return new InventoryItem(name, qty, "tester");
    }

    @Test
    public void sortByName_ordersLexicographically() {
        List<InventoryItem> list = new ArrayList<>(Arrays.asList(
                item("Banana", 2),
                item("apple", 3),
                item("carrot", 1)
        ));

        InventoryAlgorithms.sortByName(list);

        assertEquals("apple",  list.get(0).getName());
        assertEquals("Banana", list.get(1).getName());
        assertEquals("carrot", list.get(2).getName());
    }

    @Test
    public void sortByQuantity_ordersAscending() {
        List<InventoryItem> list = new ArrayList<>(Arrays.asList(
                item("Pen", 5),
                item("Notebook", 2),
                item("Pencil", 2),
                item("Eraser", 9)
        ));

        InventoryAlgorithms.sortByQuantity(list);

        assertEquals(2, list.get(0).getQuantity());
        assertEquals(2, list.get(1).getQuantity());
        assertEquals(5, list.get(2).getQuantity());
        assertEquals(9, list.get(3).getQuantity());
    }

    @Test
    public void binarySearchByName_findsExisting_afterSort() {
        List<InventoryItem> list = new ArrayList<>(Arrays.asList(
                item("Pen", 5),
                item("Notebook", 2),
                item("Marker", 3)
        ));
        InventoryAlgorithms.sortByName(list);

        InventoryItem found = InventoryAlgorithms.binarySearchByName(list, "marker");
        assertNotNull(found);
        assertEquals("Marker", found.getName());
    }

    @Test
    public void binarySearchByName_returnsNull_whenMissing() {
        List<InventoryItem> list = new ArrayList<>(Arrays.asList(
                item("A", 1), item("B", 2), item("C", 3)
        ));
        InventoryAlgorithms.sortByName(list);

        assertNull(InventoryAlgorithms.binarySearchByName(list, "Z"));
    }

    @Test
    public void binarySearchByQuantity_findsFirst_withDuplicates() {
        List<InventoryItem> list = new ArrayList<>(Arrays.asList(
                item("A", 1),
                item("B", 3),
                item("C", 3),
                item("D", 7)
        ));
        InventoryAlgorithms.sortByQuantity(list);

        InventoryItem found = InventoryAlgorithms.binarySearchByQuantity(list, 3);
        assertNotNull(found);
        // the first occurrence in the sorted list is “B”
        assertEquals("B", found.getName());
    }

    @Test
    public void index_build_lookup_upsert_remove() {
        List<InventoryItem> list = new ArrayList<>(Arrays.asList(
                item("Pen", 5), item("Eraser", 1)
        ));

        Map<String, InventoryItem> idx = InventoryAlgorithms.buildIndexByName(list);

        // lookup
        assertEquals(5, InventoryAlgorithms.getByNameIndexed(idx, "pen").getQuantity());
        assertNull(InventoryAlgorithms.getByNameIndexed(idx, "not-here"));

        // upsert (add)
        InventoryItem added = item("Ruler", 2);
        InventoryAlgorithms.upsertIntoNameIndex(idx, added);
        assertEquals(2, InventoryAlgorithms.getByNameIndexed(idx, "ruler").getQuantity());

        // upsert (replace)
        InventoryItem replaced = item("Pen", 9);
        InventoryAlgorithms.upsertIntoNameIndex(idx, replaced);
        assertEquals(9, InventoryAlgorithms.getByNameIndexed(idx, "pen").getQuantity());

        // remove
        InventoryAlgorithms.removeFromNameIndex(idx, replaced);
        assertNull(InventoryAlgorithms.getByNameIndexed(idx, "pen"));
    }
}