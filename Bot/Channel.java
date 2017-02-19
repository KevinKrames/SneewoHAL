package Bot;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import MegaHAL.MegaHAL;

//Class to hold date for individual channels
public class Channel {
	public String name, frequency, answer;
	public boolean mute, dirty;
	public Long fitsTime, fitsState;

	public MegaHAL mega, megaDirty;
	
	public Channel() {
		
	}
	
	/*
	 * default load of a channel with a name
	 */
	public Channel(String channelName) {
		this.name = channelName;
		this.frequency = "medium";
		this.mute = false;
		this.dirty = false;
		this.fitsTime = (long) 0;
		this.fitsState = (long) 0;
	}
	
	/*
	 * load the channel with a json object
	 */
	public Channel(JSONObject obj) {
		if (obj != null) {
			//We need to init this channel:
			this.name = (String) obj.get("name");
			this.frequency = (String) obj.get("frequency");
			this.mute = (boolean) obj.get("mute");
			this.dirty = (boolean) obj.get("dirty");
			this.fitsTime = (long) 0;
			this.fitsState = (long) 0;
			this.answer = "";
		}
	}
	/*
	 * converts this object to a json file and returns it
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("frequency", frequency);
		obj.put("mute", mute);
		obj.put("dirty", dirty);
		
		return obj;
	}
	
	public boolean setFrequency(String freq) {
		if (freq.equalsIgnoreCase("very low") ||
				freq.equalsIgnoreCase("low") ||
				freq.equalsIgnoreCase("medium") ||
				freq.equalsIgnoreCase("high") ||
				freq.equalsIgnoreCase("very high")) {
			frequency = freq;
			return true;
		}
		return false;
	}
}
