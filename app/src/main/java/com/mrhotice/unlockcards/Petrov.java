package com.mrhotice.unlockcards;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Petrov {
    private Petrov() {}

    public static class Row {
        public final String label;
        public final String q;
        public final String a;
        public final String n;

        Row(String label, String q, String a, String n) {
            this.label = label;
            this.q = q;
            this.a = a;
            this.n = n;
        }
    }

    public static List<Row> table(Card c) {
        String v = c.word;
        String tr = empty(c.transcription) ? v : c.transcription;
        String low = v.toLowerCase(Locale.US);
        List<Row> rows = new ArrayList<>();
        if ("be".equals(low)) {
            rows.add(new Row("will",
                    "Will I be?\nWill he be?",
                    "I will be\nhe will be",
                    "I will not be\nI won't be"));
            rows.add(new Row("am / is",
                    "Am I?\nIs he?",
                    "I am (I'm)\nhe is (he's)",
                    "I am not\nhe isn't"));
            rows.add(new Row("was / were",
                    "Was I?\nWas he?",
                    "I was\nhe was",
                    "I was not\nhe wasn't"));
            return rows;
        }
        if (isModal(low)) {
            return modal(low);
        }
        String past = empty(c.past) ? v + "ed" : c.past;
        String third = third(v);
        rows.add(new Row("will",
                "Will I " + v + "?\nWill he " + v + "?",
                "I will " + v + "\nhe will " + v,
                "I will not " + v + "\nI won't " + v));
        rows.add(new Row("do / does",
                "Do I " + v + "?\nDoes he " + v + "?",
                "I " + v + "\nhe " + third,
                "I don't " + v + "\nhe doesn't " + v));
        rows.add(new Row("did",
                "Did I " + v + "?\nDid he " + v + "?",
                "I " + past + "\nhe " + past,
                "I didn't " + v + "\nhe didn't " + v));
        return rows;
    }

    private static boolean isModal(String w) {
        return "can".equals(w) || "could".equals(w) || "may".equals(w) || "might".equals(w)
                || "must".equals(w) || "shall".equals(w) || "should".equals(w)
                || "will".equals(w) || "would".equals(w) || "have to".equals(w)
                || "used to".equals(w);
    }

    private static List<Row> modal(String w) {
        List<Row> rows = new ArrayList<>();
        if ("have to".equals(w)) {
            rows.add(new Row("will", "Will I have to?", "I will have to", "I will not have to"));
            rows.add(new Row("do / does", "Do I have to?\nDoes he have to?", "I have to\nhe has to", "I don't have to"));
            rows.add(new Row("did", "Did I have to?", "I had to", "I didn't have to"));
            return rows;
        }
        if ("used to".equals(w)) {
            rows.add(new Row("past", "Did I use to?", "I used to", "I didn't use to"));
            return rows;
        }
        String cap = w.substring(0, 1).toUpperCase(Locale.US) + w.substring(1);
        rows.add(new Row("present", cap + " I?\n" + cap + " he?", "I " + w + "\nhe " + w, "I " + w + " not"));
        return rows;
    }

    private static String third(String verb) {
        String low = verb.toLowerCase(Locale.US);
        if ("have".equals(low)) return "has";
        if ("do".equals(low)) return "does";
        if ("go".equals(low)) return "goes";
        if ("say".equals(low)) return "says";
        if (low.endsWith("s") || low.endsWith("x") || low.endsWith("z")
                || low.endsWith("ch") || low.endsWith("sh") || low.endsWith("o")) {
            return verb + "es";
        }
        if (low.length() > 1 && low.endsWith("y") && "aeiou".indexOf(low.charAt(low.length() - 2)) < 0) {
            return verb.substring(0, verb.length() - 1) + "ies";
        }
        return verb + "s";
    }

    private static boolean empty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
