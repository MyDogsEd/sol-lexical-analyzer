package dev.mydogsed.sollexicalanalyzer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

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

    public static Message.Attachment getMessageContentImage(Message message){
        if (message.getMessageReference() != null && // The message has a message reference
                message.getMessageReference().getType() == MessageReference.MessageReferenceType.FORWARD && // Forwarded Message
                !message.getMessageSnapshots().getFirst().getAttachments().isEmpty()
        ) {
            return message.getMessageSnapshots().getFirst().getAttachments().getFirst();
        }
        else {
            return message.getAttachments().getFirst();
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



    // Utility method to get the API key from the file present in the same directory
    public static String getApiKey() throws FileNotFoundException {
        File file = new File("./BOT_KEY.apikey");
        Scanner scanner = new Scanner(file);
        return scanner.next();
    }


}
