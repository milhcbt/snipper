/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper;

import static com.codencare.snipper.util.Configuration.*;
import static com.codencare.snipper.util.Utility.*;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.SortedSet;

/**
 *
 * @author milh
 */
public class GarbageCollector extends Thread{
    private long sleep;
    private static GarbageCollector instance;
    
    private GarbageCollector(long sleep) {
        this.sleep = sleep;
    }

    public static GarbageCollector getInstance(long sleep) {
        if (instance == null)
                instance = new GarbageCollector(sleep);
        return instance;
    }
    
    public void run(){
        while (true) {
            while (Dumper.flowHash.size() >= MAX_HASH_SIZE) {
                Iterator<String> e = Dumper.flowHash.keySet().iterator();
                while (e.hasNext()) {
                    String key = e.next();
                    SortedSet<Chunk> value = Dumper.flowHash.get(key);                                                                   
                    Dumper.processPurgedStream(value, key);
                    Dumper.flowHash.remove(key);
                    logger.info("clean up");                    
                }
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}
