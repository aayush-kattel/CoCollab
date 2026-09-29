package api;

import utils.AppConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Judge0Service {

    private static final Map<String, Integer> LANG_IDS = new HashMap<>();

    static {
        LANG_IDS.put("C", 50);
        LANG_IDS.put("C++", 54);
        LANG_IDS.put("Java", 62);
        LANG_IDS.put("Python", 71);
        LANG_IDS.put("JavaScript", 63);
    }

    public static class Result {
        public boolean passed;
        public String status;
        public String stdout;
        public String stderr;
        public String message;
        public int score;
    }

    public Result evaluate(String sourceCode, String languageName, String stdin, String expectedOutput) {
        Result result = new Result();
        String base = AppConfig.get("judge0.url");
        String apiKey = AppConfig.get("judge0.api.key");
        String apiHost = AppConfig.get("judge0.api.host");

        // only URL is required for Judge0
        if (base == null || base.trim().isEmpty()) {
            result.passed = false;
            result.status = "Judge0 not configured";
            result.message = "Set judge0.url in config.properties";
            result.score = 0;
            return result;
        }

        Integer langId = LANG_IDS.get(languageName);
        if (langId == null) {
            result.passed = false;
            result.status = "Unsupported language";
            result.message = "Language not supported: " + languageName;
            result.score = 0;
            return result;
        }

        try {
            String endpoint = base + "/submissions?base64_encoded=true&wait=true";
            String body = "{"
                    + "\"source_code\":\"" + b64(sourceCode) + "\","
                    + "\"language_id\":" + langId + ","
                    + "\"stdin\":\"" + b64(stdin == null ? "" : stdin) + "\","
                    + "\"expected_output\":\"" + b64(expectedOutput == null ? "" : expectedOutput) + "\""
                    + "}";

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // RapidAPI headers only if key is set
            if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.startsWith("YOUR_")) {
                conn.setRequestProperty("X-RapidAPI-Key", apiKey);
                if (apiHost != null && !apiHost.trim().isEmpty()) {
                    conn.setRequestProperty("X-RapidAPI-Host", apiHost);
                }
            }

            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response = readAll(is);
            conn.disconnect();

            if (code < 200 || code >= 300) {
                result.passed = false;
                result.status = "API error " + code;
                result.message = response;
                result.score = 0;
                return result;
            }

            String statusDesc = extractJsonString(response, "description");
            if (statusDesc == null) {
                statusDesc = "Unknown";
            }
            result.status = statusDesc;
            result.stdout = decodeB64Field(response, "stdout");
            result.stderr = decodeB64Field(response, "stderr");
            result.message = decodeB64Field(response, "message");

            int statusId = extractStatusId(response);
            result.passed = (statusId == 3); // 3 = Accepted
            result.score = result.passed ? 100 : 0;
            return result;

        } catch (Exception e) {
            result.passed = false;
            result.status = "Error";
            result.message = e.getMessage();
            result.score = 0;
            return result;
        }
    }

    /** Run multiple test cases; score = passed/total * 100 */
    public Result evaluateAll(String sourceCode, String languageName, String testCasesJson) {
        Result finalResult = new Result();
        java.util.List<String[]> cases = parseTestCases(testCasesJson);

        if (cases.isEmpty()) {
            return evaluate(sourceCode, languageName, "", "");
        }

        int passed = 0;
        StringBuilder log = new StringBuilder();

        for (int i = 0; i < cases.size(); i++) {
            String[] tc = cases.get(i);
            Result r = evaluate(sourceCode, languageName, tc[0], tc[1]);
            if (r.passed) {
                passed++;
            }
            log.append("Test ").append(i + 1).append(": ").append(r.status).append("\n");
            if (r.stderr != null && !r.stderr.isEmpty()) {
                log.append(r.stderr).append("\n");
            }
            if (r.message != null && !r.message.isEmpty() && !"null".equals(r.message)) {
                log.append(r.message).append("\n");
            }
        }

        finalResult.passed = (passed == cases.size());
        finalResult.status = finalResult.passed ? "Accepted" : "Wrong Answer";
        finalResult.message = log.toString();
        finalResult.score = (int) Math.round((passed * 100.0) / cases.size());
        finalResult.stdout = passed + "/" + cases.size() + " passed";
        return finalResult;
    }

    private java.util.List<String[]> parseTestCases(String json) {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return list;
        }

        int idx = 0;
        while (true) {
            int inKey = json.indexOf("\"input\"", idx);
            if (inKey < 0) {
                break;
            }
            String input = extractAfterKey(json, inKey);
            int outKey = json.indexOf("\"output\"", inKey);
            String output = outKey > 0 ? extractAfterKey(json, outKey) : "";
            list.add(new String[]{input, output});
            idx = Math.max(inKey, outKey) + 1;
        }
        return list;
    }

    private String extractAfterKey(String json, int keyPos) {
        int colon = json.indexOf(':', keyPos);
        if (colon < 0) {
            return "";
        }
        int p = colon + 1;
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) {
            p++;
        }
        if (p >= json.length() || json.charAt(p) != '"') {
            return "";
        }
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

    private String b64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String decodeB64Field(String json, String field) {
        String v = extractJsonString(json, field);
        if (v == null || v.isEmpty() || "null".equals(v)) {
            return "";
        }
        try {
            return new String(Base64.getDecoder().decode(v), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return v;
        }
    }

    private String extractJsonString(String json, String field) {
        String key = "\"" + field + "\"";
        int i = json.indexOf(key);
        if (i < 0) {
            return null;
        }
        int colon = json.indexOf(':', i + key.length());
        if (colon < 0) {
            return null;
        }
        int p = colon + 1;
        while (p < json.length() && Character.isWhitespace(json.charAt(p))) {
            p++;
        }
        if (p >= json.length()) {
            return null;
        }
        if (json.charAt(p) == '"') {
            p++;
            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            for (; p < json.length(); p++) {
                char ch = json.charAt(p);
                if (escape) {
                    sb.append(ch);
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
        int end = p;
        while (end < json.length() && ",}] \n\r\t".indexOf(json.charAt(end)) < 0) {
            end++;
        }
        return json.substring(p, end);
    }

    private int extractStatusId(String json) {
        int i = json.indexOf("\"status\"");
        if (i < 0) {
            return -1;
        }
        int idKey = json.indexOf("\"id\"", i);
        if (idKey < 0 || idKey > i + 80) {
            return -1;
        }
        int colon = json.indexOf(':', idKey);
        if (colon < 0) {
            return -1;
        }
        try {
            String num = json.substring(colon + 1).trim().split("[,}\\s]")[0];
            return Integer.parseInt(num);
        } catch (Exception e) {
            return -1;
        }
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toString(StandardCharsets.UTF_8.name());
    }
}