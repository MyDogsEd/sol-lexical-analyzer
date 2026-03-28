package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.Quote;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.Instant;

import static dev.mydogsed.sollexicalanalyzer.Main.jda;

public class QuotesUtil {

    public static EmbedBuilder randomQuoteEmbed(Quote quote) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Random Quote")
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setFooter(quote.getAuthor().getUserName(), quote.getAuthor().getAvatarURL())
                .setTimestamp(quote.getTimeCreated());

        // is this a text quote?
        if (quote.isTextQuote()) {
            // TODO: sometimes quote.getContent() is more than 256 chars, and errors out.
            eb.addField(quote.getContent(), quote.getJumpURL() + "\nScore: " + quote.getScore(), false);
        }

        else {
            eb.setImage(quote.getImageURL());
            eb.setDescription(quote.getJumpURL() + "\nScore: " + quote.getScore());
        }

        return eb;
    }

    public static EmbedBuilder quotesEmbed(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setAuthor("sol-lexical-analyzer", "https://mydogsed.dev", jda.getSelfUser().getAvatarUrl())
                .setColor(new Color(88, 133, 162))
                .setTimestamp(Instant.now());
    }
}
