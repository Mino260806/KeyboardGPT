package tn.amin.keyboard_gpt;

import android.content.Context;

import tn.amin.keyboard_gpt.language_model.LanguageModel;

public class SPManagerCompat extends SPManager {
    public SPManagerCompat(Context context) {
        super(context);
    }

    @Override
    public boolean hasLanguageModel() {
        return super.hasLanguageModel() || mSP.contains(PREF_LANGUAGE_MODEL_COMPAT);
    }

    @Override
    public LanguageModel getLanguageModel() {
        if (mSP.contains(PREF_LANGUAGE_MODEL_COMPAT)) {
            int index = mSP.getInt(PREF_LANGUAGE_MODEL_COMPAT, 0);
            String languageModelName = new String[] {
                    "Gemini",
                    "ChatGPT",
                    "Groq",
            } [index];
            LanguageModel languageModel = LanguageModel.valueOf(languageModelName);
            MainHook.log("Migrating legacy languageModel key \"" + PREF_LANGUAGE_MODEL_COMPAT + "\" with value \"" + index + "\"");

            mSP.edit().remove(PREF_LANGUAGE_MODEL_COMPAT).apply();
            setLanguageModel(languageModel);
            return languageModel;
        }

        return super.getLanguageModel();
    }
}
