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
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.listener.ConfigInfoProvider;
import tn.amin.keyboard_gpt.llm.LanguageModelField;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class SPManager implements ConfigInfoProvider {
    protected static final String PREF_NAME = "keyboard_gpt";

    protected static final String PREF_MODULE_VERSION = "module_version";

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
        updateVersion();
        updateGenerativeAICommands();
    }

    private void updateVersion() {
        SharedPreferences.Editor editor = sp.edit();
        int version = getVersion();
        if (version == -1) {
            MainHook.log("Clearing SP because no version found");
            editor.clear();
        }

        if (version != BuildConfig.VERSION_CODE) {
            editor.putInt(PREF_MODULE_VERSION, BuildConfig.VERSION_CODE);
        }
        editor.apply();
    }

    public int getVersion() {
        return sp.getInt(PREF_MODULE_VERSION, -1);
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

    public void setLanguageModelField(LanguageModel model, LanguageModelField field, String value) {
        String entryName = String.format("%s." + field, model.name());
        sp.edit().putString(entryName, value).apply();
    }

    public String getLanguageModelField(LanguageModel model, LanguageModelField field) {
        String entryName = String.format("%s." + field, model.name());
        return sp.getString(entryName, model.getDefault(field));
    }

    public void setApiKey(LanguageModel model, String apiKey) {
        setLanguageModelField(model, LanguageModelField.ApiKey, apiKey);
    }

    public String getApiKey(LanguageModel model) {
        return getLanguageModelField(model, LanguageModelField.ApiKey);
    }

    public void setSubModel(LanguageModel model, String subModel) {
        setLanguageModelField(model, LanguageModelField.SubModel, subModel);
    }

    public String getSubModel(LanguageModel model) {
        return getLanguageModelField(model, LanguageModelField.SubModel);
    }

    public void setBaseUrl(LanguageModel model, String baseUrl) {
        setLanguageModelField(model, LanguageModelField.BaseUrl, baseUrl);
    }

    public String getBaseUrl(LanguageModel model) {
        return getLanguageModelField(model, LanguageModelField.BaseUrl);
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

            for (LanguageModelField field: LanguageModelField.values()) {
                configBundle.putString(field.name, getLanguageModelField(model, field));
            }

            bundle.putBundle(model.name(), configBundle);
        }
        return bundle;
    }
}
