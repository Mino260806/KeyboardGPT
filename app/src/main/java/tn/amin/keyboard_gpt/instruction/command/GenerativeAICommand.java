package tn.amin.keyboard_gpt.instruction.command;

import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.UiInteracter;

abstract class GenerativeAICommand extends AbstractCommand {
    abstract protected String getTweakMessage();

    @Override
    public void consume(String text, UiInteracter interacter, GenerativeAIController aiController) {
        new Thread(() -> aiController.generateResponse(text, getTweakMessage())).start();
    }
}
