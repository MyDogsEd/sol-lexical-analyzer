package dev.mydogsed.sollexicalanalyzer.quotes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;

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
                !m.getMessageSnapshots().getFirst().getContentRaw().isBlank();
    }

    public static boolean isForwardedImageQuote(Message m) {
        return m.getMessageReference() != null && // The message has a message reference
                m.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD &&
                m.getMessageSnapshots().getFirst().getAttachments().size() == 1;
    }
}
