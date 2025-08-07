package tn.amin.keyboard_gpt.instruction.command;

import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.UiInteractor;

public abstract class GenerativeAICommand extends AbstractCommand {
    abstract public String getTweakMessage();

    @Override
    public void consume(String text, UiInteractor interacter, GenerativeAIController aiController) {
        if (aiController.needModelClient()) {
            if (interacter.showChoseModelDialog()) {
                interacter.toastLong("Chose and configure your language model");
            }
            return;
        }

        if (aiController.needApiKey()) {
            if (interacter.showChoseModelDialog()) {
                interacter.toastLong(aiController.getLanguageModel().label + " is Missing API Key");
            }
            return;
        }


        new Thread(() -> aiController.generateResponse(text, getTweakMessage())).start();
    }
}
