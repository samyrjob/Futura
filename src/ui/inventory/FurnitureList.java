package ui.inventory;

import object.Furniture;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the furniture inventory data
 * Handles loading, selection, and querying furniture items
 */
public class FurnitureList {
    
    private final List<Furniture> items;
    private Furniture selectedFurniture;
    
    public FurnitureList() {
        this.items = new ArrayList<>();
        loadFurnitureFromRes();
    }
    
    private void loadFurnitureFromRes() {
        // TODO: Dynamically scan /res folder for PNG files
        items.add(new Furniture("Chair", "/res/tile/Chair_base.png", 1, 1));
        // Add more furniture as needed
    }
    
    public void selectFurniture(int index) {
        if (index >= 0 && index < items.size()) {
            selectedFurniture = items.get(index);
        }
    }
    
    public void clearSelection() {
        selectedFurniture = null;
    }
    
    public Furniture getSelected() {
        return selectedFurniture;
    }
    
    public boolean hasSelection() {
        return selectedFurniture != null;
    }
    
    public Furniture getFurnitureAt(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }
    
    public int size() {
        return items.size();
    }
    
    public List<Furniture> getAll() {
        return new ArrayList<>(items); // Return copy for safety
    }
    
    public boolean isSelected(Furniture furniture) {
        return furniture == selectedFurniture;
    }
}
