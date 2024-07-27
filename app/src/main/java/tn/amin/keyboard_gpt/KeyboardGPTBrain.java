package tn.amin.keyboard_gpt;

import android.content.Context;
import android.content.DialogInterface;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.text.InputType;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import de.robv.android.xposed.XposedBridge;
import tn.amin.keyboard_gpt.language_model.LanguageModel;
import tn.amin.keyboard_gpt.language_model.LanguageModelClient;

public class KeyboardGPTBrain implements ConfigChangeListener, DialogInterface.OnDismissListener {
    private String mLastText = null;

    private InputMethodService mInputMethodService = null;
    private boolean mTreatingCommand = false;
    private long mLastDialogLaunch = 0L;

    private WeakReference<EditText> mEditText = null;

    private final SPManager mSPManager;
    private final UiInteracter mInteracter;
    private final CommandTreater mCommandTreater;
    private final Toaster mToaster;

    private LanguageModelClient mModelClient = null;

    public KeyboardGPTBrain(Context context) {
        mSPManager = new SPManager(context);
        mInteracter = new UiInteracter(context, mSPManager);
        mCommandTreater = new CommandTreater();
        mToaster = new Toaster(context);

        mInteracter.registerConfigChangeListener(this);
        if (mSPManager.hasLanguageModel()) {
            setModel(mSPManager.getLanguageModel());
        }
    }

    public boolean setLastText(EditText editText, String text) {
        mEditText = new WeakReference<>(editText);
        mLastText = text;

        return !isEditTextOwned();
    }

    public boolean handleClearText() {
        if (!mCommandTreater.isPrompt(mLastText)) {
            MainHook.log("Aborting handleClearText because text is not a command");
            return false;
        }

        commandTreatStart();

        if (mModelClient == null) {
            if (showChooseModelDialog()) {
                mToaster.toastLong("Chose and configure your language model");
            }
            return true;
        }

        if (mModelClient.getApiKey() == null || mModelClient.getApiKey().isEmpty()) {
            if (showChooseModelDialog()) {
                mToaster.toastLong(mModelClient.getLanguageModel().label + " is Missing API Key");
            }
            return true;
        }

        if (mCommandTreater.isConfigureCommand(mLastText)) {
            if (showChooseModelDialog()) {
                mToaster.toastLong("Chose and configure your language model");
            }
            return false;
        }

        final String lastText = mLastText;
        new Thread(() -> generateResponse(lastText)).start();

        return false;
    }

    private boolean showChooseModelDialog() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastDialogLaunch < 3000) {
            MainHook.log("Preventing spam dialog launch. (" + currentTime + " ~ " + mLastDialogLaunch + ")");
            return false;
        }
        mLastDialogLaunch = currentTime;
        mInteracter.showChoseModelDialog(this);
        return true;
    }

    public void generateResponse(String lastText) {
        MainHook.log("Getting response for text \"" + lastText + "\"");

        String prompt = lastText.substring(1).trim();

        if (prompt.isEmpty()) {
            mEditText.get().post(() -> setText(lastText));

            commandTreatEnd();

            return;
        }

        mEditText.get().post(() -> {
            setText("Generating Response...");
            mEditText.get().setInputType(InputType.TYPE_NULL);
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

                getInputConnection().commitText(s, 1);
            }

            @Override
            public void onError(Throwable t) {
                XposedBridge.log(t);
                commandTreatEnd();

                mEditText.get().post(() -> {
                    mToaster.toastLong(t.getClass().getName() + " : " + t.getMessage() + " (see logs)");
                });

                onComplete();
            }

            @Override
            public void onComplete() {
                if (completed) {
                    MainHook.log("Skipping dumlicate onComplete");
                    return;
                }
                completed = true;

                commandTreatEnd();
                mLastText = null;
                mEditText.get().post(() -> {
                    mEditText.get().setInputType(InputType.TYPE_CLASS_TEXT);
                    setText("? ");
                });
                MainHook.log("Done");
            }
        });
    }

    private InputConnection getInputConnection() {
        return mInputMethodService.getCurrentInputConnection();
    }

    public boolean isEditTextOwned() {
        return !mTreatingCommand && !mCommandTreater.isPrompt(mLastText);
    }

    public UiInteracter getInteracter() {
        return mInteracter;
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
    public void onDismiss(DialogInterface dialog) {
        commandTreatEnd();

        MainHook.log("Selected " + mModelClient);
        mToaster.toastShort("Selected " + mModelClient);
    }

    private void setModel(LanguageModel model) {
        mModelClient = LanguageModelClient.forModel(model);
        mModelClient.setApiKey(mSPManager.getApiKey(model));
        mModelClient.setSubModel(mSPManager.getSubModel(model));
        mModelClient.setBaseUrl(mSPManager.getBaseUrl(model));
    }

    private void setText(String text) {
        mLastText = text;
        mEditText.get().setText(text);
        mEditText.get().setSelection(text.length());
    }

    public void commandTreatStart() {
        mTreatingCommand = true;
    }

    public void commandTreatEnd() {
        mTreatingCommand = false;
    }

    public void onInputMethodDestroy(InputMethodService inputMethodService) {
        mInputMethodService = null;
        getInteracter().unregisterService(inputMethodService);
    }

    public void onInputMethodCreate(InputMethodService inputMethodService) {
        mInputMethodService = inputMethodService;
        getInteracter().registerService(inputMethodService);
    }
}
