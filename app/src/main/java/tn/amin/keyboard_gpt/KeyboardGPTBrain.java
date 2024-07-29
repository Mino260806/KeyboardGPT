package tn.amin.keyboard_gpt;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.widget.EditText;

import tn.amin.keyboard_gpt.instruction.InstructionTreater;

public class KeyboardGPTBrain {
    private final SPManager mSPManager;
    private final UiInteracter mInteracter;
    private final GenerativeAIController mAIController;
    private final InstructionTreater mInstructionTreater;

    public KeyboardGPTBrain(Context context) {
        mSPManager = new SPManagerCompat(context);
        mInteracter = new UiInteracter(context, mSPManager);

        mAIController = new GenerativeAIController(mSPManager, mInteracter);
        mInstructionTreater = new InstructionTreater(mSPManager, mInteracter, mAIController);
    }

    public boolean consumeText(String text) {
        return mInstructionTreater.isInstruction(text) || isEditTextOwned();
    }

    public void setEditText(EditText editText) {
        mInteracter.setEditText(editText);
    }

    public boolean performCommand() {
        String text = mInteracter.getEditText().getText().toString();

        return mInstructionTreater.treat(text);
    }

    public boolean isEditTextOwned() {
        return mInteracter.isEditTextOwned();
    }

    public UiInteracter getInteracter() {
        return mInteracter;
    }

    public void onInputMethodDestroy(InputMethodService inputMethodService) {
        getInteracter().unregisterService(inputMethodService);
    }

    public void onInputMethodCreate(InputMethodService inputMethodService) {
        getInteracter().registerService(inputMethodService);
    }


}
