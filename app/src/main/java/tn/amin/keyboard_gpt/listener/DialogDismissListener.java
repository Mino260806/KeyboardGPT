package tn.amin.keyboard_gpt.listener;

public interface DialogDismissListener {
    void onDismiss(boolean isPrompt, boolean isCommand, boolean isPattern);
}
