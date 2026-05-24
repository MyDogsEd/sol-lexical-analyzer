package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.DiscordStatics;
import dev.mydogsed.sollexicalanalyzer.framework.SlashCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.QuotesDB;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.Quote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;


public class RandomCommand implements SlashCommand {

    // upvote and downvote emojis from Fruity Factory


    @Override
    public SlashCommandData getData() {
        return Commands.slash("random", "Get a random quote")
                .addOption(
                        OptionType.USER,
                        "from",
                        "get random quotes from a specific user",
                        false
                )
                .addOption(
                        OptionType.USER,
                        "excluding",
                        "get random quotes excluding a specific user",
                        false
                );
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        List<Quote> quotes;
        // If the user option is present, fetch quotes from that user
        if (event.getOption("from") != null) {
            User user = event.getOption("from").getAsUser();
            quotes = QuotesDB.getQuoteAuthor(user.getIdLong()).getQuotes();
        } else { // Otherwise, fetch all quotes
            quotes = QuotesDB.getQuotes();
        }

        // If the excluding option is present, exclude quotes from that user
        if (event.getOption("excluding") != null) {
            User user = event.getOption("excluding").getAsUser();
            quotes = quotes.stream().filter(q -> !  q.getAuthor().getId().equals(user.getIdLong())).collect(Collectors.toList());
        }

        // If no quotes were returned, alert and return
        if (quotes.isEmpty()) {
            hook.editOriginal("No quotes found for this criteria.").queue();
            return;
        }

        Quote randomQuote = quotes.get(new Random().nextInt(quotes.size()));

        // 1% chance that qiqi is rolled
        if (new Random().nextInt(100) == 69) {
            hook.editOriginalAttachments(
                    FileUpload.fromData(
                            Objects.requireNonNull(RandomCommand.class.getResourceAsStream("/qiqi.jpg")),
                            "qiqi.jpg"
                    )
            ).queue();
            return;
        }

        EmbedBuilder eb = randomQuoteEmbed(randomQuote);
        hook.editOriginalEmbeds(eb.build()).setComponents(ActionRow.of(
                Button.of(ButtonStyle.PRIMARY, "upvote", DiscordStatics.getInstance().getUpvoteEmoji()),
                Button.of(ButtonStyle.DANGER, "downvote", DiscordStatics.getInstance().getDownvoteEmoji())
        )).queue(m -> addButtonsCallback(m, event, randomQuote));

    }

    private void addButtonsCallback(Message message, SlashCommandInteractionEvent event, Quote quote) {

        // Create a runnable to disable the ActionRows
        // This runnable should be called when shutting down the bot
        Runnable disableRow = () -> {
            // Disable the actionRows
            ActionRow ar = message.getComponents().getFirst().asActionRow();
            event.getHook().editOriginalComponents(ar.asDisabled()).queue();
        };

        AdminCommands.shutdownRunnables.add(disableRow);
        // Add the button listener to JDA
        ListenerAdapter buttonListener = getListenerAdapter(message, quote);
        event.getJDA().addEventListener(buttonListener);

        new Timer().schedule(new TimerTask() {
            public void run() {
                // Disable the actionRows
                ActionRow ar = message.getComponents().getFirst().asActionRow();
                event.getHook().editOriginalComponents(ar.asDisabled()).queue();

                // Unregister the event listener
                event.getJDA().removeEventListener(buttonListener);
                AdminCommands.shutdownRunnables.remove(disableRow);
            }
        }, 600_000); // 600,000 ms is 10 minutes
    }

    @NotNull
    private static ListenerAdapter getListenerAdapter(Message message, Quote quote) {
        long messageId = message.getIdLong();

        // Acknowledge the event
        // Get the component ID of the clicked button
        // Edit the embed to update the score of the quote
        return new ListenerAdapter() {

            final Set<User> interactedUsers = new HashSet<>();

            @Override
            public void onButtonInteraction(ButtonInteractionEvent event) {
                super.onButtonInteraction(event);

                if (event.getUser().isBot()) {return;}
                if (event.getMessage().getIdLong() != messageId) {return;}
                if (interactedUsers.contains(event.getUser())) {
                    event.getInteraction().reply("You can only react to a rolled quote once!").setEphemeral(true).queue();
                    return;
                }

                interactedUsers.add(event.getUser());

                // Acknowledge the event
                InteractionHook hook = event.getHook();
                event.getInteraction().deferEdit().queue();

                // Get the component ID of the clicked button
                String componentID = event.getComponentId();

                if (componentID.equals("upvote")) {
                    quote.voteUp();
                    QuotesDB.persistOrMergeQuote(quote);
                }
                else {
                    quote.voteDown();
                    QuotesDB.persistOrMergeQuote(quote);
                }

                // Edit the embed to update the score of the quote
                hook.editOriginalEmbeds(randomQuoteEmbed(quote).build()).queue();
            }
        };
    }

    // Template EmbedBuilder for random quote command
    private static EmbedBuilder randomQuoteEmbed(Quote quote) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Random Quote")
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", DiscordStatics.getInstance().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setFooter(quote.getAuthor().getUserName(), quote.getAuthor().getAvatarURL())
                .setTimestamp(quote.getTimeCreated());

        // is this a text quote?
        if (quote.isTextQuote()) {
            eb.addField(quote.getContent(), quote.getJumpURL() + "\nScore: " + quote.getScore(), false);
        }

        else {
            eb.setImage(quote.getImageURL());
            eb.setDescription(quote.getJumpURL() + "\nScore: " + quote.getScore());
        }

        return eb;
    }
}
