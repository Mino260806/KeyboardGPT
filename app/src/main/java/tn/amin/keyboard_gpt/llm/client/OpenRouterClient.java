package tn.amin.keyboard_gpt.llm.client;

public class OpenRouterClient extends ChatGPTClient {
    @Override
    public LanguageModel getLanguageModel() {
        return LanguageModel.OpenRouter;
    }
}
