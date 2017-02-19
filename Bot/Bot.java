package Bot;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.util.data.audio.AudioPlayer;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import MegaHAL.*;

import java.util.*;
import java.io.*;

import javax.sound.sampled.AudioInputStream;

import org.languagetool.*;
import org.languagetool.language.AmericanEnglish;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

//Static class for global functions to be referenced throughout the project
public class Bot extends Thread {
	//Instantiate variables
	public TwitchListener twitchListener = null;
	public PircBotX twitchBot = null;
	public ArrayList<String> channels = null;
	public ArrayList<String> mods = null;
	public ArrayList<String> ignores = null;
	public ArrayList<String> badWords = null;
	public ArrayList<String> banFits = null;
	public ArrayList<String> spellIgnores = null;
	public ArrayList<Channel> channelObjects = null;
	public List<Message> newMessages = Collections.synchronizedList(new ArrayList<Message>());
	//public ArrayList<Message> messages = new ArrayList<Message>();
	public ArrayList<User> users = null;
	public ArrayList<String> lastWords = new ArrayList<String>();
	public Stemmer stemmer = new Stemmer();
	public String owner = "";
	private boolean userLock = false;
	//private Timer timer = new Timer(true);
	public DatabaseManager databaseManager;
	
	public MegaHAL megaDirty, mega;
	
	char commandChar = '!';

	Twitter twitter;
	public String consumerKey = dbids.consumerKey;
	public String consumerSecret = dbids.consumerSecret;
	public String accessToken = dbids.accessToken;
	public String accessSecret = dbids.accessSecret;

	private final Random rng = new Random();

