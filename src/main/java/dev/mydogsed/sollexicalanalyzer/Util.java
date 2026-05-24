package dev.mydogsed.sollexicalanalyzer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.util.List;
import java.util.Random;

public class Util {

    // get message content raw, including forwarded messages
    public static String getMessageContentRaw(Message message){
        if (message.getMessageReference() != null &&
                message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD
        ) {
            return message.getMessageSnapshots().get(0).getContentRaw();
        }
        else {
            return message.getContentRaw();
        }
    }

    public static Message.Attachment getMessageContentImage(Message message){
        if (message.getMessageReference() != null && // The message has a message reference
                message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD && // Forwarded Message
                !message.getMessageSnapshots().get(0).getAttachments().isEmpty()
        ) {
            return message.getMessageSnapshots().get(0).getAttachments().get(0);
        }
        else {
            return message.getAttachments().get(0);
        }
    }

    // get message content raw, including forwarded messages
    public static String getMessageContentSanitized(Message message){
        if (message.getMessageReference() != null && message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD) {
            return MarkdownSanitizer.sanitize(message.getMessageSnapshots().get(0).getContentRaw());
        } else {
            return MarkdownSanitizer.sanitize(message.getContentRaw());
        }
    }

    // return a random keyboard smash
    public static Message randomSmash() {
        List<Message> filtered = Main.smashesCache.getMessages()
                .stream().filter((Message message) -> !message.getContentRaw().contains("//")).toList();
        return filtered.get(new Random().nextInt(filtered.size()));
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
