package tn.amin.keyboard_gpt.external.dialog.box;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import java.util.Arrays;

import tn.amin.keyboard_gpt.external.ConfigContainer;
import tn.amin.keyboard_gpt.external.dialog.DialogBoxManager;
import tn.amin.keyboard_gpt.external.dialog.DialogType;

public class SettingsDialogBox extends DialogBox {
    public SettingsDialogBox(DialogBoxManager dialogManager, Activity parent,
                             Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @Override
    protected Dialog build() {
        DialogType[] items = Arrays.stream(DialogType.values())
                .filter(t -> t.inSettings).toArray(DialogType[]::new);
        String[] itemsTitles = Arrays.stream(items).map(t -> t.title)
                .toArray(String[]::new);

        return new AlertDialog.Builder(getContext())
                .setTitle("KeyboardGPT Settings")
                .setItems(itemsTitles, (dialog, which) -> {
                    DialogType type = items[which];
                    switchToDialog(type);
                })
                .create();
    }
}
