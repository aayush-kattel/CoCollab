package socket;

public class SocketProtocol {

    public static String buildMessage(String type, String roomId, int userId, String userName, String payload) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"type\":\"").append(escape(type)).append("\",");
        sb.append("\"roomId\":\"").append(escape(roomId)).append("\",");
        sb.append("\"userId\":").append(userId).append(",");
        sb.append("\"userName\":\"").append(escape(userName)).append("\",");
        sb.append("\"payload\":\"").append(escape(payload == null ? "" : payload)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public static String getField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int i = json.indexOf(key);
        if (i < 0) return null;
        int start = i + key.length();
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int p = start; p < json.length(); p++) {
            char ch = json.charAt(p);
            if (escape) {
                if (ch == 'n') sb.append('\n');
                else if (ch == 't') sb.append('\t');
                else if (ch == 'r') sb.append('\r');
                else if (ch == '"') sb.append('"');
                else if (ch == '\\') sb.append('\\');
                else if (ch == 'u' && p + 4 < json.length()) {
                    try {
                        sb.append((char) Integer.parseInt(json.substring(p + 1, p + 5), 16));
                    } catch (NumberFormatException ignored) {
                    }
                    p += 4;
                } else {
                    sb.append(ch);
                }
                escape = false;
            } else if (ch == '\\') {
                escape = true;
            } else if (ch == '"') {
                break;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static int getIntField(String json, String field) {
        String key = "\"" + field + "\":";
        int i = json.indexOf(key);
        if (i < 0) return -1;
        int start = i + key.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return -1;
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}