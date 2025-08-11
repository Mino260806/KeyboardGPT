package tn.amin.keyboard_gpt.text.parse;

public enum PatternType {
    Settings("Settings", 0, "\\*#settings#\\*$", false),
    CommandAI("Default AI command", 1, "\\$([^$]*)\\$$", true),
    CommandCustom("Custom command", 2, "©(?:([^ ©]+) *)?([^©]+)?©$", true),
    FormatBold("Bold", 1, "¿([^¿]+)¿$", true),
    FormatItalic("Italic", 1, "¡([^¡]+)¡$", true),
    FormatCrossout("Crossout", 1, "~([^~]+)~$", true),
    FormatUnderline("Underline", 1, "ū([^~]+)ū$", true),
    ;

    public final String title;
    public final int groupCount;
    public final String defaultPattern;
    public final boolean editable;

    PatternType(String title, int groupCount, String defaultPattern, boolean editable) {
        this.title = title;
        this.groupCount = groupCount;
        this.defaultPattern = defaultPattern;
        this.editable = editable;
    }
}
