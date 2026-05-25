package com.coinmarket.common.util;

import java.util.concurrent.ThreadLocalRandom;

public final class BarcodeUtil {

    private static final String PREFIX = "76";

    private BarcodeUtil() {}

    /**
     * Generate a 13-digit Code128-compatible barcode number.
     * Format: 76 + 10-digit timestamp-random + 1 Luhn check digit
     */
    public static String generate() {
        long base = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        String body = PREFIX + (base % 10_000_000_000L) + random;
        body = body.substring(0, 12); // ensure 12 digits for 13 total with check
        return body + luhnCheckDigit(body);
    }

    public static boolean isValid(String barcode) {
        if (barcode == null || barcode.length() != 13) return false;
        if (!barcode.startsWith(PREFIX)) return false;
        return barcode.charAt(12) == luhnCheckDigit(barcode.substring(0, 12));
    }

    /**
     * Extract the base barcode from a filename like "762202220222.01.jpg"
     */
    public static String fromFilename(String filename) {
        if (filename == null || filename.isBlank()) return null;
        int dot = filename.indexOf('.');
        String candidate = dot > 0 ? filename.substring(0, dot) : filename;
        // Remove extension after last dot if present
        if (candidate.contains(".")) {
            candidate = candidate.substring(0, candidate.indexOf('.'));
        }
        return isValid(candidate) ? candidate : null;
    }

    private static char luhnCheckDigit(String digits) {
        int sum = 0;
        boolean alternate = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        int check = (10 - (sum % 10)) % 10;
        return (char) ('0' + check);
    }
}
