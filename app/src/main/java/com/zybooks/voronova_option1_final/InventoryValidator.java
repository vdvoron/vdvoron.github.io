package com.zybooks.voronova_option1_final;

/** Small helper so we can unit-test business rules without Android stuff. */
public final class InventoryValidator {

    /** App-wide max quantity to prevent nonsense values. Keep in sync with UI. */
    public static final int MAX_QTY = 1_000_000;

    private InventoryValidator() {}

    /** Return null if invalid; otherwise the parsed non-negative quantity within MAX_QTY. */
    public static Integer parseQuantity(String qtyStr) {
        if (qtyStr == null) return null;
        qtyStr = qtyStr.trim();
        if (qtyStr.isEmpty()) return null;

        // quick length guard (Integer.MAX_VALUE is 10 digits)
        if (qtyStr.length() > 10) return null;

        try {
            long asLong = Long.parseLong(qtyStr);
            if (asLong < 0 || asLong > MAX_QTY) return null;
            return (int) asLong;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** True when a name looks usable: not null/blank. */
    public static boolean isNameValid(String name) {
        return name != null && !name.trim().isEmpty();
    }
}