package org.alps.core.common;

public class AssertHelper {

    public static void assertTrue(boolean expr, String errorMsg) {
        if (!expr) {
            throw new AssertionError(errorMsg);
        }
    }
}
