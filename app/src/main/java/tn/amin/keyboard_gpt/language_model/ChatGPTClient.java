package tn.amin.keyboard_gpt.language_model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.net.HttpURLConnection;
import java.net.URL;

import tn.amin.keyboard_gpt.language_model.publisher.ExceptionPublisher;
import tn.amin.keyboard_gpt.language_model.publisher.InputStreamPublisher;

public class ChatGPTClient extends LanguageModelClient {
    private static final String BASE_URL = "https://api.openai.com";

    @Override
    public Publisher<String> submitPrompt(String prompt) {
        if (getApiKey() == null || getApiKey().isEmpty()) {
            return LanguageModelClient.MISSING_API_KEY_PUBLISHER;
        }

        String url = BASE_URL + "/v1/chat/completions";
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + getApiKey());

            JSONArray messagesJson = new JSONArray();
            messagesJson.put(new JSONObject()
                    .accumulate("role", "system")
                    .accumulate("content", "You are a helpful assistant."));
            messagesJson.put(new JSONObject()
                    .accumulate("role", "user")
                    .accumulate("content", prompt));
            JSONObject rootJson = new JSONObject();
            rootJson.put("model", getSubModel());
            rootJson.put("messages", messagesJson);
            rootJson.put("stream", true);

            con.setDoOutput(true);
            con.getOutputStream().write(rootJson.toString().getBytes());

            return new InputStreamPublisher(con.getInputStream(), line -> {
                try {
                    JSONObject choice = new JSONObject(line)
                            .getJSONArray("choices")
                            .getJSONObject(0);
                    if (choice.has("delta")) {
                        return choice.getJSONObject("delta")
                                .getString("content");
                    }
                    return choice.getJSONObject("message")
                            .getString("content");
                } catch (JSONException e) {
                    return line;
                }
            });
        } catch (Exception e) {
            return new ExceptionPublisher(e);
        }
    }

    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.ChatGPT;
    }
}
