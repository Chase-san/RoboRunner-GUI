package robowiki.runner.gui;

import static robowiki.runner.RunnerUtil.round;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import robowiki.runner.BotList;
import robowiki.runner.ChallengeConfig.BotListGroup;
import robowiki.runner.RoboRunner.ScoreSummary;
import robowiki.runner.RoboRunner;
import robowiki.runner.RunnerUtil;
import robowiki.runner.ScoreLog;

public class ResultsDialog extends JDialog {
	private static final long serialVersionUID = -6250890458359982350L;
	private QueueItem item;
	
	private DefaultTableModel model;
	private JTable table;

	/**
	 * Create the dialog.
	 */
	public ResultsDialog(Window parent, QueueItem queueItem) {
		this.item = queueItem;
		setContentPane(getContentPanel());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(620, 120);
		setLocationRelativeTo(parent);
		setTitle(queueItem.getTitle() + ":: " + queueItem.challenge.name);
	}
	
	private JPanel getContentPanel() {
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel buttonPane = new JPanel();
		contentPanel.add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		JButton wikiButton = new JButton("Copy as Wiki");
		wikiButton.setEnabled(false);
		wikiButton.setActionCommand("OK");
		buttonPane.add(wikiButton);
		getRootPane().setDefaultButton(wikiButton);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		table.setModel(getTableModel());
		scrollPane.setViewportView(table);
		
		return contentPanel;
	}
	
	private DefaultTableModel getTableModel() {
		//get column names
		ArrayList<String> columns = new ArrayList<String>();
		ArrayList<Object> data = new ArrayList<Object>();
		
		ScoreLog log = item.scoreLog;
		
		columns.add("Challenger");
		data.add(RunnerUtil.getRobotAlias(item.challenger));
		
		//setup groups and such
		int index = 0;
		double groupSum = 0;
		for(BotListGroup group : item.challenge.referenceBotGroups) {
			for(BotList list : group.referenceBots) {
				//it's a list of bots... but for now treat it as just one robot.
				columns.add(RunnerUtil.getRobotAlias(list.getBotNames().get(0)));
				
				double score = RoboRunner.getWikiScore(log, list, item.challenge.scoringStyle);
				data.add(score);
			}
			
			columns.add("Sub " + ++index);
			ScoreSummary summary = RoboRunner.getScoreSummary(log, group.referenceBots, item.challenge.scoringStyle);
			data.add(summary.getTotalScore());
			
			groupSum += summary.getTotalScore();
		}
		
		columns.add("Total");
		groupSum = round(groupSum / item.challenge.referenceBotGroups.size(), 2);
		data.add(groupSum);
		
		columns.add("Ssns");
		ScoreSummary summary = RoboRunner.getScoreSummary(log, item.challenge.allReferenceBots, item.challenge.scoringStyle);
		double challengeBotLists = item.challenge.allReferenceBots.size();
		data.add(round(summary.numBattles / challengeBotLists, 2));
		
		
		model = new DefaultTableModel(
				new Object[][] { data.toArray() },
				columns.toArray(new String[0])
			);
		return model;
	}

}
