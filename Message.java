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

//Class to hold the message data from different clients
public class Message {
	//Instantiate variables
	public String message, sender, channel, type = "";
	public boolean isMod, isOwner = false;
	public long time;
	
	//Message constructor
	public Message() {
	}

	//Message constructor
	public Message(String message, String sender, String channel, String type, long time, boolean isMod, boolean isOwner) {
		this.message = message;
		this.sender = sender;
		this.channel = channel;
		this.type = type;
		this.isMod = isMod;
		this.isOwner = isOwner;
		this.time = time;
	}
}