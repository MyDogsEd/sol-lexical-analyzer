package dev.mydogsed.sollexicalanalyzer;

import dev.mydogsed.sollexicalanalyzer.misc.AnalyzerCommands;
import dev.mydogsed.sollexicalanalyzer.misc.MiscCommands;
import dev.mydogsed.sollexicalanalyzer.framework.CommandRegistry;
import dev.mydogsed.sollexicalanalyzer.framework.MessageCache;
import dev.mydogsed.sollexicalanalyzer.framework.RegistrySlashCommandListener;
import dev.mydogsed.sollexicalanalyzer.framework.SimpleSlashCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.AdminCommands;
import dev.mydogsed.sollexicalanalyzer.quotes.CountCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.LeaderboardCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.RandomCommand;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.QuotesDBListener;
import dev.mydogsed.sollexicalanalyzer.quotes.persist.SessionFactoryManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;

import static dev.mydogsed.sollexicalanalyzer.Util.getApiKey;

public class Main extends ListenerAdapter {

    public static JDA jda;

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static CommandRegistry commandRegistry = CommandRegistry.getInstance();

    public static MessageCache smashesCache;

    private static final long startTime = System.currentTimeMillis();

    static void main() {
        // Log the bot in
        try {
            jda = JDABuilder.createDefault(getApiKey())
                    .addEventListeners(new Main())
                    .enableIntents(EnumSet.allOf(GatewayIntent.class))
                    .enableCache(CacheFlag.EMOJI)
                    .setActivity(Activity.customStatus("starting..."))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .build();
        }
        // File not found
        catch (FileNotFoundException e) {
            logger.error("API Key file not found.");
            logger.error("You must create the BOT_KEY.apikey file in the same directory as the .jar file.");
            logger.error("(Checking in {} for key file)", Paths.get("").toAbsolutePath());
        }
        // Token is not valid
        catch (InvalidTokenException e) {
            logger.error("The provided token is invalid.");
        }
        // Everything Else
        catch (IllegalArgumentException e){
            logger.error("One of the provided arguments is invalid.");
        }
        // The bot has already started at this point, so all code is handled by events
    }

    // Register all commands and things after the bot is logged in and ready for us to do so
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Starting sol-lexical-analyzer on JDA version {}", JDAInfo.VERSION);

        // Set the bot's status to idle
        jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.customStatus("starting..."), false);

        // TODO: move this to a command or something, this really should only be done once, not every time the bot logs in
        // MessageCache has to come first, before we attach command executors

        createSmashesCache();
        registerCommandExecutors();
        registerSlashCommandsToDiscord();
        registerListeners();

        // Set timer for pulling a random line as a status
        new Timer().schedule(new TimerTask(){
            public void run() {
                String smash = Util.randomSmash().getContentRaw();
                if (smash.length() > 126) {
                    smash = smash.substring(0, 126);
                }
                jda.getPresence().setPresence(
                        OnlineStatus.ONLINE,
                        Activity.customStatus(String.format("\"%s\"", smash)),
                        false
                );

            }},0,1_800_000); // 1.8 million ms is 30 min

        logger.info("sol-lexical-analyzer is ready!");
        logger.info ("Startup took {} s", (System.currentTimeMillis() - startTime) / 1000);
    }

    // Create the smashes cache
    private static void createSmashesCache() {

        logger.info("Creating the smashes cache...");
        long startTime = System.currentTimeMillis();
        // Create the smashes cache
        smashesCache = new MessageCache(Objects.requireNonNull(jda.getTextChannelById(1293961375273451615L)));
        logger.info("smashes cache took {}s", (System.currentTimeMillis() - startTime) / 1000.0d);

        // All caches created
        logger.info("All message caches created!");
    }

    // Register the slash commands to discord (does NOT register executors)
    public static void registerSlashCommandsToDiscord(){
        // MyDogsBot guild: 734502410952769607
        // Fruity Factory: 1233092684198182943
        // GDC: 612467012018634753

        // Register slash commands for the two guilds:
        try {
            registerCommandsForGuild(Objects.requireNonNull(jda.getGuildById("734502410952769607"))); // MyDogsBot
            registerCommandsForGuild(Objects.requireNonNull(jda.getGuildById("1233092684198182943"))); // Fruity Factory
            // registerCommandsForGuild(Objects.requireNonNull(jda.getGuildById("612467012018634753"))); // GDC
        } catch (NullPointerException e) {
            logger.error("Guilds not found for registering slash commands!");
        }
        logger.info("Registered Slash Commands");
    }

    // For the given guild, register all commands in the command registry as slash commands for that guild
    private static void registerCommandsForGuild(Guild guild){
        CommandRegistry registry = CommandRegistry.getInstance();
        Set<String> commandNames = registry.getCommandNames();
        CommandListUpdateAction updateAction = guild.updateCommands();
        for(String commandName : commandNames){
            updateAction = updateAction.addCommands(
                    registry.getExecutor(commandName).getData()
            );
        }
        updateAction.queue();
    }

    // Register the command Executors so the commands actually do something lmao
    private static void registerCommandExecutors(){

        // Register simple slash commands
        commandRegistry.register(new SimpleSlashCommand(
                "invite",
                "Prints the invite link for the bot to the channel!",
                "Invite the bot here: "
                        + "https://discord.com/oauth2/authorize?client_id=1294039897316917278&permissions=8&scope=bot"
        ));

        // Register classes that use the @SlashCommand decorators
        commandRegistry.registerMethods(MiscCommands.class);

        // Register Classes that implement SlashCommand
        commandRegistry.register(new AnalyzerCommands());
        commandRegistry.register(new AdminCommands());

        // Register Quotes Commands
        commandRegistry.register(new CountCommand());
        commandRegistry.register(new LeaderboardCommand());
        commandRegistry.register(new RandomCommand());

        // Log that command executors have been registered
        logger.info("Registered Command Executors");
    }

    // register all listeners across the project
    // all listeners must be registered here
    private static void registerListeners(){
        // Event listener for the command registry
        jda.addEventListener(new RegistrySlashCommandListener());

        // Event listener to shut down SessionFactory
        jda.addEventListener(new SessionFactoryManager());

        // Event listener for the quote db updates
        jda.addEventListener(new QuotesDBListener());

        logger.info("Registered Event Listeners");
    }
}
