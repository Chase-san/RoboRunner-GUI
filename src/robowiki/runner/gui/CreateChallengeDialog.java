package robowiki.runner.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SpinnerNumberModel;

import robowiki.runner.BotList;
import robowiki.runner.ChallengeConfig;
import robowiki.runner.ChallengeConfig.BotListGroup;
import robowiki.runner.RobotScore.ScoringStyle;

import com.google.common.io.Files;

import chase.EndsWithFileFilter;
import chase.WindowToolkit;

/**
 * @author Robert Maupin (Chase)
 */
public class CreateChallengeDialog extends JDialog {

	private static final long serialVersionUID = 2929462354513658187L;

	private final JPanel contentPanel = new JPanel();
	private JFileChooser challengeChooser = null;
	private JFileChooser robotChooser = null;
	private JTextField txtName;
	private DefaultMutableTreeNode root;
	private JPopupMenu menu;
	private JTree tree;
	private JButton btnMoveUp;
	private JButton btnMoveDown;
	private JButton btnRemove;
	private JButton btnAddRobot;
	private JLabel lblGroups;
	private JLabel lblName;
	private JComboBox<String> boxScoreType;
	private JSpinner spnRounds;
	private JLabel lblBattlefield;
	private JPanel panel;
	private JLabel lblWidth;
	private JSpinner spnWidth;
	private JLabel lblHeight;
	private JSpinner spnHeight;

