package com.github.davidmoten.odata.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Formats Java values as OData URL literals (the value side of expressions like
 * {@code age gt 18} or {@code name eq 'bob'}). The format depends on the Java type and
 * (where relevant) the EDM type name.
 */
final class LiteralFormatter {

    private LiteralFormatter() {
    }

    static String format(Object value, Class<?> type, String edmType) {
        Preconditions.checkNotNull(value);
        // Enum values are emitted as <namespace>.'Member' in OData v4 URL syntax
        if (value instanceof java.lang.Enum) {
            return formatEnum((java.lang.Enum<?>) value);
        }
        if (type == String.class) {
            return "'" + escapeString((String) value) + "'";
        } else if (type == Boolean.class) {
            return ((Boolean) value).booleanValue() ? "true" : "false";
        } else if (type == Integer.class) {
            return value.toString();
        } else if (type == Long.class) {
            return value.toString() + "L";
        } else if (type == Short.class) {
            return value.toString();
        } else if (type == Byte.class) {
            return value.toString();
        } else if (type == Float.class) {
            return formatFloat((Float) value) + "f";
        } else if (type == Double.class) {
            return formatDouble((Double) value) + "d";
        } else if (type == BigDecimal.class) {
            return ((BigDecimal) value).toPlainString() + "m";
        } else if (type == Instant.class) {
            // OData DateTimeOffset: cast(..., 'Edm.DateTimeOffset') with ISO-8601 value
            return formatInstant((Instant) value);
        } else if (type == LocalDate.class) {
            return formatDate((LocalDate) value);
        } else if (type == LocalTime.class) {
            return formatTime((LocalTime) value);
        } else if (type == UUID.class) {
            return ((UUID) value).toString();
        } else if (type == byte[].class) {
            return "binary'" + toHex((byte[]) value) + "'";
        } else if (value instanceof Number) {
            return value.toString();
        } else {
            // fallback: toString, then single-quote (works for most non-string values)
            return "'" + escapeString(value.toString()) + "'";
        }
    }

    private static String formatInstant(Instant instant) {
        // OData 4.01 DateTimeOffset: 2002-10-10T17:00:00Z
        return instant.toString();
    }

    private static String formatDate(LocalDate date) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }

    private static String formatTime(LocalTime time) {
        // OData Edm.TimeOfDay: HH:mm:ss[.fffffff] (24-hour)
        return DateTimeFormatter.ISO_LOCAL_TIME.format(time);
    }

    private static String formatFloat(Float f) {
        if (f.isNaN() || f.isInfinite()) {
            return f.toString();
        }
        return f.toString();
    }

    private static String formatDouble(Double d) {
        if (d.isNaN() || d.isInfinite()) {
            return d.toString();
        }
        return d.toString();
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    static String formatEnum(java.lang.Enum<?> value) {
        Class<?> cls = value.getDeclaringClass();
        String typeName;
        if (cls.isMemberClass()) {
            // nested enums (e.g. PersonGender inside Person) - use just enum class name
            typeName = cls.getSimpleName();
        } else {
            // top-level enums - use class name (generator emits an `odataTypeName()`
            // method, but for $filter we use the simple class name as the namespace is
            // implicit from the URL).
            typeName = cls.getSimpleName();
        }
        return typeName + "'" + value.name() + "'";
    }

    static String escapeString(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                sb.append("''");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
