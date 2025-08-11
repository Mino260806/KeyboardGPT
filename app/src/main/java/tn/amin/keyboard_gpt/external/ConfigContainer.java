package tn.amin.keyboard_gpt.external;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import tn.amin.keyboard_gpt.instruction.command.Commands;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.text.parse.ParsePattern;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class ConfigContainer {
    public Bundle languageModelsConfig;

    public LanguageModel selectedModel;

    public List<GenerativeAICommand> commands;
    public List<ParsePattern> patterns;

    public int focusCommandIndex = -1;

    public int focusPatternIndex = -1;

    public void fillIntent(Intent intent) {
        if (selectedModel != null)
            intent.putExtra(UiInteractor.EXTRA_CONFIG_SELECTED_MODEL, selectedModel.name());
        if (languageModelsConfig != null)
            intent.putExtra(UiInteractor.EXTRA_CONFIG_LANGUAGE_MODEL, languageModelsConfig);
        if (commands != null)
            intent.putExtra(UiInteractor.EXTRA_COMMAND_LIST, Commands.encodeCommands(commands));
    }
}
