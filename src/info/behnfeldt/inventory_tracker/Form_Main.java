package info.behnfeldt.inventory_tracker;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Name:   Form_Main.java
 * By:     Reed Behnfeldt
 * Date:   04/29/2019
 *
 * Description:
 * This is the main form's class and acts as a central
 * hub for all the calls for the components and functions
 * needed to complete the goal this program set out to
 * complete.
 *
 * WIP Features:
 * -Proper log file output.
 * -CSV File Exporting
 * -Emergency local backup in case of failures
 * -Undo button
 *
 * @extends JFrame
*/
public class Form_Main extends JFrame{

    private int selectedDepartment;                                                      //Stores the currently selected department
    private int selectedItem;                                                            //Stores the currently selected item
    private String[] connectionInfo;                                                     //Stores data about our database connection
    private JMenuBar menuBar = new JMenuBar();                                           //The main menu bar
    private JMenu fileMenu = new JMenu("File");                                       //Menu category: File
    private JMenu editMenu = new JMenu("Edit");                                       //Menu category: Edit
    private JMenuItem exitMenuItem = new JMenuItem("Exit");                         //Menu button: Exit
    private JMenuItem saveMenuItem = new JMenuItem("Save");                         //Menu button: Save
    private JMenuItem undoMenuItem = new JMenuItem("Undo");                         //Menu button: Undo
    private JMenuItem addDepartmentButton = new JMenuItem("Add Department");        //Menu button: Add Department
    private JMenuItem editDepartmentButton = new JMenuItem("Edit Department");      //Menu button: Edit Department
    private JMenuItem deleteDepartmentButton = new JMenuItem("Delete Department");  //Menu button: Delete Department
    private JMenuItem addItemButton = new JMenuItem("Add Item");                    //Menu button: Add Item
    private JMenuItem editItemButton = new JMenuItem("Edit Selected Item");         //Menu button: Edit Item
    private JMenuItem deleteItemButton = new JMenuItem("Delete Selected Item");     //Menu button: Delete Item
    private JPanel mainPanel;                                                            //Main form area that holds all components
    private JList departmentList;                                                        //The main department list GUI
    private DefaultListModel departmentData = new DefaultListModel();                    //Object to manipulate the department list
    private ArrayList<Department> departments = new ArrayList<>();                       //Array to store our departments
    private ArrayList<Department> modifiedDepartments = new ArrayList<>();               //Departments that need updated in the database
    private ArrayList<Department> newDepartments = new ArrayList<>();                    //Departments that need inserted in the database
    private ArrayList<Department> deletedDepartments = new ArrayList<>();                //Array to store deleted departments
    private HashMap<Item, Department> modifiedItems = new HashMap<>();                   //Items that need updated in the database
    private HashMap<Item, Department> newItems = new HashMap<>();                        //Items that need inserted in the database
    private ArrayList<Item> deletedItems = new ArrayList<>();                            //Array to store deleted items
    private JList itemList;                                                              //The main item list GUI
    private DefaultListModel itemData = new DefaultListModel();                          //Used to manipulate the item list
    private JTextArea descriptionText;                                                   //Area to display an item's information
    private SQLHandler sqlhandler = new SQLHandler();                                    //Used to run MySQL queries
    private Connection connector;                                                        //Make the handshake to the database
    private PreparedStatement statement;                                                 //Stores the query statement
    private ResultSet rs;                                                                //Stores the results of the query


