package tn.amin.keyboard_gpt.external.dialog.box;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import java.util.stream.Stream;

import tn.amin.keyboard_gpt.external.ConfigContainer;
import tn.amin.keyboard_gpt.external.dialog.DialogBoxManager;
import tn.amin.keyboard_gpt.external.dialog.DialogType;
import tn.amin.keyboard_gpt.instruction.command.AbstractCommand;
import tn.amin.keyboard_gpt.text.parse.ParsePattern;

public class PatternListDialogBox extends DialogBox {
    public PatternListDialogBox(DialogBoxManager dialogManager, Activity parent,
                                Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @Override
    protected Dialog build() {
        safeguardPatterns();

        CharSequence[] names = getConfig().patterns.stream().map(p -> p.getType().title)
                .toArray(CharSequence[]::new);

        return new AlertDialog.Builder(getContext())
                .setTitle("Select Pattern")
                .setItems(names, (dialog, which) -> {
                    getConfig().focusPatternIndex = which;
                    switchToDialog(DialogType.EditCommand);
                })
                .setNegativeButton("Back", (dialog, which) -> {
                    switchToDialog(DialogType.Settings);
                })
                .create();
    }
}
