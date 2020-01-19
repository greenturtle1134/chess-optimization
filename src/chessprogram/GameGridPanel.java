package chessprogram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import chessprogram.Tournament.State;
import static chessprogram.Tournament.State.*;

public class GameGridPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final Border SUGGEST_BORDER = BorderFactory.createDashedBorder(Color.BLUE, 3, 5, 2, false);
	private static final Border SELECT_BORDER = BorderFactory.createLineBorder(Color.BLACK);
	private static final Border SUGGEST_SELECT_BORDER = BorderFactory.createLineBorder(Color.BLUE, 3);
	private static final Border UNSELECTABLE_BORDER = BorderFactory.createLineBorder(Color.RED);
	private static final Border NO_BORDER = BorderFactory.createEmptyBorder();
	
	private Tournament tournament;
	private JLabel[][] labels;
	private JLabel[] scores;
	private JButton[] buttons;
	private int highX = 0;
	private int highY = 0;
	private Application application;
	public GameGridPanel(Tournament tournament, Application application) {
		this.tournament = tournament;
		this.application = application;
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
			buttons[i] = new JButton("Present");
			buttons[i].addActionListener(new ToggleListener(i));
			side.add(buttons[i]);
		}
		
		this.setLayout(new BorderLayout());
		this.add(center, BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
	}
	
	public void update(int i, int j) {
		if(tournament.isPresent(i)&&tournament.isPresent(j)){
			setLabel(i, j, tournament.getResult(i, j));
			scores[i].setText(tournament.getScore(i)+"");
		}
		else {
			setLabel(i, j, UNPLAYABLE);
		}
	}
	
	private void setLabel(int i, int j, State state) {
		labels[i][j].setText(state.getName());
		labels[i][j].setBackground(state.getColor());
	}
	
	public void setHighlight(int x, int y) {
		removeHighlight();
		highX = x;
		highY = y;
		labels[x][y].setBorder(SUGGEST_BORDER);
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
			if(tournament.isPresent(i)) {
				tournament.setAttend(i, false);
				for(int j = 0; j<labels[i].length; j++) {
					buttons[i].setText("Absent");
					setLabel(i, j, UNPLAYABLE);
					setLabel(j, i, UNPLAYABLE);
				}
			}
			else {
				tournament.setAttend(i, true);
				for(int j = 0; j<labels[i].length; j++) {
					buttons[i].setText("Present");
					update(i, j);
					update(j, i);
				}
			}
			application.makeSuggestion();
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
			if(i!=highX||j!=highY) {
				if((tournament.isAvailable(i)&&tournament.isAvailable(j))||!tournament.getResult(i, j).equals(UNPLAYED)) {
					labels[i][j].setBorder(SELECT_BORDER);
				}
				else {
					labels[i][j].setBorder(UNSELECTABLE_BORDER);
				}
			}
			else {
				labels[i][j].setBorder(SUGGEST_SELECT_BORDER);
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			if(i!=highX||j!=highY) {
				labels[i][j].setBorder(NO_BORDER);
			}
			else {
				labels[i][j].setBorder(SUGGEST_BORDER);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			State result = tournament.getResult(i, j);
			String[] players = tournament.getPlayers();
			if(result.equals(PLAYING)||result.equals(DRAW)||result.equals(WON)||result.equals(LOST)) {
				String[] options = {players[i], "Draw", players[j], "Clear cell"};
				int choice = JOptionPane.showOptionDialog(null, "Who won?", "Entering result: "+players[i]+" v.s. "+players[j], JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
				switch(choice) {
				case 0:
					tournament.setResult(i, j, WON);
					break;
				case 1:
					tournament.setResult(i, j, DRAW);
					break;
				case 2:
					tournament.setResult(i, j, LOST);
					break;
				case 3:
					tournament.setResult(i, j, UNPLAYED);
					break;
				}
			}
			if(result.equals(UNPLAYED)&&tournament.isAvailable(i)&&tournament.isAvailable(j)) {
				if(JOptionPane.showConfirmDialog(null, "Game in progress between "+players[i]+" and "+players[j]+"?", "Confirming update", JOptionPane.YES_NO_OPTION) == 0) {
					tournament.setResult(i, j, PLAYING);
				}
			}
			if(result.equals(UNPLAYABLE)) {
				tournament.setResult(i, j, UNPLAYED);
			}
			update(i, j);
			update(j, i);
			application.makeSuggestion();
			try {
				tournament.write(Application.PREFIX+tournament.getName()+Application.SUFFIX);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Error opening file: "+Application.PREFIX+tournament.getName()+Application.SUFFIX+"\n"+e1.getLocalizedMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
