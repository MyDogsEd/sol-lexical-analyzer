package dev.mydogsed.sollexicalanalyzer.framework;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class MessageCache {

    private final Map<Long, Message> cache;

    private final TextChannel activeChannel;

    // Create a new MessageCache
    public MessageCache(TextChannel textChannel) {

        // Create the map for the cache
        this.cache = new HashMap<>();

        // Set the active channel
        this.activeChannel = textChannel;

        // Get all the messages in the channel
        List<Message> messages = new LinkedList<>();
        MessageHistory messageHistory = textChannel.getHistory();
        while(true){
            List<Message> history = messageHistory.retrievePast(100).complete();
            messages.addAll(history);
            if (history.size() < 100){
                break;
            }
            messageHistory = textChannel.getHistoryAfter(messages.getLast(), 100).complete();
        }

        // Put all the retrieved messages in the map
        for(Message message : messages){
            this.cache.put(message.getIdLong(), message);
        }

        // Register the listener
        textChannel.getJDA().addEventListener(new MessageCacheListener());
    }

    // Return a list of all messages in the cache
    public List<Message> getMessages() {
        return cache.values().stream().toList();
    }

    private void addMessage(Message message) {
        cache.put(message.getIdLong(), message);
    }

    private void updateMessage(Message message) {
        cache.put(message.getIdLong(), message);
    }

    private void deleteMessage(long messageId) {
        cache.remove(messageId);
    }

    // Listener Class to handle edited messages, etc
    class MessageCacheListener extends ListenerAdapter {

        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            if (event.getChannel().getIdLong() == activeChannel.getIdLong()){
                addMessage(event.getMessage());
            }
        }

        public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
            if (event.getChannel().getIdLong() == activeChannel.getIdLong()){
                updateMessage(event.getMessage());
            }
        }

        public void onMessageDelete(@NotNull MessageDeleteEvent event) {
            if (event.getChannel().getIdLong() == activeChannel.getIdLong()) {
                deleteMessage(event.getMessageIdLong());
            }
        }
    }
}
