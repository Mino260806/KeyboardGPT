package tn.amin.keyboard_gpt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.view.View;

import tn.amin.keyboard_gpt.language_model.LanguageModel;
import tn.amin.keyboard_gpt.ui.DialogType;

public class UiInteracter {
    private final Context mContext;

    public static final String ACTION_DIALOG_RESULT = "tn.amin.keyboard_gpt.DIALOG_RESULT";

    public static final String EXTRA_DIALOG_TYPE = "tn.amin.keyboard_gpt.overlay.DIALOG_TYPE";

    public static final String EXTRA_CONFIG_SELECTED_MODEL = "tn.amin.keyboard_gpt.config.SELECTED_MODEL";

    public static final String EXTRA_CONFIG_LANGUAGE_MODEL = "tn.amin.keyboard_gpt.config.model";

    public static final String EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY = "tn.amin.keyboard_gpt.config.model.EXTRA_CONFIG_BUNDLE_API_KEY";

    public static final String EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL = "tn.amin.keyboard_gpt.config.model.EXTRA_CONFIG_BUNDLE_SUB_MODEL";


    private final ConfigInfoProvider mConfigInfoProvider;
    private ConfigChangeListener mConfigChangeListener = null;
    private DialogInterface.OnDismissListener mOnDismissListener = null;

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

                                    mConfigChangeListener.onApiKeyChange(languageModel, apiKey);
                                    mConfigChangeListener.onSubModelChange(languageModel, subModel);
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

    public UiInteracter(Context context, ConfigInfoProvider configInfoProvider) {
        mContext = context;
        mConfigInfoProvider = configInfoProvider;
    }

    public void onInputMethodService(InputMethodService inputMethodService) {
        IntentFilter filter = new IntentFilter(ACTION_DIALOG_RESULT);
        inputMethodService.registerReceiver(mDialogResultReceiver, filter);
    }

    public void showChoseModelDialog(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
        Intent intent = new Intent("tn.amin.keyboard_gpt.OVERLAY");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_DIALOG_TYPE, DialogType.ChoseModel.name());
        intent.putExtra(EXTRA_CONFIG_LANGUAGE_MODEL, mConfigInfoProvider.getConfigBundle());
        intent.putExtra(EXTRA_CONFIG_SELECTED_MODEL,
                mConfigInfoProvider.getLanguageModel().name());
        mContext.startActivity(intent);
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

    public void unregisterService(InputMethodService inputMethodService) {
        inputMethodService.unregisterReceiver(mDialogResultReceiver);
    }
}
