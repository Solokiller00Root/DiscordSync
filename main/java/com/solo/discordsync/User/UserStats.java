package com.solo.discordsync.User;

import com.solo.discordsync.util.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserStats extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        String commandName = event.getName();
        if (!commandName.equals("stats")) {
            return;
        }
        if (commandName.equals("stats")) {
            if (event.getOption("mcname") == null) {
                event.reply("Error: Minecraft name not provided.").setEphemeral(true).queue();
                return;
            }

            String minecraftName = event.getOption("mcname").getAsString();
            String minecraftUUID = Database.getMinecraftUUIDByMinecraftName(minecraftName);
            if (minecraftUUID == null) {
                event.reply("Error: Minecraft account not found.").setEphemeral(true).queue();
                return;
            }

            String rank = Database.getRankByMinecraftUUID(minecraftUUID);
            int onlineStatus = Database.getOnlineStatusByMinecraftUUID(minecraftUUID);
            String discordId = Database.getDiscordIdByMinecraftUUID(minecraftUUID);
            String linked = discordId != null ? "<@" + discordId + ">" : "not linked";
            String minecraftAvatarUrl = "https://minotar.net/helm/" + minecraftName + ".png";

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(0x1F8B4C);
            embedBuilder.setThumbnail(minecraftAvatarUrl);
            embedBuilder.setTitle("Player Stats for " + minecraftName);
            embedBuilder.addField("Minecraft Name", "```\n" + minecraftName + "\n```", false);
            embedBuilder.addField("Rank", "```\n" + rank + "\n```", false);
            embedBuilder.addField("Online Status", "```\n" + (onlineStatus == 1 ? "Online" : "Offline") + "\n```", false);
            embedBuilder.addField("Linked", linked, false);
            embedBuilder.setFooter(responseTime + "ms");
            event.replyEmbeds(embedBuilder.build()).queue();
        } else {
            event.reply("Error: Invalid command.").setEphemeral(true).queue();
        }
    }
}