import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.hooks.events.MessageEvent;

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

//Static class for global functions to be referenced throughout the project
public class TwitchListener extends ListenerAdapter {
	//Instantiate variables
	public Bot bot;
	//Constructor
	public TwitchListener(Bot bot) {
		this.bot = bot;
	}

	@Override
	//Generic on message received event:
    public void onMessage(MessageEvent event)  {
    	//Make sure our bot is initialized
    	if (bot != null) {
    		Message message = new Message(event.getMessage(), event.getUser().getNick(), event.getChannel().getName(), "twitch", System.currentTimeMillis(),  false, false);
    		bot.newMessages.add(message);
    	}// end of bot null check
    }// end of on generic message
}//end of twitch listener class