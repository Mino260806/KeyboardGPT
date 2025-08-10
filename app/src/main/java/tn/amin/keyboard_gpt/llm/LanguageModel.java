package tn.amin.keyboard_gpt.llm;

public enum LanguageModel {
    Gemini("Gemini", "gemini-2.5-flash-lite", "https://generativelanguage.googleapis.com/v1beta/openai"),
    ChatGPT("ChatGPT", "gpt-4o-mini", "https://api.openai.com/v1"),
    Groq("Groq", "llama3-8b-8192", "https://api.groq.com/openai/v1"),
    OpenRouter("OpenRouter", "meta-llama/llama-3.3-70b-instruct:free", "https://openrouter.ai/api/v1"),
    Claude("Claude", "claude-3-5-sonnet-20240620", "https://api.anthropic.com"),
//    HuggingChat("Hugging Chat"),
    ;

    public final String label;
    public final String defaultSubModel;
    public final String defaultBaseUrl;

    LanguageModel(String label, String defaultSubModel, String defaultBaseUrl) {
        this.label = label;
        this.defaultSubModel = defaultSubModel;
        this.defaultBaseUrl = defaultBaseUrl;
    }
}
