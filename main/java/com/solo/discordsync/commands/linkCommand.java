package com.solo.discordsync.commands;

import com.solo.discordsync.discord.DiscordBot;
import com.solo.discordsync.discord.Server;
import com.solo.discordsync.util.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Map;

public class linkCommand implements CommandExecutor {
    private final JDA jda;
    private final Map<String, String> rankToRoleId;

    public linkCommand(JDA jda, Map<String, String> rankToRoleId) {
        this.jda = jda;
        this.rankToRoleId = rankToRoleId;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage("Usage: /link <private_key>");
            return true;
        }

        String privateKey = args[0];
        if (!Database.isValidSyncCode(privateKey)) {
            player.sendMessage("Invalid private key format.");
            return true;
        }

        if (Database.isAccountLinked(privateKey)) {
            player.sendMessage("This account is already linked.");
            return true;
        }

        String discordUserId = Database.getDiscordIdBySyncCode(privateKey);

        if (discordUserId == null) {
            player.sendMessage("Invalid private key.");
            return true;
        }

        String playerUUID = player.getUniqueId().toString();

        try {
            Database.linkMinecraftToDiscord(playerUUID, discordUserId);
            player.sendMessage("Your Minecraft account has been linked with your Discord account.");

            String rank = Database.getRankByUUID(playerUUID);

            String roleId = rankToRoleId.get(rank.toLowerCase());

            if (roleId == null) {
                player.sendMessage("No role associated with your rank.");
                return true;
            }

            Role role = DiscordBot.getShardManager().getRoleById(roleId);
            String guildId = Server.guildId;
            Guild guild = DiscordBot.getShardManager().getGuildById(guildId);

            Member member = guild.getMemberById(discordUserId);

            if (member != null) {
                guild.addRoleToMember(member, role).queue();
            } else {
                player.sendMessage("The Discord user is not a member of the guild.");
            }

        } catch (Exception e) {
            player.sendMessage("An error occurred while linking accounts. Please try again later.");
        }

        return true;
    }
}