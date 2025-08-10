package tn.amin.keyboard_gpt.external;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import tn.amin.keyboard_gpt.R;
import tn.amin.keyboard_gpt.instruction.command.AbstractCommand;
import tn.amin.keyboard_gpt.instruction.command.Commands;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.instruction.command.SimpleGenerativeAICommand;
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.llm.LanguageModelField;
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
