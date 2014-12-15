package com.codencare.snipper.util;

import com.eaio.stringsearch.BoyerMooreHorspool;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jpcap.packet.TCPPacket;

public class BytesManager {
        
    
        /*Using BMH Algorythm for byte search*/
        public final static byte[] getBodyStream(byte[] stream, boolean isChunked){
            BoyerMooreHorspool bmh = new BoyerMooreHorspool();        
            byte[] CLRF = {0x0D,0x0A,0x0D,0x0A};
            byte[] separator = {0x0D,0x0A};
            int start = bmh.searchBytes(stream, CLRF)+4;
            if(isChunked){  
                int startcontent = bmh.searchBytes(stream, start, separator)+2;
                return subBytes(stream, startcontent, stream.length-startcontent);
            }else{
                return subBytes(stream, start, stream.length-start);
            }
        }
        
        public final static byte[] getHeadStream(byte[] stream){
            BoyerMooreHorspool bmh = new BoyerMooreHorspool();        
            byte[] CLRF = {0x0D,0x0A,0x0D,0x0A};
            int end = bmh.searchBytes(stream, CLRF)-1;
            return subBytes(stream, 0, end);
        }
        
        /*Asumsi byte awal sudah bersih (header http sudah dibuang)*/
        public final static byte[] cleanStream(byte[] rawStream){
            //System.out.println("cleaning stream from chunk info");
            BoyerMooreHorspool bmh = new BoyerMooreHorspool();
            byte[] CLRF = {0x0D,0x0A};
            byte[] EOC = {0x0D,0x0A,0x30,0x0D,0x0A};
            byte[] stream,buff,result=null;
            int i = 0;
            int CLRFPos = 0;
            int EOCPos = bmh.searchBytes(rawStream, EOC);
            //System.out.println("EOC Pos: "+EOCPos);
            if(EOCPos!=-1){
                stream = subBytes(rawStream, 0, rawStream.length-7);

                while(i<EOCPos){
                    buff=null;
                    //System.out.println("i: "+i);
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
                    
                    //System.out.println("CLRFPos before chunk info: "+CLRFPos);
                    result = mergeStream(result, buff);

                    if(i<stream.length){
                        CLRFPos = bmh.searchBytes(stream, i, CLRF);
                        //System.out.println("CLRFPos after chunk info: "+CLRFPos);
                        i=CLRFPos+2;
                    }else{
                        break;
                    }
                }
                
                return result;
            }else{
                return null;
            }
        }
    
        /*===========*/
    
	public final static byte[] mergeStream(byte[] current, byte[] newData) {
                if(current==null){
                    return newData;
                }
		byte temp[] = new byte[current.length + newData.length];
		System.arraycopy(current, 0, temp, 0, current.length);
		System.arraycopy(newData, 0, temp, current.length, newData.length);
		return temp;
	}

	public final static byte decreaseTimeOut (byte[] data) {
		byte temp = data[0];
		data[0]= --temp;
		return data[0];
	}

	public final static byte[] createData(byte[] data, byte timeout) throws InvalidParameterException{
		if (timeout < 0 || timeout > 255)
			throw new InvalidParameterException();
		byte newData[] = new byte[data.length + 1];// plus time out header
		newData[0] = timeout;
		System.arraycopy(data, 0, newData, 1, data.length);
		return newData;
	}
	public final static byte[] cleanData(byte[] data){
		byte temp[] = new byte[data.length-1];
		System.arraycopy(data, 1, temp, 0, data.length-1);
		return temp;
	}
	public final static String byteToString(byte[] bytes){
		StringBuilder sb = new StringBuilder();
		for (int b : bytes){
			sb.append((char)b);
		}
		return sb.toString();
	}
	
	public final static byte [] subBytes(byte[] source,int pos,int length){
		byte[] result  = null; 
                if(pos>=0 && length >= 0){
                    result  = new byte[length];
                    System.arraycopy(source, pos, result, 0, length);
                }else{
                    result = source;
                }
		return result;
	}
	public final static String getHttpContentType(byte[] packet){
		if (packet.length<13)
			return null;
		String s = byteToString(packet);
		String [] sa= s.split(ProtocolSignature.HTTP_CONTENT_TYPE_PATTERN);
		if (sa.length > 1 && sa[1].length()>4 && sa[1].endsWith("\n") ){
			return sa[1].substring(1, sa[1].indexOf("\n")).trim();
		}else{
			return null;
		}
	}
	public final static String getHttpContentEncoding(byte[] packet){
		if (packet.length<17)
			return null;
		String s = byteToString(packet);
		String [] sa= s.split(ProtocolSignature.HTTP_CONTENT_ENCODING_PATTERN);
		if (sa.length > 1 && sa[1].length()>3 && sa[1].endsWith("\n") ){
			return sa[1].substring(1, sa[1].indexOf("\n")).trim();
		}else{
			return null;
		}
	}
        public final static String getHttpTransferEncoding(byte[] packet){
		if (packet.length<17)
			return null;
		String s = byteToString(packet);
		String [] sa= s.split(ProtocolSignature.HTTP_TRANSFER_ENCODING);
		if (sa.length > 1 && sa[1].length()>3 && sa[1].endsWith("\n") ){
			return sa[1].substring(1, sa[1].indexOf("\n")).trim();
		}else{
			return null;
		}
	}

