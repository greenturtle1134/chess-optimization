package chessprogram;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import chessprogram.Tournament.State;

public class ColorScheme {
	private Map<State, Color> fillColor = new HashMap<State, Color>();
	private Map<State, Color> textColor = new HashMap<State, Color>();
	private Map<State, String> text = new HashMap<State, String>();
	
	public ColorScheme(Properties properties) {
		for(State state : State.values()) {
			
		}
	}
	
	public Color getFill(State state) {
		return fillColor.get(state);
	}
	
	public Color getTextColor(State state) {
		return textColor.get(state);
	}
	
	public String getText(State state) {
		return text.get(state);
	}
}
