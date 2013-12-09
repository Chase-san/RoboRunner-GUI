package robowiki.runner.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import com.google.common.collect.Lists;

import robowiki.runner.BattleRunner.BattleOutputHandler;
import robowiki.runner.BattleRunner;
import robowiki.runner.BotList;
import robowiki.runner.RobotScore;
import robowiki.runner.RunnerUtil;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

public class RoboRunnerGUI extends JFrame {
	private class ThreadController implements BattleOutputHandler {
		boolean isNew = true;
		private QueueItem queueItem;
		private String bots;

		@Override
		public void processNewBattle(int id, BotList list) {
			StringBuilder buf = new StringBuilder();
			boolean first = true;
			for (String name : list.getBotNames()) {
				if (!first) {
					buf.append(", ");
				}
				first = false;
				buf.append(RunnerUtil.getRobotAlias(name));
			}

			bots = buf.toString();
			String str = String.format("Thread #%d - New Battle - %s", id, bots);

			if (!isNew) {
				threads.set(id, str);
			} else {
				threads.addElement(str);
			}
			threadList.revalidate();
			isNew = false;
		}

		@Override
		public void processResults(int id, List<RobotScore> robotScores, long elapsedTime) {
			String str = String.format("Thread #%d - Completed! - %s", id, bots);
			threads.set(id, str);
			threadList.revalidate();

			queueItem.scoreLog.addBattle(robotScores, queueItem.challenge.rounds, elapsedTime);

			startNextBattle();
		}

		@Override
		public void processRound(int id, int round) {
			String str = String.format("Thread #%d - Round %d - %s", id, round + 1, bots);
			threads.set(id, str);
			threadList.revalidate();
		}

		public void startNextBattle() {
			if (runner != null) {
				Enumeration<QueueItem> enm = queue.elements();
				while (enm.hasMoreElements()) {
					QueueItem item = enm.nextElement();
					List<BotList> list = item.getBattleList();
					if (list.size() != 0) {
						BotList botList = list.remove(0);
						queueItem = item;
						runner.runBattlesNonBlocking(Lists.newArrayList(botList), this, item.challenge.rounds,
								item.challenge.battleFieldWidth, item.challenge.battleFieldHeight);
						return;
					}
				}
			}
		}
	}

	private static final long serialVersionUID = 7337717745952162130L;

	private JList<String> threadList;
	private JList<QueueItem> queueList;
	private JButton btnRemove;
	private JButton btnConfigure;
	private JButton btnResults;
	private JButton btnMoveUp;
	private JButton btnMoveDown;
	private JButton btnStart;
	private JButton btnStop;
	private JSpinner spnThreadCount;

	private AddDialog addDialog;
	private BattleRunner runner;

	private ExecutorService service;
	private DefaultListModel<QueueItem> queue;

	private DefaultListModel<String> threads;
	private JMenuItem mntmOptions;

	public RoboRunnerGUI() {
		setTitle("RoboRunner GUI v0.9.0");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setJMenuBar(createMenuBar());
		setContentPane(createContentPane());
		pack();
		
		setLocationByPlatform(true);
	}

	private JPanel createContentPane() {
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new MigLayout("insets 0,gap 5", "[grow][]", "[grow][growprio 50,grow]"));

		JScrollPane queueScrollPane = new JScrollPane();
		queueScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		queueScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		contentPane.add(queueScrollPane, "cell 0 0,grow");

		queueList = new JList<QueueItem>(queue = new DefaultListModel<QueueItem>());
		queueList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		queueList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int[] indices = queueList.getSelectedIndices();
				if (indices.length > 0) {
					int first = indices[0];
					int last = indices[indices.length - 1];
					if (first != -1) {
						if (first > 0) {
							// enable up
							btnMoveUp.setEnabled(true);
						} else {
							btnMoveUp.setEnabled(false);
						}
						if (last < queue.size() - 1) {
							// enable down
							btnMoveDown.setEnabled(true);
						} else {
							btnMoveDown.setEnabled(false);
						}

						btnRemove.setEnabled(true);
						btnConfigure.setEnabled(true);
						btnResults.setEnabled(true);
						return;
					}
				}

