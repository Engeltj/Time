/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.mysql;

import com.tengel.time.Time;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim
 */
public class TimeSQL {
    private final Time plugin;
    private final String host;
    private final String db;
    private final String user;
    private final String pass;
    private Connection con;
           
    public TimeSQL(Time plugin, String host, String db, String user, String pass){
        this.plugin = plugin;
        this.host = host;
        this.db = db;
        this.user = user;
        this.pass = pass;
        if (initialize()){
            verifyTableHomes();
            verifyTablePlayers();
        }
    }
    
    private boolean initialize(){
        try {
            con = DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+db, user, pass);
            plugin.sendConsole("MySQL connection initialized to " + host);
        } catch (SQLException ex) {
            plugin.sendConsole("Failed to make MySQL connection to " + host);
            con = null;
            return false;
        }
        return true;
    }
    
    public boolean verifyConnection(){
        try {
            Statement st = con.createStatement();
            st.executeQuery("SELECT 1");
        } catch (SQLException ex) {
            initialize();
        }
        return (con != null);
    }
    
    private void verifyTableHomes(){
        try {
            Statement st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS homes (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, price DOUBLE, type BOOLEAN, owner VARCHAR(100), renter VARCHAR(100), PRIMARY KEY (id));");
        }catch (SQLException ex) {
            plugin.sendConsole(ex.getMessage());
        }
    }
    
    private void verifyTablePlayers(){
         try {
            Statement st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS players (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, password VARCHAR(255), PRIMARY KEY (id));");
        }catch (SQLException ex) {
            plugin.sendConsole(ex.getMessage());
        }
    }
    
    public void addHome(String home, double price, boolean farm){
        verifyConnection();
        try {
            Statement st = con.createStatement();
            st.executeUpdate("INSERT INTO `homes` (name, price, farm) VALUES ('"+home+"',"+String.valueOf(price)+","+String.valueOf(farm)+");");
        } catch (SQLException ex) {
            plugin.sendConsole(ex.getMessage());
        }    
    }
    
    public void setHomeOwner(String home, String owner){
        verifyConnection();
        try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE `homes` SET owner='"+owner+"' WHERE name='"+home+"';");
        } catch (SQLException ex) {
            plugin.sendConsole(ex.getMessage());
        }    
    }
    
    public void setHomeRenter(String home, String renter){
        verifyConnection();
        try {
            Statement st = con.createStatement();
            st.executeUpdate("UPDATE `homes` SET renter='"+renter+"' WHERE name='"+home+"';");
        } catch (SQLException ex) {
            plugin.sendConsole(ex.getMessage());
        }    
    }
    
    public void removeHome(String home){
        verifyConnection();
    }
        
    private String md5(String md5) {
        try {
             java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
             byte[] array = md.digest(md5.getBytes());
             StringBuffer sb = new StringBuffer();
             for (int i = 0; i < array.length; ++i) {
               sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
             return sb.toString();
         } catch (java.security.NoSuchAlgorithmException e) {
         }
         return null;
     }
    
    public void addPlayer(String name, String password){
        verifyConnection();
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `players` WHERE name='"+ name +"';");
            int rows = 0;
            if (rs.last())
                rows = rs.getRow();
            if (rows == 0)
                st.executeUpdate("INSERT INTO `players` (name, password) VALUES ('"+name+"','"+md5(password)+"');");
            else
                st.executeUpdate("UPDATE `players` SET password='"+md5(password)+"' WHERE name='"+name+"';");
        } catch (SQLException ex) {
            plugin.sendConsole(ex.getMessage());
        }
    }
    
    
    public Connection getConnection(){
        return this.con;
    }
}
