/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper;

import static com.codencare.snipper.util.Configuration.*;
import static com.codencare.snipper.util.Utility.*;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

/**
 *
 * @author milh
 */
public class Main implements PacketReceiver{
    
    private static int THREAD_COUNT = 16;
    private static ThreadPoolExecutor pool = null;

    /**
     * @param args the command line arguments
     * <interface name> <thread number> <max packet> <Warrant Protocol ID>
     */
    public static void main(String[] args){
        logger.info("Starting snipper");
	
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        int used = -1;
        if (args.length != 4) {
            System.out.println("usage: java -jar snipper.jar <interface name> <thread number> <buffer size> <Warrant Protocol ID>");
            /*for (int i = 0; i < devices.length; i++) {
                logger.info(i + " :" + devices[i].name + "("
                                + devices[i].description + ")");
                logger.info("    data link:" + devices[i].datalink_name
                                + "(" + devices[i].datalink_description + ")");
                System.out.print("    MAC address:");
                for (byte b : devices[i].mac_address)
                        System.out.print(Integer.toHexString(b & 0xff) + ":");

                for (NetworkInterfaceAddress a : devices[i].addresses)
                        logger.info("    address:" + a.address + " "
                                        + a.subnet + " " + a.broadcast);
            }*/
        }else{
            for(int i=0;i<devices.length;i++){
                if(devices[i].name.equals(args[0])){
                    used = i;
                    break;
                }
            }
            
            THREAD_COUNT = 2;//Integer.parseInt(args[1]);  
            MAX_PACKET = Integer.parseInt(args[2]);                      
            WarrantProtocolID = args[3];

            pool = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 1,
                            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            
            JpcapCaptor jpcap = null;
            try {
                String path = "I:/snipper/facebook.pcap";//"/Users/milh/Desktop/test.pcap";
                //jpcap = JpcapCaptor.openDevice(devices[used], 2000, false, 20);//open a file to save captured packets
                jpcap = JpcapCaptor.openFile(path);
                //JpcapWriter writer=JpcapWriter.openDumpFile(jpcap,SECRET_PATH+"dump.pcap");
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
            Thread gc = GarbageCollector.getInstance(GC_SLEEP);
            gc.start();
            jpcap.loopPacket(-1, new Main());
            
            
            logger.info("Exit snipper");
            pool.shutdown();
        }
    }

    @Override
    public void receivePacket(Packet packet) {
        if (packet instanceof TCPPacket) {
            if (packet.data.length > 0) {
                if (MAIN_DEBUG) {
                        logger.info("new packet:"+packetNumber);
                        packetNumber++;
                }
                pool.execute(new Dumper((TCPPacket) packet,WarrantProtocolID));
            }
        }
    }
}
