package tn.amin.keyboard_gpt.llm.service;

public interface InternetRequestListener {
    void onRequestStatusCode(int code);
    void onRequestComplete();
}
