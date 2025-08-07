package tn.amin.keyboard_gpt.llm;

public class GroqClient extends ChatGPTClient {
    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.Groq;
    }
}
