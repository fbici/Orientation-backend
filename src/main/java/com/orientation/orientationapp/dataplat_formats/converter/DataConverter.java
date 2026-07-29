package com.orientation.orientationapp.dataplat_formats.converter;

import java.util.Map;

/**
 * Utility for converting between data types in import rows.
 */
public final class DataConverter {

    private DataConverter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String getString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value != null ? value.toString() : null;
    }

    public static Integer getInteger(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double getDouble(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof Double d) return d;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean getBoolean(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        String str = value.toString().trim().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str) || "oui".equals(str);
    }

    public static String normalizeString(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("\\s+", " ");
    }
}
