package tn.amin.keyboard_gpt.llm;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.listener.GenerativeAIListener;
import tn.amin.keyboard_gpt.llm.client.LanguageModelClient;
import tn.amin.keyboard_gpt.listener.ConfigChangeListener;
import tn.amin.keyboard_gpt.llm.internet.InternetProvider;
import tn.amin.keyboard_gpt.llm.internet.SimpleInternetProvider;
import tn.amin.keyboard_gpt.llm.publisher.SimpleStringPublisher;
import tn.amin.keyboard_gpt.llm.service.ExternalInternetProvider;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class GenerativeAIController implements ConfigChangeListener {
    private LanguageModelClient mModelClient = null;

    private final SPManager mSPManager;
    private final UiInteractor mInteractor;
    private ExternalInternetProvider mExternalClient = null;

    private List<GenerativeAIListener> mListeners = new ArrayList<>();
    private InternetProvider mInternetProvider = null;

    public GenerativeAIController() {
        mSPManager = SPManager.getInstance();
        mInteractor = UiInteractor.getInstance();

        mInteractor.registerConfigChangeListener(this);

        if (ContextCompat.checkSelfPermission(MainHook.getApplicationContext(),
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            MainHook.log("Missing INTERNET permission, using ExternalInternetProvider");
            mExternalClient = new ExternalInternetProvider(MainHook.getApplicationContext());
            mExternalClient.connect();
            mInternetProvider = mExternalClient;
        } else {
            MainHook.log("Found INTERNET permission, using SimpleInternetProvider");
            mInternetProvider = new SimpleInternetProvider();
        }

        if (mSPManager.hasLanguageModel()) {
            setModel(mSPManager.getLanguageModel());
        } else {
            mModelClient = LanguageModelClient.forModel(LanguageModel.Gemini);
            mModelClient.setInternetProvider(mInternetProvider);
        }
    }

    public boolean needModelClient() {
        return mModelClient == null;
    }

    public boolean needApiKey() {
        return mModelClient.getApiKey() == null || mModelClient.getApiKey().isEmpty();
    }

    private void setModel(LanguageModel model) {
        MainHook.log("setModel " + model.label);
        mModelClient = LanguageModelClient.forModel(model);
        mModelClient.setApiKey(mSPManager.getApiKey(model));
        mModelClient.setSubModel(mSPManager.getSubModel(model));
        mModelClient.setBaseUrl(mSPManager.getBaseUrl(model));
        mModelClient.setInternetProvider(mInternetProvider);
    }

    @Override
    public void onLanguageModelChange(LanguageModel model) {
        mSPManager.setLanguageModel(model);

        if (mModelClient == null || mModelClient.getLanguageModel() != model) {
            setModel(model);
        }
    }

    @Override
    public void onApiKeyChange(LanguageModel languageModel, String apiKey) {
        mSPManager.setApiKey(languageModel, apiKey);
        if (mModelClient != null && mModelClient.getLanguageModel() == languageModel) {
            mModelClient.setApiKey(apiKey);
        }
    }

    @Override
    public void onSubModelChange(LanguageModel languageModel, String subModel) {
        mSPManager.setSubModel(languageModel, subModel);
        if (mModelClient != null && mModelClient.getLanguageModel() == languageModel) {
            mModelClient.setSubModel(subModel);
        }
    }

    @Override
    public void onBaseUrlChange(LanguageModel languageModel, String baseUrl) {
        mSPManager.setBaseUrl(languageModel, baseUrl);
        if (mModelClient != null && mModelClient.getLanguageModel() == languageModel) {
            mModelClient.setBaseUrl(baseUrl);
        }
    }

    @Override
    public void onCommandsChange(String commandsRaw) {
        mSPManager.setGenerativeAICommandsRaw(commandsRaw);
    }

    public void addListener(GenerativeAIListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(GenerativeAIListener listener) {
        mListeners.remove(listener);
    }

    public void generateResponse(String prompt) {
        generateResponse(prompt, null);
    }

    public void generateResponse(String prompt, String systemMessage) {
        MainHook.log("Getting response for text \"" + prompt + "\"");

        if (prompt.isEmpty()) {
            return;
        }

        mInteractor.post(() ->
                mListeners.forEach(GenerativeAIListener::onAIPrepare));

        Publisher<String> publisher;
        if (needModelClient()) {
            publisher = new SimpleStringPublisher("Missing API Key");
        }
        else {
            publisher = mModelClient.submitPrompt(prompt, systemMessage);
        }

        publisher.subscribe(new Subscriber<String>() {
            boolean completed = false;

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                if (s == null || s.isEmpty()) {
                    return;
                }

                MainHook.log("onNext: string with length " + s.length());

                mInteractor.post(() -> mListeners.forEach(
                        l -> l.onAINext(s)));
            }

            @Override
            public void onError(Throwable t) {
                MainHook.log(t);
                onComplete();
            }

            @Override
            public void onComplete() {
                if (completed) {
                    MainHook.log("Skipping duplicate onComplete");
                    return;
                }
                completed = true;

                mInteractor.post(() ->
                        mListeners.forEach(GenerativeAIListener::onAIComplete));
                MainHook.log("Done");
            }
        });
    }

    public LanguageModel getLanguageModel() {
        return mModelClient.getLanguageModel();
    }

    public LanguageModelClient getModelClient() {
        return mModelClient;
    }
}
