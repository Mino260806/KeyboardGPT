package tn.amin.keyboard_gpt.external.dialog.box;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import tn.amin.keyboard_gpt.external.ConfigContainer;
import tn.amin.keyboard_gpt.external.dialog.DialogBoxManager;
import tn.amin.keyboard_gpt.external.dialog.DialogType;
import tn.amin.keyboard_gpt.instruction.command.Commands;
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public abstract class DialogBox {
    private final Activity mParent;
    private final Dialog mDialog;
    private final Bundle mInputBundle;
    private final ConfigContainer mConfigContainer;
    private final DialogBoxManager mManager;

    private boolean canClose = true;

    public DialogBox(DialogBoxManager dialogManager, Activity parent,
                     Bundle inputBundle, ConfigContainer configContainer) {
        mManager = dialogManager;
        mParent = parent;
        mInputBundle = inputBundle;
        mConfigContainer = configContainer;
        mDialog = build();
        mDialog.setOnDismissListener(d -> {
            if (canClose) {
                returnToKeyboard();
            } else {
                canClose = true;
            }
        });
    }
    
    protected abstract Dialog build();

    public Dialog getDialog() {
        return mDialog;
    }

    public ConfigContainer getConfig() {
        return mConfigContainer;
    }

    public Bundle getInput() {
        return mInputBundle;
    }

    public Context getContext() {
        return mParent;
    }

    public Activity getParent() {
        return mParent;
    }

    public void switchToDialog(DialogType type) {
        silentDismiss();
        mManager.showDialog(type);
    }

    protected void returnToKeyboard() {
        Intent broadcastIntent = new Intent(UiInteractor.ACTION_DIALOG_RESULT);
        getConfig().fillIntent(broadcastIntent);
        getContext().sendBroadcast(broadcastIntent);
        getParent().finish();
    }

    protected void silentDismiss() {
        canClose = false;
        getDialog().dismiss();
    }

    protected void safeguardCommands() {
        if (getConfig().commands == null) {
            getConfig().commands = Commands.decodeCommands(
                    getInput().getString(UiInteractor.EXTRA_COMMAND_LIST));
        }
    }

    protected void safeguardModelData() {
        if (getConfig().selectedModel == null)
            getConfig().selectedModel =
                    LanguageModel.valueOf(getInput().getString(UiInteractor.EXTRA_CONFIG_SELECTED_MODEL));
        if (getConfig().languageModelsConfig == null)
            getConfig().languageModelsConfig =
                    getInput().getBundle(UiInteractor.EXTRA_CONFIG_LANGUAGE_MODEL);
    }

}
