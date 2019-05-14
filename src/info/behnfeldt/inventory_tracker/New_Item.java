package info.behnfeldt.inventory_tracker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Name:   New_Item.java
 * By:     Reed Behnfeldt
 * Date:   05/04/2019
 *
 * Description:
 * This form class is used to help users add and update new and existing
 * items retrieved from the departments that currently exist. Error
 * checking in this class insures the data the user inputs is standardized
 *
 *
 * @extends JDialog
 */
public class New_Item extends JDialog{
    private String nameInputted;                    //Variable to store user input for the Item name
    private int quantityInputted;                   //Variable to store user input for the Item quantity
    private int selectedDepartment;                 //Variable to store user input for the Item department
    private String descriptionInputted = "";        //Variable to store user input for the Item description
    private JTextArea itemDescriptionInput;         //Text area to input the item description
    private JPanel mainPanel;                       //The main parent for all the components of the form
    private JTextField itemNameInput;               //Text field to input the item's name
    private JTextField itemQuantityInput;           //Text field to input the item's quantity
    private JButton addItemButton;                  //Button that finalizes the users input and adds/edits the item desired
    private JComboBox departmentSelectionList;      //Combo box to select an existing department where the item is stored
    private DefaultComboBoxModel departmentData = new DefaultComboBoxModel();   //Variable that holds the department selection data
    private ArrayList<Department> departments;      //Array to hold existing departments passed to this form from the parent object

