package dev.mydogsed.sollexicalanalyzer.quotes;

import dev.mydogsed.sollexicalanalyzer.quotes.persist.models.Quote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;

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

    /**
     * A Quote is a message that meets the definition of a Text Quote, Image Quote,
     * Forwarded Text Quote, or Forwarded Image Quote.
     * @param m The Message to test.
     * @return Whether `m` is a quote.
     */
    public static boolean isQuote(Message m) {
        return isTextQuote(m) || isImageQuote(m) || isForwardedTextQuote(m) || isForwardedImageQuote(m);
    }

    /**
     * A Text Quote is a message that contains `"`, `“`, or `”`, or starts with `>`.
     * @param m The Message to test.
     * @return Whether `m` is a quote
     */
    public static boolean isTextQuote(Message m) {
        // If the message is blank, this is not a text quote
        if (m.getContentRaw().isBlank()) return false;

        // Normal test case for text quotes
        return m.getContentRaw().contains("\"") || // straight quotes
                m.getContentRaw().contains("“") || // curly starting quote
                m.getContentRaw().contains("”") || // curly ending quote
                m.getContentRaw().startsWith(">")|| // markdown quotes syntax
                m.getAuthor().getIdLong() == 555955826880413696L; // epic rpg because its funnie
    }

    /**
     * An Image Quote is a message that contains exactly one image attachment
     * @param m The Message to test.
     * @return Whether `m` is an Image Quote
     */
    public static boolean isImageQuote(Message m) {
        return m.getAttachments().size() == 1;
    }


    public static boolean isForwardedTextQuote(Message m) {
        return m.getMessageReference() != null && // The message has a message reference
                m.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD &&
                !m.getMessageSnapshots().get(0).getContentRaw().isBlank();
    }

    public static boolean isForwardedImageQuote(Message m) {
        return m.getMessageReference() != null && // The message has a message reference
                m.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD &&
                m.getMessageSnapshots().get(0).getAttachments().size() == 1;
    }
}
