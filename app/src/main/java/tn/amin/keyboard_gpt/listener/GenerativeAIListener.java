package tn.amin.keyboard_gpt.listener;

public interface GenerativeAIListener {
    void onAIPrepare();
    void onAINext(String chunk);
    void onAIError(Throwable t);
    void onAIComplete();
}