	/**
	 * Create the dialog.
	 */
	public CreateChallengeDialog(Frame owner) {
		super(owner);
		setTitle("Create Challenge");
		setModal(true);
		createDialog();
		
		menu = new JPopupMenu();
		JMenuItem rename = new JMenuItem("Rename");
		rename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rename();
			}
		});
		menu.add(rename);

		setSize(450, 400);
		setLocationRelativeTo(owner);
	}

	private void addRobot() {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			Object[] nodes = tree.getSelectionPath().getPath();
			if (nodes.length > 1) {
				// get group item
				DefaultMutableTreeNode group = (DefaultMutableTreeNode) nodes[1];

				String[] names = browseForRobots();
				if (names != null) {
					if(names.length > 0) {
						//add all robots
						for(String name : names) {
							group.add(new DefaultMutableTreeNode(name, false));
						}
	
						// update tree
						updateTree();
	
						// Select parent
						tree.setSelectionPath(new TreePath(group.getPath()));
					}
				}
			}
		}
	}
	
	private String[] browseForRobots() {
		if(robotChooser == null) {
			Options.robotsDir.mkdirs();
			WindowToolkit.setFileChooserReadOnly(true);
			robotChooser = new JFileChooser();
			FileFilter filter = new EndsWithFileFilter(".jar", "Java Archive");
			robotChooser.setMultiSelectionEnabled(true);
			robotChooser.addChoosableFileFilter(filter);
			robotChooser.setFileFilter(filter);
		}
		//reset the directory to the one in options
		robotChooser.setCurrentDirectory(Options.robotsDir);
		if(robotChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		
		File[] files = robotChooser.getSelectedFiles();
		String[] names = new String[files.length];
		for(int i = 0; i < files.length; ++i) {
			names[i] = Files.getNameWithoutExtension(files[i].getName()).replace('_', ' ');
		}
		
		System.out.println(Arrays.toString(names));

		return names;
	}

	
	
	/*
	 * 
	 */
	private List<BotListGroup> createBotListGroupList() {
		ArrayList<BotListGroup> botListGroupList = new ArrayList<BotListGroup>();

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getFirstChild();
		while (node != null) {
			//create group
			String name = node.toString();
			
			ArrayList<BotList> botListList = new ArrayList<BotList>();
			DefaultMutableTreeNode subnode = (DefaultMutableTreeNode) node.getFirstChild();
			while(subnode != null) {
				botListList.add(new BotList(subnode.toString()));
				subnode = subnode.getNextSibling();
			}
			
			botListGroupList.add(new BotListGroup(name,botListList));
			node = node.getNextSibling();
		}
		
		return botListGroupList;
	}

	private void createDialog() {
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][grow]", "[][][][][][grow]"));
		{
			lblName = new JLabel("Name");
			contentPanel.add(lblName, "cell 0 0,alignx trailing");
		}
		{
			txtName = new JTextField();
			contentPanel.add(txtName, "cell 1 0,growx");
			txtName.setColumns(10);
		}
		{
			JLabel lblType = new JLabel("Type");
			contentPanel.add(lblType, "cell 0 1,alignx trailing");
		}
		{
			boxScoreType = new JComboBox<String>();
			boxScoreType.setModel(new DefaultComboBoxModel(ScoringStyle.values()));
			contentPanel.add(boxScoreType, "cell 1 1,growx");
		}
		{
			JLabel lblRounds = new JLabel("Rounds");
			contentPanel.add(lblRounds, "cell 0 2,alignx trailing");
		}
		{
			spnRounds = new JSpinner();
			spnRounds.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
			contentPanel.add(spnRounds, "cell 1 2,growx");
		}
		{
			lblBattlefield = new JLabel("Battlefield");
			contentPanel.add(lblBattlefield, "cell 0 3");
		}
		{
			panel = new JPanel();
			contentPanel.add(panel, "cell 1 3,grow");
			panel.setLayout(new MigLayout("", "[][grow][][grow]", "[]"));
			{
				lblWidth = new JLabel("Width");
				panel.add(lblWidth, "cell 0 0");
			}
			{
				spnWidth = new JSpinner();
				spnWidth.setModel(new SpinnerNumberModel(800, 400, 5000, 10));
				panel.add(spnWidth, "cell 1 0,growx");
			}
			{
				lblHeight = new JLabel("Height");
				panel.add(lblHeight, "cell 2 0");
			}
			{
				spnHeight = new JSpinner();
				spnHeight.setModel(new SpinnerNumberModel(600, 400, 5000, 10));
				panel.add(spnHeight, "cell 3 0,growx");
			}
		}
		{
			lblGroups = new JLabel("Groups");
			contentPanel.add(lblGroups, "flowx,cell 0 4,alignx leading");
		}
		{
			JPanel groupPanel = new JPanel();
			contentPanel.add(groupPanel, "cell 0 5 2 1,grow");
			groupPanel.setLayout(new MigLayout("", "[grow][]", "[grow]"));
			{
				JScrollPane scrollPane = new JScrollPane();
				groupPanel.add(scrollPane, "cell 0 0,grow");
				{
					root = new DefaultMutableTreeNode("root", true);
					tree = new JTree(new DefaultTreeModel(root));
					tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
					tree.setEditable(true);
					tree.setRootVisible(false);
					tree.addTreeSelectionListener(new TreeSelectionListener() {
						@Override
						public void valueChanged(TreeSelectionEvent e) {
							onTreeSelection(e);
						}
					});

					scrollPane.setViewportView(tree);
				}
			}
			{
				JPanel buttonPanel = new JPanel();
				groupPanel.add(buttonPanel, "cell 1 0,aligny top");
				buttonPanel.setLayout(new GridLayout(0, 1, 0, 0));
				{
					JButton btnAddGroup = new JButton("Add Group");
					btnAddGroup.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							root.add(new DefaultMutableTreeNode("New Group", true));
							updateTree();
						}
					});
					buttonPanel.add(btnAddGroup);
				}
				{
					btnAddRobot = new JButton("Add Robot");
					btnAddRobot.setEnabled(false);
					btnAddRobot.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							addRobot();
						}
					});
					buttonPanel.add(btnAddRobot);
				}
				{
					btnRemove = new JButton("Remove");
					btnRemove.setEnabled(false);
					btnRemove.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							removeNode();
						}
					});
					buttonPanel.add(btnRemove);
				}
				{
					btnMoveUp = new JButton("Move Up");
					btnMoveUp.setEnabled(false);
					btnMoveUp.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							moveUp();
						}
					});
					buttonPanel.add(btnMoveUp);
				}
				{
					btnMoveDown = new JButton("Move Down");
					btnMoveDown.setEnabled(false);
					btnMoveDown.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							moveDown();
						}
					});
					buttonPanel.add(btnMoveDown);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton saveButton = new JButton("Save");
				saveButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (saveChallenge()) {
							setVisible(false);
						}
					}
				});
				saveButton.setActionCommand("OK");
				buttonPane.add(saveButton);
				getRootPane().setDefaultButton(saveButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	private void moveDown() {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			Object[] nodes = tree.getSelectionPath().getPath();

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[nodes.length - 1];
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			int index = parent.getIndex(node);

			if (index < parent.getChildCount() - 1) {
				parent.remove(index);
				parent.insert(node, index + 1);

				updateTree();

				tree.setSelectionPath(new TreePath(node.getPath()));
			}
		}
	}

	private void moveUp() {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			Object[] nodes = tree.getSelectionPath().getPath();

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[nodes.length - 1];
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			int index = parent.getIndex(node);

			if (index > 0) {
				parent.remove(index);
				parent.insert(node, index - 1);

				updateTree();

				tree.setSelectionPath(new TreePath(node.getPath()));
			}
		}
	}

	private void onTreeSelection(TreeSelectionEvent e) {
		TreePath path = e.getPath();
		if (path != null) {
			Object[] nodes = path.getPath();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[nodes.length - 1];

			if (node.getParent() != null) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

				int index = parent.getIndex(node);

				// set popup menu
				tree.setComponentPopupMenu(menu);
				btnRemove.setEnabled(true);
				btnAddRobot.setEnabled(true);

				if (index > 0) {
					// enable up
					btnMoveUp.setEnabled(true);
				} else {
					btnMoveUp.setEnabled(false);
				}
				if (index < parent.getChildCount() - 1) {
					// enable down
					btnMoveDown.setEnabled(true);
				} else {
					btnMoveDown.setEnabled(false);
				}

				// all good, return and skip our disables
				return;
			}
		}

		// disable everything.
		btnMoveUp.setEnabled(false);
		btnMoveDown.setEnabled(false);
		btnRemove.setEnabled(false);
		btnAddRobot.setEnabled(false);
		tree.setComponentPopupMenu(null);
	}

	private void removeNode() {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			Object[] nodes = tree.getSelectionPath().getPath();

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[nodes.length - 1];
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			int index = parent.getIndex(node);
			parent.remove(index);

			updateTree();

			// select a sibling
			if (index >= parent.getChildCount()) {
				index = parent.getChildCount() - 1;
			}
			if (index != -1) {
				DefaultMutableTreeNode sibling = (DefaultMutableTreeNode) parent.getChildAt(index);
				tree.setSelectionPath(new TreePath(sibling.getPath()));
			} else if (parent != root) {
				// if there is no siblings select parent
				// if the parent is not root, select it instead
				tree.setSelectionPath(new TreePath(parent.getPath()));
			}
		}
	}

	private void rename() {
		TreePath path = tree.getSelectionPath();
		if (path != null) {
			Object[] nodes = tree.getSelectionPath().getPath();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[nodes.length - 1];

			tree.startEditingAtPath(new TreePath(node.getPath()));
		}
	}

	private boolean saveChallenge() {
		// reset colors
		lblName.setForeground(Color.BLACK);
		lblGroups.setForeground(Color.BLACK);

		boolean cannotSave = false;

		// check and make sure we have everything we need to save
		String name = txtName.getText();
		if (name == null) {
			lblName.setForeground(Color.RED);
			cannotSave = true;
		} else {
			name = name.trim();
			if (name.length() == 0) {
				lblName.setForeground(Color.RED);
				cannotSave = true;
			}
		}

		// we need at least 1 group with one robot
		// all groups must have at least 1 robot
		if (root.getChildCount() == 0) {
			lblGroups.setForeground(Color.RED);
			cannotSave = true;
		} else {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getFirstChild();
			while (node != null) {
				if (node.getChildCount() == 0) {
					// all groups must have at least 1 child
					lblGroups.setForeground(Color.RED);
					cannotSave = true;
					break;
				}

				node = node.getNextSibling();
			}

		}

		if (cannotSave) {
			return false;
		}
		
		if (challengeChooser == null) {
			Options.challengeDir.mkdirs();
			WindowToolkit.setFileChooserReadOnly(true);
			challengeChooser = new JFileChooser(Options.challengeDir);
			challengeChooser.addChoosableFileFilter(new EndsWithFileFilter(".rrc","Challenge Files"));
		}

		// let the user choose
		int status = challengeChooser.showSaveDialog(this);
		if (status != JFileChooser.APPROVE_OPTION) {
			return false;
		}
		File file = challengeChooser.getSelectedFile();

		if(!file.getName().endsWith("rrc")) {
			file = new File(file.getParentFile(),file.getName() + ".rrc");
		}
		
		ChallengeConfig config = new ChallengeConfig(
				txtName.getText(),
				(int)spnRounds.getValue(),
				(ScoringStyle)boxScoreType.getSelectedItem(),
				(int)spnWidth.getValue(),
				(int)spnHeight.getValue(),
				createBotListGroupList()
			);
		
		config.save(file);

		return true;
	}

	private void updateTree() {
		// There are better ways to do this, but they involve
		// about 200 more lines of code
		((DefaultTreeModel) tree.getModel()).reload(root);

		// expand the tree
		for (int i = 0; i < tree.getRowCount(); ++i) {
			tree.expandRow(i);
		}
	}
}
