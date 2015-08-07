package com.example.manuelsanchez.spotifystreamer.util;

import java.util.concurrent.TimeUnit;

/**
 * Created by Manuel Sanchez on 8/6/15
 */
public class TimeStringHelper {

    public static String getFormatedString(int seconds) {
        long min = TimeUnit.SECONDS.toMinutes(seconds);
        long sec = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toSeconds(min);
        return String.format("%d:%02d", min, sec);
    }
}
