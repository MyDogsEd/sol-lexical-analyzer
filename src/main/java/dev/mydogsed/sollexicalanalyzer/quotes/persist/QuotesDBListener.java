package dev.mydogsed.sollexicalanalyzer.quotes.persist;

import dev.mydogsed.sollexicalanalyzer.DiscordStatics;
import dev.mydogsed.sollexicalanalyzer.quotes.QuotesUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuotesDBListener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(QuotesDBListener.class);

    // Check if message is a quote, add it to the database if it is a quote
    public void onMessageReceived(MessageReceivedEvent event) {

        // Check that message is in the quotes channel
        if (event.getChannel().getIdLong() != DiscordStatics.getInstance().getQuotesChannel().getIdLong()) {
            log.debug("Message received not in quotes channel");
            return;
        }

        // Get the message object
        Message message = event.getMessage();

        // Check if it is a quote
        if (!QuotesUtil.isQuote(message)) {return;}

        // Add it to the database
        QuotesDB.addOrUpdateQuote(message);
    }

    //
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (event.getChannel().getIdLong() != DiscordStatics.getInstance().getQuotesChannel().getIdLong()) return;

        if (!QuotesDB.quoteExists(event.getMessageIdLong())) return;

        Message message = event.getMessage();
        QuotesDB.addOrUpdateQuote(message);
    }

    public void onMessageDelete(MessageDeleteEvent event) {
        if (event.getChannel().getIdLong() != DiscordStatics.getInstance().getQuotesChannel().getIdLong()) return;
        if (!QuotesDB.quoteExists(event.getMessageIdLong())) return;

        QuotesDB.removeQuote(event.getMessageIdLong());
    }
}
