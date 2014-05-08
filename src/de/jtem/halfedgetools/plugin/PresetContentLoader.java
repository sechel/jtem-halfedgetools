package de.jtem.halfedgetools.plugin;

import static de.jreality.util.SceneGraphUtility.getFirstGeometry;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ViewShrinkPanelPlugin;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;

public class PresetContentLoader extends ViewShrinkPanelPlugin implements ActionListener, TreeSelectionListener {

	private final FilenameFilter		
		SUPPORTED_FILES_FILTER = new SupportedFilesFilter(),
		FOLDERS_FILES_FILTER = new FoldersFilesFilter();
	
	private HalfedgeInterface
		hif = null;
	private List<File>
		presetFolders = new ArrayList<File>(),
		folderFiles = new ArrayList<File>();
	
	private Icon
		folderIcon = ImageHook.getIcon("folder_brick.png"),
		fileIcon = ImageHook.getIcon("brick.png");
	private PresetTreeNode
		presetRoot = new PresetTreeNode(null);
	private JTree
		presetTree = new JTree(new DefaultTreeModel(presetRoot, false));
	private JList
		fileList = new JList(new FileModel());
	private JScrollPane
		presetTreeScroller = new JScrollPane(presetTree),
		fileScroller = new JScrollPane(fileList);
	private JPanel
		topPanel = new JPanel(),
		bottomPanel = new JPanel();
	private JSplitPane
		splitter = new JSplitPane(VERTICAL_SPLIT, true, topPanel, bottomPanel);
	private JFileChooser
		locationChooser = new JFileChooser();
	private JButton
		addFolderButton = new JButton("Add", ImageHook.getIcon("folder_add.png")),
		removeFolderButton = new JButton("Remove", ImageHook.getIcon("folder_delete.png")),
		loadButton = new JButton("Load", ImageHook.getIcon("brick_go.png"));
	
