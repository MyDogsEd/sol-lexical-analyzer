package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.Quote;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.Instant;

import static dev.mydogsed.sollexicalanalyzer.Main.jda;

public class QuotesUtil {

    // Base EmbedBuilder for quotes commands
    public static EmbedBuilder quotesEmbed(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setTimestamp(Instant.now());
    }
}
