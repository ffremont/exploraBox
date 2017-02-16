/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox.services;

import com.github.ffremont.explorabox.exceptions.UnreachableServiceException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class DiscoverService {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoverService.class);

    private final static int DEFAULT_TIMEOUT = 5;

    private String name;
    private int port;
    private String question;
    private String exceptAnswer;
    private int timeout;

    public DiscoverService(String name, int port, String question, String exceptAnswer) {
        this.name = name;
        this.port = port;
        this.question = question;
        this.exceptAnswer = exceptAnswer;
        this.timeout = DEFAULT_TIMEOUT;
    }

    public InetAddress find() throws UnreachableServiceException {
        DatagramSocket c = null;
        // Find the server using UDP broadcast
        try {
            //Open a random port to send the package
            c = new DatagramSocket();
            c.setBroadcast(true);
            c.setSoTimeout(timeout);

            byte[] sendData = question.getBytes();

            //Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
                c.send(sendPacket);
                LOG.info(">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
            } catch (Exception e) {
            }

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, port);
                        c.send(sendPacket);
                    } catch (Exception e) {
                    }

                    LOG.info(">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }

            LOG.info(">>> Done looping over all network interfaces. Now waiting for a reply!");

            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.receive(receivePacket);

            //We have a response
            LOG.info(">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

            //Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals(exceptAnswer)) {
                //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                //Controller_Base.setServerIp(receivePacket.getAddress());
                return receivePacket.getAddress();
            }
        } catch (SocketTimeoutException ex) {
            LOG.error("Service " + name + "introuvable", ex);
            
            throw new UnreachableServiceException("", ex);
        } catch (IOException ex) {
            LOG.error("Impossibe de détecter sur le réseau le service", ex);
            throw new UnreachableServiceException("", ex);
        } finally {
            if (c != null) {
                //Close the port!
                c.close();
            }
        }
        
        throw new UnreachableServiceException("Aucun service "+name+" sur le réseau");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getExceptAnswer() {
        return exceptAnswer;
    }

    public void setExceptAnswer(String exceptAnswer) {
        this.exceptAnswer = exceptAnswer;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
