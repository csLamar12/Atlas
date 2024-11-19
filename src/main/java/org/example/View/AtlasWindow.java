package org.example.View;

import org.example.Controller.ImageBasedPreview;
import org.example.Controller.TextBasedPreview;
import org.example.Controller.VideoBasedPreview;
import org.example.Model.FileNode;
import javafx.scene.media.MediaException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AtlasWindow extends JFrame {
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private Map<Integer, FileNode> fileNodeMap = new HashMap<>();
    // Header Panel components
    private JPanel headerPanel, pSPanel, previewPanel, summaryPanel;
    private JButton backBtn, forwardBtn;
    private JScrollPane scrollPane;
    private JSplitPane splitPane;
    private String workingDirectory;
    private JLabel currentDirectory, addAccountbtn;
    VideoBasedPreview videoVideoBasedPreview;

    public AtlasWindow() {
        setTitle("Atlas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        // Show drive Panels with available volumes

        // When folders are available for viewing
        initHeader();
        initTable();
        initPreview_Summary();
        initAddressBar();

        revalidate();
        repaint();
    }

    public void initHeader(){
        headerPanel = new JPanel();
        headerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        headerPanel.setPreferredSize(new Dimension(800, 60));

        backBtn = new JButton("<");
        forwardBtn = new JButton(">");
        backBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        forwardBtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forwardBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addAccountbtn = new JLabel("<html><u>Add Account</u></html>");
        addAccountbtn.setFont(new Font("Tahoma", Font.BOLD, 12));
        addAccountbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addAccountbtn.setForeground(Color.BLUE);


        c.insets = new Insets(5, 5, 5, 0);
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 0;
        headerPanel.add(backBtn, c);
        c.gridx = 1;
        headerPanel.add(forwardBtn, c);
        c.gridx = 2;
        c.insets = new Insets(5, 0, 5, 10);
        c.anchor = GridBagConstraints.LINE_END;
        c.weightx = 1;
        headerPanel.add(addAccountbtn, c);
        add(headerPanel, BorderLayout.NORTH);
    }


    public void initPreview_Summary(){
        pSPanel = new JPanel();
        pSPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        pSPanel.setLayout(new BorderLayout());

        JLabel pPLabel = new JLabel("Preview", JLabel.CENTER);
        pPLabel.setFont(new Font("Tahoma", Font.BOLD, 18));

        // ToDO - create a preview panel
        previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(220, 20));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        previewPanel.setLayout(new BorderLayout());
        // ToDO - Create a summary panel
        summaryPanel = new JPanel();
        summaryPanel.setPreferredSize(new Dimension(220, 280));
        summaryPanel.setLayout(new BorderLayout());
        // TODO - Add them to the pSPanel
        pSPanel.add(pPLabel, BorderLayout.NORTH);
        pSPanel.add(previewPanel, BorderLayout.CENTER);
        pSPanel.add(summaryPanel, BorderLayout.SOUTH);
        add(pSPanel, BorderLayout.EAST);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, pSPanel);
        splitPane.setDividerLocation(800); // Initial position of the divider
        splitPane.setResizeWeight(0.5); // Allows both panels to resize proportionally
        splitPane.setOneTouchExpandable(true); // Adds a small arrow to collapse/expand

        // Add split pane to frame
        add(splitPane);
    }

    public void showPreviewPane(){
        previewPanel.removeAll();
        splitPane.setDividerLocation(480);
    }

    public void initTable(){
        // Set up the table
        String[] columnNames = {"","Name", "Size", "Type","Date Modified"};
        tableModel = new DefaultTableModel(columnNames, 0){
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if(columnIndex == 0) return Icon.class;
                if(columnIndex == 2) return Long.class;
                return String.class;
            }
        };
        fileTable = new JTable(tableModel){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileTable.setFillsViewportHeight(true);
        fileTable.getTableHeader().setReorderingAllowed(false);
        fileTable.setShowGrid(false);
        //ToDo remove header background and add line

        // Adjust icon column
        TableColumn iconColumn = fileTable.getColumnModel().getColumn(0);
        iconColumn.setPreferredWidth(30);  // Set the column width to fit the icon
        iconColumn.setMaxWidth(30);        // Prevent it from expanding
        iconColumn.setResizable(false);    // Disable resizing by the user

        // Enable sorting of table columns
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        fileTable.setRowSorter(sorter);
        sorter.setModel(tableModel);

        sorter.setComparator(2, (o1, o2) -> {
            String size1Str = (String) o1;
            String size2Str = (String) o2;
            long size1InBytes = convertToBytes(size1Str);
            long size2InBytes = convertToBytes(size2Str);
            return Long.compare(size1InBytes, size2InBytes);
        });

        // Custom comparator to place "Folder" at the top
        sorter.setComparator(3, (o1, o2) -> {
            String type1 = (String) o1;
            String type2 = (String) o2;

            if (type1.equals("Folder") && !type2.equals("Folder")) {
                return -1; // Folder should come before non-folders
            } else if (!type1.equals("Folder") && type2.equals("Folder")) {
                return 1; // Non-folder should come after folders
            } else {
                return ((String) o1).compareTo((String) o2); // Default comparison for non-folder items
            }
        });

        // Set row selection feature
        fileTable.setRowSelectionAllowed(true);
        fileTable.setColumnSelectionAllowed(false);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add the table to a scroll pane and add it to the frame
        scrollPane = new JScrollPane(fileTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void initAddressBar(){
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(800, 25));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        currentDirectory = new JLabel();
        panel.add(currentDirectory);
        add(panel, BorderLayout.SOUTH);
    }

    public void expandFolder(List<FileNode> fileNodes){
        tableModel.setRowCount(0);
        fileNodeMap.clear();
        int x = 0;


        for(FileNode fileNode : fileNodes){
            Object[] row = {
                    fileNode.getImg(),
                    fileNode.getName(),
                    fileNode.convertSize(), // Human-readable size string
                    fileNode.getType(),
                    fileNode.getLastModified()
            };
            tableModel.addRow(row);
            fileNodeMap.put(x, fileNode);
            x++;
        }
    }

    public FileNode getFileNodeAt(int modelIndex) {
        return fileNodeMap.get(modelIndex);
    }

    public JTable getFileTable() {
        return fileTable;
    }

    public JButton getBackBtn() {
        return backBtn;
    }

    public JButton getForwardBtn() {
        return forwardBtn;
    }

    public JLabel getAddAccountbtn(){
        return addAccountbtn;
    }

    public void setWorkingDir(String workingDir){
        currentDirectory.setText(workingDir);
        repaint();
    }

    // File Preview Methods
    public void addVideoPreview(String videoPath){
        try {
            videoVideoBasedPreview = new VideoBasedPreview(videoPath);
            previewPanel.add(videoVideoBasedPreview);
        } catch (MediaException e) {
            System.out.println("MediaException: " + e.getMessage());
        }
    }

    public VideoBasedPreview getVideoPreview(){
        return videoVideoBasedPreview;
    }

    public void addTextBasedPreview(File file) {
        TextBasedPreview tbp = new TextBasedPreview(file.getAbsolutePath());

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true); // Wraps by word rather than character
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Set the content from the TextBasedPreview
        textArea.append(tbp.showPreview());
        textArea.setCaretPosition(0);

        // Wrap the JTextArea in a JScrollPane for scrollability
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Add the JScrollPane to the preview panel
        previewPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public void addImageBasedPreview(String imagePath) {
        ImageBasedPreview ibp = new ImageBasedPreview(imagePath);
        JLabel imageLabel = new JLabel(new ImageIcon(ibp.showPreview())); // Set the scaled image to JLabel
        previewPanel.add(imageLabel);
    }

    private long convertToBytes(String sizeStr) {
        long sizeInBytes = 0;
        if (sizeStr.endsWith("KBs")) {
            sizeInBytes = Long.parseLong(sizeStr.replace("KBs", "")) * 1024;
        } else if (sizeStr.endsWith("MBs")) {
            sizeInBytes = Long.parseLong(sizeStr.replace("MBs", "")) * 1024 * 1024;
        } else if (sizeStr.endsWith("GBs")) {
            sizeInBytes = Long.parseLong(sizeStr.replace("GBs", "")) * 1024 * 1024 * 1024;
        } else if (sizeStr.endsWith("Bytes")) {
            sizeInBytes = Long.parseLong(sizeStr.replace("Bytes", ""));
        }
        return sizeInBytes;
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
