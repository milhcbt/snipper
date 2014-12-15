/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper;

import com.eaio.stringsearch.BoyerMooreHorspool;
import static com.codencare.snipper.util.ProtocolSignature.*;
import static com.codencare.snipper.util.BytesManager.*;

import jpcap.packet.TCPPacket;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author milh
 */
public class Chunk implements Comparable,Serializable{
    
    private long sequence;
    private byte[] data;
    
    public Chunk(TCPPacket tcpPacket){
        this.sequence = tcpPacket.sequence;
        this.data = tcpPacket.data;
    }
    
    public byte[] getData() {
        return data;
    }

    public long getSequence() {
        return sequence;
    }
    
    public boolean isHeader(){
        String sdata = "";
        if(this.data.length > 20){
            sdata = byteToString(subBytes(this.data, 0, 20));
        }

        if (sdata.matches(VALID_HTTP_RESPONSE_PATTERN)){
            return true;
        }else{
            return false;
        }
    }
    
    
    public boolean isEndOfChunk(){
            BoyerMooreHorspool bmh = new BoyerMooreHorspool();
            byte[] mark = {0x0D,0x0A,0x30,0x0D,0x0A};
            
            int markPos = bmh.searchBytes(this.data, mark);//indexOf(stream, mark);
            
            if(markPos!=-1)
                return true;
            else
                return false;
    }
    
    public int compareTo(Object t) {
        if(t==null){
            throw new InvalidParameterException();
        }
        if(!(t instanceof Chunk)){
            throw new InvalidParameterException();
        }
        if(this.sequence > ((Chunk)t).sequence){
            return 1;
        }else if(this.sequence < ((Chunk)t).sequence){
            return -1;
        }else{
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Chunk other = (Chunk) obj;
        if (this.sequence != other.sequence) {
            return false;
        }
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (int) (this.sequence ^ (this.sequence >>> 32));
        hash = 73 * hash + Arrays.hashCode(this.data);
        return hash;
    }
    
}
