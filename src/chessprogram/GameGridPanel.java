package chessprogram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import chessprogram.Tournament.State;

public class GameGridPanel extends JPanel {
	private static final Border BLUE_BORDER = BorderFactory.createLineBorder(Color.BLUE, 3);
	private static final Border BLACK_BORDER = BorderFactory.createLineBorder(Color.BLACK);
	private static final Border NO_BORDER = BorderFactory.createEmptyBorder();
	
	private Tournament tournament;
	private JLabel[][] labels;
	private JLabel[] scores;
	private JButton[] buttons;
	private int highX = 0;
	private int highY = 0;
	private ColorScheme scheme;
	public GameGridPanel(Tournament tournament) {
		this.tournament = tournament;
		String[] players = tournament.getPlayers();
		JPanel center = new JPanel(new GridLayout(players.length+1, players.length+1));
		JPanel side = new JPanel(new GridLayout(players.length+1, 3));
		labels = new JLabel[players.length][players.length];
		scores = new JLabel[players.length];
		buttons = new JButton[players.length];
		center.add(new JLabel(""));
		for(String s : players) {
			JLabel label = new JLabel(s);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			center.add(label);
		}
		side.add(new JLabel(""));
		side.add(new JLabel(""));
		side.add(new JLabel(""));
		for(int i = 0; i<labels.length; i++) {
			JLabel label = new JLabel(players[i]);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			center.add(label);
			scores[i] = new JLabel(tournament.getScore(i)+"");
			for(int j = 0; j<labels.length; j++) {
				labels[i][j] = new JLabel();
				labels[i][j].setOpaque(true);
				update(i, j);
				labels[i][j].setHorizontalAlignment(SwingConstants.CENTER);
				labels[i][j].addMouseListener(new MouseListener(i, j));
				center.add(labels[i][j]);
			}
			side.add(new JLabel("Score:"));
			side.add(scores[i]);
			buttons[i] = new JButton("Toggle");
			buttons[i].addActionListener(new ToggleListener(i));
			side.add(buttons[i]);
		}
		
		this.setLayout(new BorderLayout());
		this.add(center, BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
		
		this.scheme = new ColorScheme();
	}
	
	public void update(int i, int j) {
		setLabel(i, j, tournament.getResult(i, j));
		scores[i].setText(tournament.getScore(i)+"");
	}
	
	private void setLabel(int i, int j, State state) {
		labels[i][j].setText(scheme.getText(state));
		labels[i][j].setForeground(scheme.getTextColor(state));
		labels[i][j].setBackground(scheme.getFill(state));
	}
	
	public void setHighlight(int x, int y) {
		removeHighlight();
		highX = x;
		highY = y;
		labels[x][y].setBorder(BLUE_BORDER);
	}
	
	public void removeHighlight() {
		labels[highX][highY].setBorder(NO_BORDER);
	}

	private class ToggleListener implements ActionListener {
		private int i;
		
		private ToggleListener(int i) {
			this.i = i;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(GameGridPanel.this.tournament.isPresent(i)) {
				GameGridPanel.this.tournament.setAttend(i, false);
				for(int j = 0; j<GameGridPanel.this.labels[i].length; j++) {
					setLabel(i, j, State.UNPLAYABLE);
				}
			}
			else {
				GameGridPanel.this.tournament.setAttend(i, true);
				for(int j = 0; j<GameGridPanel.this.labels[i].length; j++) {
					update(i, j);
				}
			}
			Application.makeSuggestion();
		}
	}
	
	private class MouseListener extends MouseAdapter {
		int i, j;
		
		private MouseListener(int i, int j) {
			this.i = i;
			this.j = j;
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			GameGridPanel.this.labels[i][j].setBorder(BLACK_BORDER);
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			if(i!=GameGridPanel.this.highX||j!=GameGridPanel.this.highY) {
				GameGridPanel.this.labels[i][j].setBorder(NO_BORDER);
			}
			else {
				GameGridPanel.this.labels[i][j].setBorder(BLUE_BORDER);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			State result = GameGridPanel.this.tournament.getResult(i, j);
			String[] players = GameGridPanel.this.tournament.getPlayers();
			if(result.equals(State.PLAYING)||result.equals(State.DRAW)||result.equals(State.WON)||result.equals(State.LOST)) {
				String[] options = {players[i], "Draw", players[j], "Clear cell"};
				int choice = JOptionPane.showOptionDialog(null, "Who won?", "Entering result: "+players[i]+" v.s. "+players[j], JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
				switch(choice) {
				case 0:
					GameGridPanel.this.tournament.setResult(i, j, State.WON);
					break;
				case 1:
					GameGridPanel.this.tournament.setResult(i, j, State.DRAW);
					break;
				case 2:
					GameGridPanel.this.tournament.setResult(i, j, State.LOST);
					break;
				case 3:
					GameGridPanel.this.tournament.setResult(i, j, State.UNPLAYED);
					break;
				}
			}
			if(result.equals(State.UNPLAYED)&&GameGridPanel.this.tournament.isAvailable(i)&&GameGridPanel.this.tournament.isAvailable(j)) {
				if(JOptionPane.showConfirmDialog(null, "Game in progress between "+players[i]+" and "+players[j]+"?", "Confirming update", JOptionPane.YES_NO_OPTION) == 0) {
					GameGridPanel.this.tournament.setResult(i, j, State.PLAYING);
				}
			}
			if(result.equals(State.UNPLAYABLE)) {
				GameGridPanel.this.tournament.setResult(i, j, State.UNPLAYED);
			}
			GameGridPanel.this.update(i, j);
			GameGridPanel.this.update(j, i);
			Application.makeSuggestion();
		}
	}
}
