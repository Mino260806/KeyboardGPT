package tn.amin.keyboard_gpt.listener;

import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.llm.LanguageModelField;

public interface ConfigChangeListener {
    void onLanguageModelChange(LanguageModel model);

    void onLanguageModelFieldChange(LanguageModel model, LanguageModelField field, String value);

    void onCommandsChange(String commandsRaw);
}
