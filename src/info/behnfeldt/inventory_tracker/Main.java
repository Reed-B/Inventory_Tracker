package info.behnfeldt.inventory_tracker;

import javax.swing.*;

public class Main {
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //The form is the main class that centralizes all functionality of the program
                Form_Main form = new Form_Main();
                form.setVisible(true);
            }
        });
    }
}
