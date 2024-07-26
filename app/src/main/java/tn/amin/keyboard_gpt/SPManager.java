package tn.amin.keyboard_gpt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tn.amin.keyboard_gpt.language_model.LanguageModel;

public class SPManager implements ConfigInfoProvider {
    private static final String PREF_NAME = "keyboard_gpt";

    private static final String PREF_LANGUAGE_MODEL = "language_model";

    private static final String PREF_API_KEY = "%s.api_key";

    private static final String PREF_SUB_MODEL = "%s.sub_model";

    private final SharedPreferences mSP;

    public SPManager(Context context) {
        mSP = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean hasLanguageModel() {
        return mSP.contains(PREF_LANGUAGE_MODEL);
    }

    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.values()[mSP.getInt(PREF_LANGUAGE_MODEL, 0)];
    }

    public void setLanguageModel(LanguageModel model) {
        mSP.edit().putInt(PREF_LANGUAGE_MODEL, model.ordinal()).apply();
    }

    public void setApiKey(LanguageModel model, String apiKey) {
        String key = String.format(PREF_API_KEY, model.name());
        mSP.edit().putString(key, apiKey).apply();
    }

    public String getApiKey(LanguageModel model) {
        String key = String.format(PREF_API_KEY, model.name());
        return mSP.getString(key, null);
    }

    public void setSubModel(LanguageModel model, String subModel) {
        String key = String.format(PREF_SUB_MODEL, model.name());
        mSP.edit().putString(key, subModel).apply();
    }

    public String getSubModel(LanguageModel model) {
        String key = String.format(PREF_SUB_MODEL, model.name());
        return mSP.getString(key, null);
    }

    public Map<LanguageModel, String> getApiKeyMap() {
        return Arrays.stream(LanguageModel.values())
                .collect(Collectors.toMap(model -> model, model -> {
                    String apiKey = getApiKey(model);
                    if (apiKey == null)
                        apiKey = "";
                    return apiKey;
                }));
    }

    @Override
    public Bundle getConfigBundle() {
        Bundle bundle = new Bundle();
        for (LanguageModel model: LanguageModel.values()) {
            Bundle configBundle = new Bundle();

            configBundle.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY, getApiKey(model));
            configBundle.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL, getSubModel(model));

            bundle.putBundle(model.name(), configBundle);
        }
        return bundle;
    }
}
