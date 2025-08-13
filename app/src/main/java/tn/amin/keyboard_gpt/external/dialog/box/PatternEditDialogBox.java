package tn.amin.keyboard_gpt.external.dialog.box;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.regex.Pattern;

import tn.amin.keyboard_gpt.R;
import tn.amin.keyboard_gpt.external.ConfigContainer;
import tn.amin.keyboard_gpt.external.dialog.DialogBoxManager;
import tn.amin.keyboard_gpt.external.dialog.DialogType;
import tn.amin.keyboard_gpt.text.parse.ParsePattern;

public class PatternEditDialogBox extends DialogBox {
    public PatternEditDialogBox(DialogBoxManager dialogManager, Activity parent,
                                Bundle inputBundle, ConfigContainer configContainer) {
        super(dialogManager, parent, inputBundle, configContainer);
    }

    @Override
    protected Dialog build() {
        safeguardPatterns();

        LinearLayout layout = (LinearLayout)
                getParent().getLayoutInflater().inflate(R.layout.dialog_pattern_edit, null);

        EditText regexEditText = layout.findViewById(R.id.edit_regex);
        ImageView regexButton = layout.findViewById(R.id.button_regex);

        final int patternIndex = getConfig().focusPatternIndex;
        ParsePattern pattern = getConfig().patterns.get(patternIndex);
        regexEditText.setText(pattern.getPattern().pattern());

        if (!pattern.getType().editable) {
            regexEditText.setEnabled(false);
            regexButton.setEnabled(false);
        }

        regexButton.setOnClickListener(v -> {
            String symbol = regexEditText.getText().toString().trim();
            if (symbol.length() != 1) {
                Toast.makeText(getContext(), "Symbol length must be 1", Toast.LENGTH_LONG).show();
                return;
            }

            if (List.of("]", "[", "-", " ", "\n", "\t").contains(symbol)) {
                Toast.makeText(getContext(), "Forbidden symbol", Toast.LENGTH_LONG).show();
                return;
            }

            String litteralSymbol = symbol;
            if (List.of("\\", "^").contains(symbol)) {
                litteralSymbol = "\\" + symbol;
            }

            String regex;
            if (pattern.getType().groupCount == 1) {
                String regexBlueprint = "%s([^%s]*)%s$";
                regex = String.format(regexBlueprint, Pattern.quote(symbol), litteralSymbol, Pattern.quote(symbol));
            } else if (pattern.getType().groupCount == 2) {
                String regexBlueprint = "%s(?:([^ %s]+) *)?([^%s]+)?%s$";
                regex = String.format(regexBlueprint, Pattern.quote(symbol),
                        litteralSymbol, litteralSymbol, Pattern.quote(symbol));
            } else {
                Toast.makeText(getContext(), "Automatic regex not supported by this pattern", Toast.LENGTH_LONG).show();
                return;
            }

            regexEditText.setText(regex);
        });

        String title = "Edit pattern " + pattern.getType().title;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Ok", (dialog, which) -> {
                    String regex = regexEditText.getText().toString();
                    if (!regex.matches(".*(?<!\\\\)\\$")) {
                        Toast.makeText(getContext(), "Regular expression must end with $", Toast.LENGTH_LONG).show();
                        return;
                    }

                    long similarCount = getConfig().patterns.stream()
                            .filter((c) -> c.getPattern().pattern().equals(regex)).count();
                    if (similarCount >= 1) {
                        Toast.makeText(getContext(), "There is another pattern with same regex", Toast.LENGTH_LONG).show();
                        return;
                    }

                    getConfig().patterns.remove(patternIndex);
                    getConfig().patterns.add(patternIndex, new ParsePattern(pattern.getType(), regex));

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    switchToDialog(DialogType.EditPatternList);
                });

        return dialogBuilder.create();

    }

}
