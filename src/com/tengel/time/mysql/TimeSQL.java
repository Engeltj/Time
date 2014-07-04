/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tengel.time.mysql;

import com.tengel.time.Time;

import java.sql.*;

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
        } catch (Exception ex) {
            plugin.sendConsole("Failed to make MySQL connection to " + host);
            plugin.sendConsole(ex.toString());
            con = null;
            return false;
        }
        return true;
    }
    
    public boolean verifyConnection(){
        try {
            Statement st = con.createStatement();
            st.executeQuery("SELECT 1");
        } catch (Exception ex) {
            initialize();
        }
        return (con != null);
    }
    
    private void verifyTableHomes(){
        try {
            Statement st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS homes (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, price DOUBLE, type BOOLEAN, owner VARCHAR(100), renter VARCHAR(100), PRIMARY KEY (id));");
        }catch (Exception ex) {
            plugin.sendConsole(ex.getMessage());
        }
    }
    
    private void verifyTablePlayers(){
         try {
            Statement st = con.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS players (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(100) NOT NULL, password VARCHAR(255), PRIMARY KEY (id));");
        }catch (Exception ex) {
            plugin.sendConsole(ex.getMessage());
        }
    }
        
    private String md5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
         } catch (Exception ignored) {}
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
        } catch (Exception ex) {
            plugin.sendConsole(ex.getMessage());
        }
    }
    
    public Connection getConnection(){
        verifyConnection();
        return this.con;
    }
}
