package tn.amin.keyboard_gpt.ui;

import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.List;

import tn.amin.keyboard_gpt.listener.InputEventListener;

public class IMSController {
    private InputMethodService ims = null;
    private String typedText = "";
    private int cursor = 0;
    private boolean inputNotify = false;
    private boolean inputLock = false;

    private List<InputEventListener> mListeners = new ArrayList<>();


    public IMSController() {
    }

    public static IMSController getInstance() {
        return UiInteractor.getInstance().getIMSController();
    }

    public void onUpdateSelection(int oldSelStart,
                                  int oldSelEnd,
                                  int newSelStart,
                                  int newSelEnd,
                                  int candidatesStart,
                                  int candidatesEnd) {
        if (inputNotify) {
            return;
        }
        InputConnection ic = ims.getCurrentInputConnection();
        if (ic != null) {
            ExtractedText extractedText = ic.getExtractedText(new ExtractedTextRequest(), 0);
            if (extractedText != null && extractedText.text != null) {
                typedText = extractedText.text.toString();
                cursor = newSelEnd;
                notifyTextUpdate();
            }
        }
    }

    public void addListener(InputEventListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(InputEventListener listener) {
        mListeners.remove(listener);
    }

    private void notifyTextUpdate() {
        for (InputEventListener listener: mListeners) {
            listener.onTextUpdate(typedText, cursor);
        }
    }

    public void registerService(InputMethodService ims) {
        this.ims = ims;
    }

    public void unregisterService(InputMethodService ims) {
        this.ims = null;
    }

    public void delete(int count) {
        InputConnection ic = ims.getCurrentInputConnection();
        if (ic != null) {
            ic.deleteSurroundingText(count, 0);
        }
    }

    public void commit(String text) {
        InputConnection ic = ims.getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(text, 1);
        }
    }

    public void stopNotifyInput() {
        inputNotify = true;
    }

    public void startNotifyInput() {
        inputNotify = false;
    }

    public void flush() {
        InputConnection ic = ims.getCurrentInputConnection();
        if (ic != null) {
            ic.finishComposingText();
        }
    }

    public boolean isInputLocked() {
        return inputLock;
    }

    public void startInputLock() {
        inputLock = true;
    }

    public void endInputLock() {
        inputLock = false;
    }
}
