package tn.amin.keyboard_gpt.llm;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum LanguageModel {
    Gemini("Gemini", "gemini-2.5-flash-lite", "https://generativelanguage.googleapis.com/v1beta"),
    ChatGPT("ChatGPT", "gpt-4o-mini", "https://api.openai.com/v1"),
    Groq("Groq", "llama3-8b-8192", "https://api.groq.com/openai/v1"),
    OpenRouter("OpenRouter", "openai/gpt-oss-20b:free", "https://openrouter.ai/api/v1"),
    Claude("Claude", "claude-3-5-sonnet-20240620", "https://api.anthropic.com/v1"),
    Mistral("Mistral", "mistral-small-latest", "https://api.mistral.ai/v1"),
    ;

    public final String label;

    public final Map<LanguageModelField, String> defaults;

    LanguageModel(String label, String defaultSubModel, String defaultBaseUrl) {
        this.label = label;

        defaults = ImmutableMap.of(
                LanguageModelField.SubModel, defaultSubModel,
                LanguageModelField.BaseUrl, defaultBaseUrl,
                LanguageModelField.MaxTokens, "4096",
                LanguageModelField.Temperature, "1.0",
                LanguageModelField.TopP, "1.0"
        );
    }

    public String getDefault(LanguageModelField field) {
        return defaults.getOrDefault(field, null);
    }
}
