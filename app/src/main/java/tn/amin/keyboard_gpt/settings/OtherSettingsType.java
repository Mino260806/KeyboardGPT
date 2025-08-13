package tn.amin.keyboard_gpt.settings;

public enum OtherSettingsType {
    EnableLogs("Enable logging", "Disable for performance. You won't be able to report errors.",
            Nature.Boolean, true),
    EnableExternalInternet("Use external internet service", "Recommended to keep on unless chat completion is not working.",
            Nature.Boolean, true);

    public final String title;
    public final String description;
    public final Nature nature;
    public final Object defaultValue;

    OtherSettingsType(String title, String description, Nature nature, Object defaultValue) {
        this.title = title;
        this.description = description;
        this.nature = nature;
        this.defaultValue = defaultValue;
    }

    public enum Nature {
        Boolean
    }
}
