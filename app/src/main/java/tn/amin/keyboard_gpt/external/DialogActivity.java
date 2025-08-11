package tn.amin.keyboard_gpt.external;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import tn.amin.keyboard_gpt.external.dialog.DialogBoxManager;
import tn.amin.keyboard_gpt.external.dialog.DialogType;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class DialogActivity extends Activity {
    private ConfigContainer mConfig = new ConfigContainer();

    private DialogBoxManager mDialogManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogType dialogType = DialogType.valueOf(
                getIntent().getStringExtra(UiInteractor.EXTRA_DIALOG_TYPE));

        mConfig = new ConfigContainer();
        Bundle inputBundle = getIntent().getExtras();
        mDialogManager = new DialogBoxManager(this, inputBundle, mConfig);
        mDialogManager.showDialog(dialogType);
    }
}
