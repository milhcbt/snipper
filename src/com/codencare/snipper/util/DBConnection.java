/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper.util;


import static com.codencare.snipper.util.Configuration.*;
import static com.codencare.snipper.util.Utility.*;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author milh
 */
public class DBConnection {
    
    private Connection conn = null;

    public DBConnection(String host, String port, String user, String pass, String dbName) {
         try
           {
               String url = "jdbc:mysql://"+host+":"+port+"/"+dbName;
               Class.forName ("com.mysql.jdbc.Driver").newInstance();
               this.conn = DriverManager.getConnection (url, user, pass);
               System.out.println ("Database connection established");
               System.out.println ("Warrant Protocol ID: "+WarrantProtocolID);
           }
           catch (Exception e)
           {
               System.err.println (e.getMessage());
           }
    }
    
    
    public void insertResult(String url, String dest, String source, String wrProtID, String info, String size){
    
        String query = "insert into WarrantContent(url,dest,source,warrantProtocol_id,info,timeStamp,size)"
                + " values('"
                + url +"','"
                + dest+"','"
                + source+"','"
                + wrProtID+"','"
                + info+"','"
                + new Date(System.currentTimeMillis())+"','"
                + size+"')";
        
        insertNewData(query);
    }
    
    public void insertNewData(String query){
        Statement statement = null;
        try {	
            statement = this.conn.createStatement();

            // execute insert SQL stetement
            statement.executeUpdate(query);

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } 
    }
    
    public String getWarrantInfo(String field, String id){
        Statement statement = null;
        String result = null,query = "select "+field+" from WarrantProtocol where id="+id;
                
        ResultSet rs = null;
        try {
            rs = statement.executeQuery(query);
            result = rs.getString("id");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        
        return result;
    }
    
    public String getKeywords(String wpID){
        Statement statement = null;
        String result = null,query = "select w.keyword from WarrantProtocol wp "
                + "join Warrant w on w.id = wp.warrant_id "
                + "where wp.id = "+wpID;
        ResultSet rs = null;
        try {
            statement = this.conn.createStatement();
            rs = statement.executeQuery(query);
            rs.next();
            result = rs.getString("keyword");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        
        return result;
    }
}
