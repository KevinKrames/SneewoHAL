package Bot;
import java.util.Iterator;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//Static class for global functions to be referenced throughout the project
public class BotUtilities {
	//Function to read a file from the working program directory, returns an ArrayList
	public static ArrayList<String> readFile(String fileName) {
		ArrayList<String> newString = new ArrayList<String>();
		int counter = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/" + fileName)))
		{

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				newString.add(counter, sCurrentLine);
				counter++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} 

		return newString;
	} // end of read file

	//Function to write a text file of an ArrayList of a string to a file on each new line
	public static void writeFile(ArrayList<String> s, String fileName) {
		String input = "";
		for (int i=0; i < s.size(); i++) {
			input = input + s.get(i) + System.getProperty("line.separator");
		}

		try {
			File file = new File(System.getProperty("user.dir") + "/" + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(input);
			output.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}// end of writefile

	public static void addLineToFile(String line, String fileName) {

		try {
			File file = new File(System.getProperty("user.dir") + "/" + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file, true));
			output.append(line + System.getProperty("line.separator"));
			output.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}// end of writefile

	/*
	 * Reads a json file
	 */

	public static JSONObject readObjectFromFile(String fileName) {
		try {
			//Get the object
			JSONParser parser = new JSONParser();

			FileReader reader = new FileReader(System.getProperty("user.dir") + "/" + fileName);
			Object obj = parser.parse(reader);
			return (JSONObject)obj;

		} catch (FileNotFoundException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} catch (ParseException e) {

			e.printStackTrace();

		}
		return null;
	}

	public static ArrayList<Channel> initChannels(JSONObject obj) {
		ArrayList<Channel> channels = new ArrayList<Channel>();

		JSONArray channelsList = (JSONArray) obj.get("channelsList");
		Iterator<JSONObject> iterator = channelsList.iterator();
		JSONObject temp;
		Channel chan;

		while (iterator.hasNext()) {
			temp = iterator.next();
			chan = new Channel(temp);
			channels.add(chan);
		}

		return channels;
	}

	/*
	 * converts to a json array and saves it to the given file:
	 */
	@SuppressWarnings("unchecked")
	public static boolean saveChannels(String fileName, ArrayList<Channel> channels) {
		JSONArray array = new JSONArray();
		for (int i = 0; i < channels.size(); i++) {
			array.add(channels.get(i).toJSON());
		}

		JSONObject obj = new JSONObject();
		obj.put("channelsList", array);
		writeObjectToFile(fileName, obj);

		return true;
	}

	public static void writeObjectToFile(String fileName, JSONObject obj) {
		try (FileWriter file = new FileWriter(System.getProperty("user.dir") + "/" + fileName)){
			file.write(obj.toJSONString());
			System.out.println("Saved Object to " + fileName);
		} catch (Exception E) {

		}
	}

	/*
	 * Finds an associated channel from an array list
	 */
	public static Channel getChannelWithName(String name, ArrayList<Channel> list) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).name.equalsIgnoreCase(name)) {
				return list.get(i);
			}
		}
		return null;
	}

	public static String removeStrings(String data, String file, boolean spaces) {

		ArrayList<String> tempArray = readFile("files/" + file);
		if (spaces) {
			for (int i = 0; i < tempArray.size()-1; i++) {

				for (int j = 0; j < (data.length() - tempArray.get(i).length() - 2); j++) {
					if ((" " + tempArray.get(i) + " ").equalsIgnoreCase(data.substring(j, j+tempArray.get(i).length()+2))) {
						data = data.substring(0, j+1) + data.substring(j+2+tempArray.get(i).length());
					}
				}
				if (data.length() >= tempArray.get(i).length()+1) {
					if ((tempArray.get(i) + " ").equalsIgnoreCase(data.substring(0, tempArray.get(i).length()+1))) {
						data = data.substring(tempArray.get(i).length()+1);
					}
				}
				if (data.length() >= tempArray.get(i).length()+1) {
					if ((" " + tempArray.get(i)).equalsIgnoreCase(data.substring(data.length() - (tempArray.get(i).length()+1), data.length()))) {
						data = data.substring(data.length() - (tempArray.get(i).length()+1));
					}
				}
			}
		}

		return data;
	}

	public static String removeSymbols(String data) {
		for (int i = 0; i < data.length(); i++) {
			switch (data.charAt(i)) {
			case ',':
			case '.':
			case ';':
			case '/':
			case '?':
			case ':':
			case '\"':
			case '\'':
			case '\\':
			case '{':
			case '}':
			case '[':
			case ']':
			case '+':
			case '=':
			case '-':
			case '_':
			case '~':
			case '`':
			case '#':
			case '$':
			case '%':
			case '^':
			case '&':
			case '*':
			case '(':
			case ')':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '0':
			data = data.substring(0, i) + data.substring(i+1);
			
			break;
			default:
			break;
			}
		}
		return data;
	}

	public static String addUnderscores(String a, String b) {
		if (a == null || b == null) {
			return null;
		}
		String u = "_";
		for (int i = 1; i < b.length(); i++) {
			u = u + " _";
		}
		a = a.replaceAll(b, u);
		return a;
	}

	public static ArrayList<String> getValues(ArrayList<String> names, ArrayList<String> array, String player) {
		ArrayList<String> values = new ArrayList<String>();
		values.add("" + names.size());
		values.add("0");
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).equalsIgnoreCase(player)) {
				values.set(0, "" + (i+1));
				values.set(1, array.get(i));
			}
		}
		return values;
	}
	
	public static void addOne(ArrayList<String> names, ArrayList<String> array, String player, int amount) {
		boolean exists = false;
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).equalsIgnoreCase(player)) {
				exists = true;
				int temp = Integer.parseInt(array.get(i)) + amount;
				array.set(i, ""+temp);
				i = names.size();
			}
		}
		if (exists == false) {
			names.add(player);
			array.add("1");
		}
		sortAL(names,array);
	}
	
	public static void sortAL(ArrayList<String> names, ArrayList<String> array) {
		boolean done = false;
		while (done == false) {
			done = true;
		for (int i = 0; i < names.size()-1; i++) {
			if (Integer.parseInt(array.get(i)) < Integer.parseInt(array.get(i+1))) {
				done = false;
				String temp = names.get(i);
				String temp2 = array.get(i);
				
				names.set(i, names.get(i+1));
				array.set(i, array.get(i+1));

				names.set(i+1, temp);
				array.set(i+1, temp2);
			}
		}

		}
		int i = 0;
	}
}