	public final static byte[] generateKey(TCPPacket tcpPacket) {
		return (tcpPacket.src_ip.getHostAddress() + "." + tcpPacket.src_port
				+ "-" + tcpPacket.dst_ip.getHostAddress() + "." + tcpPacket.dst_port)
				.getBytes();
	}
        
        public final static byte[] getHTTPMessageBody(byte[] stream){
                byte[] CLRF = {0x0D,0x0A,0x0D,0x0A};
                
                //if(stream!=null && indexOf(stream,CLRF)<0)
                    return subBytes(stream, indexOf(stream,CLRF)+4, stream.length-indexOf(stream,CLRF)-4);
                //else
                  //  return null;
                
        }
        
        public final static byte[] getHTTPMessageBodyStream(byte[] stream, boolean chunked){
                byte[] bodystream = getHTTPMessageBody(stream);
                byte[] separator = {0x0D,0x0A};
                byte[] result = null;
                
                //if(stream!=null){
                  //  if(indexOf(bodystream,separator)>0){
                        if(chunked)                
                            result = subBytes(bodystream,indexOf(bodystream,separator)+2,bodystream.length-indexOf(bodystream,separator)-6);
                        else
                            result = bodystream;
                    //}
                //}else{
                  //  result = stream;
                //}
                return result;
        }
        
        public final static byte[] getChunkedStream(byte[] stream){
            byte[] separator = {0x0D,0x0A};
            byte[] result = null;
            int start = indexOf(stream,separator)+2;
            
            if(start!=-1)
                result =  subBytes(stream,start,stream.length-2);
            
            return result;
        }
        
        public final static boolean isEndofChunk(byte[] stream){
            BoyerMooreHorspool bmh = new BoyerMooreHorspool();
            byte[] mark = {0x0D,0x0A,0x30,0x0D,0x0A};
            
            int markPos = bmh.searchBytes(stream, mark);//indexOf(stream, mark);
            
            if(markPos!=-1)
                return true;
            else
                return false;
        }
        
        public final static int getChunkedLength(byte[] stream){
            byte[] bodyStream = stream;//getHTTPMessageBody(stream);
            byte[] separator = {0x0D,0x0A};
            int length;
            int end = indexOf(bodyStream,separator)-1;
            System.out.println("Chunked Length: "+byteToString(subBytes(bodyStream,0,end)));
            if(end!=-1){    
                try{
                    length = Integer.parseInt(byteToString(subBytes(bodyStream,0,end)).trim(),16); 
                }catch(Exception e){
                    System.out.println("EXCEPTION: "+e.getMessage());
                    length = -1;
                }
                return length;
            }else{ 
                return -1;
            }
        }
        
        public final static boolean isGziped(byte[] stream){
            byte[] GZIP = {0x47,0x5a,0x49,0x50};
            byte[] gzip = {0x67,0x7a,0x69,0x70};
            if(indexOf(stream,gzip)>0)
                return true;
            else
                return false;
        }
        
        /**
         * Knuth-Morris-Pratt Algorithm for Pattern Matching
         */
         public static int indexOf(byte[] data, byte[] pattern) {
            int[] failure = computeFailure(pattern);

            int j = 0;
            if (data.length == 0) return -1;

            for (int i = 0; i < data.length; i++) {
                while (j > 0 && pattern[j] != data[i]) {
                    j = failure[j - 1];
                }
                if (pattern[j] == data[i]) { j++; }
                if (j == pattern.length) {
                    return i - pattern.length + 1;
                }
            }
            return -1;
        }

        /**
         * Computes the failure function using a boot-strapping process,
         * where the pattern is matched against itself.
         */
        private static int[] computeFailure(byte[] pattern) {
            int[] failure = new int[pattern.length];

            int j = 0;
            for (int i = 1; i < pattern.length; i++) {
                while (j > 0 && pattern[j] != pattern[i]) {
                    j = failure[j - 1];
                }
                if (pattern[j] == pattern[i]) {
                    j++;
                }
                failure[i] = j;
            }

            return failure;
        }
}
