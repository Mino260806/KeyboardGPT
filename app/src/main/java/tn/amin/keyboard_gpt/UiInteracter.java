package tn.amin.keyboard_gpt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import tn.amin.keyboard_gpt.language_model.LanguageModel;
import tn.amin.keyboard_gpt.ui.DialogType;

public class UiInteracter {
    private final Context mContext;

    public static final String ACTION_DIALOG_RESULT = "tn.amin.keyboard_gpt.DIALOG_RESULT";

    public static final String EXTRA_DIALOG_TYPE = "tn.amin.keyboard_gpt.overlay.DIALOG_TYPE";

    public static final String EXTRA_CONFIG_SELECTED_MODEL = "tn.amin.keyboard_gpt.config.SELECTED_MODEL";

    public static final String EXTRA_CONFIG_LANGUAGE_MODEL = "tn.amin.keyboard_gpt.config.model";

    public static final String EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL = "tn.amin.keyboard_gpt.config.model.BASE_URL";

    public static final String EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY = "tn.amin.keyboard_gpt.config.model.API_KEY";

    public static final String EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL = "tn.amin.keyboard_gpt.config.model.SUB_MODEL";


    public static final String EXTRA_WEBVIEW_TITLE = "tn.amin.keyboard_gpt.webview.TITLE";

    public static final String EXTRA_WEBVIEW_URL = "tn.amin.keyboard_gpt.webview.URL";


    public static final String EXTRA_COMMAND_LIST = "tn.amin.keyboard_gpt.command.LIST";

    public static final String EXTRA_COMMAND_INDEX = "tn.amin.keyboard_gpt.command.INDEX";


    private final ConfigInfoProvider mConfigInfoProvider;
    private final ArrayList<ConfigChangeListener> mConfigChangeListeners = new ArrayList<>();
    private final ArrayList<DialogDismissListener> mOnDismissListeners = new ArrayList<>();

    private long mLastDialogLaunch = 0L;
    private Object mEditTextOwner = null;

    private WeakReference<View> mRootView = null;
    private WeakReference<EditText> mEditText = null;
    private InputMethodService mInputMethodService;

    public UiInteracter(Context context, ConfigInfoProvider configInfoProvider) {
        mContext = context;
        mConfigInfoProvider = configInfoProvider;
    }

