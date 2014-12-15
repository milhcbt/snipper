/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codencare.snipper;

import static com.codencare.snipper.util.Configuration.*;
import static com.codencare.snipper.util.Utility.*;
import static com.codencare.snipper.Main.*;
import java.util.concurrent.ThreadPoolExecutor;

import jpcap.PacketReceiver;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

/**
 *
 * @author milh
 */
public class SnipperReceiver implements PacketReceiver{
    private static ThreadPoolExecutor pool = null;

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
