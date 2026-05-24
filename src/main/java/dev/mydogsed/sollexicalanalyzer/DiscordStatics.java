package dev.mydogsed.sollexicalanalyzer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Objects;

public class DiscordStatics {

    private static DiscordStatics instance;

    private final JDA jda;

    private DiscordStatics(JDA jda) {
        this.jda = jda;
    }

    public static void init(JDA jda) {
        if (instance == null) {
            instance = new DiscordStatics(jda);
        }
    }

    public static DiscordStatics getInstance() {
        return instance;
    }

    public Guild getGuild() {
        if (Config.IS_PROD){
            return Objects.requireNonNull(jda.getGuildById(Prod.GUILD));
        }
        return Objects.requireNonNull(jda.getGuildById(Test.GUILD_TEST));
    }

    public TextChannel getSmashesChannel() {
        if (Config.IS_PROD){
            return Objects.requireNonNull(jda.getTextChannelById(Prod.SMASHES_CHANNEL));
        }
        return Objects.requireNonNull(jda.getTextChannelById(Test.SMASHES_TEST));
    }

    public TextChannel getQuotesChannel() {
        if (Config.IS_PROD){
            return Objects.requireNonNull(jda.getTextChannelById(Prod.QUOTES_CHANNEL));
        }
        return Objects.requireNonNull(jda.getTextChannelById(Test.QUOTES_TEST));
    }

    public Emoji getUpvoteEmoji() {
        if (Config.IS_PROD){
            return Objects.requireNonNull(jda.getEmojiById(Prod.UPVOTE));
        }
        return Objects.requireNonNull(jda.getEmojiById(Test.UPVOTE_TEST));
    }

    public Emoji getDownvoteEmoji() {
        if (Config.IS_PROD){
            return Objects.requireNonNull(jda.getEmojiById(Prod.DOWNVOTE));
        }
        return Objects.requireNonNull(jda.getEmojiById(Test.DOWNVOTE_TEST));
    }

    public String getAvatarUrl() {
        return jda.getSelfUser().getAvatarUrl();
    }


    // Discord IDs

    private static class Test {
        // MyDogsBot Guild
        public static final long GUILD_TEST = 734502410952769607L;

        public static final long SMASHES_TEST = 1508165454617776299L;
        public static final long QUOTES_TEST = 1508165402243366932L;

        public static final long UPVOTE_TEST = 835708999806484592L;
        public static final long DOWNVOTE_TEST = 740584643929178142L;
    }

    private static class Prod {
        // Fruity Factory Guild
        public static final long GUILD = 1233092684198182943L;

        // Channels
        public static final long SMASHES_CHANNEL = 1293961375273451615L;
        public static final long QUOTES_CHANNEL = 1233098767658520668L;

        // Emojis
        public static final long UPVOTE = 1233196810793783356L;
        public static final long DOWNVOTE = 1313221080659394660L;
    }

}
