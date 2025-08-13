package tn.amin.keyboard_gpt.settings;

public enum OtherSettingsType {
    EnableLogs("Enable logging", Nature.Boolean, true),
    EnableExternalInternet("Use external internet service", Nature.Boolean, true);

    public final String title;
    public final Nature nature;
    public final Object defaultValue;

    OtherSettingsType(String title, Nature nature, Object defaultValue) {
        this.title = title;
        this.nature = nature;
        this.defaultValue = defaultValue;
    }

    public enum Nature {
        Boolean
    }
}
