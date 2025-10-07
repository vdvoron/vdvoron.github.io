package com.zybooks.voronova_option1_final;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Temporarily disabled: relies on DB wiring we haven't finished yet.
 * Keeping this class so the androidTest source set compiles cleanly.
 */
@RunWith(AndroidJUnit4.class)
@Ignore("Disabled until DB enhancement week")
public class UserDaoAndroidTest {

    @Test
    public void placeholder() {
        // This passes and is skipped due to @Ignore.
        assertTrue(true);
    }
}