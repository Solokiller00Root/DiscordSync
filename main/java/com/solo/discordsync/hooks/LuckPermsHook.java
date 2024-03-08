package com.solo.discordsync.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class LuckPermsHook {
    private LuckPerms luckPermsAPI;

    public LuckPermsHook() {
        this.luckPermsAPI = LuckPermsProvider.get();
    }

    public LuckPerms getAPI() {
        return this.luckPermsAPI;
    }
}