    /**
     * Form_Main() is the method that initializes the form
     * normally from the Main class due to the lack of methods
     * that we allow the parent to use. All listeners are
     * added in Form_Main() for each component that requires
     * listeners.
     */
    public Form_Main() {
        createMenu();                                       //Create the menu bar and its content
        add(mainPanel);                                     //Add the main GUI to the form
        setTitle("Inventory Tracker");                      //Set the title of the main window
        setSize(800, 500);                    //Set the size of the main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);     //Set the close button to close the form
        departmentList.setModel(departmentData);            //Sets what data the departmentList gets information from
        itemList.setModel(itemData);                        //Sets what data the itemList gets information from
        populateDepartmentList();                           //Gets all the departments in the database and adds it to the list

        //Listens for selection changes made in the department list
        departmentList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //Prevents the listener from running twice for mouse up and mouse down
                if (!e.getValueIsAdjusting()) {
                    selectedDepartment = departmentList.getSelectedIndex();     //Stores what department is selected
                    refreshItemList();                                          //Refresh the item list
                }
            }
        });

        //Listens for selection changes made in the items list
        itemList.addListSelectionListener(new ListSelectionListener() {
            @Override
            //Prevents the listener from running twice for mouse up and mouse down
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    //Stores the currently selected item
                    selectedItem = itemList.getSelectedIndex();
                    //Sets the item description text area using the string from loadItemDescription()
                    descriptionText.setText(loadItemDescription(selectedDepartment, selectedItem));
                }
            }
        });

        //Listener for the 'Exit' menu item. Simply closes the main form
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        /*
         * Listener for the 'Save' menu item. Parses through the 4 lists: departments, items, deletedItems, and
         * deletedDepartments. When parsing through departments and items, these classes have a boolean set that marks
         * if the class has changed which is checked with the method isModified(). All instances of Department and Item
         * that were initialized with a given ID are assumed to already be in the database and will be updated in the
         * database accordingly. Those without ID's will be inserted into the database as a new Item entry. Anything in
         * the lists deletedDepartments and deletedItems will be deleted from the database completely.
        */
        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });

        /**
         * WIP
         */
        undoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //WIP
            }
        });

        //Listener for the 'Add Department' button (in the 'Edit' menu).
        addDepartmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Prompt the user for a department name and store their response.
                String response = JOptionPane.showInputDialog("Enter the department name to add:");
                //If the user hit the cancel button
                if (response != null) {
                    //If their response is not blank
                    if (!response.isBlank()) {
                        departments.add(new Department(response));      //Add the department to the departments list
                        refreshDepartmentList();                        //Refresh the list so the user sees the changes
                    } else {
                        //Show user this error if their input was blank
                        JOptionPane.showMessageDialog(null, "Error: name cannot be blank.");
                    }
                }
            }
        });

        //Listener for the 'Edit Item' menu button (in the 'Edit' menu).
        editDepartmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*
                See if the user has selected a department at all. Then an Input Dialog will display with their selected
                department to revise the name how they see fit.
                */
                if(selectedDepartment != -1) {
                    String response = JOptionPane.showInputDialog("Edit department name:",
                            departments.get(selectedDepartment).getName());
                    if (response != null) {
                        //If the users response is not blank
                        if (!response.isEmpty()) {
                            departments.get(selectedDepartment).setName(response);   //Set the department name
                            refreshDepartmentList();                                                //Refresh the changes for the user
                        } else {
                            JOptionPane.showMessageDialog(null, "Error: name cannot be blank.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "You must select a department to edit.");
                }
            }
        });

        //Listener for the 'Delete Department' button (in the 'Edit' menu).
        deleteDepartmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Check if the user selected a department then store the department object in a variable marked for deletion.
                if (selectedDepartment != -1) {
                    Department departmentToDelete = departments.get(selectedDepartment);
                    //Get the users confirmation that they would like to delete the department they have selected.
                    int response = JOptionPane.showConfirmDialog(null, "Delete department " +
                            departmentToDelete.getName() + "? All items within will also be deleted.");
                    if (response == JOptionPane.YES_OPTION) {           //If the user wants to delete the department
                        deletedDepartments.add(departmentToDelete);     //Add the department object to the deleted departments list
                        departments.remove(departmentToDelete);         //Remove the department from the main list
                        refreshDepartmentList();                        //Refresh the department list for the user
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No item selected.");
                }
            }
        });

        //Listener for the 'Add Item' button (in the 'Edit' menu).
        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog jDialog = new New_Item(departments);    //Opens a new form window to add the new item
                jDialog.setModal(true);                         //Sets the window as active over the main window
                //jDialog.setResizable(false);                  //Prevent the user from resizing the window
                jDialog.setVisible(true);                       //Set the form to be visible
                refreshItemList();                              //Refresh the item list after the user closes the item form
            }
        });

        //Listener for the 'Edit Item' button (in the 'Edit' menu).
        editItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Check if the user has selected an item to edit
                if (selectedItem != -1) {
                    //Open a new form window and send the item location that needs to be edited.
                    JDialog jDialog = new New_Item(departments, selectedDepartment, selectedItem);
                    jDialog.setModal(true);             //Sets the window as active over the main window
                    //jDialog.setResizable(false);      //Prevent the user from resizing the window
                    jDialog.setVisible(true);           //Set the form to be visible
                    refreshItemList();                  //Refresh the item list after the user closes the item form
                } else {
                    JOptionPane.showMessageDialog(null, "Please select an item to edit.");
                }
            }
        });

        //Listener for the 'Delete Item' button (in the 'Edit' menu).
        deleteItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Check if the user has selected an item to be deleted. The reference to the item is stored in a variable for later
                if (selectedItem != -1) {
                    Item itemToDelete = departments.get(selectedDepartment).getItem(selectedItem);
                    //Confirm with the user if they wish to delete the selected item then check if they said YES.
                    int response = JOptionPane.showConfirmDialog(null, "Delete item " + itemToDelete.getName() + "?");
                    if (response == JOptionPane.YES_OPTION) {
                        deletedItems.add(itemToDelete);                                //Add the item to the deleted items list
                        departments.get(selectedDepartment).removeItem(itemToDelete);  //Remove the item from the main list
                        refreshItemList();                                             //Refresh the item list for the user
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No item selected.");
                }
            }
        });

    }

    /**
     * Method used to populate the main menu bar at the top of
     * the main form.
     */
    private void createMenu(){
        menuBar.add(fileMenu);                  //Adds the 'File' category
        menuBar.add(editMenu);                  //Adds the 'Edit' category
        fileMenu.add(saveMenuItem);             //Adds the 'Save' button
        fileMenu.add(exitMenuItem);             //Adds the 'Exit' button
        //editMenu.add(undoMenuItem);           //Adds the 'Undo' button
        //editMenu.add(new JSeparator());       //Adds a separator in the 'Edit' category
        editMenu.add(addDepartmentButton);      //Adds the 'Add Department' button
        editMenu.add(editDepartmentButton);     //Adds the 'Edit Department' button
        editMenu.add(deleteDepartmentButton);   //Adds the 'Delete Department' button
        editMenu.add(new JSeparator());         //Adds a separator in the 'Edit' category
        editMenu.add(addItemButton);            //Adds the 'Add Item' button
        editMenu.add(editItemButton);           //Adds the 'Edit Item' button
        editMenu.add(deleteItemButton);         //Adds the 'Delete Item' button
        setJMenuBar(menuBar);                   //Sets the menu bar onto the main form for use
    }

    /**
     * Initializes our departments array and then adds the
     * department choices to our Department Selection List.
     */
    private void populateDepartmentList(){
        try {
            int i = 0;
            //This block of code connects to the database then executes a query to select everyithing from the departments table
            connectionInfo = sqlhandler.getConnectionInfo();
            connector = DriverManager.getConnection(connectionInfo[0], connectionInfo[1], connectionInfo[2]);
            statement = connector.prepareStatement("SELECT * FROM departments");
            rs = statement.executeQuery();

            /*
            * While there is still rows to read from the query, add a new department instance to the departments array
            * then add the department to the Department Selection List
            */
            while (rs.next()) {
                departments.add(new Department(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
                departmentData.addElement(departments.get(i).getName());
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: Cannot load application. Check the connection configuration file.");
        } finally {
            sqlhandler.closeConnections(connector, statement, rs);
        }
    }

    /**
     * Refreshes the department list data by presenting the user
     * with the current iteration of the departments array
     */
    private void refreshDepartmentList(){
        departmentData.clear();
        for (Department department : departments) {
            departmentData.addElement(department.getName());
        }
    }

    /**
     * Refreshes the item list data by presenting the
     * user with the current iteration of the items list
     * of the department selected.
     */
    private void refreshItemList(){
        //Clear the data in the item list
        itemData.clear();
        /*
        Checks if a department is selected first then loops through the selected departments items and adds the ID
        and name to the item list
        */
        if(selectedDepartment != -1) {
            for (Item item : departments.get(selectedDepartment).getAllItems()) {
                itemData.addElement(String.format("%06d", item.getQuantity()) + "   " + item.getName());
            }
        }
    }

    /**
     * Method to call to retrieve the string data of a certain item in the
     * items array.
     *
     * @param departmentIndex
     * @param itemIndex
     * @return String
     */
    private String loadItemDescription(int departmentIndex, int itemIndex){
        String s = "";
        if(itemIndex != -1) {
            s += "Name: " + departments.get(departmentIndex).getItem(itemIndex).getName() + "\n";
            s += "Quantity: " + departments.get(departmentIndex).getItem(itemIndex).getQuantity() + "\n\n";
            s += departments.get(departmentIndex).getItem(itemIndex).getDescription();
        } else {
            s = "";
        }

        return s;
    }

    /**
     * Main method to call when the application must be saved. Saved data is split into 6 different groups that only
     * temporary holds the references to the respected object:
     * 1. Departments that needs updated
     * 2. Departments that need inserted
     * 3. Departments that need deleted
     * 4. Items that need updated
     * 5. Items that need inserted
     * 6. Items that need deleted
     *
     * saveData() only splits the data into their respective groups and then calls on saveDepartments() and saveItems()
     * to update the database.
     */
    private void saveData() {
        //Parse through the departments array
        for (Department department : departments){
            //If the department has not received an ID, which indicates it has never been in the database
            if (department.getID() != 0) {
                //If the department has already been in the database but was modified later
                if (department.isModified()) {
                    modifiedDepartments.add(department);
                }
            } else {
                newDepartments.add(department);
            }

            //Parse through the department items array
            for (Item item : department.getAllItems()) {
                //If the item has not received an ID, which indicates it has never been in the database
                if (item.getID() != 0) {
                    //If the item has already been in the database but was modified later
                    if (item.isModified()) {
                        //We need to store both the item and its department in a HashTable to retrieve the item's department_id
                        modifiedItems.put(item, department);
                    }
                } else {
                    //We need to store both the item and its department in a HashTable to retrieve the item's department_id
                    newItems.put(item, department);
                }
            }
        }

        //Call the methods to finalize changes into the database then reload the applications data from the database
        saveDepartments();
        saveItems();
        reloadAllContent();
    }

    /**
     * Prerequisite: saveData()
     *
     * Used to parse through 3 department lists of departments that needs to be inserted into the database, departments
     * that needs updated in the database, and departments that needs deleted from the database
     */
    private void saveDepartments(){
        try {
            //Establishes a connection to the database
            connectionInfo = sqlhandler.getConnectionInfo();
            connector = DriverManager.getConnection(connectionInfo[0], connectionInfo[1], connectionInfo[2]);

            /*Using the established SQL prepared statement, parses through the modifiedDepartments list and updates the
            department data in the database*/
            if(!modifiedDepartments.isEmpty()) {
                statement = connector.prepareStatement("UPDATE departments SET name = ? WHERE id = ?");
                for (Department department : modifiedDepartments) {
                    statement.setString(1, department.getName());
                    statement.setInt(2, department.getID());
                    statement.addBatch();
                }
                //Execute the batch, sent to console of the update success, then clear the modifiedDepartments list
                statement.executeBatch();
                System.out.println("Departments updated in the database");
                modifiedDepartments.clear();
            }

            /*Out of the 6 lists, this list of departments to be inserted into the database must be parsed differently
            * than the rest of the lists. We must first insert the department, but each iteration we must select the
            * department that was must inserted into the database, then retrieve the department's newly generated ID so
            * that we can set the item department ids in the database when we go to insert and update the item lists.*/
            if(!newDepartments.isEmpty()) {
                for (Department department : newDepartments) {
                    statement = connector.prepareStatement("INSERT INTO departments (name, description) VALUES (?,'')");
                    statement.setString(1, department.getName());
                    statement.executeUpdate();

                    //Find the department just added and add the department's ID to the department object instance.
                    statement = connector.prepareStatement("SELECT * FROM departments WHERE name = ?");
                    statement.setString(1, department.getName());
                    rs = statement.executeQuery();
                    rs.next();
                    department.setID(rs.getInt("id"));
                }
                //Print in the console the success of inserting the new departments then clear the newDepartments list.
                System.out.println("Departments inserted in the database");
                newDepartments.clear();
            }

            /*Using the established SQL prepared statement, parses through the deletedDepartments list and delete the
            department data in the database*/
            if(!deletedDepartments.isEmpty()) {
                statement = connector.prepareStatement("DELETE FROM departments WHERE id = ?");
                for (Department department : deletedDepartments) {
                /*First we must move all remaining items to the deletedItems list to be deleted from the database and
                remove clutter.*/
                    for (Item item : department.getAllItems()) {
                        deletedItems.add(item);
                    }
                    department.removeAllItems();

                    //If the department ID equals 0, then there is nothing to delete in the database
                    if (department.getID() != 0) {
                        statement.setInt(1, department.getID());
                        statement.addBatch();
                    }
                }
                //Execute the batch of departments that need deleted, clear the deletedDepartments list and send confirmation to the console
                statement.executeBatch();
                System.out.println("Departments deleted in the database");
                deletedDepartments.clear();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: Departments not saved properly!");
        } finally {
            sqlhandler.closeConnections(connector, statement, rs);
        }
    }

    /**
     * Prerequisite: saveData(), saveDepartments()
     *
     * Used to parse through 3 item lists of items that needs to be inserted into the database, items that needs updated
     * in the database, and items that needs deleted from the database.
     */
    private void saveItems(){
        try {
            //Establishes a connection to the database
            connectionInfo = sqlhandler.getConnectionInfo();
            connector = DriverManager.getConnection(connectionInfo[0], connectionInfo[1], connectionInfo[2]);

            /*Using the established SQL prepared statement, parses through the modifiedItems list and updates the
            item data in the database*/
            if(!modifiedItems.isEmpty()) {
                statement = connector.prepareStatement("UPDATE items SET name = ?, quantity = ?, department_id = ?, description = ? WHERE id = ?");
                for (Item item : modifiedItems.keySet()) {
                    statement.setString(1, item.getName());
                    statement.setInt(2, item.getQuantity());
                    statement.setInt(3, modifiedItems.get(item).getID());
                    statement.setString(4, item.getDescription());
                    statement.setInt(5, item.getID());
                    statement.addBatch();
                }
                //Execute the batch of items that need updated, clear the modifiedItems list and send confirmation to the console
                statement.executeBatch();
                System.out.println("Items updated in the database");
                modifiedItems.clear();
            }

            /*Using the established SQL prepared statement, parses through the newItems list and inserts the item data
            in the database*/
            if(!newItems.isEmpty()) {
                statement = connector.prepareStatement("INSERT INTO items (description, department_id, quantity, name) VALUES (?,?,?,?)");
                for (Item item : newItems.keySet()) {
                    statement.setString(1, item.getDescription());
                    statement.setInt(2, newItems.get(item).getID());
                    statement.setInt(3, item.getQuantity());
                    statement.setString(4, item.getName());
                    statement.addBatch();
                }
                //Execute the batch of items that need inserted, clear the newItems list and send confirmation to the console
                statement.executeBatch();
                System.out.println("Items inserted in the database");
                newItems.clear();
            }

            /*Using the established SQL prepared statement, parses through the deletedItems list and deletes the item
            data in the database*/
            if(!deletedItems.isEmpty()) {
                statement = connector.prepareStatement("DELETE FROM items WHERE id = ?");
                for (Item item : deletedItems) {
                    if (item.getID() != 0) {
                        statement.setInt(1, item.getID());
                        statement.addBatch();
                    }
                }
                //Execute the batch of items that need deleted, clear the deletedItems list and send confirmation to the console
                statement.executeBatch();
                System.out.println("Items deleted in the database");
                deletedItems.clear();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: Items not saved properly!");
        } finally {
            sqlhandler.closeConnections(connector, statement, rs);
        }
    }

    /**
     * Fully reloads the department and item instances with data that is currently in the database. All data not backedup
     * beforehand will be lost.
     *
     * WIP to create local CSV file backups in case data is not saved properly.
     */
    private void reloadAllContent() {
        departmentData.clear();
        departments.clear();
        populateDepartmentList();
        departmentList.setSelectedIndex(selectedDepartment);
    }
}
