package tn.amin.keyboard_gpt.instruction.command;

import java.util.ArrayList;
import java.util.List;

import tn.amin.keyboard_gpt.ConfigChangeListener;
import tn.amin.keyboard_gpt.DialogDismissListener;
import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.InstructionCategory;
import tn.amin.keyboard_gpt.instruction.TextTreater;
import tn.amin.keyboard_gpt.language_model.LanguageModel;

public class CommandTreater implements TextTreater, ConfigChangeListener, DialogDismissListener {
    private static final List<AbstractCommand> BUILTIN_COMMANDS = List.of(
            new WebSearchCommand()
            );

    private final SPManager mSPManager;
    private final UiInteracter mInteracter;
    private final GenerativeAIController mAIController;

    private final ArrayList<AbstractCommand> mCommands = new ArrayList<>();

    public CommandTreater(SPManager spManager, UiInteracter interacter, GenerativeAIController aiController) {
        mSPManager = spManager;
        mInteracter = interacter;
        mAIController = aiController;

        mInteracter.registerConfigChangeListener(this);
        mInteracter.registerOnDismissListener(this);

        mCommands.addAll(BUILTIN_COMMANDS);
        mCommands.addAll(mSPManager.getGenerativeAICommands());
    }

    @Override
    public boolean treat(String text) {
        if (text.startsWith(InstructionCategory.Command.prefix)) {
            mInteracter.showEditCommandsDialog(mSPManager.getGenerativeAICommandsRaw());
            return false;
        }

        for (AbstractCommand command: mCommands) {
            if (text.startsWith(command.getCommandPrefix())
                    && (text.length() == command.getCommandPrefix().length()
                        || !Character.isLetterOrDigit(text.charAt(command.getCommandPrefix().length())))) {
                text = text.substring(command.getCommandPrefix().length()).trim();
                command.consume(text, mInteracter, mAIController);
                return false;
            }
        }

        return false;
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
        ArrayList<GenerativeAICommand> commands = Commands.decodeCommands(commandsRaw);
        if (mCommands.size() > BUILTIN_COMMANDS.size()) {
            mCommands.subList(BUILTIN_COMMANDS.size(), mCommands.size()).clear();
        }
        mCommands.addAll(commands);
    }

    @Override
    public void onDismiss(boolean isPrompt, boolean isCommand) {
        if (!isCommand) {
            return;
        }

        mInteracter.post(() -> {
            mInteracter.toastShort("New Commands Saved");
        });
    }
}
