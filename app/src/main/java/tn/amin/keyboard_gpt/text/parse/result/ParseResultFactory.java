package tn.amin.keyboard_gpt.text.parse.result;

import java.util.List;

public interface ParseResultFactory {
    ParseResult getParseResult(List<String> groups, int indexStart, int indexEnd);
}
