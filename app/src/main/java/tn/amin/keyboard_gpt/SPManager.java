package tn.amin.keyboard_gpt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tn.amin.keyboard_gpt.instruction.command.Commands;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.llm.client.LanguageModel;
import tn.amin.keyboard_gpt.listener.ConfigInfoProvider;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class SPManager implements ConfigInfoProvider {
    protected static final String PREF_NAME = "keyboard_gpt";

    protected static final String PREF_LANGUAGE_MODEL_COMPAT = "language_model";

    protected static final String PREF_LANGUAGE_MODEL = "language_model_v2";

    protected static final String PREF_API_KEY = "%s.api_key";

    protected static final String PREF_SUB_MODEL = "%s.sub_model";

    protected static final String PREF_BASE_URL = "%s.base_url";

    protected static final String PREF_GEN_AI_COMMANDS = "gen_ai_commands";

    protected final SharedPreferences sp;

    private List<GenerativeAICommand> generativeAICommands = List.of();

    private static SPManager instance = null;

    public static void init(Context context) {
        instance = new SPManager(context);
    }

    public static SPManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("Missing call to SPManager.init(Context)");
        }
        return instance;
    }

    private SPManager(Context context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        updateGenerativeAICommands();
    }

    public boolean hasLanguageModel() {
        return sp.contains(PREF_LANGUAGE_MODEL);
    }

    @Override
    public LanguageModel getLanguageModel() {
        String languageModelName = sp.getString(PREF_LANGUAGE_MODEL, null);
        if (languageModelName == null) {
            languageModelName = LanguageModel.Gemini.name();
        }
        return LanguageModel.valueOf(languageModelName);
    }

    public void setLanguageModel(LanguageModel model) {
        sp.edit().putString(PREF_LANGUAGE_MODEL, model.name()).apply();
    }

    public void setApiKey(LanguageModel model, String apiKey) {
        String key = String.format(PREF_API_KEY, model.name());
        sp.edit().putString(key, apiKey).apply();
    }

    public String getApiKey(LanguageModel model) {
        String key = String.format(PREF_API_KEY, model.name());
        return sp.getString(key, null);
    }

    public void setSubModel(LanguageModel model, String subModel) {
        String key = String.format(PREF_SUB_MODEL, model.name());
        sp.edit().putString(key, subModel).apply();
    }

    public String getSubModel(LanguageModel model) {
        String key = String.format(PREF_SUB_MODEL, model.name());
        return sp.getString(key, null);
    }

    public void setBaseUrl(LanguageModel model, String baseUrl) {
        String key = String.format(PREF_BASE_URL, model.name());
        sp.edit().putString(key, baseUrl).apply();
    }

    public String getBaseUrl(LanguageModel model) {
        String key = String.format(PREF_BASE_URL, model.name());
        return sp.getString(key, null);
    }

    public void setGenerativeAICommandsRaw(String commands) {
        sp.edit().putString(PREF_GEN_AI_COMMANDS, commands).apply();
        updateGenerativeAICommands();
    }

    public String getGenerativeAICommandsRaw() {
        return sp.getString(PREF_GEN_AI_COMMANDS, "[]");
    }

    public void setGenerativeAICommands(List<GenerativeAICommand> commands) {
        setGenerativeAICommandsRaw(Commands.encodeCommands(commands));
    }

    public List<GenerativeAICommand> getGenerativeAICommands() {
        return generativeAICommands;
    }

    private void updateGenerativeAICommands() {
        generativeAICommands = Collections.unmodifiableList(
                Commands.decodeCommands(sp.getString(PREF_GEN_AI_COMMANDS, "[]")));
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

            configBundle.putString(UiInteractor.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY, getApiKey(model));
            configBundle.putString(UiInteractor.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL, getSubModel(model));
            configBundle.putString(UiInteractor.EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL, getBaseUrl(model));

            bundle.putBundle(model.name(), configBundle);
        }
        return bundle;
    }
}
