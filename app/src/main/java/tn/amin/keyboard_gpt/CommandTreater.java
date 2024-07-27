package tn.amin.keyboard_gpt;

import java.util.List;
import java.util.regex.Pattern;

import tn.amin.keyboard_gpt.command.AbstractCommand;
import tn.amin.keyboard_gpt.command.WebSearchCommand;

public class CommandTreater {
    private static final Pattern CONFIGURE_REGEX = Pattern.compile("^\\s*\\?\\s*\\?\\s*$");

    private static final AbstractCommand[] COMMANDS = {
            new WebSearchCommand()
    };

    public boolean isPrompt(String text) {
        return text != null && text.startsWith("?");
    }

    public boolean isConfigureCommand(String text) {
        return text != null && CONFIGURE_REGEX.matcher(text).matches();
    }

    public boolean consumeIfCommand(String text, UiInteracter interacter) {
        if (!text.startsWith("?")) {
            return false;
        }

        text = text.substring(1).trim();
        for (AbstractCommand command: COMMANDS) {
            if (text.startsWith(command.getCommandPrefix())) {
                text = text.substring(command.getCommandPrefix().length()).trim();
                command.consume(text, interacter);
                return true;
            }
        }

        return false;
    }
}
