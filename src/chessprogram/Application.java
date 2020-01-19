package chessprogram;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jgrapht.alg.util.Pair;

import chessprogram.Tournament.State;

public class Application {
	
	public static final String PREFIX = "tournament_";
	public static final String SUFFIX = ".txt";
	
	private GameGridPanel grid;
	private Tournament tournament;
	private Pair<Integer, Integer> suggestion;
	private JLabel output;
	private JButton yes, no;
	private JFrame frame;

	public static void main(String[] args) {
		new Application();
	}

	public Application() {
		frame = new JFrame();
		this.changeFile();
	}
	
	public void changeFile() {
		File here = new File(".");
		
		String[] options = Stream.concat(
				Stream.of("Create new..."),
				Arrays.stream(here.listFiles())
				.filter(file -> file.isFile())
				.map(file -> file.getName())
				.filter(file -> file.length()>PREFIX.length() 
						&& file.substring(0, PREFIX.length()).equalsIgnoreCase(PREFIX) 
						&& file.substring(file.length()-SUFFIX.length()).equals(SUFFIX))
				.map(file -> file.substring(PREFIX.length(), file.length()-SUFFIX.length())))
				.toArray(String[]::new);
		int choice = JOptionPane.showOptionDialog(null, "Which file to open?", "Open file", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
		if(choice == -1) {
			System.exit(0);
		}
		if(choice == 0) {
			this.tournament = new Tournament(JOptionPane.showInputDialog("Enter name:"), JOptionPane.showInputDialog("Enter players, separate with commas:").split(", *"));
		}
		else {
			try {
				this.tournament = Tournament.read(PREFIX+options[choice]+SUFFIX);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Error opening file: "+PREFIX+options[choice]+SUFFIX+"\n"+e.getLocalizedMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		frame.setTitle("Tournament Assist - "+tournament.getName());
		
		JPanel panel = new JPanel(new BorderLayout());
		
		grid = new GameGridPanel(tournament, this);
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
				tournament.regenerateGraph();
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

	public void makeSuggestion() {
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

	private class BooleanCallback implements ActionListener {
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
