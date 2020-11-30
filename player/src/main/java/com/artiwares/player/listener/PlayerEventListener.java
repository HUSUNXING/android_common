package com.artiwares.player.listener;

public interface PlayerEventListener {

    void onError(String message);

    void onCompletion();

    void onInfo(int what, int extra);

    void onPrepared();

}