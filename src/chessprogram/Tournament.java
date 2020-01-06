package chessprogram;

import java.io.BufferedReader;
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
		if(state.equals(State.WON)) {
			results[target][source] = State.LOST;
		}
		if(state.equals(State.LOST)) {
			results[target][source] = State.WON;
		}
		if(state.equals(State.DRAW)) {
			results[target][source] = State.DRAW;
		}
		if(state.equals(State.UNPLAYED)) {
			unplayed.addEdge(source, target);
		}
		else {
			unplayed.removeEdge(source, target);
		}
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
	}
	
	public void enablePlayer(int player) {
		unplayed.addVertex(player);
		for(int i = 0; i<results[player].length; i++) {
			if(i!=player&&present[i]&&results[player][i].equals(State.UNPLAYED)) {
				unplayed.addEdge(player, i);
			}
		}
	}
	
	public void disablePlayer(int player) {
		unplayed.removeVertex(player);
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
	
	public static Tournament read(BufferedReader in) throws IOException {
		String name = in.readLine();
		String[] players = in.readLine().split(" ");
		Tournament tournament = new Tournament(name, players);
		return tournament;
	}
	
	public void write(PrintWriter out) {
		out.println(name);
		out.println(String.join(", ", players));
		for(int i = 0; i<results.length; i++) {
			
		}
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
	
	public static enum State {

		WON{
			public String symbol() {
				return "W";
			}
			public String writeAs() {
				return "W";
			}
		},
		LOST{
			public String symbol() {
				return "L";
			}
			public String writeAs() {
				return "L";
			}
		},
		DRAW{
			public String symbol() {
				return "D";
			}
			public String writeAs() {
				return "D";
			}
		},
		UNPLAYED{
			public String symbol() {
				return ".";
			}
			public String writeAs() {
				return ".";
			}
		},
		PLAYING{
			public String symbol() {
				return "?";
			}
			public String writeAs() {
				return ".";
			}
		},
		UNPLAYABLE{
			public String symbol() {
				return "-";
			}
			public String writeAs() {
				return ".";
			}
		},
		N_A{
			public String symbol() {
				return "X";
			}
			public String writeAs() {
				return "X";
			}
		}
	}
}
