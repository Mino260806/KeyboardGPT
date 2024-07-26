package tn.amin.keyboard_gpt.language_model;

import androidx.annotation.NonNull;

import org.reactivestreams.Publisher;

public abstract class LanguageModelClient {
    private String mApiKey = null;

    private String mSubModel = null;

    private String mBaseUrl = null;

    abstract public Publisher<String> submitPrompt(String prompt);

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
            default:
                return new GeminiClient();
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
}
