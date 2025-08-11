package tn.amin.keyboard_gpt.external.dialog.box;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import java.util.Arrays;

import tn.amin.keyboard_gpt.external.ConfigContainer;
import tn.amin.keyboard_gpt.external.dialog.DialogBoxManager;
import tn.amin.keyboard_gpt.external.dialog.DialogType;
import tn.amin.keyboard_gpt.llm.LanguageModel;

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
                .setNegativeButton("Back", (dialog, which) -> {
                    switchToDialog(DialogType.Settings);
                })
                .create();
    }

}
