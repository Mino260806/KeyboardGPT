package tn.amin.keyboard_gpt;

import java.util.regex.Pattern;

public class CommandTreater {
    private static final Pattern CONFIGURE_REGEX = Pattern.compile("^\\s*\\?\\s*\\?\\s*$");

    public boolean isPrompt(String text) {
        return text != null && text.startsWith("?");
    }

    public boolean isConfigureCommand(String text) {
        return text != null && CONFIGURE_REGEX.matcher(text).matches();
    }
}
