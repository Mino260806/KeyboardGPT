package tn.amin.keyboard_gpt.llm.client;

import tn.amin.keyboard_gpt.llm.LanguageModel;

public class GeminiClient extends ChatGPTClient {
    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Gemini;
    }
}
