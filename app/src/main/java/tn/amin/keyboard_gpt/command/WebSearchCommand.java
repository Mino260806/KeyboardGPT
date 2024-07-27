package tn.amin.keyboard_gpt.command;

import tn.amin.keyboard_gpt.UiInteracter;

public class WebSearchCommand extends AbstractCommand {
    @Override
    public String getCommandPrefix() {
        return "s";
    }

    @Override
    public void consume(String text, UiInteracter interacter) {
        String url = "https://www.google.com/search?q=" + text;
        interacter.showWebSearchDialog("Web Search", url);
    }
}
