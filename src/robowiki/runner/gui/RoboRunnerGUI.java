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

import net.miginfocom.swing.MigLayout;

import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;

import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class RoboRunnerGUI extends JFrame {
	private static final long serialVersionUID = 7337717745952162130L;
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		
		RoboRunnerGUI rrg = new RoboRunnerGUI();
		rrg.setVisible(true);
	}
	
	private JList threadList;
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

	private DefaultListModel<QueueItem> queue;
	
	public RoboRunnerGUI() {
		setTitle("RoboRunner GUI");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(createContentPane());
		pack();
		setLocationByPlatform(true);
	}
	
	public JPanel createContentPane() {
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
			public void valueChanged(ListSelectionEvent e) {
				int[] indices = queueList.getSelectedIndices();
				if(indices.length > 0) {
					int first = indices[0];
					int last = indices[indices.length - 1];
					if(first != -1) {
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
				
				//disable everything
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
				public void actionPerformed(ActionEvent e) {
					showAddDialog();
				}
			});
			btnAdd.setFocusable(false);
			buttonPanel.add(btnAdd);
			
			btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					int[] selected = queueList.getSelectedIndices();
					//do it backwards, otherwise the indexes will change.
					for(int i = selected.length - 1; i >= 0; --i) {
						queue.remove(selected[i]);
					}
					
					queueList.revalidate();
				}
			});
			btnRemove.setFocusable(false);
			btnRemove.setEnabled(false);
			buttonPanel.add(btnRemove);
			
			btnConfigure = new JButton("Configure");
			btnConfigure.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showConfigDialog();
				}
			});
			btnConfigure.setFocusable(false);
			btnConfigure.setEnabled(false);
			buttonPanel.add(btnConfigure);
			
			btnResults = new JButton("Results");
			btnResults.setFocusable(false);
			btnResults.setEnabled(false);
			buttonPanel.add(btnResults);
			
			btnMoveUp = new JButton("Move Up");
			btnMoveUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int[] indices = queueList.getSelectedIndices();
					
					//start from the top
					for(int i = 0; i < indices.length; ++i) {
						QueueItem item = queue.get(indices[i]);
						queue.removeElementAt(indices[i]);
						queue.insertElementAt(item, --indices[i]);
					}
					
					queueList.setSelectedIndices(indices);
					
					queueList.revalidate();
					
				}
			});
			btnMoveUp.setFocusable(false);
			btnMoveUp.setEnabled(false);
			buttonPanel.add(btnMoveUp);
			
			btnMoveDown = new JButton("Move Down");
			btnMoveDown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int[] indices = queueList.getSelectedIndices();
					
					//start from the bottom
					for(int i = indices.length - 1; i >= 0; --i) {
						QueueItem item = queue.get(indices[i]);
						queue.removeElementAt(indices[i]);
						queue.insertElementAt(item, ++indices[i]);
					}
					
					queueList.setSelectedIndices(indices);
					
					queueList.revalidate();
				}
			});
			btnMoveDown.setFocusable(false);
			btnMoveDown.setEnabled(false);
			buttonPanel.add(btnMoveDown);
		}
		
		JScrollPane threadScrollPane = new JScrollPane();
		threadScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		threadScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(threadScrollPane, "cell 0 1,grow");
		
		threadList = new JList();
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
			spnThreadCount.setFocusable(false);
			spnThreadCount.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
			threadPanel.add(spnThreadCount);
			
			btnStart = new JButton("Start");
			btnStart.setFocusable(false);
			btnStart.setToolTipText("Once you click start, you will not be able to alter the thread count until either you stop it or it finishes.");
			btnStart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					startRunner();
				}
			});
			threadPanel.add(btnStart);
			
			btnStop = new JButton("Stop");
			btnStop.setFocusable(false);
			btnStop.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stopRunner();
				}
			});
			btnStop.setEnabled(false);
			threadPanel.add(btnStop);
		}
		
		return contentPane;
	}
	
	public void showAddDialog() {
		if(addDialog == null) {
			addDialog = new AddDialog(this,queue);
			//queueList.getModel();
		}
		addDialog.setLocationRelativeTo(this);
		addDialog.setVisible(true);
	}
	
	public void showConfigDialog() {
		for(QueueItem item : queueList.getSelectedValuesList()) {
			new ConfigureDialog(this,item).setVisible(true);
		}
	}
	
	public void startRunner() {
		//grey out start and thread #
		//ungrey out stop
		btnStop.setEnabled(true);
		btnStart.setEnabled(false);
		spnThreadCount.setEnabled(false);
	}
	
	public void stopRunner() {
		//grey out stop and ungrey start/thread #
		btnStop.setEnabled(false);
		btnStart.setEnabled(true);
		spnThreadCount.setEnabled(true);
	}
	
	public void dispose() {
		
		super.dispose();
	}
}
