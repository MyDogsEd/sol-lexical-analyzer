package dev.mydogsed.sollexicalanalyzer.misc;

import dev.mydogsed.sollexicalanalyzer.DiscordStatics;
import dev.mydogsed.sollexicalanalyzer.Util;
import dev.mydogsed.sollexicalanalyzer.Main;
import dev.mydogsed.sollexicalanalyzer.framework.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.util.*;
import java.util.List;

public class AnalyzerCommands implements SlashCommand {

    // Switch for all the declared commands
    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "random" -> randomCommand(event);
            case "letters" -> lettersCommand(event);
            case "averagelength" -> averageLengthCommand(event);
            case "longest" -> longestCommand(event);
            case "days" -> daysCommand(event);
            case "csv" -> csvCommand(event);
            case "count" -> countCommand(event);

        }
    }

    // Used to register all the commands
    @Override
    public SlashCommandData getData() {
        return Commands.slash("analyzer", "analyze sol's keyboard smashes")
                .addSubcommands(
                        new SubcommandData("random", "Random Keyboard Smash"),
                        new SubcommandData(
                                "letters",
                                "Calculate the % of each letter in sol's keyboard smashes"
                        ),
                        new SubcommandData(
                                "averagelength",
                                "Calculate the average length of sol's keyboard smashes"
                        ),
                        new SubcommandData("longest", "Show the single longest keyboard smash"),
                        new SubcommandData("days", "What days does sol keyboard smash?"),
                        new SubcommandData("csv", "Record of all keyboard smashes in a csv file."),
                        new SubcommandData("count", "How many total keyboard smashes?")
                );
    }

    // COMMANDS

    // /analyzer random
    private void randomCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        Message random = Util.randomSmash();
        EmbedBuilder eb = analyzerEmbed("Random Keyboard Smash")
                .addField(Util.getMessageContentRaw(random), random.getJumpUrl(), false);
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    // /analyzer letters
    private void lettersCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        char[] chars = getCharactersInMessages(
                Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList()
        );
        Map<Character, Integer> map = getCharacterOccurencesMap(chars);

        // Convert the keys in the hashmap to a list
        List<Character> keys = new ArrayList<>(map.keySet().stream().toList());

        // Sort that list based on the key's value in the map
        keys.sort(Comparator.comparing(map::get));

        // Build an embed with that information
        EmbedBuilder eb = analyzerEmbed("Letter Percentages");
        for (int k = keys.size() - 1; k > keys.size() - 10; k--) {
            eb.addField(String.valueOf(keys.get(k)), map.get(keys.get(k)) + "%", false);
        }
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    // /analyzer averagelength
    private void averageLengthCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        List<Message> messages = Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();

        int sum = 0;
        for (Message message : messages) {
            sum += Util.getMessageContentRaw(message).length();
        }
        double avg = (double) sum / (double) messages.size();

        EmbedBuilder eb = analyzerEmbed("Average Length")
                .setDescription("The average length of each keyboard smash is " + new DecimalFormat("#.#").format(avg) + " characters");

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    // /analyzer longest
    private void longestCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        List<Message> messages = new ArrayList<>(Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList());
        messages.sort(Comparator.comparing(message -> message.getContentRaw().length()));
        Message longestMessage = messages.getLast();
        EmbedBuilder eb = analyzerEmbed("Longest")
                .setDescription("The longest single keyboard smash is " + Util.getMessageContentRaw(longestMessage).length() + " characters")
                .addField(Util.getMessageContentRaw(longestMessage), longestMessage.getJumpUrl(), false);
        hook.editOriginalEmbeds(eb.build()).queue();
    }

    // /analyzer days
    private void daysCommand(SlashCommandInteractionEvent event) {
        // Command Boilerplate
        InteractionHook hook = event.getHook();
        event.deferReply().queue();

        // get the keyboard smashes
        List<Message> smashes = Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();

        // Sort all the keyboard smashes by day
        Map<DayOfWeek, List<Message>> days = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            days.put(day, new LinkedList<>());
        }

        for (Message message : smashes) {
            List<Message> list = days.get(message.getTimeCreated().getDayOfWeek());
            list.add(message);
        }

        List<DayOfWeek> keys = new ArrayList<>(days.keySet().stream().toList());
        keys.sort(
                Comparator.comparing(
                                key -> days.get((DayOfWeek) key).size()
                        )
                        .reversed()
        );

        EmbedBuilder eb = analyzerEmbed("Days");
        for (DayOfWeek day : keys) {
            eb.addField(String.valueOf(day), days.get(day).size() + " keyboard smashes", false);
        }

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    // /analyzer csv
    private void csvCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().setEphemeral(false).queue();
        List<Message> smashes = Main.smashesCache.getMessages().stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();

        StringBuilder csv = new StringBuilder();
        for (Message message : smashes) {
            csv.append(String.format("%s, %tc%n", Util.getMessageContentRaw(message), message.getTimeCreated()));
        }

        InputStream stream = new ByteArrayInputStream(csv.toString().getBytes());
        FileUpload upload = FileUpload.fromData(stream, "keyboard_smashes.csv");
        hook.editOriginalAttachments(upload).queue();

    }

    // /analyzer count
    private void countCommand(SlashCommandInteractionEvent event) {
        InteractionHook hook = event.getHook();
        event.deferReply().queue();
        int number = Main.smashesCache.getMessages()
                .stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList().size();

        EmbedBuilder eb = analyzerEmbed("Number of Keyboard Smashes")
                .setDescription("sol has archived " + number +  " keyboard smashes.");

        hook.editOriginalEmbeds(eb.build()).queue();
    }

    // UTILITY

    // Get all the characters in the list of messages
    private static char[] getCharactersInMessages(List<Message> messages) {
        // Get the messages in the channel and write all of them into one string
        StringBuilder letters = new StringBuilder();
        for (Message message : messages) {
            letters.append(Util.getMessageContentSanitized(message).toLowerCase());
        }

        // Get a character array from the string and sort it
        char[] arr = letters.toString().replaceAll("\\s", "").toCharArray();
        Arrays.sort(arr);
        return arr;
    }

    /*
    Returns a Map<Character, Integer> where the Integer is the number of occurrences of that character
    in the char[] array.
     */
    @NotNull
    private static Map<Character, Integer> getCharacterOccurencesMap(char[] arr) {
        Map<Character, Integer> map = new HashMap<>();

        // Count occurrences of each letter in the sorted array
        int i = 0;
        while (i < arr.length) {
            char letter = arr[i];
            int j = i + 1;
            int letterCount = 1;
            while (j < arr.length && arr[j] == letter) {
                letterCount++;
                j++;
            }
            i = j;

            if (!(letter == ' ')) {
                map.put(letter, (int) ((((double) letterCount) / arr.length) * 100));
            }
        }
        return map;
    }

    // A basic embed to use for all the commands in this class.
    private static EmbedBuilder analyzerEmbed(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", DiscordStatics.getInstance().getAvatarUrl())
                .setColor(new Color(184, 56, 59))
                .setTimestamp(new Date().toInstant());
    }
}
