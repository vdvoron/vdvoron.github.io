package com.zybooks.voronova_option1_final;

import java.util.*;

/**
 * Pure algorithm helpers for InventoryItem.
 * These methods have no Android or database dependencies,
 * which makes them easy to unit test.
 */
public final class InventoryAlgorithms {

    private InventoryAlgorithms() { }

    // -------------------------------------------------
    // Sorting
    // -------------------------------------------------

    /** Sorts items by name (case-insensitive), nulls first. */
    public static void sortByName(List<InventoryItem> list) {
        list.sort(Comparator.comparing(
                InventoryItem::getName,
                Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)
        ));
    }

    /** Sorts items by quantity in ascending order. */
    public static void sortByQuantity(List<InventoryItem> list) {
        list.sort(Comparator.comparingInt(InventoryItem::getQuantity));
    }

    // -------------------------------------------------
    // Binary search (requires pre-sorted list)
    // -------------------------------------------------

    /** Case-insensitive binary search by name on a list already sorted by name. */
    public static InventoryItem binarySearchByName(List<InventoryItem> sorted, String target) {
        int lo = 0, hi = sorted.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            InventoryItem m = sorted.get(mid);
            String name = m.getName();

            int cmp = (name == null && target == null) ? 0
                    : (name == null ? -1
                    : (target == null ? 1
                    : String.CASE_INSENSITIVE_ORDER.compare(name, target)));

            if (cmp == 0) return m;
            if (cmp < 0) lo = mid + 1; else hi = mid - 1;
        }
        return null;
    }

    /**
     * Binary search by quantity on a list already sorted by quantity.
     * Returns the first matching element if duplicates exist.
     */
    public static InventoryItem binarySearchByQuantity(List<InventoryItem> sorted, int targetQty) {
        int lo = 0, hi = sorted.size() - 1;
        int foundIndex = -1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int q = sorted.get(mid).getQuantity();

            if (q == targetQty) {
                foundIndex = mid;
                hi = mid - 1;           // keep searching left for the first occurrence
            } else if (q < targetQty) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return (foundIndex >= 0) ? sorted.get(foundIndex) : null;
    }

    // -------------------------------------------------
    // Fast lookups by name (case-insensitive HashMap)
    // -------------------------------------------------

    /** Builds a case-insensitive index by item name. */
    public static Map<String, InventoryItem> buildIndexByName(List<InventoryItem> items) {
        Map<String, InventoryItem> map = new HashMap<>();
        for (InventoryItem i : items) {
            String key = (i.getName() == null) ? "" : i.getName().toLowerCase(Locale.US);
            map.put(key, i);
        }
        return map;
    }

    /** Retrieves an item by name from the index (case-insensitive). */
    public static InventoryItem getByNameIndexed(Map<String, InventoryItem> index, String name) {
        String key = (name == null) ? "" : name.toLowerCase(Locale.US);
        return index.get(key);
    }

    /** Adds or replaces an item in the name index. */
    public static void upsertIntoNameIndex(Map<String, InventoryItem> index, InventoryItem item) {
        String key = (item.getName() == null) ? "" : item.getName().toLowerCase(Locale.US);
        index.put(key, item);
    }

    /** Removes an item from the name index. */
    public static void removeFromNameIndex(Map<String, InventoryItem> index, InventoryItem item) {
        String key = (item.getName() == null) ? "" : item.getName().toLowerCase(Locale.US);
        index.remove(key);
    }

    // -------------------------------------------------
    // Optional: O(1) lookups by ID
    // -------------------------------------------------

    /** Builds a map for fast ID-based lookups using the primary key. */
    public static Map<Long, InventoryItem> buildIdCache(List<InventoryItem> list) {
        Map<Long, InventoryItem> map = new HashMap<>();
        for (InventoryItem i : list) {
            map.put((long) i.getId(), i);
        }
        return map;
    }
}