package tn.amin.keyboard_gpt.llm.client;

public class GeminiClient extends ChatGPTClient {
    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Gemini;
    }
}
