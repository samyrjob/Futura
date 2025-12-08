package admin.ui;

import admin.AdminNetworkClient;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing rooms
 */
public class RoomsPanel extends JPanel {
    
    private AdminNetworkClient client;
    
    private DefaultTableModel tableModel;
    private JTable roomTable;
    private JLabel countLabel;
    
    // Room details panel
    private JTextArea detailsArea;
    
    // Parsing state
    private boolean parsingRooms = false;
    private boolean parsingRoomInfo = false;
    private List<String[]> pendingRooms = new ArrayList<>();
    private StringBuilder roomInfoBuilder = new StringBuilder();
    
    public RoomsPanel(AdminNetworkClient client) {
        this.client = client;
        initializeUI();
    }
    
    public void setClient(AdminNetworkClient client) {
        this.client = client;
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Split pane - rooms list on left, details on right
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        
        // Left panel - room list
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        
        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        countLabel = new JLabel("Rooms: 0");
        countLabel.setFont(new Font("Arial", Font.BOLD, 14));
        toolbar.add(countLabel, BorderLayout.WEST);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        toolbar.add(refreshBtn, BorderLayout.EAST);
        
        leftPanel.add(toolbar, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Room ID", "Players", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        
        roomTable = new JTable(tableModel);
        roomTable.setRowHeight(35);
        roomTable.setFont(new Font("Arial", Font.PLAIN, 13));
        roomTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        
        // Selection listener for details
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = roomTable.getSelectedRow();
                if (row >= 0) {
                    String roomId = (String) tableModel.getValueAt(row, 0);
                    loadRoomDetails(roomId);
                }
            }
        });
        
        // Action buttons
        roomTable.getColumn("Actions").setCellRenderer(new RoomButtonRenderer());
        roomTable.getColumn("Actions").setCellEditor(new RoomButtonEditor(new JCheckBox()));
        
        leftPanel.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        
        // Right panel - room details
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Room Details"));
        
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setText("Select a room to view details");
        
        rightPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE HANDLING
    // ═══════════════════════════════════════════════════════════
    
    public void handleMessage(String message) {
        // Room list parsing
        if (message.equals("ROOMS_START")) {
            parsingRooms = true;
            pendingRooms.clear();
        } else if (message.equals("ROOMS_END")) {
            parsingRooms = false;
            updateTable();
        } else if (message.startsWith("COUNT ") && parsingRooms) {
            int count = Integer.parseInt(message.substring(6));
            countLabel.setText("Rooms: " + count);
        } else if (message.startsWith("ROOM ") && parsingRooms && !message.startsWith("ROOM_")) {
            // Format: ROOM <roomId> <playerCount>
            String[] parts = message.substring(5).split(" ");
            if (parts.length >= 2) {
                pendingRooms.add(parts);
            }
        }
        
        // Room info parsing
        if (message.equals("ROOM_INFO_START")) {
            parsingRoomInfo = true;
            roomInfoBuilder = new StringBuilder();
        } else if (message.equals("ROOM_INFO_END")) {
            parsingRoomInfo = false;
            detailsArea.setText(roomInfoBuilder.toString());
        } else if (parsingRoomInfo) {
            roomInfoBuilder.append(formatInfoLine(message)).append("\n");
        }
    }
    
    private String formatInfoLine(String line) {
        if (line.startsWith("ROOM_ID ")) {
            return "Room ID: " + line.substring(8);
        } else if (line.startsWith("STATUS ")) {
            return "Status: " + line.substring(7);
        } else if (line.startsWith("PLAYER_COUNT ")) {
            return "Player Count: " + line.substring(13);
        } else if (line.startsWith("PLAYER_DETAIL ")) {
            // Format: PLAYER_DETAIL <name> <gender> <x> <y> <direction>
            String[] parts = line.substring(14).split(" ");
            if (parts.length >= 5) {
                return "  • " + parts[0] + " (" + parts[1] + ") at (" + parts[2] + ", " + parts[3] + ") facing " + parts[4];
            }
        }
        return line;
    }
    
    private void updateTable() {
        tableModel.setRowCount(0);
        
        for (String[] room : pendingRooms) {
            String roomId = room[0];
            String playerCount = room[1] + " players";
            
            tableModel.addRow(new Object[]{roomId, playerCount, "Actions"});
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACTIONS
    // ═══════════════════════════════════════════════════════════
    
    public void refresh() {
        if (client != null && client.isAuthenticated()) {
            client.listRooms();
        }
    }
    
    private void loadRoomDetails(String roomId) {
        if (client != null && client.isAuthenticated()) {
            detailsArea.setText("Loading...");
            client.getRoomInfo(roomId);
        }
    }
    
    private void clearRoom(String roomId) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Clear all players from room: " + roomId + "?",
            "Confirm Clear",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.clearRoom(roomId);
            
            Timer timer = new Timer(500, e -> {
                refresh();
                loadRoomDetails(roomId);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // TABLE BUTTON RENDERER/EDITOR
    // ═══════════════════════════════════════════════════════════
    
    private class RoomButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton clearBtn = new JButton("Clear");
        private JButton infoBtn = new JButton("Info");
        
        public RoomButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            clearBtn.setMargin(new Insets(2, 5, 2, 5));
            infoBtn.setMargin(new Insets(2, 5, 2, 5));
            add(infoBtn);
            add(clearBtn);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    
    private class RoomButtonEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        private JButton clearBtn = new JButton("Clear");
        private JButton infoBtn = new JButton("Info");
        private int currentRow;
        
        public RoomButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            
            clearBtn.setMargin(new Insets(2, 5, 2, 5));
            infoBtn.setMargin(new Insets(2, 5, 2, 5));
            
            infoBtn.addActionListener(e -> {
                String roomId = (String) tableModel.getValueAt(currentRow, 0);
                loadRoomDetails(roomId);
                fireEditingStopped();
            });
            
            clearBtn.addActionListener(e -> {
                String roomId = (String) tableModel.getValueAt(currentRow, 0);
                clearRoom(roomId);
                fireEditingStopped();
            });
            
            panel.add(infoBtn);
            panel.add(clearBtn);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }
}