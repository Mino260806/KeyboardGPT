package tn.amin.keyboard_gpt.text.transform.format;

public interface CharacterFilter {

    CharacterFilter noCharacterFilter = c -> true;

    boolean filterCharacter(char c);
}