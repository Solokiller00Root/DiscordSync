package com.solo.discordsync.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

public class Server {

    private static ShardManager shardManager = DiscordBot.getShardManager();
    public static String guildId = Enviroment.get("discord.guildId");
    public static String general = Enviroment.get("discord.generalChannel");


}
