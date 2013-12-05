package robowiki.runner.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.SwingConstants;

import com.google.common.base.Splitter;

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
	private JList challengeList;
	private JButton btnStart;
	private JButton btnStop;
	private JSpinner spnThreadCount;
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
		
		JScrollPane challengeScrollPane = new JScrollPane();
		challengeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		challengeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		contentPane.add(challengeScrollPane, "cell 0 0,grow");
		
		challengeList = new JList();
		challengeScrollPane.setViewportView(challengeList);
		
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
			
			JButton btnRemove = new JButton("Remove");
			btnRemove.setFocusable(false);
			btnRemove.setEnabled(false);
			buttonPanel.add(btnRemove);
			
			JButton btnConfigure = new JButton("Configure");
			btnConfigure.setFocusable(false);
			btnConfigure.setEnabled(false);
			buttonPanel.add(btnConfigure);
			
			JButton btnResults = new JButton("Results");
			btnResults.setFocusable(false);
			btnResults.setEnabled(false);
			buttonPanel.add(btnResults);
			
			JButton btnMoveUp = new JButton("Move Up");
			btnMoveUp.setFocusable(false);
			btnMoveUp.setEnabled(false);
			buttonPanel.add(btnMoveUp);
			
			JButton btnMoveDown = new JButton("Move Down");
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
	
	private AddDialog addDialog;
	public void showAddDialog() {
		if(addDialog == null) {
			addDialog = new AddDialog(this);
		}
		addDialog.setLocationRelativeTo(this);
		addDialog.setVisible(true);
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