	//Bot constructor
	public Bot() {
		databaseManager = new DatabaseManager();
		channels = BotUtilities.readFile("files/channels.txt");
		if (BotUtilities.readObjectFromFile("files/channels.json") != null) {
			//File exists:
			channelObjects = BotUtilities.initChannels(BotUtilities.readObjectFromFile("files/channels.json"));
		} else {
			channelObjects = new ArrayList<Channel>();
			//init our json file:
			for (int i = 0; i < channels.size(); i++) {
				channelObjects.add(new Channel(channels.get(i)));
			}
			BotUtilities.saveChannels("files/channels.json", channelObjects);
		}
		//Initialize DatabaseManager
		
		mods = BotUtilities.readFile("files/mods.txt");
		ignores = BotUtilities.readFile("files/ignores.txt");
		badWords = BotUtilities.readFile("files/badWords.txt");
		spellIgnores = BotUtilities.readFile("files/spellIgnores.txt");
		owner = dbids.twitchOwner;
		
		try {
			mega = new MegaHAL(false, stemmer, databaseManager, "global");
			megaDirty = new MegaHAL(true, stemmer, databaseManager, "global");
			for (int i = 0; i < channelObjects.size(); i++) {
				channelObjects.get(i).mega = new MegaHAL(false, stemmer, databaseManager, channelObjects.get(i).name);
				channelObjects.get(i).megaDirty = new MegaHAL(true, stemmer, databaseManager, channelObjects.get(i).name);
			}
			//megaDirty = new MegaHAL(true);
		} catch (IOException e) {

		}

		//INITIALIZE TWITTER
		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret).setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessSecret);
			TwitterFactory factory = new TwitterFactory(cb.build());
			twitter = factory.getInstance();
			/*
		    		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		    		AccessToken accessToken = new AccessToken(accessToken, accessSecret);
		    		twitter.setOAuthAccessToken(accessToken);*/

		} catch (Exception te) {
			te.printStackTrace();
		}
		//
		//TWITTER DONE
		//
		start();
	}


	//Main function that updates at the given timer task rate
	public void run() {
		while (true) {
		//update timers
		for (int i = 0; i < channelObjects.size(); i++) {
			if (channelObjects.get(i).fitsState >= 0) {
				MegaHAL currentMega = channelObjects.get(i).dirty?channelObjects.get(i).mega:channelObjects.get(i).megaDirty;
			if (System.currentTimeMillis() - channelObjects.get(i).fitsTime > 1000 && channelObjects.get(i).fitsState >= 5) {
				String temp = currentMega.formulateReply(channelObjects.get(i).answer, "");
				if (BotUtilities.addUnderscores(temp, channelObjects.get(i).answer) != null)
				twitchBot.send().message(channelObjects.get(i).name, BotUtilities.addUnderscores(temp, channelObjects.get(i).answer));
				channelObjects.get(i).fitsState--;
			}
			if (System.currentTimeMillis() - channelObjects.get(i).fitsTime > 8000 && channelObjects.get(i).fitsState == 4) {
				String temp = currentMega.formulateReply(channelObjects.get(i).answer, "");
				if (BotUtilities.addUnderscores(temp, channelObjects.get(i).answer) != null)
				twitchBot.send().message(channelObjects.get(i).name, BotUtilities.addUnderscores(temp, channelObjects.get(i).answer));
				channelObjects.get(i).fitsState--;
			}
			if (System.currentTimeMillis() - channelObjects.get(i).fitsTime > 16000 && channelObjects.get(i).fitsState == 3) {
				String temp = currentMega.formulateReply(channelObjects.get(i).answer, "");
				if (BotUtilities.addUnderscores(temp, channelObjects.get(i).answer) != null)
				twitchBot.send().message(channelObjects.get(i).name, BotUtilities.addUnderscores(temp, channelObjects.get(i).answer));
				channelObjects.get(i).fitsState--;
			}
			if (System.currentTimeMillis() - channelObjects.get(i).fitsTime > 24000 && channelObjects.get(i).fitsState == 2) {
				String temp = currentMega.formulateReply(channelObjects.get(i).answer, "");
				if (BotUtilities.addUnderscores(temp, channelObjects.get(i).answer) != null)
				twitchBot.send().message(channelObjects.get(i).name, BotUtilities.addUnderscores(temp, channelObjects.get(i).answer));
				channelObjects.get(i).fitsState--;
			}
			if (System.currentTimeMillis() - channelObjects.get(i).fitsTime >= 30000 && channelObjects.get(i).fitsState == 1) {
				twitchBot.send().message(channelObjects.get(i).name, "Times up no one guessed the answer: " + channelObjects.get(i).answer);
				channelObjects.get(i).fitsTime = (long) 0;
				channelObjects.get(i).fitsState = (long) 0;
			}
			}
		}
		/*
		//We have some messages to parse:
		if (newMessages != null) {
			if (newMessages.size() > 0) {
				messages = newMessages;
			}
		}
		newMessages = new ArrayList<Message>();*/
		synchronized (newMessages) {
		//loop thru all the messages
		for (int i = 0; i < newMessages.size(); i++) {
			//if the message is not 0 length
			if (newMessages.get(i).message != null) {
				//print out the message to the console:
				System.out.println(newMessages.get(i).channel + " " + newMessages.get(i).type + " " + newMessages.get(i).time + " " + newMessages.get(i).sender+":"+newMessages.get(i).message);

				//check if correct sneep guess:


				if (newMessages.get(i).message.length() > 0) {
					if (newMessages.get(i).message.charAt(0) == commandChar) {
						//This is a command, handle it
						this.handleCommands(newMessages.get(i));
					} else {
						//If the person is not ignored:
						if (!ignores.contains(newMessages.get(i).sender.toLowerCase())) {
							//else record the message to local files
							BotUtilities.addLineToFile(newMessages.get(i).channel + " " + newMessages.get(i).type + " " + newMessages.get(i).time + " " + newMessages.get(i).sender + ":" + newMessages.get(i).message, "files/log.txt");


							//Dont speak if theres a fits running:
							if (BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).fitsState > 0) {
								//We are playing the snee game here
								if (newMessages.get(i).message.toLowerCase().contains(BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).answer.toLowerCase())) {

									lastWords.add(BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).answer.toLowerCase());
									if (lastWords.size() > 5) {
										lastWords.remove(lastWords.get(0));
									}

									BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).fitsTime = (long) 0;
									BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).fitsState = (long) 0;
									ArrayList<String> local = new ArrayList<String>();
									ArrayList<String> global = new ArrayList<String>();
									//System.out.print("1");
									ArrayList<String> tempArray = BotUtilities.readFile("files/sneeps" + newMessages.get(i).channel + ".txt");
									ArrayList<String> tempNames = BotUtilities.readFile("files/sneeps" + newMessages.get(i).channel + "Names.txt");
									//System.out.print("2");
									BotUtilities.addOne(tempNames, tempArray, newMessages.get(i).sender, 1);
									//System.out.print("3");
									BotUtilities.writeFile(tempArray, "files/sneeps" + newMessages.get(i).channel + ".txt");
									BotUtilities.writeFile(tempNames, "files/sneeps" + newMessages.get(i).channel + "Names.txt");
									//System.out.print("4");
									local = BotUtilities.getValues(tempNames, tempArray, newMessages.get(i).sender);
									//System.out.print("5");

									tempArray = BotUtilities.readFile("files/sneeps.txt");
									tempNames = BotUtilities.readFile("files/sneepsNames.txt");
									BotUtilities.addOne(tempNames, tempArray, newMessages.get(i).sender, 1);
									BotUtilities.writeFile(tempArray, "files/sneeps.txt");
									BotUtilities.writeFile(tempNames, "files/sneepsNames.txt");
									global = BotUtilities.getValues(tempNames, tempArray, newMessages.get(i).sender);

									twitchBot.send().message(newMessages.get(i).channel, "Congrats " + newMessages.get(i).sender + "! You won! Sneeps: " + local.get(1));
								}
							} else {
								//Check for random speak:
								int chanceToSpeak = 0;
								switch (BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).frequency) {
								case "very low":
									chanceToSpeak = 3;
									break;
								case "low":
									chanceToSpeak = 5;
									break;
								case "medium":
									chanceToSpeak = 10;
									break;
								case "high":
									chanceToSpeak = 25;
									break;
								case "very high":
									chanceToSpeak = 50;
									break;
								default:
									break;
								}
								
								//Check memory for higher chance to speak:
								/* WIP
								if (!mega.keywordMemory.containsKey(messages.get(i).channel.toLowerCase())) {
									mega.keywordMemory.put(messages.get(i).channel.toLowerCase(), new HashMap());
								}
								HashMap tempMap = (HashMap)mega.keywordMemory.get(messages.get(i).channel.toLowerCase());*/

								if (newMessages.get(i).message.toLowerCase().contains("sneewo") || rng.nextInt(100) <= chanceToSpeak) {

									//If we are not mute
									if (!BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).mute && dbids.speak) {
										String reply = null;
										
											//reply = (BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).dirty?BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).megaDirty:BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).mega)
														//.formulateReply(newMessages.get(i).message, newMessages.get(i).channel);\
										
										reply = mega.formulateReply(newMessages.get(i).message, newMessages.get(i).channel);
										
										if (reply != null) {
											twitchBot.send().message(newMessages.get(i).channel, reply);

											System.out.println("Sent message in " + newMessages.get(i).channel + ":" + reply);
										}
									}
								} else {
									
									//else learn the message:
									(BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).mega).trainOnly(newMessages.get(i).message, newMessages.get(i).channel);
									(BotUtilities.getChannelWithName(newMessages.get(i).channel.toLowerCase(), channelObjects).megaDirty).trainOnly(newMessages.get(i).message, newMessages.get(i).channel);
									//megaDirty.trainOnly(messages.get(i).message, messages.get(i).channel);
								}
							}
						}
					}
				}
				newMessages.remove(newMessages.get(i));
			}
		}
		}//end of syncronized
		}
	}

	private void handleCommands(Message message) {
		
		if (!dbids.speak) {
			return;
		}
		boolean isMod = mods.contains(message.sender);
		boolean isOwner = message.sender.equalsIgnoreCase(owner);
		//Help command:
		if (message.message.equalsIgnoreCase("!help")) {
			twitchBot.send().message(message.channel, "Type !commands for a list of commands, or use !help COMMAND for help with a specific command.");
		}

		if (message.message.equalsIgnoreCase("!commands")) {
			twitchBot.send().message(message.channel, "!help !join !leave !mute !unmute !dirty !undirty MOD COMMANDS: !fits !ignore !unignore !banword !unbanword !spellignore !spellunignore");
		}

		/*
		 * HELP COMMANDS
		 */
		if (message.message.equalsIgnoreCase("!help set") || message.message.equalsIgnoreCase("!help !set")) {
			twitchBot.send().message(message.channel, "To set how much Sneewo speaks, use !set AMOUNT, where AMOUNT is one of these: very low, low, medium, high, or very high.");
		}
		if (message.message.equalsIgnoreCase("!help join") || message.message.equalsIgnoreCase("!help !join")) {
			twitchBot.send().message(message.channel, "To have Sneewo join your chat use !join");
		}
		if (message.message.equalsIgnoreCase("!help leave") || message.message.equalsIgnoreCase("!help !leave")) {
			twitchBot.send().message(message.channel, "To have Sneewo leave your chat use !leave");
		}
		if (message.message.equalsIgnoreCase("!help mute") || message.message.equalsIgnoreCase("!help !mute")) {
			twitchBot.send().message(message.channel, "To mute Sneewo, you must be the owner of the chat or be a Sneewo mod and type !mute");
		}
		if (message.message.equalsIgnoreCase("!help unmute") || message.message.equalsIgnoreCase("!help !unmute")) {
			twitchBot.send().message(message.channel, "To unmute Sneewo, you must be the owner of the chat or be a Sneewo mod and type !unmute");
		}
		if (message.message.equalsIgnoreCase("!help dirty") || message.message.equalsIgnoreCase("!help !dirty")) {
			twitchBot.send().message(message.channel, "To remove Sneewo's bad word filter, you must be the owner of the chat or be a Sneewo mod and type !dirty");
		}
		if (message.message.equalsIgnoreCase("!help undirty") || message.message.equalsIgnoreCase("!help !undirty")) {
			twitchBot.send().message(message.channel, "To add Sneewo's bad word filter, you must be the owner of the chat or be a Sneewo mod and type !undirty");
		}
		if (message.message.equalsIgnoreCase("!help fits") || message.message.equalsIgnoreCase("!help !fits")) {
			twitchBot.send().message(message.channel, "Fill in the Sneewo is a mini game that mods can run in chat by typing !fits");
		}
		if (message.message.equalsIgnoreCase("!help banword") || message.message.equalsIgnoreCase("!help !banword")) {
			twitchBot.send().message(message.channel, "To ignore a word in Sneewo's bad word filter, you must be a Sneewo mod and type !banword WORD");
		}
		if (message.message.equalsIgnoreCase("!help unbanword") || message.message.equalsIgnoreCase("!help !unbanword")) {
			twitchBot.send().message(message.channel, "To unignore a word in Sneewo's bad word filter, you must be a Sneewo mod and type !unbanword WORD");
		}
		if (message.message.equalsIgnoreCase("!help ignore") || message.message.equalsIgnoreCase("!help !ignore")) {
			twitchBot.send().message(message.channel, "To ignore a person in Sneewo's filter, you must be a Sneewo mod and type !ignore PERSON");
		}
		if (message.message.equalsIgnoreCase("!help unignore") || message.message.equalsIgnoreCase("!help !unignore")) {
			twitchBot.send().message(message.channel, "To unignore a person in Sneewo's filter, you must be a Sneewo mod and type !unignore PERSON");
		}
		if (message.message.equalsIgnoreCase("!help spellignore") || message.message.equalsIgnoreCase("!help !spellignore")) {
			twitchBot.send().message(message.channel, "To ignore spelling of a word in Sneewo's filter, you must be a Sneewo mod and type !spellignore WORD");
		}
		if (message.message.equalsIgnoreCase("!help unspellignore") || message.message.equalsIgnoreCase("!help !unspellignore")) {
			twitchBot.send().message(message.channel, "To unignore spelling of a word in Sneewo's filter, you must be a Sneewo mod and type !unspellignore WORD");
		}
		
/*
 * MOD COMMANDS:
 */
		if ((isOwner == true || isMod == true) && (message.message.toLowerCase().equals("!fillinthesneewo") || message.message.toLowerCase().equals("!fits"))) {

			if (BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).fitsTime == 0) {
				//START SNEEWO MINI GAME
				twitchBot.send().message(message.channel, "Fill in the sneewo started! Sneewo will talk about a random word for 30 seconds, type the word in chat to guess what she is talking about and win sneeps!");
				BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).fitsTime = System.currentTimeMillis();
				String answer = getAnswer(BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects));
				BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).fitsState = (long)5;
				BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).answer = answer;
			}
		}

		if (message.message.equalsIgnoreCase("!sneeps") ) {

			ArrayList<String> local = new ArrayList<String>();
			ArrayList<String> global = new ArrayList<String>();

			ArrayList<String> tempArray = BotUtilities.readFile("files/sneeps" + message.channel + ".txt");
			ArrayList<String> tempNames = BotUtilities.readFile("files/sneeps" + message.channel + "Names.txt");
			local = BotUtilities.getValues(tempNames, tempArray, message.sender);

			tempArray = BotUtilities.readFile("files/sneeps.txt");
			tempNames = BotUtilities.readFile("files/sneepsNames.txt");
			global = BotUtilities.getValues(tempNames, tempArray, message.sender);

			twitchBot.send().message(message.channel, message.sender + ", Global Sneeps: (#" + global.get(0) + ") " + global.get(1) + " | Sneeps in " + message.channel + ": (#" + local.get(0) + ") " + local.get(1));
		}

		if (message.message.equalsIgnoreCase("!join")) {
			channels = BotUtilities.readFile("files/channels.txt");
			if (channels.contains("#" + message.sender)) {
				//Sneewo is already in this channel
				twitchBot.send().message(message.channel, "I'm already in your channel.");
			} else {
				//Actually join the channel
				channels.add("#" + message.sender);
				Collections.sort(channels);
				twitchBot.send().joinChannel("#" + message.sender);
				BotUtilities.writeFile(channels, "files/channels.txt");
				//now handle channel variables:
				channelObjects.add(new Channel("#" + message.sender));
				BotUtilities.saveChannels("files/channels.json", channelObjects);

				twitchBot.send().message(message.channel, "Joined channel: #" + message.sender);
			}
		}

		if ((isMod || isOwner) && message.message.contains("!tweet ")) {
			if (message.message.substring(0, 7).equalsIgnoreCase("!tweet ") && message.message.length() > 7) {
				//Send the tweet
				try {
					//Send the update
					twitter.updateStatus(message.message.substring(7));
					System.out.println("Posted: " + message.message.substring(7));
				} catch (TwitterException te) {
					te.printStackTrace();
				}
			}
		}

		if (message.message.equalsIgnoreCase("!leave")) {
			channels = BotUtilities.readFile("files/channels.txt");
			if (channels.contains("#" + message.sender)) {
				//Actually leave the channel
				channels.remove("#" + message.sender);
				//Remove channel object:
				Channel objToRemove = null;
				for (int i = 0; i < channelObjects.size(); i++) {
					if (channelObjects.get(i).name.equalsIgnoreCase("#" + message.sender)) {
						objToRemove = channelObjects.get(i);
					}
				}
				if (objToRemove != null) {
					channelObjects.remove(objToRemove);
				}
				BotUtilities.saveChannels("files/channels.json", channelObjects);

				twitchBot.send().message(message.channel, "Leaving channel: #" + message.sender);
				twitchBot.sendRaw().rawLine("PART #" + message.sender);
				Collections.sort(channels);
				BotUtilities.writeFile(channels, "files/channels.txt");
			} else {
				twitchBot.send().message(message.channel, "I have already left your channel.");
			}
		}

		/*
		 * adding and removing mods
		 */

		if (message.message.contains("!mod") && isOwner) {
			mods = BotUtilities.readFile("files/mods.txt");
			if (message.message.length() > 5) {
				if (!mods.contains(message.message.substring(5).toLowerCase())) {
					//Actually mod the person
					mods.add(message.message.substring(5).toLowerCase());
					twitchBot.send().message(message.channel, "Modding: " + message.message.substring(5).toLowerCase());
					Collections.sort(mods);
					BotUtilities.writeFile(mods, "files/mods.txt");
				} else {
					twitchBot.send().message(message.channel, "That person is already modded.");
				}
			}
		}
		if (message.message.contains("!unmod") && isOwner) {
			mods = BotUtilities.readFile("files/mods.txt");
			if (message.message.length() > 7) {
				if (mods.contains(message.message.substring(7).toLowerCase())) {
					//Actually mod the person
					mods.remove(message.message.substring(7).toLowerCase());
					twitchBot.send().message(message.channel, "Unmodding: " + message.message.substring(7).toLowerCase());
					Collections.sort(mods);
					BotUtilities.writeFile(mods, "files/mods.txt");
				} else {
					twitchBot.send().message(message.channel, "That person is not modded.");
				}
			}
		}

		/*
		 * HANDLE MUTING AND UNMUTING FOR EACH CHANNEL
		 */
		if (message.message.equals("!mute") && (message.sender.equalsIgnoreCase(message.channel.substring(1)) || isOwner || isMod)) {
			if (!BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).mute) {
				//Actually mute
				BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).mute = true;
				twitchBot.send().message(message.channel, "Muting");
				BotUtilities.saveChannels("files/channels.json", channelObjects);
			} else {
				twitchBot.send().message(message.channel, "Already muted");
			}
		}
		if (message.message.equals("!unmute") && (message.sender.equalsIgnoreCase(message.channel.substring(1)) || isOwner || isMod)) {
			if (BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).mute) {
				//Actually unmute
				BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).mute = false;
				twitchBot.send().message(message.channel, "Unmuting");
				BotUtilities.saveChannels("files/channels.json", channelObjects);
			} else {
				twitchBot.send().message(message.channel, "Already unmuted");
			}
		}

		/*
		 * HANDLE Dirty setting
		 */
		if (message.message.equals("!dirty") && (message.sender.equalsIgnoreCase(message.channel.substring(1)) || isOwner || isMod)) {
			if (!BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).dirty) {
				//Actually dirty
				BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).dirty = true;
				twitchBot.send().message(message.channel, "I am now dirty");
				BotUtilities.saveChannels("files/channels.json", channelObjects);
			} else {
				twitchBot.send().message(message.channel, "Already dirty.");
			}
		}
		if (message.message.equals("!undirty") && (message.sender.equalsIgnoreCase(message.channel.substring(1)) || isOwner || isMod)) {
			if (BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).dirty) {
				//Actually unmute
				BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).dirty = false;
				twitchBot.send().message(message.channel, "I am no longer dirty.");
				BotUtilities.saveChannels("files/channels.json", channelObjects);
			} else {
				twitchBot.send().message(message.channel, "Already not dirty.");
			}
		}

		/*
		 * HANDLE frequency settings
		 */
		if (message.message.contains("!set") && (message.sender.equalsIgnoreCase(message.channel.substring(1)) || isOwner || isMod)) {
			if (message.message.length() > 5) {
				//Attempts to set fequency, returns true if it worked
				if (BotUtilities.getChannelWithName(message.channel.toLowerCase(), channelObjects).setFrequency(message.message.substring(5).toLowerCase())) {

					//Already set frequency, just save channels
					twitchBot.send().message(message.channel, "Set frequency to: " + message.message.substring(5).toLowerCase());
					BotUtilities.saveChannels("files/channels.json", channelObjects);
				}
			} else {
				twitchBot.send().message(message.channel, "Unrecognize setting, type \"!help set\" for help.");
			}
		}


		//Handling ignored ppl:
		if (message.message.contains("!ignore") && (isOwner || isMod)) {
			ignores = BotUtilities.readFile("files/ignores.txt");
			if (message.message.length() > 8) {
				if (!ignores.contains(message.message.substring(8).toLowerCase())) {
					//Actually ignore the person
					ignores.add(message.message.substring(8).toLowerCase());
					twitchBot.send().message(message.channel, "Ignoring: " + message.message.substring(8).toLowerCase());
					Collections.sort(ignores);
					BotUtilities.writeFile(ignores, "files/ignores.txt");
				} else {
					twitchBot.send().message(message.channel, "That person is already ignored.");
				}
			}
		}
		if (message.message.contains("!unignore") && (isOwner || isMod)) {
			ignores = BotUtilities.readFile("files/ignores.txt");
			if (message.message.length() > 10) {
				if (ignores.contains(message.message.substring(10).toLowerCase())) {
					//Actually unignore the person
					ignores.remove(message.message.substring(10).toLowerCase());
					twitchBot.send().message(message.channel, "Unignoring: " + message.message.substring(10).toLowerCase());
					Collections.sort(ignores);
					BotUtilities.writeFile(ignores, "files/ignores.txt");
				} else {
					twitchBot.send().message(message.channel, "That person is not ignored.");
				}
			}
		}

		//Handling bad words:
		if (message.message.contains("!banword") && (isOwner || isMod)) {
			badWords = BotUtilities.readFile("files/badWords.txt");
			if (message.message.length() > 9) {
				if (!badWords.contains(message.message.substring(9).toLowerCase())) {
					//Actually ignore the person
					badWords.add(message.message.substring(9).toLowerCase());
					twitchBot.send().message(message.channel, "Banning word: " + message.message.substring(9).toLowerCase());
					Collections.sort(badWords);
					BotUtilities.writeFile(badWords, "files/badWords.txt");
					for (int i = 0; i < channelObjects.size(); i++) {
						channelObjects.get(i).mega.updateFiles();
					}
				} else {
					twitchBot.send().message(message.channel, "That word is already banned.");
				}
			}
		}
		if (message.message.contains("!unbanword") && (isOwner || isMod)) {
			badWords = BotUtilities.readFile("files/badWords.txt");
			if (message.message.length() > 11) {
				if (badWords.contains(message.message.substring(11).toLowerCase())) {
					//Actually unignore the person
					badWords.remove(message.message.substring(11).toLowerCase());
					twitchBot.send().message(message.channel, "Unbanning word: " + message.message.substring(11).toLowerCase());
					Collections.sort(badWords);
					BotUtilities.writeFile(badWords, "files/badWords.txt");
					for (int i = 0; i < channelObjects.size(); i++) {
						channelObjects.get(i).mega.updateFiles();
					}
				} else {
					twitchBot.send().message(message.channel, "That word is already unbanned.");
				}
			}
		}
		
				//Handling ban fits words:
				if (message.message.contains("!banfits") && (isOwner || isMod)) {
					banFits = BotUtilities.readFile("files/banFits.txt");
					if (message.message.length() > 9) {
						if (!banFits.contains(message.message.substring(9).toLowerCase())) {
							//Actually ignore the person
							banFits.add(message.message.substring(9).toLowerCase());
							twitchBot.send().message(message.channel, "Banning fits word: " + message.message.substring(9).toLowerCase());
							Collections.sort(banFits);
							BotUtilities.writeFile(banFits, "files/banFits.txt");
							for (int i = 0; i < channelObjects.size(); i++) {
								channelObjects.get(i).mega.updateFiles();
								channelObjects.get(i).megaDirty.updateFiles();
							}
						} else {
							twitchBot.send().message(message.channel, "That word is already banned.");
						}
					}
				}
				if (message.message.contains("!unbanfits") && (isOwner || isMod)) {
					banFits = BotUtilities.readFile("files/banFits.txt");
					if (message.message.length() > 11) {
						if (banFits.contains(message.message.substring(11).toLowerCase())) {
							//Actually unignore the person
							banFits.remove(message.message.substring(11).toLowerCase());
							twitchBot.send().message(message.channel, "Unbanning fits word: " + message.message.substring(11).toLowerCase());
							Collections.sort(banFits);
							BotUtilities.writeFile(banFits, "files/banFits.txt");
							for (int i = 0; i < channelObjects.size(); i++) {
								channelObjects.get(i).mega.updateFiles();
								channelObjects.get(i).megaDirty.updateFiles();
							}
						} else {
							twitchBot.send().message(message.channel, "That word is already unbanned.");
						}
					}
				}

		//Handling spell check words:
		if (message.message.contains("!spellignore") && (isOwner || isMod)) {
			spellIgnores = BotUtilities.readFile("files/spellIgnores.txt");
			if (message.message.length() > 13) {
				if (!spellIgnores.contains(message.message.substring(13).toLowerCase())) {
					//Actually ignore the person
					spellIgnores.add(message.message.substring(13).toLowerCase());
					twitchBot.send().message(message.channel, "Ignoring Spelling Check: " + message.message.substring(13).toLowerCase());
					Collections.sort(spellIgnores);
					BotUtilities.writeFile(spellIgnores, "files/spellIgnores.txt");
					for (int i = 0; i < channelObjects.size(); i++) {
						channelObjects.get(i).mega.updateFiles();
						channelObjects.get(i).megaDirty.updateFiles();
						channelObjects.get(i).mega.wordIgnore(message.message.substring(13).toLowerCase(), true);
						channelObjects.get(i).megaDirty.wordIgnore(message.message.substring(13).toLowerCase(), true);
					}
				} else {
					twitchBot.send().message(message.channel, "That word is already spell Ignored.");
				}
			}
		}
		if (message.message.contains("!unspellignore") && (isOwner || isMod)) {
			spellIgnores = BotUtilities.readFile("files/spellIgnores.txt");
			if (message.message.length() > 15) {
				if (spellIgnores.contains(message.message.substring(15).toLowerCase())) {
					//Actually unignore the person
					spellIgnores.remove(message.message.substring(15).toLowerCase());
					twitchBot.send().message(message.channel, "Unignoring Spelling Check: " + message.message.substring(15).toLowerCase());
					Collections.sort(spellIgnores);
					BotUtilities.writeFile(spellIgnores, "files/spellIgnores.txt");
					for (int i = 0; i < channelObjects.size(); i++) {
						channelObjects.get(i).mega.updateFiles();
						channelObjects.get(i).megaDirty.updateFiles();
						channelObjects.get(i).mega.wordIgnore(message.message.substring(15).toLowerCase(), false);
						channelObjects.get(i).megaDirty.wordIgnore(message.message.substring(15).toLowerCase(), false);
					}
				} else {
					twitchBot.send().message(message.channel, "That word is already not spell Ignored.");
				}
			}
		}

		/*
		 * Handle frequencies:
		 */

	}


	//Function for getting an answer to FITS:
	public String getAnswer(Channel channel) {
		String answer = "";
		boolean done = false;
		Set banFitsTemp = null;
		try {
			banFitsTemp = Utils.readStringSetFromFile("files/badWords.txt", true);
		} catch (IOException e) {
			System.out.println("Answer Read Issue");
			return "";
		}
		while (done == false) {
			done = true;
			MegaHAL currentMega = (channel.dirty?channel.mega:channel.megaDirty);
			String tempSentence = currentMega.formulateReply("", "");
			tempSentence = BotUtilities.removeSymbols(tempSentence);
			tempSentence = BotUtilities.removeStrings(tempSentence, "smallWords.txt", true);
			List<Symbol> newTriggers = currentMega.splitter.split(tempSentence);
			int randomNumber = rng.nextInt(newTriggers.size()-1);
			answer = newTriggers.get(randomNumber).toString();

			for (int i = 0; i < lastWords.size(); i++) {
				if (answer.equalsIgnoreCase(lastWords.get(i))) {
					done = false;
				}
			}
			if (answer.length() < 3) {
				done = false;
			}
			if (answer.equals("<START>") || answer.equals("<END>")) {
				done = false;
			}
			if (Utils.checkBadWords(banFitsTemp, null, answer)) {
				done = false;
			}
		}
		System.out.println(answer);
		return answer;
	}

	//Function for writing users to disk


	//Function for reading users from disk
}