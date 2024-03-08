package com.solo.discordsync.discord;

import com.solo.discordsync.User.UserCommand;
import com.solo.discordsync.User.UserListener;
import com.solo.discordsync.User.UserStats;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;


public class DiscordBot extends ListenerAdapter {
    private static ShardManager shardManager;
    public DiscordBot(Plugin plugin){
        String token = plugin.getConfig().getString("discord.token");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token)
                .setActivity(Activity.playing("Hello world"))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_INVITES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL);
        builder.setStatus(OnlineStatus.ONLINE);
        shardManager = builder.build();
        shardManager.addEventListener(new UserListener());
        shardManager.addEventListener(new UserCommand());
        shardManager.addEventListener(new UserStats());
        shardManager.addEventListener(this);
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("link", "Link mc account"));
        commandData.add(Commands.slash("stats", "dawda").addOption(OptionType.STRING, "mcname", "Minecraft name", true));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    public static ShardManager getShardManager() {
        return shardManager;
    }

    public static void shutdown() {
        shardManager.shutdown();
    }
}
