package tn.amin.keyboard_gpt.text.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tn.amin.keyboard_gpt.text.parse.result.ParseResult;
import tn.amin.keyboard_gpt.text.parse.result.ParseResultFactory;

public class ParseDirective {
    private final Pattern pattern;

    private final ParseResultFactory factory;

    public ParseDirective(Pattern pattern, ParseResultFactory factory) {
        this.pattern = pattern;
        this.factory = factory;
    }

    public ParseResult parse(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            List<String> groups = new ArrayList<>();
            for (int i = 0; i < matcher.groupCount()+1; i++) {
                groups.add(matcher.group(i));
            }
            return factory.getParseResult(groups, matcher.start(), matcher.end());
        }
        return null;
    }
}
