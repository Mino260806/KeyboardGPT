package tn.amin.keyboard_gpt.llm;

public class OpenRouterClient extends ChatGPTClient {
    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.OpenRouter;
    }
}
