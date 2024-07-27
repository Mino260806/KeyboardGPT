package tn.amin.keyboard_gpt.language_model;

import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public enum LanguageModel {
    Gemini("Gemini", "gemini-1.5-flash", "Not configurable"),
    ChatGPT("ChatGPT", "gpt-4o-mini", "https://api.openai.com"),
    Groq("Groq", "", "https://api.groq.com/openai"),
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

    public static Map<LanguageModel, String> decodeMap(String raw) {
        if (raw == null) {
            return Collections.emptyMap();
        }

        return Arrays.stream(raw.split("#####", -1))
                .collect(Collectors.toMap(
                        value -> LanguageModel.valueOf(value.split("~~~~~~~~~", -1)[0]),
                        value -> value.split("~~~~~~~~~", -1)[1]));
    }

    public static String encodeMap(Map<LanguageModel, String> map) {
        return map.keySet().stream().map(model -> model.name() + "~~~~~~~~~" + map.get(model))
                .collect(Collectors.joining("#####"));
    }
}
