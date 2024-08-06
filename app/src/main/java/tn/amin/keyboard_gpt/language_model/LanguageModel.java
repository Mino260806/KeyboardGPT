package tn.amin.keyboard_gpt.language_model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public enum LanguageModel {
    Gemini("Gemini", "gemini-1.5-flash", "Not configurable"),
    ChatGPT("ChatGPT", "gpt-4o-mini", "https://api.openai.com"),
    Groq("Groq", "llama3-8b-8192", "https://api.groq.com/openai"),
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
