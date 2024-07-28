package tn.amin.keyboard_gpt.instruction.prompt;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import de.robv.android.xposed.XposedBridge;
import tn.amin.keyboard_gpt.ConfigChangeListener;
import tn.amin.keyboard_gpt.DialogDismissListener;
import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.InstructionCategory;
import tn.amin.keyboard_gpt.instruction.TextTreater;
import tn.amin.keyboard_gpt.language_model.LanguageModel;
import tn.amin.keyboard_gpt.language_model.LanguageModelClient;

public class PromptTreater implements TextTreater, ConfigChangeListener, DialogDismissListener {
    private LanguageModelClient mModelClient = null;

    private final SPManager mSPManager;
    private final UiInteracter mInteracter;

    public PromptTreater(SPManager spManager, UiInteracter interacter) {
        mSPManager = spManager;
        mInteracter = interacter;

        mInteracter.registerConfigChangeListener(this);
        mInteracter.registerOnDismissListener(this);
        if (mSPManager.hasLanguageModel()) {
            setModel(mSPManager.getLanguageModel());
        }
    }

    @Override
    public boolean treat(String instruction) {
        if (instruction.startsWith("?")) {
            if (mInteracter.showChoseModelDialog()) {
                mInteracter.toastLong("Chose and configure your language model");
            }
            return true;
        }

        if (mModelClient == null) {
            if (mInteracter.showChoseModelDialog()) {
                mInteracter.toastLong("Chose and configure your language model");
            }
            return true;
        }

        if (mModelClient.getApiKey() == null || mModelClient.getApiKey().isEmpty()) {
            if (mInteracter.showChoseModelDialog()) {
                mInteracter.toastLong(mModelClient.getLanguageModel().label + " is Missing API Key");
            }
            return true;
        }

        new Thread(() -> generateResponse(instruction)).start();

        return false;
    }

    private void setModel(LanguageModel model) {
        mModelClient = LanguageModelClient.forModel(model);
        mModelClient.setApiKey(mSPManager.getApiKey(model));
        mModelClient.setSubModel(mSPManager.getSubModel(model));
        mModelClient.setBaseUrl(mSPManager.getBaseUrl(model));
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
    public void onDismiss(boolean isPrompt) {
        if (!isPrompt) {
            return;
        }

        MainHook.log("Selected " + mModelClient);
        mInteracter.post(() -> {
            mInteracter.toastShort("Selected " + mModelClient);
        });
    }

    public void generateResponse(String prompt) {
        MainHook.log("Getting response for text \"" + prompt + "\"");

        if (prompt.isEmpty()) {
            return;
        }

        if (!mInteracter.requestEditTextOwnership(InstructionCategory.Prompt)) {
            return;
        }

        mInteracter.post(() -> {
            mInteracter.setText("Generating Response...");
        });

        Publisher<String> publisher = mModelClient.submitPrompt(prompt);

        publisher.subscribe(new Subscriber<String>() {
            boolean completed = false;

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                if (s.isEmpty()) {
                    return;
                }

                MainHook.log("onNext: \"" + s + "\"");

                mInteracter.getInputConnection().commitText(s, 1);
            }

            @Override
            public void onError(Throwable t) {
                XposedBridge.log(t);

                mInteracter.post(() -> {
                    mInteracter.toastLong(t.getClass().getSimpleName() + " : " + t.getMessage() + " (see logs)");
                });

                onComplete();
            }

            @Override
            public void onComplete() {
                if (completed) {
                    MainHook.log("Skipping duplicate onComplete");
                    return;
                }
                completed = true;

                mInteracter.releaseEditTextOwnership(InstructionCategory.Prompt);
                mInteracter.post(() -> {
                    mInteracter.setText("? ");
                });
                MainHook.log("Done");
            }
        });
    }
}