	public PresetContentLoader() {
		shrinkPanel.setTitle("Content Presets");
		shrinkPanel.setLayout(new GridLayout());
		topPanel.setLayout(new GridBagLayout());
		bottomPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		presetTreeScroller.setPreferredSize(new Dimension(10, 100));
		fileScroller.setPreferredSize(new Dimension(10, 150));
		
		topPanel.add(presetTreeScroller, c);
		c.weighty = 0.0;
		c.weightx = 0.5;
		c.gridwidth = GridBagConstraints.RELATIVE;
		topPanel.add(addFolderButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		topPanel.add(removeFolderButton, c);
		c.weighty = 1.0;
		c.weightx = 1.0;
		bottomPanel.add(fileScroller, c);
		c.weighty = 0.0;
		bottomPanel.add(loadButton, c);
		
		splitter.setDividerLocation(0.3);
		shrinkPanel.add(splitter);
		
		locationChooser.setFileSelectionMode(DIRECTORIES_ONLY);
		locationChooser.setMultiSelectionEnabled(true);
		locationChooser.setDialogTitle("Choose Preset Directories");
		
		presetTree.setRootVisible(false);
		presetTree.getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
		fileList.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		
		presetTree.setCellRenderer(new PresetTreeCellRenderer());
		fileList.setCellRenderer(new FileListCellRenderer());
		
		fileList.addMouseListener(new DoubleClickLoadListener());
		addFolderButton.addActionListener(this);
		removeFolderButton.addActionListener(this);
		loadButton.addActionListener(this);
		presetTree.getSelectionModel().addTreeSelectionListener(this);
	}
	
	
	private class PresetTreeNode implements TreeNode {

		private File folder = null;
		
		public PresetTreeNode(File folder) {
			this.folder = folder;
		}
		
		protected Vector<TreeNode> getChildren() {
			Vector<TreeNode> children = new Vector<TreeNode>();
			if (folder == null) {
				for (File f : presetFolders) {
					children.add(new PresetTreeNode(f));
				}
			} else {
				for (File f : folder.listFiles(FOLDERS_FILES_FILTER)) {
					if (f.getName().startsWith(".")) continue;
					children.add(new PresetTreeNode(f));
				}
			}
			return children;
		}
		
		@Override
		public Enumeration<TreeNode> children() {
			Vector<TreeNode> children = getChildren();
			return children.elements();
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return getChildren().elementAt(childIndex);
		}

		@Override
		public int getChildCount() {
			return getChildren().size();
		}

		@Override
		public int getIndex(TreeNode node) {
			return getChildren().indexOf(node);
		}

		@Override
		public TreeNode getParent() {
			if (folder == null) {
				return null;
			} else {
				if (presetFolders.contains(folder)) {
					return new PresetTreeNode(null);
				} else {
					return new PresetTreeNode(folder.getParentFile());
				}
			}
		}

		@Override
		public boolean isLeaf() {
			return getChildCount() == 0;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PresetTreeNode) {
				PresetTreeNode p = (PresetTreeNode)obj;
				if (folder == null || p.folder == null) {
					return folder == p.folder;
				} else {
					return folder.equals(p.folder);
				}
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			if (folder == null) {
				return super.hashCode();
			} else {
				return folder.hashCode();
			}
		}
		
		@Override
		public String toString() {
			if (folder == null) {
				return "Presets";
			} 
			String name = null;
			if (presetRoot.equals(getParent())) {
				File parent = folder.getParentFile().getParentFile();
				URI uri = parent.toURI().relativize(folder.toURI());
				name = uri.toString();
			} else {
				name = folder.getName();
			}
			File[] filesArray = folder.listFiles(SUPPORTED_FILES_FILTER);
			if (filesArray != null && filesArray.length != 0) {
				name += " (" + filesArray.length + ")";
			}
			return name;
		}
		
	}
	
	private class FileListCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof File) {
				File f = (File)value;
				setText(f.getName());
			}
			if (result instanceof JLabel) {
				((JLabel) result).setIcon(fileIcon);
			}
			return result;
		}
		
	}
	
	private class PresetTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (result instanceof JLabel) {
				((JLabel) result).setIcon(folderIcon);
			}
			return result;
		}
		
	}
	
	private class FileModel extends AbstractListModel {

		private static final long 	
			serialVersionUID = 1L;

		@Override
		public Object getElementAt(int index) {
			return folderFiles.get(index);
		}

		@Override
		public int getSize() {
			return folderFiles.size();
		}
		
	}
	
	private class DoubleClickLoadListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				loadSelectedFile();
			}
		}
		
	}
	
	
	private class SupportedFilesFilter implements FilenameFilter {

		@Override
		public boolean accept(File parent, String name) {
			return name.toLowerCase().endsWith(".obj");
		}
		
	}
	
	private class FoldersFilesFilter implements FilenameFilter {

		@Override
		public boolean accept(File parent, String name) {
			File f = new File(parent, name);
			return f.isDirectory();
		}
		
	}
	
	public void updateStates() {
		presetTree.setModel(new DefaultTreeModel(presetRoot, false));
		fileList.setModel(new FileModel());
	}
	
	protected File getSelectedPresetFolder() {
		TreePath path = presetTree.getSelectionPath();
		if (path == null) return null;
		PresetTreeNode node = (PresetTreeNode)path.getLastPathComponent();
		return node.folder;
	}
	
	public void updateFiles() {
		File folder = getSelectedPresetFolder();
		folderFiles.clear();
		if (folder != null) {
			File[] filesArray = folder.listFiles(SUPPORTED_FILES_FILTER);
			List<File> files = Arrays.asList(filesArray);
			folderFiles.addAll(files);
		}
		fileList.setModel(new FileModel());
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if (presetTree.getSelectionModel() == e.getSource()) {
			updateFiles();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
		if (addFolderButton == e.getSource()) {
			File selectedFolder = getSelectedPresetFolder();
			if (selectedFolder != null) {
				locationChooser.setCurrentDirectory(selectedFolder.getParentFile());
			}
			int result = locationChooser.showOpenDialog(w);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			for (File f : locationChooser.getSelectedFiles()) {
				presetFolders.add(f);
			}
			updateStates();
			updateFiles();
		}
		if (removeFolderButton == e.getSource()) {
			File selectedFolder = getSelectedPresetFolder();
			if (selectedFolder != null) {
				presetFolders.remove(selectedFolder);
				updateStates();
				updateFiles();
			}
		}
		if (loadButton == e.getSource()) {
			loadSelectedFile();
		}
	}

	private void loadSelectedFile() {
		Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
		File selectedFile = (File)fileList.getSelectedValue();
		ReaderOBJ objReader = new ReaderOBJ();
		try {
			SceneGraphComponent c = objReader.read(selectedFile);
			IndexedFaceSet g = (IndexedFaceSet)getFirstGeometry(c);
			IndexedFaceSetUtility.calculateAndSetNormals(g);
			hif.set(g);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(w, "Could not load file " + selectedFile.getName() + "\n" + e1.getMessage());
			return;
		}
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "folders", presetFolders);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		presetFolders = c.getProperty(getClass(), "folders", presetFolders);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		updateStates();
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentSupport(ContentType.Raw);
		v.addContentUI();
		v.setPropertiesResource(PresetContentLoader.class, null);
		v.setPropertiesFile("PresetContentLoader.xml");
		v.registerPlugin(PresetContentLoader.class);
		v.startup();
	}
	
}
