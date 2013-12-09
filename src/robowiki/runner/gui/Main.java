package robowiki.runner.gui;

import javax.swing.UIManager;

public class Main {
	public static final void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		Options.loadOptions();
		
		RoboRunnerGUI rrg = new RoboRunnerGUI();
		rrg.setVisible(true);
	}
}
