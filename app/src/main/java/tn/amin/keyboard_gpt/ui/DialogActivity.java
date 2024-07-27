package tn.amin.keyboard_gpt.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.Arrays;

import tn.amin.keyboard_gpt.R;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.language_model.LanguageModel;

public class DialogActivity extends Activity {
    private DialogType mLastDialogType = null;

    private Bundle mLanguageModelsConfig;

    private LanguageModel mSelectedModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogType dialogType = DialogType.valueOf(
                getIntent().getStringExtra(UiInteracter.EXTRA_DIALOG_TYPE));

        Dialog dialog = buildDialog(dialogType);
        showDialog(dialog, dialogType);
    }

    private Dialog buildDialog(DialogType dialogType) {
        Dialog dialog;
        switch (dialogType) {
            case ChoseModel:
                dialog = buildChoseModelDialog();
                break;
            case SetApiKey:
                dialog = buildConfigureModelDialog();
                break;
            case WebSearch:
                dialog = buildWebSearchDialog();
                break;
            default:
                dialog = buildChoseModelDialog();
                break;
        }
        dialog.setOnDismissListener(d -> returnToKeyboard(dialogType));
        return dialog;
    }

    private Dialog buildWebSearchDialog() {
        String title = getIntent().getStringExtra(UiInteracter.EXTRA_WEBVIEW_TITLE);
        if (title == null) {
            title = "Untitled";
        }

        String url = getIntent().getStringExtra(UiInteracter.EXTRA_WEBVIEW_URL);
        if (url == null) {
            throw new NullPointerException(UiInteracter.EXTRA_WEBVIEW_URL + " cannot be null");
        }

        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient()); // Ensures links open in the WebView
        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript (optional)
        webView.loadUrl(url); // Replace with your URL
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(webView)
                .create();
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 100, 200, 100, 200);
        dialog.getWindow().setBackgroundDrawable(inset);

        return dialog;
    }

    private Dialog buildConfigureModelDialog() {
        ensureHasReadModelData();

        Bundle modelConfig = mLanguageModelsConfig.getBundle(mSelectedModel.name());
        if (modelConfig == null) {
            throw new RuntimeException("No model " + mSelectedModel.name());
        }

        String subModel = modelConfig.getString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL);
        subModel = subModel != null ? subModel : mSelectedModel.defaultSubModel;
        String apiKey = modelConfig.getString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY);
        String baseUrl = modelConfig.getString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL);
        baseUrl = baseUrl != null ? baseUrl : mSelectedModel.defaultBaseUrl;

        LinearLayout layout = (LinearLayout)
                getLayoutInflater().inflate(R.layout.dialog_configue_model, null);

        EditText apiKeyEditText = layout.findViewById(R.id.edit_apikey);
        EditText subModelEditText = layout.findViewById(R.id.edit_model);
        EditText baseUrlEditText = layout.findViewById(R.id.edit_baseurl);
        apiKeyEditText.setText(apiKey);
        subModelEditText.setText(subModel);
        baseUrlEditText.setText(baseUrl);

        return new AlertDialog.Builder(this)
                .setTitle(mSelectedModel.label + " configuration")
                .setView(layout)
                .setPositiveButton("Ok", (dialog, which) -> {
                    modelConfig.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY,
                            apiKeyEditText.getText().toString());
                    modelConfig.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL,
                            subModelEditText.getText().toString());
                    modelConfig.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL,
                            baseUrlEditText.getText().toString());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    showDialog(buildChoseModelDialog(), DialogType.ChoseModel);
                    dialog.dismiss();
                })
                .create();
    }

    private Dialog buildChoseModelDialog() {
        ensureHasReadModelData();

        CharSequence[] names = Arrays.stream(LanguageModel.values())
                .map((model) -> model.label).toArray(CharSequence[]::new);

        return new AlertDialog.Builder(this)
                .setTitle("Select Language Model")
                .setSingleChoiceItems(names, mSelectedModel.ordinal(), (dialog, which) -> {
                    mSelectedModel = LanguageModel.values()[which];

                    Dialog apiKeyDialog = buildConfigureModelDialog();
                    showDialog(apiKeyDialog, DialogType.SetApiKey);

                    dialog.dismiss();
                })
                .create();
    }

    private void returnToKeyboard(DialogType dialogType) {
        if (dialogType == mLastDialogType) {
            if (dialogType.isConfiguration) {
                Intent broadcastIntent = new Intent(UiInteracter.ACTION_DIALOG_RESULT);
                broadcastIntent.putExtra(UiInteracter.EXTRA_CONFIG_SELECTED_MODEL, mSelectedModel.name());
                broadcastIntent.putExtra(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL, mLanguageModelsConfig);

                sendBroadcast(broadcastIntent);
            }
            finish();
        }
    }

    private void ensureHasReadModelData() {
        mSelectedModel =
                LanguageModel.valueOf(getIntent().getStringExtra(UiInteracter.EXTRA_CONFIG_SELECTED_MODEL));
        mLanguageModelsConfig =
                getIntent().getBundleExtra(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL);
    }

    private void showDialog(Dialog dialog, DialogType dialogType) {
        mLastDialogType = dialogType;
        dialog.show();
    }
}
