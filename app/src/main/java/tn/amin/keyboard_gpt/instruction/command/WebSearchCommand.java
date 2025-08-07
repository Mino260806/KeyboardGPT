package tn.amin.keyboard_gpt.instruction.command;

import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.UiInteractor;

public class WebSearchCommand extends AbstractCommand {
    @Override
    public String getCommandPrefix() {
        return "s";
    }
//    @Override
//    public void consume(String text, UiInteractor interacter, GenerativeAIController aiController) {
//        String url = "https://www.google.com/search?q=" + text;
//        interacter.showWebSearchDialog("Web Search", url);
//    }
}
