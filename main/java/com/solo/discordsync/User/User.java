package com.solo.discordsync.User;

public class User {

    private String discordUserId;
    private String privateKey;

    public User(String discordUserId, String privateKey) {
        this.discordUserId = discordUserId;
        this.privateKey = privateKey;
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
