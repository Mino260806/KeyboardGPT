package tn.amin.keyboard_gpt.instruction.command;

import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.UiInteracter;

public class WebSearchCommand extends AbstractCommand {
    @Override
    public String getCommandPrefix() {
        return "s";
    }

    @Override
    public void consume(String text, UiInteracter interacter, GenerativeAIController aiController) {
        String url = "https://www.google.com/search?q=" + text;
        interacter.showWebSearchDialog("Web Search", url);
    }
}
