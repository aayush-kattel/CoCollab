package api;

import models.Challenge;
import utils.AppConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GroqService {

    public Challenge generateChallenge(String topic, String difficulty, String language, int roomId, int userId) {
        String apiKey = AppConfig.get("groq.api.key");
        String model = AppConfig.get("groq.model");
        if (model == null || model.isEmpty()) {
            model = "openai/gpt-oss-20b";
        }

        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            return null;
        }

        try {
            String systemMsg =
                    "You are a JSON API. Respond with ONLY a single valid JSON object. " +
                            "No markdown, no code fences, no explanation, no reasoning text outside the JSON.";

            String prompt =
                    "Generate a coding challenge as a JSON object with EXACTLY these keys:\n" +
                            "\"title\", \"problem\", \"sample_input\", \"sample_output\", \"test_cases\", \"constraints\".\n" +
                            "test_cases must be a JSON array of 2-3 objects like " +
                            "[{\"input\":\"...\",\"output\":\"...\"}].\n" +
                            "Write a concrete, specific problem — e.g. 'print numbers 1 to N', 'find the max of an array', " +
                            "'reverse a string' — NOT a vague placeholder like 'solve a problem related to X'.\n" +
                            "Topic: " + topic + "\nDifficulty: " + difficulty + "\nLanguage: " + language;

            String body = "{"
                    + "\"model\":\"" + model + "\","
                    + "\"messages\":["
                    + "{\"role\":\"system\",\"content\":" + jsonString(systemMsg) + "},"
                    + "{\"role\":\"user\",\"content\":" + jsonString(prompt) + "}"
                    + "],"
                    + "\"response_format\":{\"type\":\"json_object\"},"
                    + "\"temperature\":0.5"
                    + "}";

            URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(45000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response = readAll(is);
            conn.disconnect();

            if (code < 200 || code >= 300) {
                System.out.println("Groq error " + code + ": " + response);
                return null;
            }

            String text = extractContent(response);
            text = cleanJson(text);

            System.out.println("Groq raw content extracted:\n" + text);

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
            System.out.println("Groq failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String extractContent(String response) {
        String key = "\"content\":\"";
        int i = response.indexOf(key);
        if (i < 0) return response;
        int start = i + key.length();
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int p = start; p < response.length(); p++) {
            char ch = response.charAt(p);
            if (escape) {
                if (ch == 'n') {
                    sb.append('\n');
                } else if (ch == 't') {
                    sb.append('\t');
                } else if (ch == 'r') {
                    sb.append('\r');
                } else if (ch == '"') {
                    sb.append('"');
                } else if (ch == '\\') {
                    sb.append('\\');
                } else if (ch == 'u' && p + 4 < response.length()) {
                    String hex = response.substring(p + 1, p + 5);
                    try {
                        sb.append((char) Integer.parseInt(hex, 16));
                    } catch (NumberFormatException ignored) {
                    }
                    p += 4; // skip the 4 hex digits we just consumed
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

    // Extracts a named field's value from a JSON object substring.
    // Handles the same escapes as extractContent above.
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
                    if (ch == 'n') {
                        sb.append('\n');
                    } else if (ch == 't') {
                        sb.append('\t');
                    } else if (ch == 'r') {
                        sb.append('\r');
                    } else if (ch == '"') {
                        sb.append('"');
                    } else if (ch == '\\') {
                        sb.append('\\');
                    } else if (ch == 'u' && p + 4 < json.length()) {
                        String hex = json.substring(p + 1, p + 5);
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
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
        if (json.charAt(p) == '[') {
            int depth = 0;
            int start = p;
            for (; p < json.length(); p++) {
                char ch = json.charAt(p);
                if (ch == '[') depth++;
                else if (ch == ']') {
                    depth--;
                    if (depth == 0) return json.substring(start, p + 1);
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
        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toString(StandardCharsets.UTF_8.name());
    }

    public List<Challenge> generateChallengeBundle(String topic, String difficulty, String language,
                                                   int count, int roomId, int userId) {
        List<Challenge> result = callGroqForBundle(topic, difficulty, language, count, roomId, userId, true);

        if (result == null || result.size() < count) {
            int have = result == null ? 0 : result.size();
            System.out.println("Groq JSON mode gave " + have + "/" + count + " — retrying without strict JSON mode...");
            List<Challenge> retry = callGroqForBundle(topic, difficulty, language, count, roomId, userId, false);
            if (retry != null && retry.size() > have) {
                result = retry;
            }
        }

        if (result == null) {
            result = new ArrayList<>();
        }

        if (result.size() < count) {
            System.out.println("Still short (" + result.size() + "/" + count + ") — filling remainder with placeholders.");
            for (int i = result.size() + 1; i <= count; i++) {
                result.add(placeholderChallenge(topic, difficulty, language, roomId, userId, i));
            }
        }

        return result;
    }

    private List<Challenge> callGroqForBundle(String topic, String difficulty, String language,
                                              int count, int roomId, int userId, boolean strictJson) {
        String apiKey = AppConfig.get("groq.api.key");
        String model = AppConfig.get("groq.model");
        if (model == null || model.isEmpty()) model = "openai/gpt-oss-20b";

        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            return null;
        }

        try {
            String systemMsg = strictJson
                    ? "You are a JSON API. Respond with ONLY a single valid JSON object. No markdown, no code fences, no explanation."
                    : "You are a JSON API. Respond with a single JSON object. You may wrap it in a markdown code fence if needed, but include nothing else outside the JSON.";

            String prompt =
                    "Generate exactly " + count + " DIFFERENT coding challenges as a JSON object " +
                            "with one key \"challenges\" containing a JSON array of " + count + " objects.\n" +
                            "Each object must have EXACTLY these keys: " +
                            "\"title\", \"problem\", \"sample_input\", \"sample_output\", \"test_cases\", \"constraints\".\n" +
                            "Keep \"problem\" concise (2-3 sentences). test_cases must be a JSON array of exactly 2 objects " +
                            "like [{\"input\":\"...\",\"output\":\"...\"}]. Keep every field short and to the point.\n" +
                            "Order them from easiest to slightly harder within the same difficulty band. " +
                            "Each must be concrete and specific (e.g. 'reverse a string', 'find the max of an array') " +
                            "— never vague placeholders.\n" +
                            "Topic: " + topic + "\nDifficulty: " + difficulty + "\nLanguage: " + language;

            StringBuilder bodyBuilder = new StringBuilder("{");
            bodyBuilder.append("\"model\":\"").append(model).append("\",");
            bodyBuilder.append("\"messages\":[");
            bodyBuilder.append("{\"role\":\"system\",\"content\":").append(jsonString(systemMsg)).append("},");
            bodyBuilder.append("{\"role\":\"user\",\"content\":").append(jsonString(prompt)).append("}");
            bodyBuilder.append("],");
            if (strictJson) {
                bodyBuilder.append("\"response_format\":{\"type\":\"json_object\"},");
            }
            int maxTokens = Math.min(8000, 500 + count * 450);
            bodyBuilder.append("\"max_tokens\":").append(maxTokens).append(",");
            bodyBuilder.append("\"temperature\":0.6");
            bodyBuilder.append("}");
            String body = bodyBuilder.toString();

            URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response = readAll(is);
            conn.disconnect();

            if (code < 200 || code >= 300) {
                System.out.println("Groq error " + code + " (strictJson=" + strictJson + "): " + response);
                return null;
            }

            String text = cleanJson(extractContent(response));
            System.out.println("Groq bundle raw content (strictJson=" + strictJson + "):\n" + text);

            String arrayText = extractField(text, "challenges", "[]");
            List<String> objects = splitJsonObjects(arrayText);

            List<Challenge> result = new ArrayList<>();
            int seq = 1;
            for (String obj : objects) {
                Challenge c = new Challenge();
                c.setRoomId(roomId);
                c.setCreatedBy(userId);
                c.setSequenceNo(seq++);
                c.setTitle(extractField(obj, "title", topic + " - " + difficulty + " #" + c.getSequenceNo()));
                c.setProblem(extractField(obj, "problem", "Solve a problem related to " + topic));
                c.setSampleInput(extractField(obj, "sample_input", "1"));
                c.setSampleOutput(extractField(obj, "sample_output", "1"));
                c.setTestCases(extractField(obj, "test_cases", "[]"));
                c.setConstraints(extractField(obj, "constraints", "N/A"));
                result.add(c);
            }
            return result;

        } catch (Exception e) {
            System.out.println("Groq bundle call failed (strictJson=" + strictJson + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Challenge placeholderChallenge(String topic, String difficulty, String language,
                                           int roomId, int userId, int seq) {
        Challenge c = new Challenge();
        c.setRoomId(roomId);
        c.setCreatedBy(userId);
        c.setSequenceNo(seq);
        c.setTitle(topic + " - " + difficulty + " #" + seq);
        c.setProblem("Write a program related to " + topic + ".\nDifficulty: " + difficulty +
                "\nLanguage: " + language +
                "\n\nTask: Given an array of integers, return the sum of all elements.\n" +
                "(Local placeholder - Groq did not return enough questions)");
        c.setSampleInput("5\n1 2 3 4 5");
        c.setSampleOutput("15");
        c.setTestCases("[{\"input\":\"3\\n10 20 30\",\"output\":\"60\"},{\"input\":\"1\\n7\",\"output\":\"7\"}]");
        c.setConstraints("1 <= n <= 1000");
        return c;
    }

    private List<String> splitJsonObjects(String arrayText) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < arrayText.length(); i++) {
            char ch = arrayText.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (ch == '\\') {
                escape = true;
                continue;
            }
            if (ch == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (ch == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    result.add(arrayText.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }
}