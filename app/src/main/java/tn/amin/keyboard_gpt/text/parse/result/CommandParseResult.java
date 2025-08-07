package tn.amin.keyboard_gpt.text.parse.result;

import java.util.List;

public class CommandParseResult extends ParseResult {
    public final String command;

    public final String prompt;

    protected CommandParseResult(List<String> groups, int indexStart, int indexEnd) {
        super(groups, indexStart, indexEnd);

        this.command = groups.size() >= 2 && groups.get(1) != null ? groups.get(1) : "";
        this.prompt = groups.size() >= 3 && groups.get(2) != null ? groups.get(2) : "";
    }
}
