package robowiki.runner.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;

import chase.WindowToolkit;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class OptionsDialog extends JDialog {
	private static final long serialVersionUID = -3927100605659252220L;
	private JTextField txtRobocode;
	private JTextField txtRobots;
	private JTextField txtChallenges;
	private JTextField txtRunner;
	private JTextField txtJVMargs;
	
	private JFileChooser robocodeChooser = null;
	private JFileChooser runnerChooser = null;
	private JFileChooser robotsChooser = null;
	private JFileChooser challengeChooser = null;
	

	/**
	 * Create the dialog.
	 */
	public OptionsDialog(Window parent) {
		setSize(450,334);
		setTitle("Options");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setContentPane(createContentPane());
		setLocationRelativeTo(parent);
	}
	
	private JPanel createContentPane() {
		JPanel contentPanel = new JPanel();
		
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			contentPanel.add(buttonPane, BorderLayout.SOUTH);
			JButton okButton = new JButton("Save");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//save values
					Options.challengeDir = new File(txtChallenges.getText());
					Options.robotsDir = new File(txtRobots.getText());
					Options.robocodeLibDir = new File(txtRobocode.getText());
					Options.runnerDir = new File(txtRunner.getText());
					Options.jvmArgs = txtJVMargs.getText();
					Options.saveOptions();
					
					setVisible(false);
					dispose();
				}
			});
			okButton.setActionCommand("Save");
			buttonPane.add(okButton);
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
		}
		
		JPanel panel = new JPanel();
		contentPanel.add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[grow][]", "[][][][][][][][][][]"));
		
		JLabel lblRobocodeLibDirectory = new JLabel("Robocode Library Directory");
		panel.add(lblRobocodeLibDirectory, "cell 0 0");
		
		txtRobocode = new JTextField();
		txtRobocode.setEditable(false);
		txtRobocode.setText(Options.transformPath(Options.robocodeLibDir));
		panel.add(txtRobocode, "cell 0 1,growx");
		txtRobocode.setColumns(10);
		
		JButton btnRobocodeBrowse = new JButton("Browse");
		btnRobocodeBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForRobocodeFolder();
			}
		});
		panel.add(btnRobocodeBrowse, "cell 1 1");
		
		JLabel lblRobocodesDirectory = new JLabel("Robocodes Runner Directory");
		panel.add(lblRobocodesDirectory, "cell 0 2");
		
		txtRunner = new JTextField();
		txtRunner.setEditable(false);
		txtRunner.setText(Options.transformPath(Options.runnerDir));
		panel.add(txtRunner, "cell 0 3,growx");
		txtRunner.setColumns(10);
		
		JButton btnRunnerBrowse = new JButton("Browse");
		btnRunnerBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForRunnerFolder();
			}
		});
		panel.add(btnRunnerBrowse, "cell 1 3");
		
		JLabel lblRobotDirectory = new JLabel("Robot Directory");
		panel.add(lblRobotDirectory, "cell 0 4");
		
		txtRobots = new JTextField();
		txtRobots.setEditable(false);
		txtRobots.setText(Options.transformPath(Options.robotsDir));
		panel.add(txtRobots, "cell 0 5,growx");
		txtRobots.setColumns(10);
		
		JButton btnRobotBrowse = new JButton("Browse");
		btnRobotBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForRobotsFolder();
			}
		});
		panel.add(btnRobotBrowse, "cell 1 5");
		
		JLabel lblChallengeDirectory = new JLabel("Challenge Directory");
		panel.add(lblChallengeDirectory, "cell 0 6");
		
		txtChallenges = new JTextField();
		txtChallenges.setEditable(false);
		txtChallenges.setText(Options.transformPath(Options.challengeDir));
		panel.add(txtChallenges, "cell 0 7,growx");
		txtChallenges.setColumns(10);
		
		JButton btnChallengeBrowse = new JButton("Browse");
		btnChallengeBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseForChallengeFolder();
			}
		});
		panel.add(btnChallengeBrowse, "cell 1 7");
		
		JLabel lblJvmArguments = new JLabel("Virtual Machine Arguments");
		panel.add(lblJvmArguments, "cell 0 8");
		
		txtJVMargs = new JTextField();
		txtJVMargs.setText(Options.jvmArgs);
		panel.add(txtJVMargs, "cell 0 9,growx");
		txtJVMargs.setColumns(10);
		
		return contentPanel;
	}
	
	
	private void browseForRobocodeFolder() {
		if(robocodeChooser == null) {
			WindowToolkit.setFileChooserReadOnly(true);
			robocodeChooser = new JFileChooser(new File("."));
			robocodeChooser.setDialogTitle("Select Robocode /lib/ Directory");
			robocodeChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		if(robocodeChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = robocodeChooser.getSelectedFile();
		txtRobocode.setText(Options.transformPath(file));
	}
	
	private void browseForRunnerFolder() {
		if(runnerChooser == null) {
			WindowToolkit.setFileChooserReadOnly(true);
			runnerChooser = new JFileChooser(new File("."));
			runnerChooser.setDialogTitle("Select Robots Directory");
			runnerChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		if(runnerChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = runnerChooser.getSelectedFile();
		txtRunner.setText(Options.transformPath(file));
	}
	
	
	private void browseForRobotsFolder() {
		if(robotsChooser == null) {
			WindowToolkit.setFileChooserReadOnly(true);
			robotsChooser = new JFileChooser(new File("."));
			robotsChooser.setDialogTitle("Select Robots Directory");
			robotsChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		if(robotsChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = robocodeChooser.getSelectedFile();
		txtRobots.setText(Options.transformPath(file));
	}
	
	
	private void browseForChallengeFolder() {
		if(challengeChooser == null) {
			WindowToolkit.setFileChooserReadOnly(true);
			challengeChooser = new JFileChooser(new File("."));
			challengeChooser.setDialogTitle("Select Challenges Directory");
			challengeChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		if(challengeChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = challengeChooser.getSelectedFile();
		txtChallenges.setText(Options.transformPath(file));
	}

}
