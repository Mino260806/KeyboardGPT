package tn.amin.keyboard_gpt.external;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import tn.amin.keyboard_gpt.instruction.command.Commands;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.llm.LanguageModel;
import tn.amin.keyboard_gpt.ui.UiInteractor;

public class ConfigContainer {
    public Bundle languageModelsConfig;

    public LanguageModel selectedModel;

    public ArrayList<GenerativeAICommand> commands;

    public int focusCommandIndex = -1;

    public void fillIntent(Intent intent) {
        if (selectedModel != null)
            intent.putExtra(UiInteractor.EXTRA_CONFIG_SELECTED_MODEL, selectedModel.name());
        if (languageModelsConfig != null)
            intent.putExtra(UiInteractor.EXTRA_CONFIG_LANGUAGE_MODEL, languageModelsConfig);
        if (commands != null)
            intent.putExtra(UiInteractor.EXTRA_COMMAND_LIST, Commands.encodeCommands(commands));
    }
}
