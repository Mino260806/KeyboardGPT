package tn.amin.keyboard_gpt.text.parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import tn.amin.keyboard_gpt.MainHook;

public class ParsePattern {
    private final PatternType mType;
    private final Pattern mPattern;
    private Map<String, String> mExtras = null;

    public ParsePattern(PatternType type, String patternRe) {
        this(type, patternRe, null);
    }

    public ParsePattern(PatternType type, String patternRe, Map<String, String> extras) {
        mType = type;
        mPattern = Pattern.compile(patternRe);
        mExtras = extras;
    }

    public void putExtra(String key, String value) {
        if (mExtras == null) {
            mExtras = new HashMap<>();
        }
        mExtras.put(key, value);
    }

    public String getExtra(String key) {
        return mExtras == null ? null : mExtras.get(key);
    }

    public Map<String, String> getExtras() {
        return mExtras;
    }

    public PatternType getType() {
        return mType;
    }

    public Pattern getPattern() {
        return mPattern;
    }

    public static String encode(List<ParsePattern> patterns) {
        JSONArray patternsJson = new JSONArray();
        for (ParsePattern parsePattern: patterns) {
            JSONObject patternJson = new JSONObject();
            try {
                patternJson.put("name", parsePattern.getType().name());
                patternJson.put("pattern", parsePattern.getPattern().pattern());
                Map<String, String> extras = parsePattern.getExtras();
                if (extras != null) {
                    patternJson.put("extras", new JSONObject(extras));
                }
            } catch (JSONException e) {
                MainHook.log(e);
            }
            patternsJson.put(patternJson);
        }
        return patternsJson.toString();
    }

    public static List<ParsePattern> decode(String encodedPatterns) {
        if (encodedPatterns == null) {
            return getDefaultList();
        }

        List<ParsePattern> patterns = new ArrayList<>();
        try {
            JSONArray patternsJson = new JSONArray(encodedPatterns);
            for (int i = 0; i < patternsJson.length(); i++) {
                JSONObject patternJson = patternsJson.getJSONObject(i);

                String name = patternJson.getString("name");
                String patternStr = patternJson.getString("pattern");

                Map<String, String> extras = null;
                if (patternJson.has("extras")) {
                    JSONObject extrasJson = patternJson.getJSONObject("extras");
                    extras = new HashMap<>();
                    for (Iterator<String> it = extrasJson.keys(); it.hasNext(); ) {
                        String key = it.next();
                        extras.put(key, extrasJson.getString(key));
                    }
                }

                PatternType type = PatternType.valueOf(name);
                ParsePattern parsePattern = new ParsePattern(type, patternStr, extras);

                patterns.add(parsePattern);
            }
        } catch (JSONException e) {
            MainHook.log(e);
        }
        return patterns;
    }

    private static List<ParsePattern> getDefaultList() {
        List<ParsePattern> patterns = new ArrayList<>();
        for (PatternType type: PatternType.values()) {
            patterns.add(new ParsePattern(type, type.defaultPattern));
        }
        return patterns;
    }
}
