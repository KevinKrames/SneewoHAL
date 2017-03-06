package Bot;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import java.util.ArrayList;

//package com.javapapers.java;



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