package com.chineselingo.learning.common.dto;

public class CharacterCandidate {

    private final int charId;
    private final String character;
    private final String meaning;

    public CharacterCandidate(int charId, String character, String meaning) {
        this.charId = charId;
        this.character = character;
        this.meaning = meaning;
    }

    public int getCharId() {
        return charId;
    }

    public String getCharacter() {
        return character;
    }

    public String getMeaning() {
        return meaning;
    }
}