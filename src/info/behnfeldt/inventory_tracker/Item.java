package info.behnfeldt.inventory_tracker;

/**
 * Name:   Item.java
 * By:     Reed Behnfeldt
 * Date:   05/01/2019
 *
 * Class used to store Item data. Will load its items automatically from the
 * database when initialized with its ID.
 */
public class Item {
    private int id;                     //Item ID
    private int quantity;               //Item quantity
    private String name;                //Item name
    private String description;         //Item description
    private boolean hasChanged = false; //Marks when the Item needs to be saved

    /**
     * Main initializer for the Item class.
     *
     * @param id
     * @param quantity
     * @param name
     * @param description
     */
    public Item(int id, int quantity, String name, String description){
        this.id = id;
        this.quantity = quantity;
        this.name = name;
        this.description = description;
    }

    /**
     * Secondary initializer for the Item class for when a new item is
     * needed to be added into a department. ID should be set by the
     * database only.
     *
     * @param quantity
     * @param name
     * @param description
     */
    public Item(int quantity, String name, String description){
        this.quantity = quantity;
        this.name = name;
        this.description = description;
        hasChanged = true;
    }

    /**
     * @return
     */
    public int getID(){
        return id;
    }

    /**
     * @return
     */
    public int getQuantity(){
        return quantity;
    }

    /**
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * @return
     */
    public String getDescription(){
        return description;
    }

    /**
     * @return
     */
    public boolean isModified(){
        return hasChanged;
    }

    /**
     * @param id
     */
    public void setID(int id){
        this.id = id;
        hasChanged = true;
    }

    /**
     * @param quantity
     */
    public void setQuantity(int quantity){
        this.quantity = quantity;
        hasChanged = true;
    }

    /**
     * @param name
     */
    public void setName(String name){
        this.name = name;
        hasChanged = true;
    }

    /**
     * @param description
     */
    public void setDescription(String description){
        this.description = description;
        hasChanged = true;
    }

}
