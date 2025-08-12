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

public class CommandListDialogBox extends DialogBox {
    public CommandListDialogBox(DialogBoxManager dialogManager, Activity parent,
                                Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @Override
    protected Dialog build() {
        safeguardCommands();

        CharSequence[] names = getConfig().commands.stream().map(AbstractCommand::getCommandPrefix)
                .toArray(CharSequence[]::new);

        return new AlertDialog.Builder(getContext())
                .setTitle("Select Command")
                .setItems(names, (dialog, which) -> {
                    getConfig().focusCommandIndex = which;
                    switchToDialog(DialogType.EditCommand);
                })
                .setNegativeButton("Back", (dialog, which) -> {
                    switchToDialog(DialogType.Settings);
                })
                .setPositiveButton("New", (dialog, which) -> {
                    getConfig().focusCommandIndex = -1;
                    switchToDialog(DialogType.EditCommand);
                })
                .create();
    }
}
