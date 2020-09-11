package org.wontfix.velocitywhitelist;

public class WhitelistRecord {
    String name;
    String uuid;

    WhitelistRecord(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
}
