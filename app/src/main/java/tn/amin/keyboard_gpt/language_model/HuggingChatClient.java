package tn.amin.keyboard_gpt.language_model;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import de.robv.android.xposed.XposedBridge;
import tn.amin.keyboard_gpt.language_model.publisher.ExceptionPublisher;
import tn.amin.keyboard_gpt.language_model.publisher.InputStreamPublisher;

// TODO not working
public class HuggingChatClient extends LanguageModelClient {
    private static final String BASE_URL = "https://huggingface.co";

    @Override
    public Publisher<String> submitPrompt(String prompt, String systemMessage) {
        String url;
        HttpURLConnection con;
        try {
            url = BASE_URL + "/chat/conversation";
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("model", "meta-llama/Meta-Llama-3.1-70B-Instruct");

            con.setDoOutput(true);
            con.getOutputStream().write(body.toString().getBytes());

            String convIdResponse = new BufferedReader(new InputStreamReader(con.getInputStream()))
                    .lines().reduce((a, b) -> a+b).get();
            String conversationId = new JSONObject(convIdResponse)
                    .getString("conversationId");

            url += "/" + conversationId;
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Origin", "https://huggingface.co");

            JSONObject data = new JSONObject()
                    .accumulate("inputs", prompt)
                    .accumulate("id", UUID.randomUUID().toString())
                    .accumulate("is_retry", false)
                    .accumulate("is_continue", false)
                    .accumulate("web_search", false)
                    .accumulate("tools", new JSONObject());

            String boundary = "----WebKitFormBoundaryvHxrBC76Gkd5K0IS";
            StringBuilder requestBody = new StringBuilder();
            requestBody.append("--").append(boundary).append("\r\n");
            requestBody.append("Content-Disposition: form-data; name=\"data\"\r\n\r\n");
            requestBody.append(data).append("\r\n");
            requestBody.append("--").append(boundary).append("--").append("\r\n");

            XposedBridge.log(requestBody.toString());

            con.setDoOutput(true);
            con.getOutputStream().write(requestBody.toString().getBytes());

            return new InputStreamPublisher(con.getInputStream(), line -> {
                try {
                    line = line.replace("\\u0000", "");
                    JSONObject lineJson = new JSONObject(line);
                    if (!lineJson.has("type")) {
                        return "";
                    }
                    String type = lineJson.getString("type");
                    if (!"stream".equals(type)) {
                        return "";
                    }

                    return lineJson.getString("token");
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
