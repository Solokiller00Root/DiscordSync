    package com.solo.discordsync.User;

    import com.solo.discordsync.util.Database;
    import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
    import net.dv8tion.jda.api.exceptions.ErrorResponseException;
    import net.dv8tion.jda.api.hooks.ListenerAdapter;


    public class UserCommand extends ListenerAdapter {

        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            String commandName = event.getName();
            if (!commandName.equals("link")) {
                return;
            }
            String discordUserId = event.getUser().getId();
            String discordUserName = event.getUser().getName();
            String privateKey = Database.getSyncCodeByDiscordId(discordUserId);

            if (privateKey == null) {
                event.reply("Error: Could not generate your private key. Please try again later.").setEphemeral(true).queue();
                return;
            }

            String message = String.format("Hello, %s! This is your private key: /link %s", discordUserName, privateKey);

            try {
                event.getUser().openPrivateChannel().queue(channel -> {
                            channel.sendMessage(message).queue(
                                    success -> event.reply("Code sent.").setEphemeral(true).queue()
                            );
                        }
                );
            } catch (ErrorResponseException e) {
                if (e.getErrorCode() == 50007) {
                    event.reply("Error: Cannot send messages to you. Please make sure you have allowed direct messages from server members.").setEphemeral(true).queue();
                }
            }
        }
    }
