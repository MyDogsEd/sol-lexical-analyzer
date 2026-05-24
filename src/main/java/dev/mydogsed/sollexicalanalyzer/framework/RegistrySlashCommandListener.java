package dev.mydogsed.sollexicalanalyzer.framework;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

/*
Class that listens for slash command events and executes the related onCommand method via the registry.
 */
public class RegistrySlashCommandListener extends ListenerAdapter {

    /*
    When a slash command is received, check the registry. If the command is present, execute it.
    If the command is not present, log a warning and tell the user.
    */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        // get the name of the command
        String command = event.getName();

        // if the executor for the command is not found, log and error to user
        if (!CommandRegistry.getInstance().containsExecutor(command)){
            LoggerFactory.getLogger(RegistrySlashCommandListener.class)
                    .warn("Command executor for '{}' is not registered!", command);
            event
                    .reply("An error occurred with that command. Please try again later.")
                    .addContent("(internal error: executor not registered)")
                    .setEphemeral(true)
                    .queue();
        }
        // Execute the command.
        CommandRegistry.getInstance().getExecutor(command).onCommand(event);
    }
}
