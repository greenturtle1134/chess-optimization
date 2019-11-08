package chessprogram;

import java.util.ArrayList;
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
	 * A list of all players
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
			for(int j = i+1; j<players.length; j++) {
				unplayed.addEdge(i, j);
			}
			this.results = new State[players.length][players.length];
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
	
	public void markState(int source, int target, State state) {
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
		for(int i = 0; i<results[i].length; i++) {
			if(present[i]&&results[player][i].equals(State.UNPLAYED)) {
				unplayed.addEdge(player, i);
			}
		}
	}
	
	public void disablePlayer(int player) {
		unplayed.removeVertex(player);
	}
	
	public static enum State {
		WON, LOST, DRAW, UNPLAYED, PLAYING, UNPLAYABLE, N_A
	}
}
