package com.solo.discordsync.User;

import com.solo.discordsync.discord.Server;
import com.solo.discordsync.util.Database;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class UserListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e){
        if(!e.getGuild().getId().equals(Server.guildId)) return;

        String discordUserId = e.getUser().getId().toString();
        String discordUserName = e.getUser().getName();


        if (!Database.findDiscordUserById(discordUserId)) {
            Database.createDiscordUserAsync(discordUserId, discordUserName);
        }
        TextChannel generalChannel = e.getGuild().getTextChannelById(Server.general);
        generalChannel.sendMessage("Welcome to the server, " + discordUserName + "!").queue(null, throwable -> {
            System.out.println("Failed to send message: " + throwable.getMessage());
        });
    }

}
