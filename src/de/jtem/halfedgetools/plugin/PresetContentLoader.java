package de.jtem.halfedgetools.plugin;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ViewShrinkPanelPlugin;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.Controller;

public class PresetContentLoader extends ViewShrinkPanelPlugin implements ActionListener, ListSelectionListener {

	private HalfedgeInterface
		hif = null;
	private List<File>
		presetFolders = new ArrayList<File>(),
		folderFiles = new ArrayList<File>();
	
	private JList
		presetList = new JList(new FolderModel()),
		fileList = new JList(new FileModel());
	private JScrollPane
		presetScroller = new JScrollPane(presetList),
		fileScroller = new JScrollPane(fileList);
	private JFileChooser
		locationChooser = new JFileChooser();
	private JButton
		addFolderButton = new JButton("Add Folder"),
		loadButton = new JButton("Load");
	
	public PresetContentLoader() {
		shrinkPanel.setTitle("Content Presets");
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		presetScroller.setPreferredSize(new Dimension(10, 100));
		fileScroller.setPreferredSize(new Dimension(10, 100));
		shrinkPanel.setLayout(new GridBagLayout());
		shrinkPanel.add(presetScroller, c);
		c.weighty = 0.0;
		shrinkPanel.add(addFolderButton, c);
		c.weighty = 1.0;
		shrinkPanel.add(fileScroller, c);
		c.weighty = 0.0;
		shrinkPanel.add(loadButton, c);
		
		locationChooser.setFileSelectionMode(DIRECTORIES_ONLY);
		locationChooser.setMultiSelectionEnabled(true);
		
		presetList.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		fileList.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		
		presetList.setCellRenderer(new PresetListCellRenderer());
		fileList.setCellRenderer(new PresetListCellRenderer());
		
		addFolderButton.addActionListener(this);
		loadButton.addActionListener(this);
		presetList.getSelectionModel().addListSelectionListener(this);
	}
	
	private class PresetListCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof File) {
				File f = (File)value;
				setText(f.getName());
			}
			return result;
		}
		
	}
	
	private class FolderModel extends AbstractListModel {

		private static final long 	
			serialVersionUID = 1L;

		@Override
		public Object getElementAt(int index) {
			return presetFolders.get(index);
		}

		@Override
		public int getSize() {
			return presetFolders.size();
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
	
	
	
	private class SupportedFilesFilter implements FilenameFilter {

		@Override
		public boolean accept(File parent, String name) {
			return name.toLowerCase().endsWith(".obj");
		}
		
	}
	
	
	public void updateStates() {
		presetList.setModel(new FolderModel());
		fileList.setModel(new FileModel());
	}
	
	public void updateFiles() {
		File folder = (File)presetList.getSelectedValue();
		if (folder == null) return;
		folderFiles.clear();
		for (String filename : folder.list(new SupportedFilesFilter())) {
			folderFiles.add(new File(folder, filename));
		}
		fileList.setModel(new FileModel());
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (presetList.getSelectionModel() == e.getSource()) {
			updateFiles();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
		if (addFolderButton == e.getSource()) {
			int result = locationChooser.showOpenDialog(w);
			if (result != JFileChooser.APPROVE_OPTION) {
				return;
			}
			for (File f : locationChooser.getSelectedFiles()) {
				presetFolders.add(f);
			}
			updateStates();
		}
		if (loadButton == e.getSource()) {
			File selectedFile = (File)fileList.getSelectedValue();
			ReaderOBJ objReader = new ReaderOBJ();
			try {
				SceneGraphComponent c = objReader.read(selectedFile);
				Geometry g = SceneGraphUtility.getFirstGeometry(c);
				hif.set(g);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(w, "Could not load file " + selectedFile.getName() + "\n" + e1.getMessage());
				return;
			}
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
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentSupport(ContentType.Raw);
		v.addContentUI();
		v.setPropertiesResource(PresetContentLoader.class, null);
		v.registerPlugin(PresetContentLoader.class);
		v.startup();
	}
	
}
