package tn.amin.keyboard_gpt.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
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
        mSelectedModel =
                LanguageModel.valueOf(getIntent().getStringExtra(UiInteracter.EXTRA_CONFIG_SELECTED_MODEL));
        mLanguageModelsConfig =
                getIntent().getBundleExtra(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL);

        Dialog dialog = buildDialog(dialogType);
        showDialog(dialog, dialogType);
    }

    private Dialog buildDialog(DialogType dialogType) {
        switch (dialogType) {
            case ChoseModel:
                return buildChoseModelDialog();
            case SetApiKey:
                return buildConfigureModelDialog();
            default:
                return buildChoseModelDialog();
        }
    }

    private Dialog buildConfigureModelDialog() {
        Bundle modelConfig = mLanguageModelsConfig.getBundle(mSelectedModel.name());
        if (modelConfig == null) {
            throw new RuntimeException("No model " + mSelectedModel.name());
        }

        String subModel = modelConfig.getString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL);
        subModel = subModel != null ? subModel : mSelectedModel.defaultSubModel;
        String apiKey = modelConfig.getString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY);

        LinearLayout layout = (LinearLayout)
                getLayoutInflater().inflate(R.layout.dialog_configue_model, null);

        EditText apiKeyEditText = layout.findViewById(R.id.edit_apikey);
        EditText subModelEditText = layout.findViewById(R.id.edit_model);
        apiKeyEditText.setText(apiKey);
        subModelEditText.setText(subModel);

        return new AlertDialog.Builder(this)
                .setTitle(mSelectedModel.label + " configuration")
                .setView(layout)
                .setPositiveButton("Ok", (dialog, which) -> {
                    modelConfig.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY,
                            apiKeyEditText.getText().toString());
                    modelConfig.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL,
                            subModelEditText.getText().toString());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    showDialog(buildChoseModelDialog(), DialogType.ChoseModel);
                    dialog.dismiss();
                })
                .setOnDismissListener(dialog -> returnToKeyboard(DialogType.SetApiKey))
                .create();
    }

    private Dialog buildChoseModelDialog() {
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
                .setOnDismissListener(dialog -> returnToKeyboard(DialogType.ChoseModel))
                .create();
    }

    private void returnToKeyboard(DialogType dialogType) {
        if (dialogType == mLastDialogType) {
            Intent broadcastIntent = new Intent(UiInteracter.ACTION_DIALOG_RESULT);
            broadcastIntent.putExtra(UiInteracter.EXTRA_CONFIG_SELECTED_MODEL, mSelectedModel.name());
            broadcastIntent.putExtra(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL, mLanguageModelsConfig);

            sendBroadcast(broadcastIntent);
            finish();
        }
    }

    private void showDialog(Dialog dialog, DialogType dialogType) {
        mLastDialogType = dialogType;
        dialog.show();
    }
}
