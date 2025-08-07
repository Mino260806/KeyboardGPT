package tn.amin.keyboard_gpt.text.transform.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharacterTable {
    final static Pattern PATTERN_REPLACEMENT = Pattern.compile("([^ ]+) ([^ ]+)");

    private Map<String, String> mCharMap;

    public CharacterTable(Map<String, String> charMap) {
        mCharMap = charMap;
    }

    public static CharacterTable fromFile(File file) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            return new CharacterTable(null);
        }

        HashMap<String, String> charMap = new HashMap<>();
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            Matcher matcher = PATTERN_REPLACEMENT.matcher(line);
            if (matcher.find()) {
                if (matcher.groupCount() >= 2) {
                    String character = matcher.group(1);
                    String replacement = matcher.group(2);

                    charMap.put(character, replacement);
                }
            }
        }

        return new CharacterTable(charMap);
    }

    public String replace(char c) {
        if (mCharMap.containsKey(String.valueOf(c)))
            return mCharMap.get(String.valueOf(c));
        return String.valueOf(c);
    }
}
