package tn.amin.keyboard_gpt.llm.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.llm.LanguageModelField;
import tn.amin.keyboard_gpt.llm.publisher.ExceptionPublisher;
import tn.amin.keyboard_gpt.llm.publisher.InternetRequestPublisher;

public class MistralClient extends LanguageModelClient {
    @Override
    public Publisher<String> submitPrompt(String prompt, String systemMessage) {
        if (getApiKey() == null || getApiKey().isEmpty()) {
            return LanguageModelClient.MISSING_API_KEY_PUBLISHER;
        }

        if (systemMessage == null) {
            systemMessage = getDefaultSystemMessage();
        }

        String url = getBaseUrl() + "/chat/completions";
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
            rootJson.put("max_tokens", getIntField(LanguageModelField.MaxTokens));
            rootJson.put("temperature", getDoubleField(LanguageModelField.Temperature));
            rootJson.put("top_p", getDoubleField(LanguageModelField.TopP));

            InternetRequestPublisher publisher = new InternetRequestPublisher(
                    (s, reader) -> {
                        String response = reader.lines().collect(Collectors.joining(""));
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.has("choices")) {
                            JSONArray choices = responseJson.getJSONArray("choices");
                            for (int i = 0; i < choices.length(); i++) {
                                JSONObject choice = choices.getJSONObject(i).getJSONObject("message");
                                if (choice.has("role") && "assistant".equals(choice.getString("role"))) {
                                    s.onNext(choice
                                            .getString("content"));
                                    return;
                                }
                            }
                            if (choices.length() > 0) {
                                s.onNext(choices.getJSONObject(0)
                                        .getString("content"));
                            }
                            else {
                                throw new JSONException("choices has length 0");
                            }
                        } else {
                            throw new JSONException("no \"choices\" attribute found");
                        }
                    },
                    (s, reader) -> {
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
                    });
            InputStream inputStream = sendRequest(con, rootJson.toString(), publisher);
            publisher.setInputStream(inputStream);
            return publisher;
        } catch (Throwable t) {
            return new ExceptionPublisher(t);
        }
    }

    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Mistral;
    }
}
