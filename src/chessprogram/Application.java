package chessprogram;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jgrapht.alg.util.Pair;

import chessprogram.Tournament.State;

public class Application {
	
	private static GameGridPanel grid;
	private static Tournament tournament;
	private static Pair<Integer, Integer> suggestion;
	private static JLabel output;
	private static JButton yes, no;

	public static void main(String[] args) {
		String[] names = {"A", "B", "C", "D", "E", "F", "G"};
		tournament = new Tournament("Test", names);
		
		JFrame frame = new JFrame("Tournament Assist - "+tournament.getName());
		JPanel panel = new JPanel(new BorderLayout());
		
		grid = new GameGridPanel(tournament);
		panel.add(grid, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		output = new JLabel("Initializing...");
		output.setBorder(BorderFactory.createTitledBorder("Optimal pairing:"));
		bottomPanel.add(output, BorderLayout.CENTER);
		
		JPanel choices = new JPanel(new GridLayout(3,1));
		yes = new JButton("Accept");
		no = new JButton("Reject");
		JButton update = new JButton("Update");
		yes.addActionListener(new BooleanCallback(true));
		no.addActionListener(new BooleanCallback(false));
		update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				makeSuggestion();
			}
		});
		choices.add(yes);
		choices.add(no);
		choices.add(update);
		bottomPanel.add(choices, BorderLayout.EAST);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(panel);
		
		makeSuggestion();
	}
	
	public static void makeSuggestion() {
		suggestion = tournament.nextPair();
		if(suggestion != null) {
			String[] players = tournament.getPlayers();
			output.setText("Have "+players[suggestion.getFirst()]+" and "+players[suggestion.getSecond()]+" play.");
			grid.setHighlight(suggestion.getFirst(), suggestion.getSecond());
			yes.setEnabled(true);
			no.setEnabled(true);
		}
		else {
			output.setText("No matches possible.");
			grid.removeHighlight();
			yes.setEnabled(false);
			no.setEnabled(false);
		}
	}

	private static class BooleanCallback implements ActionListener {
		private boolean value;
		
		private BooleanCallback(boolean value) {
			this.value = value;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			State state;
			if(value) {
				state = State.PLAYING;
			}
			else {
				state = State.UNPLAYABLE;
			}
			tournament.setResult(suggestion.getFirst(), suggestion.getSecond(), state);
			grid.update(suggestion.getSecond(), suggestion.getFirst());
			grid.update(suggestion.getFirst(), suggestion.getSecond());
			makeSuggestion();
		}
		
	}
	
}
