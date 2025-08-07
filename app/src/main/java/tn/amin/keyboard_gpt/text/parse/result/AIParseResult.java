package tn.amin.keyboard_gpt.text.parse.result;

import java.util.List;

public class AIParseResult extends ParseResult {
    public final String prompt;

    protected AIParseResult(List<String> groups, int indexStart, int indexEnd) {
        super(groups, indexStart, indexEnd);

        this.prompt = groups.get(1);
    }
}
