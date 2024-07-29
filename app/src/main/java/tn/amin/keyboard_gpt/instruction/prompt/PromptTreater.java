package tn.amin.keyboard_gpt.instruction.prompt;

import tn.amin.keyboard_gpt.DialogDismissListener;
import tn.amin.keyboard_gpt.GenerativeAIController;
import tn.amin.keyboard_gpt.MainHook;
import tn.amin.keyboard_gpt.SPManager;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.TextTreater;

public class PromptTreater implements TextTreater, DialogDismissListener {
    private final SPManager mSPManager;
    private final UiInteracter mInteracter;
    private final GenerativeAIController mAIController;

    public PromptTreater(SPManager spManager, UiInteracter interacter, GenerativeAIController aiController) {
        mSPManager = spManager;
        mInteracter = interacter;
        mAIController = aiController;

        mInteracter.registerOnDismissListener(this);
    }

    @Override
    public boolean treat(String instruction) {
        if (instruction.startsWith("?")) {
            if (mInteracter.showChoseModelDialog()) {
                mInteracter.toastLong("Chose and configure your language model");
            }
            return false;
        }

        if (mAIController.needModelClient()) {
            if (mInteracter.showChoseModelDialog()) {
                mInteracter.toastLong("Chose and configure your language model");
            }
            return true;
        }

        if (mAIController.needApiKey()) {
            if (mInteracter.showChoseModelDialog()) {
                mInteracter.toastLong(mAIController.getLanguageModel().label + " is Missing API Key");
            }
            return true;
        }

        new Thread(() -> mAIController.generateResponse(instruction)).start();

        return false;
    }

    @Override
    public void onDismiss(boolean isPrompt, boolean isCommand) {
        if (!isPrompt) {
            return;
        }

        MainHook.log("Selected " + mAIController.getLanguageModel());
        mInteracter.post(() -> {
            mInteracter.toastShort("Selected " + mAIController.getLanguageModel());
        });
    }
}
