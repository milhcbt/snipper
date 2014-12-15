/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper.util;

import static com.codencare.snipper.util.Utility.*;

import com.eaio.stringsearch.BoyerMooreHorspool;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author milh
 */
public class HTTPEntity {
    private Map<String,String> headers = new HashMap<String,String>();;;
    private byte[] body;
    //private String textBody;
    private boolean isGzip = false;
    private boolean isChunked = false;
    
    public HTTPEntity(byte[] stream){
        if(stream==null)
            throw new InvalidParameterException();
        
        setHeader(getHeadStream(stream));
        
        if(this.headers.get("Content-Encoding")!=null && this.headers.get("Content-Encoding").toLowerCase().startsWith("gzip")){
            this.isGzip = true;
        }
        if(this.headers.get("Transfer-Encoding")!=null && this.headers.get("Transfer-Encoding").toLowerCase().startsWith("chunked")){
            this.isChunked = true;
            this.body = getChunkedBodyStream(stream);
        }else{
            this.body = getBodyStream(stream);
        }
    }
    
    public String getBodyContent(){
        if(this.isGzip){
            if(this.isChunked)
                return unzip(cleanStream(this.body));
            return unzip(this.body);
        }else{
            return byteToString(this.body);
        }
    }
    
    public byte[] getRawBodyContent(){
        return this.body;
    }
    
    public void mergeBodyStream(byte[] newStream){
        this.body = mergeStream(this.body, newStream);
    }
    
    public boolean isGzip(){
        return this.isGzip;
    }
    
    public boolean isChunked(){
        return this.isChunked;
    }
    
    public String getHeaderValue(String field){
        return this.headers.get(field);
    }
    
    private void setHeader(byte[] headerStream){
        
        if(headerStream==null)
            throw new InvalidParameterException();
        
        String[] tmp, rawHeader = byteToString(headerStream).split("\n");
        
        if(rawHeader.length>0){
            this.headers.put("Type", rawHeader[0]);

            for(int i=1;i<rawHeader.length;i++){            

                tmp = rawHeader[i].split(": ");
                if(tmp.length==2)
                    this.headers.put(tmp[0], tmp[1]);
            }        
        }
    }
    
    /********BYTE UTILITY*********/
    private byte[] mergeStream(byte[] current, byte[] newData) {
        if(current==null)
            return newData;
                
        byte temp[] = new byte[current.length + newData.length];
        System.arraycopy(current, 0, temp, 0, current.length);
        System.arraycopy(newData, 0, temp, current.length, newData.length);
        return temp;
    }
    private byte[] getHeadStream(byte[] stream){
        BoyerMooreHorspool bmh = new BoyerMooreHorspool();        
        byte[] CLRF = {0x0D,0x0A,0x0D,0x0A};
        int end = bmh.searchBytes(stream, CLRF);
        return subBytes(stream, 0, end);
    }
    
    private byte[] getBodyStream(byte[] stream){        
        BoyerMooreHorspool bmh = new BoyerMooreHorspool();        
        byte[] CLRF = {0x0D,0x0A,0x0D,0x0A};
        int start = bmh.searchBytes(stream, CLRF)+4;
        
        return subBytes(stream, start, stream.length-start);
    }
    
    private byte[] getChunkedBodyStream(byte[] stream){        
        BoyerMooreHorspool bmh = new BoyerMooreHorspool();        
        byte[] CLRF = {0x0D,0x0A,0x0D,0x0A};
        byte[] separator = {0x0D,0x0A};
        int start = bmh.searchBytes(stream, CLRF)+4;
        
        int startcontent = bmh.searchBytes(stream, start, separator)+2;
        
        return subBytes(stream, startcontent, stream.length-startcontent);
    }
    
    private byte [] subBytes(byte[] source,int pos,int length){
        byte[] result  = null; 
        if(pos>=0 && length >= 0){
            result  = new byte[length];
            System.arraycopy(source, pos, result, 0, length);
        }else{
            result = source;
        }
        return result;
    }
    
    private String byteToString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (int b : bytes){
                sb.append((char)b);
        }
        return sb.toString();
    }
    /**
     * Asumsi byte pertama sudah bersih
     * @param stream
     * @return 
     */
    private byte[] cleanStream(byte[] rawStream){
        logger.info("cleaning stream from chunk info");
        BoyerMooreHorspool bmh = new BoyerMooreHorspool();
        byte[] CLRF = {0x0D,0x0A};
        byte[] EOC = {0x0D,0x0A,0x30,0x0D,0x0A};
        byte[] stream,buff,result=null;
        int i = 0;
        int CLRFPos = 0;
        int EOCPos = bmh.searchBytes(rawStream, EOC);
        
        logger.info("EOC Pos: "+EOCPos);
        
        if(EOCPos!=-1){
            stream = subBytes(rawStream, 0, rawStream.length-7);
            
            while(i<EOCPos){
                buff=null;
                logger.info("i: "+i);
                if(i==0){
                    CLRFPos = bmh.searchBytes(stream, CLRF);
                    buff = subBytes(stream, i, CLRFPos-i);
                    //i=CLRFPos+2;
                }else{
                    CLRFPos = bmh.searchBytes(stream, i, CLRF);
                    buff = subBytes(stream, i, CLRFPos-i);
                    //i=CLRFPos+2;
                }

                if(CLRFPos==-1){
                    //result = mergeStream(result, buff);
                    buff = subBytes(stream, i, EOCPos-i-1);
                    result = mergeStream(result, buff);
                    break;
                }else{
                    i=CLRFPos+2;
                }

                logger.info("CLRFPos before chunk info: "+CLRFPos);
                result = mergeStream(result, buff);

                if(i<stream.length){
                    CLRFPos = bmh.searchBytes(stream, i, CLRF);
                    logger.info("CLRFPos after chunk info: "+CLRFPos);
                    if(CLRFPos==-1)
                        break;
                    else
                        i=CLRFPos+2;
                }else{
                    break;
                }
            }

            return result;
        }else{
            return rawStream;
        }
    }
    /***********END OF BYTE UTILITY************/
    
    /**************UNZIP***********************/
    public static String unzip(byte[] gzipstream){
        //String result="";
        //int i = 0;
        String result = "";
        ByteArrayInputStream bais = new ByteArrayInputStream(gzipstream);
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(bais);
        } catch (IOException ex) {
            logger.error("GZIP input error!!!");
        }
        if(gis!=null){
            try {
                result = getString(gis);
            } catch (Exception ex) {
                logger.error("Error reading GZIP stream!!!");
            }
        }
        return result;
    }
    
    private static String getString(InputStream is) throws Exception {

        StringBuffer sb = new StringBuffer();
        String result = null;
        char c = '\0';

        while (is.available() > 0) {
            try {
                c = (char) is.read();

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                return result;
            }
            if(c!='\0'){
                if (result == null)
                    result = String.valueOf(c);
                else
                    result += c;
                sb.append(c);
            }

        }
        return sb.toString();

    }
    /************END OF UNZIP******************/
}
