package tn.amin.keyboard_gpt;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.widget.EditText;

import tn.amin.keyboard_gpt.instruction.InstructionTreater;

public class KeyboardGPTBrain {
    private String mLastText = null;

    private InputMethodService mInputMethodService = null;
    private boolean mTreatingInstruction = false;

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

    public boolean performCommand(EditText editText) {
        mInteracter.setEditText(editText);
        String text = editText.getText().toString();

        return mInstructionTreater.treat(text);
    }

    public boolean isEditTextOwned() {
        return mInteracter.isEditTextOwned();
    }

    public UiInteracter getInteracter() {
        return mInteracter;
    }

    public void onInputMethodDestroy(InputMethodService inputMethodService) {
        mInputMethodService = null;
        getInteracter().unregisterService(inputMethodService);
    }

    public void onInputMethodCreate(InputMethodService inputMethodService) {
        mInputMethodService = inputMethodService;
        getInteracter().registerService(inputMethodService);
    }


}
