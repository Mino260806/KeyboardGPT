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

public class GeminiClient extends LanguageModelClient {
    @Override
    public Publisher<String> submitPrompt(String prompt, String systemMessage) {
        if (getApiKey() == null || getApiKey().isEmpty()) {
            return LanguageModelClient.MISSING_API_KEY_PUBLISHER;
        }

        if (systemMessage == null) {
            systemMessage = "[system message] " + getDefaultSystemMessage();
        }

        String url = String.format("%s/models/%s:generateContent", getBaseUrl(), getSubModel());
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("x-goog-api-key", getApiKey());

            JSONArray contentsJson = new JSONArray();
            contentsJson.put(new JSONArray().put(new JSONObject().put("role", "user").put("parts",
                    new JSONArray().put(new JSONObject().put("text", systemMessage)))));
            contentsJson.put(new JSONArray().put(new JSONObject().put("role", "user").put("parts",
                    new JSONArray().put(new JSONObject().put("text", prompt)))));
            JSONObject generationConfigJson = new JSONObject()
                    .put("temperature", getDoubleField(LanguageModelField.Temperature));
            JSONArray safetySettings = new JSONArray()
                    .put(new JSONObject().put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT")
                            .put("threshold", "BLOCK_NONE"))
                    .put(new JSONObject().put("category", "HARM_CATEGORY_HATE_SPEECH")
                            .put("threshold", "BLOCK_NONE"))
                    .put(new JSONObject().put("category", "HARM_CATEGORY_HARASSMENT")
                            .put("threshold", "BLOCK_NONE"))
                    .put(new JSONObject().put("category", "HARM_CATEGORY_DANGEROUS_CONTENT")
                            .put("threshold", "BLOCK_NONE"));
            JSONObject rootJson = new JSONObject();
            rootJson.put("safetySettings", safetySettings);
            rootJson.put("contents", contentsJson);
            rootJson.put("generationConfig", generationConfigJson);

            InternetRequestPublisher publisher = new InternetRequestPublisher(
                    (s, reader) -> {
                        String response = reader.lines().collect(Collectors.joining(""));
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.has("candidates")) {
                            JSONArray candidates = responseJson.getJSONArray("candidates");
                            for (int i = 0; i < candidates.length(); i++) {
                                JSONObject candidate = candidates.getJSONObject(i).getJSONObject("content");
                                if (candidate.has("role") && "model".equals(candidate.getString("role"))) {
                                    s.onNext(candidate
                                            .getJSONArray("parts")
                                            .getJSONObject(0)
                                            .getString("text"));
                                    return;
                                }
                            }
                            if (candidates.length() > 0) {
                                s.onNext(candidates
                                        .getJSONObject(0)
                                        .getJSONArray("parts")
                                        .getJSONObject(0)
                                        .getString("text"));
                            }
                            else {
                                throw new JSONException("candidates has length 0");
                            }
                        } else {
                            throw new JSONException("no \"candidates\" attribute found");
                        }
                    },
                    (s, reader) -> {
                        String response = reader.lines().collect(Collectors.joining(""));
                        throw new RuntimeException(response);
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
        return LanguageModel.Gemini;
    }
}
