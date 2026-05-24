package dev.mydogsed.sollexicalanalyzer;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static final String DISCORD_TOKEN = dotenv.get("DISCORD_TOKEN");

    public static final Boolean IS_PROD =  Boolean.parseBoolean(dotenv.get("IS_PROD", "true"));
}