    private final BroadcastReceiver mDialogResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_DIALOG_RESULT.equals(intent.getAction())) {
                MainHook.log("Got result");
                boolean isPrompt = false;
                if (!mConfigChangeListeners.isEmpty() && intent.getExtras() != null) {
                    for (String key: intent.getExtras().keySet()) {
                        switch (key) {
                            case EXTRA_CONFIG_SELECTED_MODEL:
                                LanguageModel selectedLanguageModel = LanguageModel.valueOf(
                                        intent.getStringExtra(EXTRA_CONFIG_SELECTED_MODEL));
                                mConfigChangeListeners.forEach((l) -> l.onLanguageModelChange(selectedLanguageModel));
                                isPrompt = true;
                                break;
                            case EXTRA_CONFIG_LANGUAGE_MODEL:
                                Bundle bundle = intent.getBundleExtra(EXTRA_CONFIG_LANGUAGE_MODEL);
                                for (String modelName: bundle.keySet()) {
                                    LanguageModel configuredlanguageModel = LanguageModel.valueOf(modelName);
                                    Bundle languageModelBundle = bundle.getBundle(modelName);

                                    String apiKey = languageModelBundle.getString(EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY);
                                    String subModel = languageModelBundle.getString(EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL);
                                    String baseUrl = languageModelBundle.getString(EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL);

                                    mConfigChangeListeners.forEach((l) -> l.onApiKeyChange(configuredlanguageModel, apiKey));
                                    mConfigChangeListeners.forEach((l) -> l.onSubModelChange(configuredlanguageModel, subModel));
                                    mConfigChangeListeners.forEach((l) -> l.onBaseUrlChange(configuredlanguageModel, baseUrl));
                                }
                                isPrompt = true;
                                break;
                        }
                    }
                }
                final boolean finalIsPrompt = isPrompt;
                mOnDismissListeners.forEach((l) -> l.onDismiss(finalIsPrompt));
            }
        }
    };

    public boolean showChoseModelDialog() {
        if (isDialogOnCooldown()) {
            return false;
        }

        Intent intent = new Intent("tn.amin.keyboard_gpt.OVERLAY");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_DIALOG_TYPE, DialogType.ChoseModel.name());
        intent.putExtra(EXTRA_CONFIG_LANGUAGE_MODEL, mConfigInfoProvider.getConfigBundle());
        intent.putExtra(EXTRA_CONFIG_SELECTED_MODEL,
                mConfigInfoProvider.getLanguageModel().name());

        MainHook.log("Launching configure dialog");
        mContext.startActivity(intent);
        return true;
    }

    public boolean showWebSearchDialog(String title, String url) {
        if (isDialogOnCooldown()) {
            return false;
        }

        Intent intent = new Intent("tn.amin.keyboard_gpt.OVERLAY");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_DIALOG_TYPE, DialogType.WebSearch.name());
        intent.putExtra(EXTRA_WEBVIEW_TITLE, title);
        intent.putExtra(EXTRA_WEBVIEW_URL, url);

        MainHook.log("Launching web search");
        mContext.startActivity(intent);

        return true;
    }

    public boolean showEditCommandsDialog(String rawCommands) {
        if (isDialogOnCooldown()) {
            return false;
        }

        Intent intent = new Intent("tn.amin.keyboard_gpt.OVERLAY");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_DIALOG_TYPE, DialogType.EditCommandsList.name());
        intent.putExtra(EXTRA_COMMAND_LIST, rawCommands);

        MainHook.log("Launching commands edit");
        mContext.startActivity(intent);

        return true;
    }

    public void updateRootView(View view) {
        MainHook.log("Root View is " + view.getRootView().getClass().getName());
        mRootView = new WeakReference<>(view);
    }

    public void setEditText(EditText editText) {
        mEditText = new WeakReference<>(editText);
        updateRootView(editText.getRootView());
    }

    public void registerConfigChangeListener(ConfigChangeListener listener) {
        mConfigChangeListeners.add(listener);
    }

    public void registerOnDismissListener(DialogDismissListener listener) {
        mOnDismissListeners.add(listener);
    }

    public void registerService(InputMethodService inputMethodService) {
        mInputMethodService = inputMethodService;
        IntentFilter filter = new IntentFilter(ACTION_DIALOG_RESULT);
        ContextCompat.registerReceiver(inputMethodService.getApplicationContext(), mDialogResultReceiver,
                filter, ContextCompat.RECEIVER_EXPORTED);
    }

    public void unregisterService(InputMethodService inputMethodService) {
        inputMethodService.getApplicationContext().unregisterReceiver(mDialogResultReceiver);
        if (inputMethodService != mInputMethodService) {
            MainHook.log("[W] inputMethodService do not correspond in unregisterService");
            mInputMethodService = null;
        }
    }


    private boolean isDialogOnCooldown() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastDialogLaunch < 3000) {
            MainHook.log("Preventing spam dialog launch. (" + currentTime + " ~ " + mLastDialogLaunch + ")");
            return true;
        }
        mLastDialogLaunch = currentTime;
        return false;
    }

    public void setText(String text) {
        mEditText.get().setText(text);
        mEditText.get().setSelection(text.length());
    }

    public void setInputType(int inputType) {
        mEditText.get().setInputType(inputType);
    }

    public void toastShort(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void toastLong(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void post(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public InputConnection getInputConnection() {
        return mInputMethodService.getCurrentInputConnection();
    }

    public boolean requestEditTextOwnership(Object newOwner) {
        if (mEditText == null || mEditText.get() == null) {
            MainHook.log("Refused EditText ownership to " + newOwner + " because EditText is null");
            return false;
        }

        if (mEditTextOwner != null && !newOwner.equals(mEditTextOwner)) {
            MainHook.log("Refused EditText ownership to " + newOwner + " because owned by " + mEditTextOwner);
            return false;
        }

        mEditTextOwner = newOwner;
        setInputType(InputType.TYPE_NULL);
        return true;
    }

    public void releaseEditTextOwnership(Object owner) {
        if (owner != mEditTextOwner) {
            MainHook.log("EditText owner " + owner + " cannot release EditText");
            return;
        }

        setInputType(InputType.TYPE_CLASS_TEXT);
        mEditTextOwner = null;
    }
}
