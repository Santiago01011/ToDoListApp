package service.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class LogTasksUtil {
    private static final ZoneOffset USER_DEFAULT_OFFSET = ZoneOffset.ofHours(-3);

    private LogTasksUtil() {}

    public static JsonNode parseFunctionRow(ResultSet rs, ObjectMapper objectMapper) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int c = md.getColumnCount();

            if (hasColumn(md, "log_tasks")) {
                String s = getJsonStringIfPresent(rs, md, "log_tasks");
                if (s != null && !s.isEmpty()) return objectMapper.readTree(s);
            }

            boolean hasData = hasColumn(md, "data");
            boolean hasColumns = hasColumn(md, "columns");
            if (hasData) {
                com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
                String dataStr = getJsonStringIfPresent(rs, md, "data");
                if (dataStr != null) root.set("data", objectMapper.readTree(dataStr));
                if (hasColumns) {
                    String colsStr = getJsonStringIfPresent(rs, md, "columns");
                    if (colsStr != null) root.set("columns", objectMapper.readTree(colsStr));
                }
                String ls = getStringIfPresent(rs, md, "last_sync");
                if (ls != null) root.put("last_sync", ls);
                return root;
            }

            if (hasColumn(md, "result")) {
                String s = getJsonStringIfPresent(rs, md, "result");
                if (s != null && !s.isEmpty()) return objectMapper.readTree(s);
            }

            if (c == 1) {
                Object o = rs.getObject(1);
                if (o != null) {
                    String s = String.valueOf(o);
                    if (!s.isEmpty()) {
                        try { return objectMapper.readTree(s); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("LogTasksUtil.parseFunctionRow error: " + ex.getMessage());
        }
        return null;
    }

    public static JsonNode normalizeRow(JsonNode root, JsonNode row, ObjectMapper mapper) {
        if (row == null) return row;
        if (row.isObject()) return row;
        if (!row.isArray()) return row;
        JsonNode cols = root.get("columns");
        if (cols == null || !cols.isArray()) return row;
        com.fasterxml.jackson.databind.node.ObjectNode obj = mapper.createObjectNode();
        int size = Math.min(cols.size(), row.size());
        for (int i = 0; i < size; i++) {
            String col = cols.get(i).asText();
            obj.set(col, row.get(i));
        }
        return obj;
    }

    public static String textOf(JsonNode node, String... keys) {
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && !v.isNull()) return v.asText();
        }
        return null;
    }

    public static boolean boolOf(JsonNode node, boolean def, String... keys) {
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && !v.isNull()) return v.asBoolean();
        }
        return def;
    }

    public static LocalDateTime timeOfAny(JsonNode node, String... keys) {
        if (node == null || keys == null) return null;
        for (String k : keys) {
            LocalDateTime t = timeOf(node, k);
            if (t != null) return t;
        }
        return null;
    }

    public static LocalDateTime timeOf(JsonNode node, String key) {
        JsonNode v = node.get(key);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        if (s == null || s.isEmpty()) return null;
        try {
            var odt = java.time.OffsetDateTime.parse(s);
            var shifted = odt.withOffsetSameInstant(USER_DEFAULT_OFFSET);
            return shifted.toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(s.replace("Z", ""));
            } catch (Exception ignore) {
                try {
                    return LocalDateTime.ofInstant(Instant.parse(s), USER_DEFAULT_OFFSET);
                } catch (Exception ignore2) {
                    return null;
                }
            }
        }
    }

    private static boolean hasColumn(ResultSetMetaData md, String name) throws SQLException {
        int c = md.getColumnCount();
        for (int i = 1; i <= c; i++) {
            if (name.equalsIgnoreCase(md.getColumnLabel(i))) return true;
        }
        return false;
    }

    private static String getStringIfPresent(ResultSet rs, ResultSetMetaData md, String name) {
        try {
            int c = md.getColumnCount();
            for (int i = 1; i <= c; i++) {
                if (name.equalsIgnoreCase(md.getColumnLabel(i))) {
                    Object o = rs.getObject(i);
                    return o != null ? String.valueOf(o) : null;
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    private static String getJsonString(ResultSet rs, String column) throws SQLException {
        Object o = rs.getObject(column);
        if (o == null) return null;
        if (o instanceof java.sql.SQLXML xml) return xml.getString();
        return String.valueOf(o);
    }

    private static String getJsonStringIfPresent(ResultSet rs, ResultSetMetaData md, String column) throws SQLException {
        if (!hasColumn(md, column)) return null;
        return getJsonString(rs, column);
    }
}
