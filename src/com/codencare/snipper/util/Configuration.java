/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper.util;

/**
 *
 * @author milh
 */
public class Configuration {
    public static  final long GC_SLEEP = 60000;//Garbage Collector Timeout
    public static final int HASHFLOW_INITIAL_SIZE = 1000;
    public static final int MAX_HASH_SIZE= 65536*1000;
    public static  int MAX_PACKET = 50;
    public static String SECRET_PATH = "I:/snipper/";//"/home/snipper/jboss-4.2.1.GA/secret/";
    
    
    /**********DATABASE CONFIGURATION**************/
    public static String host = "localhost";
    public static String port = "3306";
    public static String user = "root";
    public static String pass = "coplo!";
    public static String dbName = "snipperdb";
        
    /*****DEVELOPMENT DEBUG********/
    public static final boolean MAIN_DEBUG = true;
    public static final boolean MAIN_VERBOSE = true;
    public static final boolean PROCESS_VERBOSE = false;
    public static final boolean VIEW_CONTENT = false;
}
