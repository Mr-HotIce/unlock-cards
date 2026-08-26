package com.mrhotice.unlockcards;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CardStore {
    private static CardStore instance;
    private final List<Card> all = new ArrayList<>();
    private final List<Card> verbs = new ArrayList<>();

    public static synchronized CardStore get(Context c) {
        if (instance == null) {
            instance = new CardStore(c.getApplicationContext());
        }
        return instance;
    }

    private CardStore(Context c) {
        try {
            InputStream in = c.getAssets().open("cards.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            in.close();
            JSONArray arr = new JSONArray(new String(out.toByteArray(), StandardCharsets.UTF_8));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                boolean verb = o.optInt("v", 0) == 1;
                Card card = new Card(
                        o.optString("w"),
                        o.optString("ru"),
                        o.optString("ipa"),
                        o.optString("tr"),
                        o.optString("pos"),
                        o.optString("lvl"),
                        verb,
                        o.optString("past"),
                        o.optString("kind")
                );
                all.add(card);
                if (verb) verbs.add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int size(boolean verbsOnly) {
        return (verbsOnly ? verbs : all).size();
    }

    public Card get(int index, boolean verbsOnly) {
        List<Card> src = verbsOnly ? verbs : all;
        if (src.isEmpty()) return null;
        int i = index % src.size();
        if (i < 0) i += src.size();
        return src.get(i);
    }
}
