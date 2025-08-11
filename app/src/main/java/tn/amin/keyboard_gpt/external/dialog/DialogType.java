package tn.amin.keyboard_gpt.external.dialog;

public enum DialogType {
    ChoseModel("Choose Model", true),
    ConfigureModel("Configure Model", false),
    WebSearch("Web Search", false),
    EditCommandsList("Commands List", true),
    EditCommand("Edit Command", false),
    EditPatternList("Patterns List", true),
    Settings("Settings", false);

    public final String title;
    public final boolean inSettings;

    DialogType(String title, boolean inSettings) {
        this.title = title;
        this.inSettings = inSettings;
    }
}
