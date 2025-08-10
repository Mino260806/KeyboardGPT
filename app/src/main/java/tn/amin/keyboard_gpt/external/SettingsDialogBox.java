package tn.amin.keyboard_gpt.external;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import tn.amin.keyboard_gpt.instruction.command.AbstractCommand;

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
