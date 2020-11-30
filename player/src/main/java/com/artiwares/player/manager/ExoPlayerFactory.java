package com.artiwares.player.manager;

import android.content.Context;


import com.artiwares.player.SimplePlayer;

import java.util.HashMap;

public class ExoPlayerFactory {

    private static HashMap<Context, SimplePlayer> hashMap = new HashMap<>();

    public static ExoPlayerFactory create() {
        return new ExoPlayerFactory();
    }

    public SimplePlayer createPlayer(Context context) {
        SimplePlayer simplePlayer = new SimplePlayer(context);
        hashMap.put(context, simplePlayer);
        return hashMap.get(context);
    }

    public void releaseAll() {
        if (hashMap.size() > 0) {
            for (Context context : hashMap.keySet()) {
                SimplePlayer simplePlayer = hashMap.get(context);
                if (simplePlayer != null) {
                    simplePlayer.release();
                }
            }
            hashMap.clear();
        }
    }

}
