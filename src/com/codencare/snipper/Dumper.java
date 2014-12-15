 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper;

import static com.codencare.snipper.util.BytesManager.*;
import static com.codencare.snipper.util.Configuration.*;
import static com.codencare.snipper.util.Utility.*;

import com.codencare.snipper.util.ProtocolPort;
import com.codencare.snipper.util.HTTPEntity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import jpcap.packet.TCPPacket;

/**
 *
 * @author milh
 */
public class Dumper extends Thread{
    private TCPPacket tcpPacket;
    static HashMap<String,SortedSet<Chunk>> flowHash = new HashMap<String, SortedSet<Chunk>>(HASHFLOW_INITIAL_SIZE);
    private String wpID; //Warrant Protocol ID
    
    
    public Dumper(TCPPacket tcpPacket, String wpID) {
        this.tcpPacket = tcpPacket;
        this.wpID = wpID;
    }
    
    @Override
    public void run() {
        counter++;
        logger.info(counter+" packet processed");
        logger.info("curret hash size:" + flowHash.size());
        //Process HTTP Packet
        if (this.tcpPacket.src_port == ProtocolPort.HTTP_PORT && tcpPacket.data.length > 20) {
            logger.info("HTTP Packet received");
            String key = byteToString(generateKey(tcpPacket));
            SortedSet<Chunk> chunkSet = null;
            if(flowHash.containsKey(key)){
               logger.info("Buffering HTTP packet");
               chunkSet = flowHash.get(key);
               /*if(chunkSet==null){
                   chunkSet = new ConcurrentSkipListSet<Chunk>();
               }*/
               //Chunk data = new Chunk(tcpPacket);
               //logger.info(new String(data.getData()));
               //chunkSet.add(data);
               addChunk(tcpPacket,chunkSet);
            }else{
               logger.info("New HTTP Stream");
               Chunk data = new Chunk(tcpPacket);
               chunkSet = new ConcurrentSkipListSet<Chunk>();
               chunkSet.add(data);
               logger.info(new String(data.getData()));    
               if(data.isHeader()){
                   HTTPEntity entity = new HTTPEntity(data.getData());
                   if(entity.isChunked()){
                       flowHash.put(key, chunkSet);
                   }else{
                      // processStream(chunkSet, key);
                   }
               }
            }
            
            
            if(isCompleted(chunkSet)){
                logger.info("Processing completed stream (key: "+key+")");
                logger.info("Removing: "+flowHash.remove(key));
                processStream(chunkSet,key);
            }
        }
    }
    
    private boolean isCompleted(SortedSet<Chunk> chunkSet){     
        Chunk data = chunkSet.last();
        if(isEndofChunk(data.getData())){
            return true;
        }else{
            return false;
        }
    }
    
    private static boolean containKeyword(String content){
        String keyword = "";//get from database
        
        if(keyword.equalsIgnoreCase(""))
            return true;
        
        String[] keywords = keyword.split(",");
        boolean valid = false;
        for(int i=0;i<keywords.length;i++){
            if(content.toLowerCase().contains(keywords[i].toLowerCase()))
                valid = true;
        }
        return valid;
    }
    
    protected static void processStream(SortedSet<Chunk> chunkSet, String key){
        logger.info("Begin processing completed stream (key: "+key+")");
        Iterator<Chunk> it = chunkSet.iterator();
        Chunk chunkData;
        byte[] buff,completeStream = null;
        
        while(it.hasNext()){
            chunkData = it.next();
            buff = chunkData.getData();
            completeStream = mergeStream(completeStream,buff);
            
            if(chunkData.isEndOfChunk() || !it.hasNext()){
                HTTPEntity entity = new HTTPEntity(completeStream);
                logger.info("Content-Encoding: "+entity.getHeaderValue("Content-Encoding"));
                logger.info("Processing stream (key: "+key+") complete");
                if(containKeyword(entity.getBodyContent())){
                    saveFlow(key,entity.getBodyContent().getBytes());
                }
            }
        }
    }
    
    protected static void processPurgedStream(SortedSet<Chunk> chunkSet, String key){
        Iterator<Chunk> it = chunkSet.iterator();
        Chunk chunkData;
        byte[] buff,completeStream = null;
        boolean validStream = false;
        
        while(it.hasNext()){
            chunkData = it.next();
            if(chunkData.isHeader()){
                validStream = true;
                buff = chunkData.getData();
                completeStream = mergeStream(completeStream,buff);
            }else{
                if(validStream){
                    buff = chunkData.getData();
                    completeStream = mergeStream(completeStream,buff);
                    if(chunkData.isEndOfChunk() || !it.hasNext()){
                        HTTPEntity entity = new HTTPEntity(completeStream);
                        if(containKeyword(entity.getBodyContent())){
                            saveFlow(key,entity.getBodyContent().getBytes());
                        }
                    } 
                }
            }
        }
    }
    
    static synchronized void saveFlow(String key, byte[] data) {
        FileOutputStream fos = null;
        String path = SECRET_PATH + key + "-" + System.currentTimeMillis()+".txt";
        
        try {            
            //File f = new File(dir + "/" + key + "-" + System.currentTimeMillis()+".txt");
            File f = new File(path);
            fos = new FileOutputStream(f, true);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        try {
            //fos.write(cleanData(data));
            fos.write(data);
            fos.flush();
            fos.close();
            
            /*logger.info("insert to database");
            conn.insertResult(path, StringUtil.getDestFromKey(key), StringUtil.getSourceFromKey(key), 
                                   WarrantProtocolID , "", String.valueOf(data.length));*/
            logger.info("saving....");
            logger.info("curret hash size:" + flowHash.size());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            if (PROCESS_VERBOSE) {
                logger.info("saving valid packet...");
            }
        }
    }
    
    static synchronized void addToHash(String key, SortedSet<Chunk> chunkSet){
        flowHash.put(key, chunkSet);
    }
    
    static synchronized void removeFromHash(String key){
        flowHash.remove(key);
    }
    
    static synchronized void addChunk(TCPPacket packet, SortedSet<Chunk> chunkSet){
        Chunk data = new Chunk(packet);
        chunkSet.add(data);
    } 
}
