/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox.runs;

import com.github.ffremont.explorabox.ExploraAccount;
import com.github.ffremont.explorabox.ExploraBox;
import com.github.ffremont.explorabox.Synchronisation;
import com.github.ffremont.explorabox.exceptions.UnreachableServiceException;
import com.github.ffremont.explorabox.models.DataFolder;
import com.github.ffremont.explorabox.models.SyncState;
import com.github.ffremont.explorabox.services.DiscoverService;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class SyncCron implements Runnable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SyncCron.class);

    private final DiscoverService discoverService;
    private final ExploraAccount account;

    private static String exploraIp;
    private static int exploraPort;

    public SyncCron(ExploraAccount account, DiscoverService discoverService) {
        this.discoverService = discoverService;
        this.account = account;
    }

    private static boolean socketIsReady(String host, int port) throws UnreachableServiceException {
        Socket s = null;
        try {
            s = new Socket(host, port);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    throw new UnreachableServiceException("Une erreur s'est produite", e);
                }
            }
        }
    }

    /**
     *
     * @return @throws UnreachableServiceException
     */
    synchronized private String getExploraIp() throws UnreachableServiceException {
        if (exploraIp == null || !socketIsReady(exploraIp, discoverService.getPort())) {
            exploraIp = discoverService.find().getHostAddress();
        }

        return exploraIp;
    }

    @Override
    public void run() {
        try {
            String exploraServerIp = getExploraIp();

            for (Synchronisation sync : ExploraBox.synchronisations.values()) {
                if (sync.canLaunch()) {
                    if (sync.getLastState() == null) {
                        sync.init(exploraServerIp, exploraPort);
                    }

                    sync.setFuture((Future<Void>) ExploraBox.synchronisationPool.submit(sync));
                }else{
                    LOG.warn("La synchronisation est encore en cours...");
                }
            }
        } catch (UnreachableServiceException e) {
            ExploraBox.synchronisations.values().stream().map(mapper -> mapper.exploraServerUnreachable());
            LOG.error("Serveur explora inaccessible", e);
        } catch (Exception e) {
            ExploraBox.synchronisations.values().stream().map(mapper -> mapper.unknowIssue());
            LOG.error("Une erreur imprévue s'est produite, toutes les synchronisations sont HS", e);
        }
    }

    public static void setExploraPort(int exploraPort) {
        exploraPort = exploraPort;
    }
}
