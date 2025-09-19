package com.zybooks.voronova_option1_final;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for InventoryValidator.
 * These run on the JVM (no Android device needed).
 */
public class InventoryValidatorTest {

    @Test
    public void parseQuantity_validNumber_returnsValue() {
        // Normal positive number should parse correctly
        assertEquals(Integer.valueOf(5),
                InventoryValidator.parseQuantity("5"));
    }

    @Test
    public void parseQuantity_withSpaces_trimsAndParses() {
        // Leading/trailing spaces should be ignored
        assertEquals(Integer.valueOf(12),
                InventoryValidator.parseQuantity("   12  "));
    }

    @Test
    public void parseQuantity_empty_returnsNull() {
        // Empty input is invalid
        assertNull(InventoryValidator.parseQuantity(""));
        assertNull(InventoryValidator.parseQuantity("   "));
        assertNull(InventoryValidator.parseQuantity(null));
    }

    @Test
    public void parseQuantity_negative_returnsNull() {
        // Negative numbers are not allowed
        assertNull(InventoryValidator.parseQuantity("-3"));
    }

    @Test
    public void parseQuantity_tooLarge_returnsNull() {
        // Larger than MAX_QTY should be rejected
        int tooBig = InventoryValidator.MAX_QTY + 1;
        assertNull(InventoryValidator.parseQuantity(String.valueOf(tooBig)));
    }

    @Test
    public void parseQuantity_nonNumeric_returnsNull() {
        // Non-numeric strings should fail
        assertNull(InventoryValidator.parseQuantity("abc"));
        assertNull(InventoryValidator.parseQuantity("12x"));
    }
}