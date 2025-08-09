package tn.amin.keyboard_gpt.instruction.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.ui.UiInteractor;
import tn.amin.keyboard_gpt.listener.ConfigChangeListener;
import tn.amin.keyboard_gpt.llm.client.LanguageModel;

public class CommandManager implements ConfigChangeListener {
    private final static Map<String, AbstractCommand> STATIC_COMMAND_MAP = Map.of(
            "s", new WebSearchCommand()
    );

    private Map<String, AbstractCommand> commandMap;

    public CommandManager() {
        UiInteractor.getInstance().registerConfigChangeListener(this);

        updateCommandMap();
    }

    private void updateCommandMap() {
        List<GenerativeAICommand> generativeAICommands = SPManager.getInstance().getGenerativeAICommands();

        commandMap = new HashMap<>(STATIC_COMMAND_MAP);
        for (GenerativeAICommand command: generativeAICommands) {
            commandMap.put(command.getCommandPrefix(), command);
        }
    }

    public AbstractCommand get(String prefix) {
        return commandMap.get(prefix);
    }

    @Override
    public void onLanguageModelChange(LanguageModel model) {

    }

    @Override
    public void onApiKeyChange(LanguageModel languageModel, String apiKey) {

    }

    @Override
    public void onSubModelChange(LanguageModel languageModel, String subModel) {

    }

    @Override
    public void onBaseUrlChange(LanguageModel languageModel, String baseUrl) {

    }

    @Override
    public void onCommandsChange(String commandsRaw) {
        updateCommandMap();
    }
}
