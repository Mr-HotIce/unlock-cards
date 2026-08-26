package com.mrhotice.unlockcards;

public class Card {
    public final String word;
    public final String russian;
    public final String ipa;
    public final String transcription;
    public final String pos;
    public final String level;
    public final boolean verb;
    public final String past;
    public final String kind;

    public Card(String word, String russian, String ipa, String transcription,
                String pos, String level, boolean verb, String past, String kind) {
        this.word = word;
        this.russian = russian;
        this.ipa = ipa;
        this.transcription = transcription;
        this.pos = pos;
        this.level = level;
        this.verb = verb;
        this.past = past;
        this.kind = kind;
    }
}
