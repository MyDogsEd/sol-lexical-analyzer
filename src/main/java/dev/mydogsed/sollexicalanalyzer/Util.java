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
            return message.getMessageSnapshots().getFirst().getContentRaw();
        }
        else {
            return message.getContentRaw();
        }
    }

    // get message content raw, including forwarded messages
    public static String getMessageContentSanitized(Message message){
        if (message.getMessageReference() != null && message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD) {
            return MarkdownSanitizer.sanitize(message.getMessageSnapshots().getFirst().getContentRaw());
        } else {
            return MarkdownSanitizer.sanitize(message.getContentRaw());
        }
    }

    // return a random keyboard smash
    public static Message randomSmash() {
        List<Message> filtered = Main.smashesCache.getMessages()
                .stream().filter((Message message) -> !message.getContentRaw().contains("//") && !message.getContentRaw().isBlank()).toList();
        return filtered.get(new Random().nextInt(filtered.size()));
    }
}
