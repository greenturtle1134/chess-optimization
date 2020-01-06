package chessprogram;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameGridPanel extends JPanel {
	private Tournament tournament;
	private JLabel[][] labels;
	public GameGridPanel(Tournament tournament) {
		this.tournament = tournament;
		String[] players = tournament.getPlayers();
		this.setLayout(new GridLayout(players.length+1, players.length+2));
		labels = new JLabel[players.length][players.length+2];
		this.add(new JLabel(""));
		for(String s : players) {
			this.add(new JLabel(s));
		}
		this.add(new JLabel("SCORE:"));
		for(int i = 0; i<labels.length; i++) {
			this.add(new JLabel(players[i]));
			for(int j = 0; j<labels.length; j++) {
				labels[i][j] = new JLabel(tournament.getResult(i, j).toString());
				
			}
		}
	}
}
