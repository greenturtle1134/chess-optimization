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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import chessprogram.Tournament.State;

public class GameGridPanel extends JPanel {
	private Tournament tournament;
	private JLabel[][] labels;
	private JLabel[] scores;
	private JButton[] buttons;
	private int highX = 0;
	private int highY = 0;
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
	}
	
	public void update(int i, int j) {
		setLabel(i, j, tournament.getResult(i, j));
		scores[i].setText(tournament.getScore(i)+"");
	}
	
	private void setLabel(int i, int j, State state) {
		labels[i][j].setText(state.getSymbol());
		labels[i][j].setBackground(state.getColor());
	}
	
	public void setHighlight(int x, int y) {
		removeHighlight();
		highX = x;
		highY = y;
		labels[x][y].setBorder(BorderFactory.createLineBorder(Color.BLUE));
	}
	
	public void removeHighlight() {
		labels[highX][highY].setBorder(BorderFactory.createEmptyBorder());
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
			if(i!=GameGridPanel.this.highX||j!=GameGridPanel.this.highY) {
				GameGridPanel.this.labels[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			if(i!=GameGridPanel.this.highX||j!=GameGridPanel.this.highY) {
				GameGridPanel.this.labels[i][j].setBorder(BorderFactory.createEmptyBorder());
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			State result = GameGridPanel.this.tournament.getResult(i, j);
			if(result.equals(State.PLAYING)||result.equals(State.DRAW)||result.equals(State.WON)||result.equals(State.LOST)) {
				String[] players = GameGridPanel.this.tournament.getPlayers();
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
			if(result.equals(State.UNPLAYED)) {
				GameGridPanel.this.tournament.setResult(i, j, State.PLAYING);
			}
			GameGridPanel.this.update(i, j);
			GameGridPanel.this.update(j, i);
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("");
		String[] names = {"A", "B", "C", "D", "E", "F", "G"};
		frame.setContentPane(new GameGridPanel(new Tournament("Test", names)));
		frame.setSize(400, 400);
		frame.setVisible(true);
	}
}
