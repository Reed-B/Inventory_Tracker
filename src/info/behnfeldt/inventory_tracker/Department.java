package info.behnfeldt.inventory_tracker;

import java.sql.*;
import java.util.ArrayList;

/**
 * Name:   Department.java
 * By:     Reed Behnfeldt
 * Date:   05/01/2019
 *
 * Class used to store Department data. Will load its items automatically from the
 * database when initialized with its ID.
 */
public class Department {

    private int id;                                             //department ID
    private String name;                                        //department name
    private String info;                                        //department description
    private String[] connectionInfo;                            //Stores database connection info
    private ArrayList<Item> items = new ArrayList<>();          //Stores Item objects that are in the department
    private boolean hasChanged = false;                         //Marks when the object needs to be saved
    private static SQLHandler sqlhandler = new SQLHandler();    //Used to retrieve the department items from the database
    private Connection connector;                               //Connector for our database
    private PreparedStatement statement;                        //Used to store our SQL statements
    private ResultSet rs;                                       //Used to store query results

    /**
     * Main initializer for the Department class. Will automatically retrieve
     * its items when initialized from this method.
     *
     * @param id
     * @param name
     * @param info
     */
    public Department(int id, String name, String info){
        this.id = id;
        this.name = name;
        this.info = info;
        loadItems();
    }

    /**
     * Secondary initializer when a new department needs to be
     * added to the database by the user.
     *
     * @param name
     */
    public Department(String name){
        this.name = name;
        hasChanged = true;
    }

    /**
     * Called by the Department class only to load its own items into its Item ArrayList
     */
    private void loadItems(){
        try {
            connectionInfo = sqlhandler.getConnectionInfo();
            connector = DriverManager.getConnection(connectionInfo[0], connectionInfo[1], connectionInfo[2]);
            statement = connector.prepareStatement("SELECT * FROM items WHERE department_id = ?");
            statement.setInt(1, id);
            rs = statement.executeQuery();
            //While there is results from the database, add a new Item instance to the items array.
            while (rs.next()) {
                items.add(new Item(rs.getInt("id"), rs.getInt("quantity"), rs.getString("name"),  rs.getString("description")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sqlhandler.closeConnections(connector, statement, rs);
        }
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
    public String getName(){
        return name;
    }

    /**
     * @return
     */
    public String getInfo(){
        return info;
    }

    /**
     * @param index
     * @return
     */
    public Item getItem(int index){
        return items.get(index);
    }

    /**
     * @return
     */
    public ArrayList<Item> getAllItems(){
        return items;
    }

    /**
     * When the item has changed and needs to be saved
     *
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
     * @param name
     */
    public void setName(String name){
        this.name = name;
        hasChanged = true;
    }

    /**
     * @param info
     */
    public void setInfo(String info){
        this.info = info;
        hasChanged = true;
    }

    /**
     * @param item
     */
    public void addItem(Item item){
        items.add(item);
        hasChanged = true;
    }

    /**
     * @param index
     */
    public void removeItem(int index){
        items.remove(index);
        hasChanged = true;
    }

    /**
     * @param item
     */
    public void removeItem(Item item){
        items.remove(item);
        hasChanged = true;
    }

    /**
     * Removes all items from the departments item list
     */
    public void removeAllItems(){
        items.clear();
    }
}
