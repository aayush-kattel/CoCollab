package api;

import models.Challenge;
import utils.AppConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiService {

    public Challenge generateChallenge(String topic, String difficulty, String language, int roomId, int userId) {
        String apiKey = AppConfig.get("gemini.api.key");
        String model = AppConfig.get("gemini.model");
        if (model == null || model.isEmpty()) {
            model = "gemini-2.0-flash";
        }

        // if no key, return placeholder
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            return placeholder(topic, difficulty, language, roomId, userId);
        }

        try {
            String prompt =
                    "Generate a coding challenge as STRICT JSON only (no markdown). Fields:\n" +
                            "title, problem, sample_input, sample_output, test_cases, constraints\n" +
                            "test_cases must be a JSON array string like: " +
                            "[{\"input\":\"...\",\"output\":\"...\"},{\"input\":\"...\",\"output\":\"...\"}]\n" +
                            "Topic: " + topic + "\nDifficulty: " + difficulty + "\nLanguage: " + language + "\n" +
                            "Make it suitable for students. Include 2-3 hidden test cases in test_cases.";

            String body = "{"
                    + "\"contents\":[{\"parts\":[{\"text\":" + jsonString(prompt) + "}]}]"
                    + "}";

            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(40000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response = readAll(is);
            conn.disconnect();

            if (code < 200 || code >= 300) {
                System.out.println("Gemini error " + code + ": " + response);
                return placeholder(topic, difficulty, language, roomId, userId);
            }

            // extract text from candidates[0].content.parts[0].text
            String text = extractText(response);
            text = cleanJson(text);

            Challenge c = new Challenge();
            c.setRoomId(roomId);
            c.setCreatedBy(userId);
            c.setTitle(extractField(text, "title", topic + " - " + difficulty));
            c.setProblem(extractField(text, "problem", "Solve a problem related to " + topic));
            c.setSampleInput(extractField(text, "sample_input", "1"));
            c.setSampleOutput(extractField(text, "sample_output", "1"));
            c.setTestCases(extractField(text, "test_cases", "[]"));
            c.setConstraints(extractField(text, "constraints", "N/A"));
            return c;

        } catch (Exception e) {
            System.out.println("Gemini failed: " + e.getMessage());
            return placeholder(topic, difficulty, language, roomId, userId);
        }
    }

    private Challenge placeholder(String topic, String difficulty, String language, int roomId, int userId) {
        Challenge c = new Challenge();
        c.setRoomId(roomId);
        c.setCreatedBy(userId);
        c.setTitle(topic + " - " + difficulty);
        c.setProblem("Write a program related to " + topic + ".\nDifficulty: " + difficulty +
                "\nLanguage: " + language +
                "\n\nTask: Given integers, print their sum.\n(Placeholder - set gemini.api.key in config)");
        c.setSampleInput("5\n1 2 3 4 5");
        c.setSampleOutput("15");
        c.setTestCases("[{\"input\":\"3\\n10 20 30\",\"output\":\"60\"},{\"input\":\"1\\n7\",\"output\":\"7\"}]");
        c.setConstraints("1 <= n <= 1000");
        return c;
    }

    private String extractText(String response) {
        // very simple extraction
        int i = response.indexOf("\"text\"");
        if (i < 0) return response;
        int start = response.indexOf("\"", i + 6);
        if (start < 0) return response;
        start++;
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int p = start; p < response.length(); p++) {
            char ch = response.charAt(p);
            if (escape) {
                if (ch == 'n') sb.append('\n');
                else if (ch == 't') sb.append('\t');
                else if (ch == '"') sb.append('"');
                else if (ch == '\\') sb.append('\\');
                else sb.append(ch);
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

    private String cleanJson(String text) {
        text = text.trim();
        if (text.startsWith("```")) {
            int first = text.indexOf('\n');
            int last = text.lastIndexOf("```");
            if (first > 0 && last > first) {
                text = text.substring(first + 1, last).trim();
            }
        }
        return text;
    }

    // naive field extract from JSON-like text
    private String extractField(String json, String field, String def) {
        String key = "\"" + field + "\"";
        int i = json.indexOf(key);
        if (i < 0) return def;
        int colon = json.indexOf(':', i + key.length());
        if (colon < 0) return def;
        int p = colon + 1;
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) p++;
        if (p >= json.length()) return def;

        if (json.charAt(p) == '"') {
            p++;
            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            for (; p < json.length(); p++) {
                char ch = json.charAt(p);
                if (escape) {
                    if (ch == 'n') sb.append('\n');
                    else if (ch == 't') sb.append('\t');
                    else sb.append(ch);
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

        // array or object - take until matching bracket roughly
        if (json.charAt(p) == '[') {
            int depth = 0;
            int start = p;
            for (; p < json.length(); p++) {
                char ch = json.charAt(p);
                if (ch == '[') depth++;
                else if (ch == ']') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, p + 1);
                    }
                }
            }
        }
        return def;
    }

    private String jsonString(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            if (c == '\\') sb.append("\\\\");
            else if (c == '"') sb.append("\\\"");
            else if (c == '\n') sb.append("\\n");
            else if (c == '\r') sb.append("\\r");
            else if (c == '\t') sb.append("\\t");
            else sb.append(c);
        }
        sb.append("\"");
        return sb.toString();
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toString(StandardCharsets.UTF_8.name());
    }
}