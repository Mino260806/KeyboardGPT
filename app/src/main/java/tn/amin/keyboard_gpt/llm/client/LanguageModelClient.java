package tn.amin.keyboard_gpt.llm.client;

import androidx.annotation.NonNull;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.llm.LanguageModelField;
import tn.amin.keyboard_gpt.llm.internet.InternetProvider;
import tn.amin.keyboard_gpt.llm.internet.SimpleInternetProvider;
import tn.amin.keyboard_gpt.llm.service.InternetRequestListener;

public abstract class LanguageModelClient {
    private Map<LanguageModelField, String> mFields = new HashMap<>();

    private InternetProvider mInternetProvider = new SimpleInternetProvider();

    abstract public Publisher<String> submitPrompt(String prompt, String systemMessage);

    abstract public LanguageModel getLanguageModel();

    public void setField(LanguageModelField field, String value) {
        mFields.put(field, value);
    }

    public String getField(LanguageModelField field) {
        return mFields.getOrDefault(field, getLanguageModel().getDefault(field));
    }

    public double getDoubleField(LanguageModelField field) {
        try {
            String doubleStr = mFields.getOrDefault(field, getLanguageModel().getDefault(field));
            if (doubleStr != null) {
                return Double.parseDouble(doubleStr);
            }
        } catch (NumberFormatException | NullPointerException e) {
            MainHook.log(e);
        }
        return Double.parseDouble(getLanguageModel().getDefault(field));
    }

    public String getSubModel() {
        return getField(LanguageModelField.SubModel);
    }

    public String getApiKey() {
        return getField(LanguageModelField.ApiKey);
    }

    public String getBaseUrl() {
        return getField(LanguageModelField.BaseUrl);
    }

    public static LanguageModelClient forModel(LanguageModel model) {
        switch (model) {
            case Gemini:
                return new GeminiClient();
            case ChatGPT:
                return new ChatGPTClient();
//            case HuggingChat:
//                return new HuggingChatClient();
            case Groq:
                return new GroqClient();
            case OpenRouter:
                return new OpenRouterClient();
            case Claude:
                return new ClaudeClient();
            default:
                return new ChatGPTClient();
        }
    }

    static Publisher<String> MISSING_API_KEY_PUBLISHER = subscriber -> {
        subscriber.onNext("Missing API Key");
        subscriber.onComplete();
    };

    @NonNull
    @Override
    public String toString() {
        return getLanguageModel().label + " (" + getSubModel() + ")";
    }

    protected static String getDefaultSystemMessage() {
        return "You are a helpful assitant.";
    }

    public void setInternetProvider(InternetProvider internetProvider) {
        mInternetProvider = internetProvider;
    }

    protected InputStream sendRequest(HttpURLConnection con, String body, InternetRequestListener irl) throws IOException {
        return mInternetProvider.sendRequest(con, body, irl);
    }
}
