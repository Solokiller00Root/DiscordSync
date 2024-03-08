package com.solo.discordsync.listeners;

import com.solo.discordsync.util.Database;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerJoinQuit implements Listener {
    private LuckPerms luckPerms;

    public PlayerJoinQuit(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        User user = luckPerms.getUserManager().getUser(player.getName());
        String primaryGroup = null;
        if (user != null) {
            primaryGroup = user.getPrimaryGroup();
        }

        if (!Database.findById(uuid.toString())) {
            Database.createMcProfileAsync(uuid.toString(), player.getName(), primaryGroup);
            System.out.println("Registered " + player.getName());
        } else {
            System.out.println("Player " + player.getName() + " is already registered!");
            Database.updateStatus(uuid.toString(), 1);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Database.updateStatus(uuid.toString(), 0);
    }
}
