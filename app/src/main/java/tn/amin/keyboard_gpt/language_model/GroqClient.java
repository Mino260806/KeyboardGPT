package tn.amin.keyboard_gpt.language_model;

public class GroqClient extends ChatGPTClient {
    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Groq;
    }
}
