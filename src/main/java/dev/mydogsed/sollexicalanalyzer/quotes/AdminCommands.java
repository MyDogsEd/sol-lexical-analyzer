package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.QuotesDB;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static dev.mydogsed.sollexicalanalyzer.Main.registerSlashCommandsToDiscord;

public class AdminCommands implements SlashCommand {

    public static final Set<Runnable> shutdownRunnables = new HashSet<>();

    @Override
    public SlashCommandData getData() {
        return Commands.slash("admin", "misc admin commands")
                .addSubcommands(
                        new SubcommandData("sync", "sync #quotes-without-context to the db"),
                        new SubcommandData("shutdown", "shut down the bot"),
                        new SubcommandData("registerSlashCommands", "register slash commands for all guilds")
                );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        switch (event.getSubcommandName()) {
            case "sync" -> syncCommand(event);
            case "shutdown" -> shutdownCommand(event);
            case "registerSlashCommands" -> registerSlashCommands(event);
        }
    }

    private void registerSlashCommands(SlashCommandInteractionEvent event) {
        registerSlashCommandsToDiscord();
    }

    private void shutdownCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        if (event.getUser().getIdLong() != 335802802335121408L) {
            hook.editOriginal("https://c.tenor.com/pFeLhIX6b5cAAAAd/tenor.gif").queue();
            return;
        }

        for (Runnable runnable : shutdownRunnables) {
            runnable.run();
        }

        hook.editOriginal("Shutdown completed").queue();
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        event.getJDA().shutdown();
        System.exit(0);
    }

    private void syncCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        if (event.getUser().getIdLong() != 335802802335121408L) {
            hook.editOriginal("https://c.tenor.com/pFeLhIX6b5cAAAAd/tenor.gif").queue();
            return;
        }

        hook.editOriginal("Updating database. This could take up to 3 minutes.").queue();

        Thread thread = new Thread(() -> {
            QuotesDB.doMessageSync(event.getJDA());
            hook.editOriginal("Database updated").queue();
        }, "Quotes-Database-Sync-Thread");
        thread.start();

    }
}
