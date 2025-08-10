package tn.amin.keyboard_gpt.external;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;

import tn.amin.keyboard_gpt.R;
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.llm.LanguageModelField;

public class ChoseModelDialogBox extends DialogBox {
    public ChoseModelDialogBox(DialogBoxManager dialogManager, Activity parent,
                               Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @Override
    protected Dialog build() {
        safeguardModelData();

        CharSequence[] names = Arrays.stream(LanguageModel.values())
                .map((model) -> model.label).toArray(CharSequence[]::new);

        return new AlertDialog.Builder(getContext())
                .setTitle("Select Language Model")
                .setSingleChoiceItems(names, getConfig().selectedModel.ordinal(), (dialog, which) -> {
                    getConfig().selectedModel = LanguageModel.values()[which];

                    switchToDialog(DialogType.ConfigureModel);
                })
                .create();
    }

}
