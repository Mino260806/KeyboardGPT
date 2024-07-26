package tn.amin.keyboard_gpt;

import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditTextController {
    Map<EditText, ArrayList<TextWatcher>> mWatchers = new HashMap<>();

    private boolean mDisabled = false;

    public void addTextWatcher(EditText editText, TextWatcher textWatcher) {
        if (!mWatchers.containsKey(editText)) {
            mWatchers.put(editText, new ArrayList<>());
        }
        mWatchers.get(editText).add(textWatcher);

        if (mDisabled) {
            editText.removeTextChangedListener(textWatcher);
        }
    }

    public void disableAllWatchers() {
        if (mDisabled) {
            return;
        }
        mDisabled = true;
        mWatchers.forEach((editText, textWatchers) ->
                textWatchers.forEach(editText::removeTextChangedListener));
    }

    public void enableAllWatchers() {
        if (!mDisabled) {
            return;
        }
        mDisabled = false;
        mWatchers.forEach((editText, textWatchers) ->
                textWatchers.forEach(editText::addTextChangedListener));
    }
}
