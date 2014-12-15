/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper.util;

import static com.codencare.snipper.util.Configuration.*;

import com.codencare.snipper.Main;
import org.apache.log4j.Logger;

/**
 *
 * @author milh
 */
public class Utility {
    
    public static long packetNumber = 0; //packet counter
    public static long counter = 0;// packet counter
    public static String WarrantProtocolID; //warrant protocol ID
    public static Logger logger = 
		Logger.getLogger(Main.class.getPackage().getName());
    //public static DBConnection conn = new DBConnection(host,port,user,pass,dbName);
    
}
