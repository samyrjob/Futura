package admin.ui;

import admin.AdminNetworkClient;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing players
 */
public class PlayersPanel extends JPanel {
    
    private AdminNetworkClient client;
    
    private DefaultTableModel tableModel;
    private JTable playerTable;
    private JLabel countLabel;
    
    // Parsing state
    private boolean parsingPlayers = false;
    private List<String[]> pendingPlayers = new ArrayList<>();
    
    public PlayersPanel(AdminNetworkClient client) {
        this.client = client;
        initializeUI();
    }
    
    public void setClient(AdminNetworkClient client) {
        this.client = client;
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Top toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        
        countLabel = new JLabel("Players: 0");
        countLabel.setFont(new Font("Arial", Font.BOLD, 14));
        toolbar.add(countLabel, BorderLayout.WEST);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        toolbar.add(refreshBtn, BorderLayout.EAST);
        
        add(toolbar, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Username", "Room", "Position", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;  // Only actions column
            }
        };
        
        playerTable = new JTable(tableModel);
        playerTable.setRowHeight(35);
        playerTable.setFont(new Font("Arial", Font.PLAIN, 13));
        playerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        
        // Action buttons in table
        playerTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        playerTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(playerTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        JButton kickSelectedBtn = new JButton("Kick Selected");
        kickSelectedBtn.addActionListener(e -> kickSelected());
        
        JButton moveSelectedBtn = new JButton("Move Selected");
        moveSelectedBtn.addActionListener(e -> moveSelected());
        
        bottomPanel.add(kickSelectedBtn);
        bottomPanel.add(moveSelectedBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE HANDLING
    // ═══════════════════════════════════════════════════════════
    
    public void handleMessage(String message) {
        if (message.equals("PLAYERS_START")) {
            parsingPlayers = true;
            pendingPlayers.clear();
        } else if (message.equals("PLAYERS_END")) {
            parsingPlayers = false;
            updateTable();
        } else if (message.startsWith("COUNT ")) {
            int count = Integer.parseInt(message.substring(6));
            countLabel.setText("Players: " + count);
        } else if (message.startsWith("PLAYER ") && parsingPlayers) {
            // Format: PLAYER <name> <roomId> <x> <y>
            String[] parts = message.substring(7).split(" ");
            if (parts.length >= 4) {
                pendingPlayers.add(parts);
            }
        }
    }
    
    private void updateTable() {
        tableModel.setRowCount(0);
        
        for (String[] player : pendingPlayers) {
            String name = player[0];
            String room = player[1];
            String position = "(" + player[2] + ", " + player[3] + ")";
            
            tableModel.addRow(new Object[]{name, room, position, "Actions"});
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACTIONS
    // ═══════════════════════════════════════════════════════════
    
    public void refresh() {
        if (client != null && client.isAuthenticated()) {
            client.listPlayers();
        }
    }
    
    private void kickSelected() {
        int row = playerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a player");
            return;
        }
        
        String username = (String) tableModel.getValueAt(row, 0);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Kick player: " + username + "?",
            "Confirm Kick",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.kickPlayer(username);
            
            // Refresh after short delay
            Timer timer = new Timer(500, e -> refresh());
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    private void moveSelected() {
        int row = playerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a player");
            return;
        }
        
        String username = (String) tableModel.getValueAt(row, 0);
        
        String roomId = JOptionPane.showInputDialog(
            this,
            "Enter room ID to move " + username + " to:",
            "Move Player",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (roomId != null && !roomId.trim().isEmpty()) {
            client.movePlayer(username, roomId.trim());
            
            Timer timer = new Timer(500, e -> refresh());
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // TABLE BUTTON RENDERER/EDITOR
    // ═══════════════════════════════════════════════════════════
    
    private class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton kickBtn = new JButton("Kick");
        private JButton moveBtn = new JButton("Move");
        
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            kickBtn.setMargin(new Insets(2, 5, 2, 5));
            moveBtn.setMargin(new Insets(2, 5, 2, 5));
            add(kickBtn);
            add(moveBtn);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    
    private class ButtonEditor extends DefaultCellEditor {
        private JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        private JButton kickBtn = new JButton("Kick");
        private JButton moveBtn = new JButton("Move");
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            
            kickBtn.setMargin(new Insets(2, 5, 2, 5));
            moveBtn.setMargin(new Insets(2, 5, 2, 5));
            
            kickBtn.addActionListener(e -> {
                String username = (String) tableModel.getValueAt(currentRow, 0);
                int confirm = JOptionPane.showConfirmDialog(
                    PlayersPanel.this,
                    "Kick player: " + username + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    client.kickPlayer(username);
                    Timer timer = new Timer(500, ev -> refresh());
                    timer.setRepeats(false);
                    timer.start();
                }
                fireEditingStopped();
            });
            
            moveBtn.addActionListener(e -> {
                String username = (String) tableModel.getValueAt(currentRow, 0);
                String roomId = JOptionPane.showInputDialog("Move " + username + " to room:");
                if (roomId != null && !roomId.isEmpty()) {
                    client.movePlayer(username, roomId);
                    Timer timer = new Timer(500, ev -> refresh());
                    timer.setRepeats(false);
                    timer.start();
                }
                fireEditingStopped();
            });
            
            panel.add(kickBtn);
            panel.add(moveBtn);
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
