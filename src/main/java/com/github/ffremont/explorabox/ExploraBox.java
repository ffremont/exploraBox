/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox;

import com.github.ffremont.explorabox.exceptions.InitializeWatcherServiceFailException;
import com.github.ffremont.explorabox.exceptions.InstallationFailException;
import com.github.ffremont.explorabox.models.DataFolder;
import com.github.ffremont.explorabox.models.SyncState;
import com.github.ffremont.explorabox.runs.SyncCron;
import com.github.ffremont.explorabox.services.DiscoverService;
import com.github.ffremont.explorabox.services.RecursiveWatcherService;
import com.github.ffremont.explorabox.services.TrayService;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class ExploraBox {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExploraBox.class);

    private final static int syncEvery = 60;

    public static final String DEFAULT_SYNC_FOLDER = "explorabox";

    public static TrayService trayService;
    public static Map<Path, RecursiveWatcherService> watcherServices = new HashMap<>();
    public static DiscoverService discoverService;

    public static ExecutorService synchronisationPool = Executors.newFixedThreadPool(10);
    public static ConcurrentHashMap<Path, Synchronisation> synchronisations = new ConcurrentHashMap<>();

    public static ExecutorService writesPool = Executors.newFixedThreadPool(5);
    public static ExecutorService exploraPool = Executors.newFixedThreadPool(3);

    private static Path confFolder;

    public static void main(String[] args) throws InterruptedException {
        final int exploraPort = System.getProperty("explora.port") == null ? 8888 : Integer.valueOf(System.getProperty("explora.port"));
        final int discoveryPort = System.getProperty("discovery.port") == null ? 8889 : Integer.valueOf(System.getProperty("discovery.port"));

        String lineSeparator = System.getProperty("line.separator");
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("header.txt");
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            System.out.print(scanner.useDelimiter("\\A").next());
            System.out.println(lineSeparator+lineSeparator);
        }

        trayService = new TrayService("ExploraBox", "Démarrage en cours...");
        trayService.start();

        confFolder = Paths.get(System.getProperty("user.home"), ".explorabox");

        try {
            if (args.length > 0) {
                if ("install".equals(args[0])) {
                    (new Installation(confFolder)).make();
                } else {
                    LOG.error("Aucune commande de ce nom existe, veuillez ressayer avec la bonne commande.");
                }
            } else {
                ExploraAccount account = ExploraConf.getAccount(confFolder);
                for (DataFolder dataFolder : account.getFolders()) {
                    synchronisations.put(dataFolder.getSource(), new Synchronisation(dataFolder.getSource(), dataFolder.getFolderTargetName()));
                }

                SyncCron.setExploraPort(exploraPort);
                discoverService = new DiscoverService("explora", discoveryPort, "explora ?", "oui");
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new SyncCron(account, discoverService), 0, syncEvery, TimeUnit.SECONDS);
            }
        } catch (InstallationFailException ex) {
            LOG.error("Echec de l'installation", ex);
        }
    }

}