    /**
     * The New_Item() initializer for when it is required that a new
     * item is needed to be added into one of the departments in the
     * department array. Gives the user a GUI to add the necessary
     * information needed for the item to initialize.
     *
     * @param depArray (Department ArrayList)
     */
    public New_Item(ArrayList<Department> depArray){
        add(mainPanel);                            //Add the main gui to the form
        setTitle("Add New Item");                  //Set the title of the main window
        setSize(450, 400);           //Set the size of the main window
        this.departments = depArray;

        departmentSelectionList.setModel(departmentData);   //Set the department combo box's data model
        loadDepartmentSelectionList();                      //Load the departments into the combo box

        //Listener for the 'Add Item' button when it is activated
        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Check and make sure that there is a description for the item and a name inputted by the user
                if(!itemDescriptionInput.getText().equals("") && !itemNameInput.getText().equals("")) {
                    //Add a new item instance to the selected department
                    departments.get(selectedDepartment).addItem(new Item(quantityInputted, nameInputted, descriptionInputted));
                    dispose(); //Close the add item form
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Name or Description is blank.");
                }
            }
        });

        //Listener for when the user moves off of the Name input field
        itemNameInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                //Makes sure the name is valid by removing destructive characters
                validateName();
            }
        });

        //Listener for when the user moves off the Quantity input field
        itemQuantityInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                //Checks to see if the quantity entered is a valid integer
                validateQuantity();
            }
        });

        //Listener for when the user moves off the Item Description text area
        itemDescriptionInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                //Makes sure the description text is valid by removing destructive characters
                validateDescription();
            }
        });

        //Listener for when the user has selected a new department
        departmentSelectionList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Variable that stores the currently selected department
                selectedDepartment = departmentSelectionList.getSelectedIndex();
            }
        });
    }

    /**
     * This initialization of the New_Item form is a modification added
     * to allow the user to use the same familiar form that adds items, but
     * this instance of the New_Item() method allows the parent to pass the
     * department index and the index of the item in the department that
     * needs to be revised. After the user hits the 'Edit Item' button, the
     * form will set the variables for the item instance. The same error
     * checking for adding an item is still performed when revising an item.
     *
     * @param depArray (Department ArrayList)
     * @param originalDepartmentIndex
     * @param departmentItemIndex
     */
    public New_Item(ArrayList<Department> depArray, int originalDepartmentIndex, int departmentItemIndex){
        add(mainPanel);                         //Add the main gui to the form
        setTitle("Edit Item");                  //Set the title of the main window
        addItemButton.setText("Edit Item");     //Set the window title
        setSize(450, 400);        //Set the size of the main window
        this.departments = depArray;

        departmentSelectionList.setModel(departmentData);   //Set the department combo box's data model
        loadDepartmentSelectionList();                      //Load the departments into the combo box

        /*
         * The below code first retrieves the item that needs revised and
         * passes the reference to that item into a new variable so we can
         * easily retrieve and revise the item in question. After we get
         * the data for the item, we set the appropriate input fields of
         * the form for the user to review and revise
         */
        Item currentItem = depArray.get(originalDepartmentIndex).getItem(departmentItemIndex);
        nameInputted = currentItem.getName();
        itemNameInput.setText(nameInputted);
        departmentSelectionList.setSelectedIndex(originalDepartmentIndex);
        quantityInputted = currentItem.getQuantity();
        itemQuantityInput.setText(quantityInputted + "");
        descriptionInputted = currentItem.getDescription();
        itemDescriptionInput.setText(descriptionInputted);

        //Listener for the 'Edit Item' button when it is activated
        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Check and make sure that there is a description for the item and a name inputted by the user
                if(!itemDescriptionInput.getText().equals("") && !itemNameInput.getText().equals("")) {
                    currentItem.setName(nameInputted);                  //Set the new name of the item
                    currentItem.setQuantity(quantityInputted);          //Set the new quantity of the item
                    currentItem.setDescription(descriptionInputted);    //Set the new description of the item

                    //Check to see if the user has selected a new department to store the item in
                    if(originalDepartmentIndex != selectedDepartment){
                        departments.get(selectedDepartment).addItem(currentItem);                   //Add item to the new department
                        departments.get(originalDepartmentIndex).removeItem(departmentItemIndex);   //Remove item from the original department
                    }
                    dispose();  //Close the Edit Item form
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Name or Description is blank.");
                }
            }
        });

        //Listener for when the user moves off of the Name input field
        itemNameInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                //Makes sure the name is valid by removing destructive characters
                validateName();
            }
        });

        //Listener for when the user moves off the Quantity input field
        itemQuantityInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                //Checks to see if the quantity entered is a valid integer
                validateQuantity();
            }
        });

        //Listener for when the user moves off the Item Description text area
        itemDescriptionInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                //Makes sure the description text is valid by removing destructive characters
                validateDescription();
            }
        });

        //Listener for when the user has selected a new department
        departmentSelectionList.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //Variable that stores the currently selected department
                selectedDepartment = departmentSelectionList.getSelectedIndex();
            }
        });
    }

    /**
     * Validates and removes harmful characters the user
     * could have inputted into the item name value. Sets
     * the 'nameInputted' variable of the new string without
     * the harmful characters. Will also set the Name input
     * text field to show the changes to the user in case
     * there are discrepancies caused by this check.
     */
    private void validateName(){
        nameInputted = itemNameInput.getText().replaceAll("[^A-Za-z0-9 :%$&!.?,\\n]", "");
        itemNameInput.setText(nameInputted);
    }

    /**
     * Will try to parse the user's input for the item quantity
     * into an integer. Will notify the user if they have
     * entered an invalid whole number and then set the text
     * field back to the last known good quantity.
     */
    private void validateQuantity(){
        try{
            quantityInputted = Integer.parseInt(itemQuantityInput.getText());
        } catch (NumberFormatException n) {
            //System.out.println("User tried to enter a bad int");
            JOptionPane.showMessageDialog(null, "Please enter a valid whole number.");
            itemQuantityInput.setText(quantityInputted + "");
        }
    }

    /**
     * Validates and removes harmful characters the user
     * could have inputted into the item description value.
     * Sets the 'descriptionInputted' variable of the new string
     * without the harmful characters. Will also set the
     * Description text area input to show the changes to the user
     * in case there are discrepancies caused by this check.
     */
    private void validateDescription(){
        //We need to validate the users input with a Pattern object to retain all new lines in the users input.
        Pattern p = Pattern.compile("[A-Za-z0-9 :%$&!.?,'\n]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(itemDescriptionInput.getText());
        descriptionInputted = "";
        while(m.find()){
            descriptionInputted += m.group();
        }
        itemDescriptionInput.setText(descriptionInputted);
    }

    /**
     * Loads the departments that can be selected into the
     * combo box.
     */
    private void loadDepartmentSelectionList(){
        int i=0;
        for(Department department : departments){
            i++;
            departmentData.addElement(String.format("%02d", i) + " " + department.getName());
        }
    }
}
