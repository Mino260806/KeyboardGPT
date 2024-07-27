package tn.amin.keyboard_gpt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;

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


    private final ConfigInfoProvider mConfigInfoProvider;
    private ConfigChangeListener mConfigChangeListener = null;
    private DialogInterface.OnDismissListener mOnDismissListener = null;

    private long mLastDialogLaunch = 0L;

    public UiInteracter(Context context, ConfigInfoProvider configInfoProvider) {
        mContext = context;
        mConfigInfoProvider = configInfoProvider;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    private final BroadcastReceiver mDialogResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_DIALOG_RESULT.equals(intent.getAction())) {
                MainHook.log("Got result");
                if (mConfigChangeListener != null && intent.getExtras() != null) {
                    for (String key: intent.getExtras().keySet()) {
                        LanguageModel languageModel;
                        switch (key) {
                            case EXTRA_CONFIG_SELECTED_MODEL:
                                languageModel = LanguageModel.valueOf(
                                        intent.getStringExtra(EXTRA_CONFIG_SELECTED_MODEL));
                                mConfigChangeListener.onLanguageModelChange(languageModel);
                                break;
                            case EXTRA_CONFIG_LANGUAGE_MODEL:
                                Bundle bundle = intent.getBundleExtra(EXTRA_CONFIG_LANGUAGE_MODEL);
                                for (String modelName: bundle.keySet()) {
                                    languageModel = LanguageModel.valueOf(modelName);
                                    Bundle languageModelBundle = bundle.getBundle(modelName);

                                    String apiKey = languageModelBundle.getString(EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY);
                                    String subModel = languageModelBundle.getString(EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL);
                                    String baseUrl = languageModelBundle.getString(EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL);

                                    mConfigChangeListener.onApiKeyChange(languageModel, apiKey);
                                    mConfigChangeListener.onSubModelChange(languageModel, subModel);
                                    mConfigChangeListener.onBaseUrlChange(languageModel, baseUrl);
                                }
                                break;
                        }
                    }
                }
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss(null);
                }
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

    boolean onceFeedView = false;
    public void feedView(View view) {
        if (!onceFeedView) {
            MainHook.log("Root View is " + view.getRootView().getClass().getName());
            onceFeedView = true;
        }
    }

    public void registerConfigChangeListener(ConfigChangeListener listener) {
        mConfigChangeListener = listener;
    }

    public void registerService(InputMethodService inputMethodService) {
        IntentFilter filter = new IntentFilter(ACTION_DIALOG_RESULT);
        ContextCompat.registerReceiver(inputMethodService.getApplicationContext(), mDialogResultReceiver,
                filter, ContextCompat.RECEIVER_EXPORTED);
    }

    public void unregisterService(InputMethodService inputMethodService) {
        inputMethodService.getApplicationContext().unregisterReceiver(mDialogResultReceiver);
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
}
