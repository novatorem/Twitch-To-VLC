package com.novatorem.twitchtovlc.service;

import com.novatorem.twitchtovlc.misc.SecretKeys;

public class Services {
    public static String getApplicationClientID() {
        return SecretKeys.ApplicationID;
    }

    public static String getAnonTokenID() {
        return SecretKeys.AnonTokenID;
    }
}
