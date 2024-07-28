package tn.amin.keyboard_gpt.instruction.command;

import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.TextTreater;

public class CommandTreater implements TextTreater {
    private static final AbstractCommand[] COMMANDS = {
            new WebSearchCommand()
    };

    private final SPManager mSPManager;
    private final UiInteracter mInteracter;

    public CommandTreater(SPManager spManager, UiInteracter interacter) {
        mSPManager = spManager;
        mInteracter = interacter;
    }

    @Override
    public boolean treat(String text) {
        if (!text.startsWith("?")) {
            return false;
        }

        text = text.substring(1).trim();
        for (AbstractCommand command: COMMANDS) {
            if (text.startsWith(command.getCommandPrefix())) {
                text = text.substring(command.getCommandPrefix().length()).trim();
                command.consume(text, mInteracter);
                return true;
            }
        }

        return false;
    }
}
