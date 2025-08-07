package tn.amin.keyboard_gpt.text.parse.result;

import java.util.List;

public abstract class ParseResult {
    public final int indexStart;
    public final int indexEnd;

    public final List<String> groups;

    protected ParseResult(List<String> groups, int indexStart, int indexEnd) {
        this.groups = groups;
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
    }
}
