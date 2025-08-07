package tn.amin.keyboard_gpt.llm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.llm.publisher.ExceptionPublisher;
import tn.amin.keyboard_gpt.llm.publisher.InputStreamPublisher;

public class ChatGPTClient extends LanguageModelClient {
    @Override
    public Publisher<String> submitPrompt(String prompt, String systemMessage) {
        if (getApiKey() == null || getApiKey().isEmpty()) {
            return LanguageModelClient.MISSING_API_KEY_PUBLISHER;
        }

        if (systemMessage == null) {
            systemMessage = getDefaultSystemMessage();
        }

        String url = getBaseUrl() + "/v1/chat/completions";
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + getApiKey());

            JSONArray messagesJson = new JSONArray();
            messagesJson.put(new JSONObject()
                    .accumulate("role", "system")
                    .accumulate("content", systemMessage));
            messagesJson.put(new JSONObject()
                    .accumulate("role", "user")
                    .accumulate("content", prompt));
            JSONObject rootJson = new JSONObject();
            rootJson.put("model", getSubModel());
            rootJson.put("messages", messagesJson);
            rootJson.put("stream", true);

            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = rootJson.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            MainHook.log("Received response with code " + responseCode);

            if (responseCode == 200) {
                return new InputStreamPublisher(con.getInputStream(), line -> {
                    line = line.trim();
//                    MainHook.log("line is " + line);
                    if (line.isEmpty() || line.endsWith("[DONE]")) {
                        return "";
                    }
                    if (line.startsWith("data:")) {
                        line = line.substring("data:".length()).trim();
                    }
                    try {
                        JSONObject choice = new JSONObject(line)
                                .getJSONArray("choices")
                                .getJSONObject(0);
                        if (choice.has("delta")) {
                            return extractContent(choice.getJSONObject("delta"));
                        }
                        return extractContent(choice.getJSONObject("message"));
                    } catch (JSONException e) {
                        MainHook.log(e);
                        return "";
                    }
                });
            }

            else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String response = reader.lines().collect(Collectors.joining(""));
                JSONObject responseJson = new JSONObject(response);
                if (responseJson.has("error")) {
                    JSONObject errorJson = responseJson
                            .getJSONObject("error");
                    String message = errorJson.getString("message");
                    String type = errorJson.getString("type");

                    throw new IllegalArgumentException("(" + type + ") " + message);
                }
                else {
                    throw new IllegalArgumentException(response);
                }
            }
        } catch (Exception e) {
            return new ExceptionPublisher(e);
        }
    }

    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.ChatGPT;
    }

    private String extractContent(JSONObject message) throws JSONException {
        if (!message.has("content")) {
            return "";
        }

        return message.getString("content");
    }
}
