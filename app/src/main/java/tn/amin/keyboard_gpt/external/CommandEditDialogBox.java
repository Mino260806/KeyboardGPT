package tn.amin.keyboard_gpt.external;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import tn.amin.keyboard_gpt.R;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.instruction.command.SimpleGenerativeAICommand;

public class CommandEditDialogBox extends DialogBox {
    public CommandEditDialogBox(DialogBoxManager dialogManager, Activity parent,
                                Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @Override
    protected Dialog build() {
        safeguardCommands();

        LinearLayout layout = (LinearLayout)
                getParent().getLayoutInflater().inflate(R.layout.dialog_command_edit, null);

        EditText prefixEditText = layout.findViewById(R.id.edit_prefix);
        EditText messageEditText = layout.findViewById(R.id.edit_message);

        final int commandIndex = getConfig().focusCommandIndex;
        String title;
        if (commandIndex >= 0) {
            GenerativeAICommand command = getConfig().commands.get(commandIndex);
            prefixEditText.setText(command.getCommandPrefix());
            messageEditText.setText(command.getTweakMessage());
            title = "Edit command " + command.getCommandPrefix();
        }
        else {
            title = "New command";
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Ok", (dialog, which) -> {
                    int commandPos = commandIndex;

                    String prefix = prefixEditText.getText().toString().trim();
                    String message = messageEditText.getText().toString();
                    long similarCount = getConfig().commands.stream().filter((c) -> prefix.equals(c.getCommandPrefix())).count();
                    if ((commandPos == -1 && similarCount >= 1)
                            || (commandPos >= 0 && similarCount >= 2)) {
                        Toast.makeText(getContext(), "There is another command with same name", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (commandPos >= 0) {
                        getConfig().commands.remove(commandPos);
                    }
                    else {
                        commandPos = getConfig().commands.size();
                    }

                    getConfig().commands.add(commandPos, new SimpleGenerativeAICommand(prefix, message));

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    switchToDialog(DialogType.EditCommandsList);
                });

        if (commandIndex >= 0) {
            dialogBuilder
                    .setNeutralButton("Delete", (dialog, which) -> {
                        getConfig().commands.remove(commandIndex);

                        switchToDialog(DialogType.EditCommandsList);
                    });
        }

        return dialogBuilder.create();

    }

}
