package tn.amin.keyboard_gpt;

import tn.amin.keyboard_gpt.language_model.LanguageModel;

public interface ConfigChangeListener {
    void onLanguageModelChange(LanguageModel model);

    void onApiKeyChange(LanguageModel languageModel, String apiKey);

    void onSubModelChange(LanguageModel languageModel, String subModel);

    void onBaseUrlChange(LanguageModel languageModel, String baseUrl);

    void onCommandsChange(String commandsRaw);
}
