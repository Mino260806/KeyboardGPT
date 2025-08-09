package tn.amin.keyboard_gpt.llm.client;

import androidx.annotation.NonNull;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.llm.internet.InternetProvider;
import tn.amin.keyboard_gpt.llm.internet.SimpleInternetProvider;
import tn.amin.keyboard_gpt.llm.service.InternetRequestListener;

public abstract class LanguageModelClient {
    private String mApiKey = null;

    private String mSubModel = null;

    private String mBaseUrl = null;

    private InternetProvider mInternetProvider = new SimpleInternetProvider();

    abstract public Publisher<String> submitPrompt(String prompt, String systemMessage);

    abstract public LanguageModel getLanguageModel();

    public void setApiKey(String apiKey) {
        mApiKey = apiKey;
    }

    public void setSubModel(String subModel) {
        mSubModel = subModel;
    }

    public void setBaseUrl(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    public String getSubModel() {
        return mSubModel != null ? mSubModel : getLanguageModel().defaultSubModel;
    }

    public String getApiKey() {
        return mApiKey;
    }

    public String getBaseUrl() {
        return mBaseUrl != null ? mBaseUrl : getLanguageModel().defaultBaseUrl;
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
