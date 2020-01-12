package chessprogram;

import static java.awt.Color.BLACK;
import static java.awt.Color.CYAN;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.YELLOW;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.EdmondsMaximumCardinalityMatching;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Tournament {
	/**
	 * A list of all players by ID
	 */
	private List<String> players = new ArrayList<String>();
	/**
	 * A map indicating the ID for each player
	 */
	private Map<String, Integer> ID = new HashMap<String, Integer>();
	/**
	 * A graph of all possible assignments
	 */
	private Graph<Integer, DefaultEdge> unplayed = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	/**
	 * A table of results
	 * Won/Lost applies to first index
	 */
	private State[][] results;
	/**
	 * Attendance
	 */
	private boolean[] present;
	/**
	 * A name for saving and stuff
	 */
	private String name;
	/**
	 * The panel display this tournament's results
	 */
	private GameGridPanel panel = null;
	
	public Tournament(String name, String[] players) {
		this.name = name;
		for(int i = 0; i<players.length; i++) {
			this.players.add(players[i]);
			this.ID.put(players[i], i);
			this.unplayed.addVertex(i);
			for(int j = 0; j<i; j++) {
				unplayed.addEdge(i, j);
			}
		}
		this.results = new State[players.length][players.length];
		for(State[] array : results) {
			Arrays.fill(array, State.UNPLAYED);
		}
		this.present = new boolean[players.length];
		Arrays.fill(present, true);
		for(int i = 0; i<players.length; i++) {
			this.setResult(i, i, State.N_A);
		}
	}
	
	public Pair<Integer, Integer> nextPair() {
		EdmondsMaximumCardinalityMatching<Integer, DefaultEdge> a = new EdmondsMaximumCardinalityMatching<Integer, DefaultEdge>(unplayed);
		Matching<Integer, DefaultEdge> matching = a.getMatching();
		if(!matching.getEdges().isEmpty()) {
			DefaultEdge edge = matching.getEdges().iterator().next();
			return new Pair<Integer, Integer>(unplayed.getEdgeSource(edge), unplayed.getEdgeTarget(edge));
		}
		else {
			return null;
		}
	}
	
	public void setResult(int source, int target, State state) {
		results[source][target] = state;
		results[target][source] = state.invert();
		if(state.equals(State.PLAYING)) {
			disablePlayer(source);
			disablePlayer(target);
		}
		else {
			if(present[source]) {
				enablePlayer(source);
			}
			if(present[target]) {
				enablePlayer(target);
			}
		}
		if(present[source]&&present[target]) {
			if(state.equals(State.UNPLAYED)) {
				unplayed.addEdge(source, target);
			}
			else {
				unplayed.removeEdge(source, target);
			}
		}
	}
	
	private void enablePlayer(int player) {
		unplayed.addVertex(player);
		for(int i = 0; i<results[player].length; i++) {
			if(i!=player&&present[i]&&results[player][i].equals(State.UNPLAYED)&&unplayed.containsVertex(i)) {
				unplayed.addEdge(player, i);
			}
		}
	}
	
	private void disablePlayer(int player) {
		unplayed.removeVertex(player);
	}
	
	public void setAttend(int player, boolean value) {
		if(value != present[player]) {
			present[player] = value;
			if(value) {
				enablePlayer(player);
			}
			else {
				disablePlayer(player);
			}
		}
	}
	
	public int getScore(int player) {
		int score = 0;
		for(int i = 0; i<results[0].length; i++) {
			if(results[player][i].equals(State.WON)) {
				score += 2;
			}
			if(results[player][i].equals(State.DRAW)) {
				score += 1;
			}
		}
		return score;
	}
	
	/**
	 * Regenerate the graph based on the results matrix. All methods maintain the graph; called when reading a results table.
	 */
	public void regenerateGraph() {
		unplayed = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		for(int i = 0; i<results.length; i++) {
			this.unplayed.addVertex(i);
			for(int j = 0; j<i; j++) {
				if(results[i][j].equals(State.UNPLAYED)) {
					unplayed.addEdge(i, j);
				}
			}
		}
	}
	
	public static Tournament read(String file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String name = in.readLine();
		String[] players = in.readLine().split(", ");
		Tournament tournament = new Tournament(name, players);
		for(int i = 0; i<players.length; i++) {
			String[] resultLine = in.readLine().split(" ");
			for(int j = 0; j<players.length; j++) {
				tournament.results[i][j] = State.getState(resultLine[j]);
			}
		}
		tournament.regenerateGraph();
		in.close();
		return tournament;
	}
	
	public void write(String file) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		out.println(name);
		out.println(String.join(", ", players));
		for(int i = 0; i<results.length; i++) {
			if(i>0) {
				out.println();
			}
			for(int j = 0; j<results.length; j++) {
				if(j>0) {
					out.print(" ");
				}
				out.print(results[i][j].getWrite());
			}
		}
		out.close();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getPlayers() {
		return players.toArray(new String[0]);
	}

	public State getResult(int source, int target) {
		return results[source][target];
	}
	
	public boolean isAvailable(int player) {
		return unplayed.containsVertex(player);
	}
	
	public static enum State {

		WON("WON","W", GREEN),
		LOST("LOST","L", GREEN),
		DRAW("DRAW","D", GREEN),
		UNPLAYED("·",".", YELLOW),
		PLAYING("?",".", CYAN),
		UNPLAYABLE("X",".", RED),
		N_A("X","X", BLACK);
		
		private final String name;
		private final String write;
		private final Color color;
		
		private static Map<String, State> symbols = new HashMap<String, State>();
		static {
			symbols.put("W", WON);
			symbols.put("L", LOST);
			symbols.put("D", DRAW);
			symbols.put(".", UNPLAYED);
			symbols.put("X", N_A);
		}
		private static Map<State, State> inverse = new HashMap<State, State>();
		static {
			inverse.put(WON, LOST);
			inverse.put(LOST, WON);
			inverse.put(DRAW, DRAW);
			inverse.put(UNPLAYED, UNPLAYED);
			inverse.put(PLAYING, PLAYING);
			inverse.put(UNPLAYABLE, UNPLAYABLE);
			inverse.put(N_A, N_A);
		}
		
		State(String name, String write, Color color) {
			this.color = color;
			this.name = name;
			this.write = write;
		}

		public String getName() {
			return name;
		}

		public String getWrite() {
			return write;
		}

		public Color getColor() {
			return color;
		}
		
		public State invert() {
			return inverse.get(this);
		}
		
		public static State getState(String symbol) {
			return symbols.get(symbol);
		}
	}
	
	public boolean isPresent(int player) {
		return present[player];
	}
}
