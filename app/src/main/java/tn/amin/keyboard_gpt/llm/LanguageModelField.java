package tn.amin.keyboard_gpt.llm;

public enum LanguageModelField {
    ApiKey("api_key", "Api Key", Type.String, false),
    SubModel("sub_model", "Sub Model", Type.String, false),
    BaseUrl("base_url", "Base Url", Type.String, false);

    public final String name;
    public final String title;
    public final Type type;
    public final boolean advanced;

    LanguageModelField(String name, String title, Type type, boolean advanced) {
        this.name = name;
        this.title = title;
        this.type = type;
        this.advanced = advanced;
    }

    public enum Type {
        String,
        Double
    }
}
