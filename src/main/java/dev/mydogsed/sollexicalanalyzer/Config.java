package dev.mydogsed.sollexicalanalyzer;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    public static final String DISCORD_TOKEN = dotenv.get("DISCORD_TOKEN");
}
