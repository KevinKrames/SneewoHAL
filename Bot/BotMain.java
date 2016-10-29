package Bot;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import java.util.Timer;
import java.util.Random;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

//package com.javapapers.java;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.*;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class BotMain {
        //Main function where the app starts:
        public static void main(String[] args) throws Exception {

                //Read our channels file to join all the channels
                ArrayList<String> channels = BotUtilities.readFile("files/channels.txt");
                //Configure builder for what we want our bot to do
                Configuration.Builder builder = new Configuration.Builder()
                                .setAutoNickChange(false) //Twitch doesn't support multiple users
                                .setOnJoinWhoEnabled(false) //Twitch doesn't support WHO command
                                .setCapEnabled(false)
                                .addServer("irc.twitch.tv", 6667)// twitch irc server
                                .setName(dbids.twitchUsername) //Your twitch.tv username
                                .setServerPassword(dbids.twitchOAuth); //Your oauth password from http://twitchapps.com/tm
                                

                //Join each individual channenl based on files/channels.txt
                if (!dbids.debug) {
                for (int i=0; i < channels.size(); i++) {
                        builder.addAutoJoinChannel(channels.get(i));
                }
                } else {
                	builder.addAutoJoinChannel("#"+dbids.twitchOwner);
                }
                Bot bot = new Bot();
                //Instantiate our twitch listener:
                TwitchListener twitchListener = new TwitchListener(bot);

                builder.addListener(twitchListener);
                //Setup the  config for the bot
                Configuration configuration = builder.buildConfiguration();

                //Create our bot with the configuration
                PircBotX twitchBot = new PircBotX(configuration);
                //Initialize our bot that handles the logic
                bot.twitchListener = twitchListener;
                bot.twitchBot = twitchBot;
                //Give the twitch listener a reference to it's bot
                twitchListener.bot = bot;
                //Connect to the server
                twitchBot.startBot();
        }
}