				// disable everything
				btnConfigure.setEnabled(false);
				btnMoveUp.setEnabled(false);
				btnMoveDown.setEnabled(false);
				btnRemove.setEnabled(false);
				btnResults.setEnabled(false);
			}
		});
		queueScrollPane.setViewportView(queueList);

		{
			JPanel buttonPanel = new JPanel();
			contentPane.add(buttonPanel, "cell 1 0,growx,aligny top");
			buttonPanel.setLayout(new GridLayout(0, 1, 0, 4));

			JButton btnAdd = new JButton("Add...");
			btnAdd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showAddDialog();
				}
			});
			buttonPanel.add(btnAdd);

			btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					int[] selected = queueList.getSelectedIndices();
					// do it backwards, otherwise the indexes will change.
					for (int i = selected.length - 1; i >= 0; --i) {
						queue.remove(selected[i]);
					}

					queueList.revalidate();
				}
			});
			btnRemove.setEnabled(false);
			buttonPanel.add(btnRemove);

			btnConfigure = new JButton("Configure");
			btnConfigure.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showConfigDialog();
				}
			});
			btnConfigure.setEnabled(false);
			buttonPanel.add(btnConfigure);

			btnResults = new JButton("Results");
			btnResults.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showResultsDialog();
				}
			});
			btnResults.setEnabled(false);
			buttonPanel.add(btnResults);

			btnMoveUp = new JButton("Move Up");
			btnMoveUp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int[] indices = queueList.getSelectedIndices();

					// start from the top
					for (int i = 0; i < indices.length; ++i) {
						QueueItem item = queue.get(indices[i]);
						queue.removeElementAt(indices[i]);
						queue.insertElementAt(item, --indices[i]);
					}

					queueList.setSelectedIndices(indices);

					queueList.revalidate();

				}
			});
			btnMoveUp.setEnabled(false);
			buttonPanel.add(btnMoveUp);

			btnMoveDown = new JButton("Move Down");
			btnMoveDown.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int[] indices = queueList.getSelectedIndices();

					// start from the bottom
					for (int i = indices.length - 1; i >= 0; --i) {
						QueueItem item = queue.get(indices[i]);
						queue.removeElementAt(indices[i]);
						queue.insertElementAt(item, ++indices[i]);
					}

					queueList.setSelectedIndices(indices);

					queueList.revalidate();
				}
			});
			btnMoveDown.setEnabled(false);
			buttonPanel.add(btnMoveDown);
		}

		JScrollPane threadScrollPane = new JScrollPane();
		threadScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		threadScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(threadScrollPane, "cell 0 1,grow");

		threadList = new JList<String>(threads = new DefaultListModel<String>());
		threadList.setBackground(UIManager.getColor("Panel.background"));
		threadScrollPane.setViewportView(threadList);

		{
			JPanel threadPanel = new JPanel();
			contentPane.add(threadPanel, "cell 1 1,growx,aligny top");
			threadPanel.setLayout(new GridLayout(0, 1, 0, 4));

			JLabel lblThreadCount = new JLabel("Thread Count");
			lblThreadCount.setHorizontalAlignment(SwingConstants.CENTER);
			threadPanel.add(lblThreadCount);

			spnThreadCount = new JSpinner();
			spnThreadCount.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
			threadPanel.add(spnThreadCount);

			btnStart = new JButton("Start");
			btnStart.setToolTipText("Once you click start, you will not be able to alter the thread count until either you stop it or it finishes.");
			btnStart.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					startRunner();
				}
			});
			threadPanel.add(btnStart);

			btnStop = new JButton("Stop");
			btnStop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stopRunner();
				}
			});
			btnStop.setEnabled(false);
			threadPanel.add(btnStop);
		}

		return contentPane;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu mnSettings = new JMenu("View");
		mnSettings.setMnemonic(KeyEvent.VK_V);
		menuBar.add(mnSettings);
		
		mntmOptions = new JMenuItem("Options");
		mntmOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showOptionsDialog();
			}
		});
		mntmOptions.setMnemonic(KeyEvent.VK_O);
		mntmOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0));
		mnSettings.add(mntmOptions);

		return menuBar;
	}

	@Override
	public void dispose() {
		// stop the runner properly
		if (runner != null) {
			runner.shutdown();
			runner = null;
		}
		super.dispose();
		// Force an exit.
		System.exit(0);
	}

	private void showAddDialog() {
		if (addDialog == null) {
			addDialog = new AddDialog(this, queue);
		}
		addDialog.setLocationRelativeTo(this);
		addDialog.setVisible(true);
	}

	private void showConfigDialog() {
		for (QueueItem item : queueList.getSelectedValuesList()) {
			new ConfigureDialog(this, item).setVisible(true);
		}
	}
	
	private void showOptionsDialog() {
		new OptionsDialog(this).setVisible(true);
	}

	private void showResultsDialog() {
		for (QueueItem item : queueList.getSelectedValuesList()) {
			new ResultsDialog(this, item).setVisible(true);
		}
	}

	private void startRunner() {

		// disable start/thread # and enable stop
		btnStop.setEnabled(true);
		btnStart.setEnabled(false);
		spnThreadCount.setEnabled(false);

		final int threadCount = (int) spnThreadCount.getValue();

		if (service == null) {
			service = Executors.newFixedThreadPool(1);
		}

		service.execute(new Runnable() {
			@Override
			public void run() {
				HashSet<String> paths = new HashSet<String>();
				
				for (int i = 0; i < threadCount; ++i) {
					paths.add(Options.getRunnerDirectory(i));
				}

				runner = new BattleRunner(paths, Options.jvmArgs, true);

				for (int i = 0; i < threadCount; ++i) {
					ThreadController control = new ThreadController();
					control.startNextBattle();
				}
			}
		});
	}

	private void stopRunner() {
		// disable stop and enable start/thread #
		btnStop.setEnabled(false);
		btnStart.setEnabled(true);
		spnThreadCount.setEnabled(true);

		if (runner != null) {
			runner.shutdown();
			runner = null;
		}

		threads.removeAllElements();
		threadList.revalidate();
	}
}
