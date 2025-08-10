package tn.amin.keyboard_gpt.external;

import android.app.Activity;
import android.os.Bundle;

public class DialogBoxManager {
    private final Activity mParent;
    private final Bundle mInputBundle;
    private final ConfigContainer mConfig;

    private DialogBox mCurrentDialogBox;

    public DialogBoxManager(Activity parent, Bundle inputBundle, ConfigContainer config) {
        mParent = parent;
        mInputBundle = inputBundle;
        mConfig = config;
    }

    public void showDialog(DialogType type) {
        DialogBox box = buildBox(type);
        mCurrentDialogBox = box;
        box.getDialog().show();
    }

    private DialogBox buildBox(DialogType dialogType) {
        DialogBox box;
        switch (dialogType) {
            case ChoseModel:
                box = new ChoseModelDialogBox(this, mParent, mInputBundle, mConfig);
                break;
            case ConfigureModel:
                box = new ConfigureModelDialogBox(this, mParent, mInputBundle, mConfig);
                break;
            case EditCommandsList:
                box = new CommandListDialogBox(this, mParent, mInputBundle, mConfig);
                break;
            case EditCommand:
                box = new CommandEditDialogBox(this, mParent, mInputBundle, mConfig);
                break;
            case WebSearch:
                box = new WebSearchDialogBox(this, mParent, mInputBundle, mConfig);
                break;
            case Settings:
            default:
                box = new SettingsDialogBox(this, mParent, mInputBundle, mConfig);
                break;
        }
        return box;
    }
}
