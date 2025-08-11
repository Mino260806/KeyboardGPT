package tn.amin.keyboard_gpt.external.dialog.box;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tn.amin.keyboard_gpt.R;
import tn.amin.keyboard_gpt.external.ConfigContainer;
import tn.amin.keyboard_gpt.external.dialog.DialogBoxManager;
import tn.amin.keyboard_gpt.external.dialog.DialogType;
import tn.amin.keyboard_gpt.llm.LanguageModelField;

public class ConfigureModelDialogBox extends DialogBox {
    public ConfigureModelDialogBox(DialogBoxManager dialogManager, Activity parent,
                                   Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @Override
    protected Dialog build() {
        safeguardModelData();

        Bundle modelConfig = getConfig().languageModelsConfig.getBundle(getConfig().selectedModel.name());
        if (modelConfig == null) {
            throw new RuntimeException("No model " + getConfig().selectedModel.name());
        }

        LinearLayout layout = (LinearLayout)
                getParent().getLayoutInflater().inflate(R.layout.dialog_configue_model, null);

        LinearLayout advancedLayout = new LinearLayout(getContext());
        advancedLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout advancedExpand = (LinearLayout)
                getParent().getLayoutInflater().inflate(R.layout.dialog_configure_model_advanced, null);

        Bundle tempModelConfig = new Bundle();
        for (LanguageModelField field: LanguageModelField.values()) {
            RelativeLayout fieldLayout = (RelativeLayout)
                    getParent().getLayoutInflater().inflate(R.layout.dialog_configure_model_field, layout, false);
            if (!field.advanced) {
                layout.addView(fieldLayout);
            } else {
                advancedLayout.addView(fieldLayout);
            }

            TextView fieldTitle = fieldLayout.findViewById(R.id.field_tile);
            EditText fieldEdit = fieldLayout.findViewById(R.id.field_edit);

            fieldTitle.setText(field.title);
            String fieldValue = modelConfig.getString(field.name);
            fieldEdit.setText(fieldValue != null ? fieldValue : getConfig().selectedModel.getDefault(field));
            fieldEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    tempModelConfig.putString(field.name, s.toString());
                }
            });
        }

        layout.addView(advancedExpand);
        layout.addView(advancedLayout);

        advancedLayout.setVisibility(View.GONE);
        ImageView advancedArrow = advancedExpand.findViewById(R.id.icon_advanced);
        advancedExpand.setOnClickListener(new View.OnClickListener() {
            private boolean expanded = false;
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                if (expanded) {
                    advancedArrow.animate().rotation(90);
                    advancedLayout.setVisibility(View.VISIBLE);
                } else {
                    advancedArrow.animate().rotation(0);
                    advancedLayout.setVisibility(View.GONE);
                }
            }
        });

        return new AlertDialog.Builder(getContext())
                .setTitle(getConfig().selectedModel.label + " configuration")
                .setView(layout)
                .setPositiveButton("Ok", (dialog, which) -> {
                    modelConfig.putAll(tempModelConfig);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    switchToDialog(DialogType.ChoseModel);
                })
                .create();
    }

}
