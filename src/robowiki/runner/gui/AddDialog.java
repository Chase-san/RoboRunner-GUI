package robowiki.runner.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.SpinnerNumberModel;

import robowiki.runner.ChallengeConfig;
import robowiki.runner.RunnerUtil;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import chase.EndsWithFileFilter;
import chase.WindowToolkit;

/**
 * @author Robert Maupin (Chase)
 */
public class AddDialog extends JDialog {
	private static final long serialVersionUID = -4706647667646277667L;
	
	private final JPanel contentPanel = new JPanel();
	private JFileChooser rrcChooser = null;
	private JFileChooser botChooser = null;
	private JTextField txtChallenge;
	private JTextField txtRobot;
	private JTextField txtTitle;
	private JCheckBox chckbxAuto;
	private JSpinner spnSeasons;
	
	private DefaultListModel<QueueItem> queue;
	
	private String challengeName = "";

	public AddDialog(Window parent, DefaultListModel<QueueItem> model) {
		super(parent, "Add Challenge");
		queue = model;
		
		setModal(true);
		createDialog();
		setSize(330, 200);
		setLocationRelativeTo(parent);
	}
	
	private void createDialog() {
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][grow][]", "[][][][]"));
		{
			JLabel lblTitle = new JLabel("Title");
			contentPanel.add(lblTitle, "cell 0 0,alignx trailing");
		}
		{
			txtTitle = new JTextField();
			txtTitle.setEditable(false);
			contentPanel.add(txtTitle, "cell 1 0,growx");
			txtTitle.setColumns(10);
		}
		{
			chckbxAuto = new JCheckBox("Auto");
			chckbxAuto.setSelected(true);
			chckbxAuto.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					toggleAutoTitle(chckbxAuto.isSelected());
				}
			});
			contentPanel.add(chckbxAuto, "cell 2 0");
		}
		{
			JLabel lblChallenge = new JLabel("Challenge");
			contentPanel.add(lblChallenge, "cell 0 1,alignx right");
		}
		{
			txtChallenge = new JTextField();
			txtChallenge.setEditable(false);
			contentPanel.add(txtChallenge, "cell 1 1,growx");
			txtChallenge.setColumns(10);
		}
		{
			JButton btnChallengeBrowse = new JButton("Browse");
			btnChallengeBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					browseForChallenge();
				}
			});
			contentPanel.add(btnChallengeBrowse, "cell 2 1");
		}
		{
			JLabel lblChallenger = new JLabel("Robot");
			contentPanel.add(lblChallenger, "cell 0 2,alignx right");
		}
		{
			txtRobot = new JTextField();
			txtRobot.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateTitle();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateTitle();
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					updateTitle();
				}
			});
			contentPanel.add(txtRobot, "cell 1 2,growx");
			txtRobot.setColumns(10);
		}
		{
			JButton btnChallengerBrowse = new JButton("Browse");
			btnChallengerBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					browseForRobot();
				}
			});
			contentPanel.add(btnChallengerBrowse, "cell 2 2");
		}
		{
			JLabel lblSeasons = new JLabel("# Seasons");
			contentPanel.add(lblSeasons, "cell 0 3,alignx right");
		}
		{
			spnSeasons = new JSpinner();
			spnSeasons.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
			contentPanel.add(spnSeasons, "cell 1 3,growx,aligny baseline");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Add");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addChallenge();
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	private void toggleAutoTitle(boolean selected) {
		if(selected) {
			txtTitle.setEditable(false);
			updateTitle();
		} else {
			txtTitle.setEditable(true);
		}
	}
	
	private void updateTitle() {
		if(chckbxAuto.isSelected()) {
			String robotAlias = RunnerUtil.getRobotAlias(txtRobot.getText());
			
			txtTitle.setText(robotAlias + " - " + challengeName);
		}
	}
	
	private void browseForChallenge() {
		if(rrcChooser == null) {
			Options.challengeDir.mkdirs();
			WindowToolkit.setFileChooserReadOnly(true);
			rrcChooser = new JFileChooser();
			FileFilter filter = new EndsWithFileFilter(".rrc", "Challenge Files");
			rrcChooser.addChoosableFileFilter(filter);
			rrcChooser.setFileFilter(filter);
		}
		//reset the directory to the one in options
		rrcChooser.setCurrentDirectory(Options.challengeDir);
		if(rrcChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = rrcChooser.getSelectedFile();
		txtChallenge.setText(file.getAbsolutePath());
		try {
			challengeName = Files.readFirstLine(file, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		updateTitle();
	}

	private void browseForRobot() {
		if(botChooser == null) {
			Options.robotsDir.mkdirs();
			WindowToolkit.setFileChooserReadOnly(true);
			botChooser = new JFileChooser();
			FileFilter filter = new EndsWithFileFilter(".jar", "Java Archive");
			botChooser.addChoosableFileFilter(filter);
			botChooser.setFileFilter(filter);
		}
		//reset the directory to the one in options
		botChooser.setCurrentDirectory(Options.robotsDir);
		if(botChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = botChooser.getSelectedFile();
		
		String robotText = Files.getNameWithoutExtension(file.getName()).replace('_', ' ');
		txtRobot.setText(robotText);
		
		updateTitle();
	}
	
	private void addChallenge() {
		QueueItem item = new QueueItem(
				ChallengeConfig.load(txtChallenge.getText()),
				txtRobot.getText(),
				(int)spnSeasons.getValue()
			);
		item.setTitle(txtTitle.getText());
		queue.addElement(item);
	}
}
