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

public class GroqClient extends ChatGPTClient {
    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Groq;
    }

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
            rootJson.put("stream", true);
            rootJson.put("max_completion_tokens", getIntField(LanguageModelField.MaxTokens));
            rootJson.put("temperature", getDoubleField(LanguageModelField.Temperature));
            rootJson.put("top_p", getDoubleField(LanguageModelField.TopP));

            InternetRequestPublisher publisher = new InternetRequestPublisher(
                    (s, reader) -> {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty() || line.endsWith("[DONE]")) {
                                continue;
                            }
                            if (line.startsWith("data:")) {
                                line = line.substring("data:".length()).trim();
                            }
                            JSONObject choice = new JSONObject(line)
                                    .getJSONArray("choices")
                                    .getJSONObject(0);
                            if (choice.has("delta")) {
                                s.onNext(extractContent(choice.getJSONObject("delta")));
                            } else {
                                s.onNext(extractContent(choice.getJSONObject("message")));
                            }
                        }
                    },
                    (s, reader) -> {
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
            );
            InputStream inputStream = sendRequest(con, rootJson.toString(), publisher);
            publisher.setInputStream(inputStream);
            return publisher;
        } catch (Throwable t) {
            return new ExceptionPublisher(t);
        }
    }

    private String extractContent(JSONObject message) throws JSONException {
        if (!message.has("content")) {
            return "";
        }

        return message.getString("content");
    }

}
