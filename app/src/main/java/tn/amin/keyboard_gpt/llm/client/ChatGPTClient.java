package tn.amin.keyboard_gpt.llm.client;

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
import tn.amin.keyboard_gpt.llm.publisher.SimpleStringPublisher;

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
            rootJson.put("stream", false);

            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = rootJson.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            MainHook.log("Received response with code " + responseCode);

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String response = reader.lines().collect(Collectors.joining(""));
                JSONObject responseJson = new JSONObject(response);
                if (responseJson.has("choices")) {
                    JSONArray choices = responseJson.getJSONArray("choices");
                    for (int i = 0; i < choices.length(); i++) {
                        JSONObject choice = choices.getJSONObject(i);
                        if (choice.has("role") && "assistant".equals(choice.getString("role"))) {
                            return new SimpleStringPublisher(choice
                                    .getJSONObject("message")
                                    .getString("content"));
                        }
                    }
                    if (choices.length() > 0) {
                        return new SimpleStringPublisher(choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content"));
                    }
                    else {
                        throw new JSONException("choices has length 0");
                    }
                } else {
                    throw new JSONException("no \"choices\" attribute found");
                }
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String response = reader.lines().collect(Collectors.joining(""));
                JSONObject responseJson = new JSONObject(response);
                if (responseJson.has("error")) {
                    JSONObject errorJson = responseJson
                            .getJSONObject("error");
                    String message = errorJson.getString("message");
                    String type = "";
                    if (errorJson.has("type")) {
                        type = errorJson.getString("type");
                    }

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
}
