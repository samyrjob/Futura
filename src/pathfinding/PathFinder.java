package pathfinding;

import java.util.*;

/**
 * A* Pathfinding Algorithm for Isometric Tile-Based Movement
 * Like Habbo Hotel - finds the shortest path from point A to B
 */
public class PathFinder {
    
    private int maxWorldCol;
    private int maxWorldRow;
    private Node[][] grid;
    private List<Node> openList;
    private List<Node> closedList;
    
    public PathFinder(int maxWorldCol, int maxWorldRow) {
        this.maxWorldCol = maxWorldCol;
        this.maxWorldRow = maxWorldRow;
        instantiateNodes();
    }
    
    private void instantiateNodes() {
        grid = new Node[maxWorldCol][maxWorldRow];
        
        for (int col = 0; col < maxWorldCol; col++) {
            for (int row = 0; row < maxWorldRow; row++) {
                grid[col][row] = new Node(col, row);
            }
        }
    }
    
    public void resetNodes() {
        for (int col = 0; col < maxWorldCol; col++) {
            for (int row = 0; row < maxWorldRow; row++) {
                grid[col][row].reset();
            }
        }
        openList = new ArrayList<>();
        closedList = new ArrayList<>();
    }
    
    public List<Node> findPath(int startCol, int startRow, int goalCol, int goalRow) {
        resetNodes();
        
        // Validate coordinates
        if (!isValid(startCol, startRow) || !isValid(goalCol, goalRow)) {
            return null;
        }
        
        Node startNode = grid[startCol][startRow];
        Node goalNode = grid[goalCol][goalRow];
        
        openList.add(startNode);
        startNode.open = true;
        
        while (!openList.isEmpty()) {
            // Get node with lowest fCost
            Node currentNode = openList.get(0);
            for (Node node : openList) {
                if (node.fCost < currentNode.fCost || 
                    (node.fCost == currentNode.fCost && node.hCost < currentNode.hCost)) {
                    currentNode = node;
                }
            }
            
            // Found the goal
            if (currentNode == goalNode) {
                return retracePath(startNode, goalNode);
            }
            
            openList.remove(currentNode);
            currentNode.open = false;
            closedList.add(currentNode);
            currentNode.checked = true;
            
            // Check all neighbors
            for (Node neighbor : getNeighbors(currentNode)) {
                if (neighbor.checked || neighbor.solid) {
                    continue;
                }
                
                int newMovementCost = currentNode.gCost + getDistance(currentNode, neighbor);
                
                if (newMovementCost < neighbor.gCost || !neighbor.open) {
                    neighbor.gCost = newMovementCost;
                    neighbor.hCost = getDistance(neighbor, goalNode);
                    neighbor.parent = currentNode;
                    
                    if (!neighbor.open) {
                        openList.add(neighbor);
                        neighbor.open = true;
                    }
                }
            }
        }
        
        // No path found
        return null;
    }
    
    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        int col = node.col;
        int row = node.row;
        
        // 8 directions (including diagonals)
        // Up
        if (row > 0) neighbors.add(grid[col][row - 1]);
        // Down
        if (row < maxWorldRow - 1) neighbors.add(grid[col][row + 1]);
        // Left
        if (col > 0) neighbors.add(grid[col - 1][row]);
        // Right
        if (col < maxWorldCol - 1) neighbors.add(grid[col + 1][row]);
        
        // Diagonals
        if (col > 0 && row > 0) neighbors.add(grid[col - 1][row - 1]);
        if (col < maxWorldCol - 1 && row > 0) neighbors.add(grid[col + 1][row - 1]);
        if (col > 0 && row < maxWorldRow - 1) neighbors.add(grid[col - 1][row + 1]);
        if (col < maxWorldCol - 1 && row < maxWorldRow - 1) neighbors.add(grid[col + 1][row + 1]);
        
        return neighbors;
    }
    
    private List<Node> retracePath(Node startNode, Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;
        
        while (currentNode != startNode) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }
        
        Collections.reverse(path);
        return path;
    }
    
    private int getDistance(Node a, Node b) {
        int distX = Math.abs(a.col - b.col);
        int distY = Math.abs(a.row - b.row);
        
        // Diagonal distance
        if (distX > distY) {
            return 14 * distY + 10 * (distX - distY);
        }
        return 14 * distX + 10 * (distY - distX);
    }
    
    private boolean isValid(int col, int row) {
        return col >= 0 && col < maxWorldCol && row >= 0 && row < maxWorldRow;
    }
    
    public void setSolid(int col, int row, boolean solid) {
        if (isValid(col, row)) {
            grid[col][row].solid = solid;
        }
    }
    
    /**
     * Node class for A* pathfinding
     */
    public static class Node {
        public int col;
        public int row;
        public int gCost; // Distance from start
        public int hCost; // Distance to goal
        public int fCost; // gCost + hCost
        public boolean solid;
        public boolean open;
        public boolean checked;
        public Node parent;
        
        public Node(int col, int row) {
            this.col = col;
            this.row = row;
        }
        
        public void reset() {
            this.gCost = 0;
            this.hCost = 0;
            this.fCost = 0;
            this.open = false;
            this.checked = false;
            this.parent = null;
        }
        
        public void updateFCost() {
            fCost = gCost + hCost;
        }
    }
}
