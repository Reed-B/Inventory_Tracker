package info.behnfeldt.inventory_tracker;

import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Name:   SQLHandler.java
 * By:     Reed Behnfeldt
 * Date:   04/28/2019
 *
 * This class is the interface between the application and the database. When you need to run a query, you
 * should only be passing a query statement in the form of a String to the runQuery() method which will return a
 * ResultSet that can be parsed by the class requesting the information.
*/
public class SQLHandler {

    private String configName = "config";               //Name of the configuration file needed
    private String[] lineData;                          //Used to initialize variables set by the configuration file
    private static String driver = "jdbc:mysql://";     //Defines that the driver will be using the JDBC MySQL driver
    private static String ip;                           //Stores the config for the database IP address
    private static String database;                     //Stores the config for the database name we are using
    private static String username;                     //Stores the config for the database username
    private static String password;                     //Stores the config for the database password
    private static File config;                         //Stores the file we are parsing
    private static Scanner scanner;                     //Scanner to read the config file

    /**
     * Initialization method. Begins by trying to read the configuration file 'config' and retrieves
     * the variables needed to connect to the database.
     */
    public SQLHandler(){
        try{
            config = new File(configName);
            scanner = new Scanner(config);
            /*
            * While reading the file, ignore blank lines and commented(#) lines, split the line into an array, then add
            * the string data to the appropriate variable
            */
            while(scanner.hasNextLine()){
                lineData = scanner.nextLine().split("\\s+");
                if (lineData[0].length() != 0) {
                    if(lineData[0].charAt(0) != '#') {
                        switch(lineData[0]){
                            case "ip" : ip=lineData[1];
                            case "database" : database=lineData[1];
                            case "username" : username=lineData[1];
                            case "password" : password=lineData[1];
                        }
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred trying to work with the configuration file.");
            e.printStackTrace();
        }
    }

    /**
     * Provides a String array that provides the necessary information to connect to the database. Must have
     * access to the config file in the first place to get this information. The data can be used on a Connection
     * object. Returns the following:
     * Index 0: Database driver + ip + port + database name
     * Index 1: Username
     * Index 2: Password
     *
     * @return String[]
     */
    public String[] getConnectionInfo(){
        return new String[]{driver + ip + "/" + database, username, password};
    }

    /**
     * Used to close all database connection variables used.
     *
     * @param connection (Connection)
     * @param statements (Statement)
     * @param resultset (ResultSet)
     */
    public void closeConnections(Connection connection, Statement statements, ResultSet resultset){
        if (connection != null) try{connection.close();} catch (SQLException e) {}
        if (statements != null) try{ statements.close();} catch (SQLException e) {}
        if (resultset != null) try{ resultset.close();} catch (SQLException e) {}
    }
}
