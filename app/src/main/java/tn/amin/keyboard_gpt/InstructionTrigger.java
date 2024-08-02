package tn.amin.keyboard_gpt;

public enum InstructionTrigger {
    EditTextClear(true),
    LineBreak(false),
    DownUpKeyEvents(false),
    ;

    public final boolean providesEditText;

    InstructionTrigger(boolean providesEditText) {
        this.providesEditText = providesEditText;
    }
}
