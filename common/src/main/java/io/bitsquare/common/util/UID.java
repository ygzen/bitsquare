package io.bitsquare.common.util;

import io.bitsquare.app.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UID {
    private static final Logger log = LoggerFactory.getLogger(UID.class);

    public static String getUUID() {
        return UUID.randomUUID().toString() + ":" + Version.getFullVersion();
    }

    public static String getOfferVersion(String uuid) {
        return uuid.length() == 38 ? uuid.substring(uuid.length() - 1) : "";
    }

    public static boolean uuidContainsOfferVersion(String uuid) {
        return getOfferVersion(uuid).equals(String.valueOf(Version.OFFER_VERSION));
    }
}
