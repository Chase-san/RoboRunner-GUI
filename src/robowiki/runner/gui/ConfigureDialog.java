package robowiki.runner.gui;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.SpinnerNumberModel;

/**
 * 
 * @author Robert Maupin (Chase)
 *
 */
public class ConfigureDialog extends JDialog {

	private static final long serialVersionUID = -9023943942100021910L;
	
	private final JPanel contentPanel = new JPanel();
	private JTextField txtTitle;
	
	private String robotAlias;
	private String challengeName;
	private JSpinner spnSeasons;

	public ConfigureDialog(Window parent) {
		super(parent);
		setTitle("Configure: " + challengeName);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		createDialog();
		setSize(320, 140);
		setLocationRelativeTo(parent);
	}
	
	/**
	 * Create the dialog.
	 */
	public void createDialog() {
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][grow]", "[][]"));
		{
			JLabel lblTitle = new JLabel("Title");
			contentPanel.add(lblTitle, "cell 0 0,alignx right");
		}
		{
			txtTitle = new JTextField();
			contentPanel.add(txtTitle, "cell 1 0,growx");
			txtTitle.setColumns(10);
			//TODO txtTitle.setText(spec.getTitle());
		}
		{
			JLabel lblSeasons = new JLabel("Seasons");
			contentPanel.add(lblSeasons, "cell 0 1,alignx right");
		}
		{
			spnSeasons = new JSpinner();
			spnSeasons.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
			//TODO spnSeasons.setValue(spec.getSeasons());
			contentPanel.add(spnSeasons, "cell 1 1,growx");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
			{
				JButton btnResetTitle = new JButton("Reset Title");
				btnResetTitle.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						resetTitle();
					}
				});
				buttonPane.add(btnResetTitle);
			}
			{
				Component horizontalGlue = Box.createHorizontalGlue();
				buttonPane.add(horizontalGlue);
			}
			{
				JButton okButton = new JButton("Save");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveConfiguration();
						setVisible(false);
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				Component horizontalStrut = Box.createHorizontalStrut(20);
				horizontalStrut.setPreferredSize(new Dimension(5, 0));
				buttonPane.add(horizontalStrut);
			}
			{
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
		}
	}
	
	private void saveConfiguration() {
		//TODO save configuration
	}
	
	private void resetTitle() {
		txtTitle.setText(robotAlias + " - " + challengeName);
	}
}
