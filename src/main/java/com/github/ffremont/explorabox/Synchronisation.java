/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox;

import com.github.ffremont.explorabox.exceptions.InitializeWatcherServiceFailException;
import com.github.ffremont.explorabox.models.SimFile;
import com.github.ffremont.explorabox.models.SyncState;
import com.github.ffremont.explorabox.runs.ExploraCaller;
import com.github.ffremont.explorabox.services.RecursiveWatcherService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class Synchronisation implements Runnable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Synchronisation.class);

    private RecursiveWatcherService watcherService;
    private Future<Void> future;

    private SyncState lastState;
    private List<String> lastMessages;

    private Instant started;
    private Path dataFolder;
    private String rootWebPath;
    private String exploraIp;
    private int exploraPort;

    private ConcurrentLinkedQueue<Future<FolderResult>> writesResults;
    private ConcurrentLinkedQueue<Future<FolderResult>> exploraResults;

    public Synchronisation(Path dataFolder, String rootWebPath) {
        this.dataFolder = dataFolder;
        this.rootWebPath = rootWebPath;
        this.watcherService = new RecursiveWatcherService(dataFolder.toFile());

        this.writesResults = new ConcurrentLinkedQueue<>();
        this.exploraResults = new ConcurrentLinkedQueue<>();
    }

    /**
     * Init au lancement du programme
     *
     * @param exploraIp
     * @param exploraPort
     */
    public void init(final String exploraIp, final int exploraPort) {
        this.exploraIp = exploraIp;
        this.exploraPort = exploraPort;

        if (!Files.exists(dataFolder)) {
            this.lastState = SyncState.ERROR;
            this.lastMessages = Arrays.asList("Le répertoire de données est introuvable");
        } else {
            try {
                this.watcherService.start();
                this.lastState = SyncState.OK;
            } catch (InitializeWatcherServiceFailException ex) {
                LOG.error("Impossible d'initialiser la synchronisation", ex);
                lastState = SyncState.ERROR;
                lastMessages = Arrays.asList("Le scan du répertoire de données n'est pas possible");
            }
        }
    }

    @Override
    public void run() {
        this.started = Instant.now();

        String baseurlExplora = ExploraConf.TPL_URL_EXPLORA.replace("{host}", exploraIp).replace("{port}", String.valueOf(exploraPort));
        this.exploraResults.add(ExploraBox.exploraPool.submit(new ExploraCaller(this, baseurlExplora, rootWebPath)));

        for (Future<FolderResult> futureFR = this.exploraResults.poll(); futureFR != null; futureFR = this.exploraResults.poll()) {
            try {
                FolderResult folderResult = futureFR.get(30, TimeUnit.SECONDS);
                for(SimFile fileOrFolder : folderResult.getFiles()){
                    /**
                     * rootWebPath : /explorabox
                     * webPath : /explorabox/documents
                     * dataFolder : /home/florent/tests
                     *  => realFolderOnClient : /home/florent/tests/documents
                     */
                    Path realFolderOnClient = Paths.get(
                            dataFolder.toAbsolutePath().toString(), 
                            folderResult.getWebPath().replace(rootWebPath, "")
                    );
                    
                    if(fileOrFolder.isIsDir()){
                        Files.cr
                    }
                }
                
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                LOG.warn("Récupération du résultat de la requête impossible, délai d'attente dépassé", ex);
            } 
        }


        this.lastState = SyncState.OK;
    }

    public Synchronisation unknowIssue() {
        this.lastMessages = Arrays.asList("Un problème inattendu s'est produit");
        this.lastState = SyncState.ERROR;

        return this;
    }

    public Synchronisation exploraServerUnreachable() {
        this.lastMessages = Arrays.asList("Service Explora indisponible");
        this.lastState = SyncState.ERROR;

        return this;
    }

    public SyncState getLastState() {
        return lastState;
    }

    public void setLastState(SyncState lastState) {
        this.lastState = lastState;
    }

    public List<String> getLastMessages() {
        return lastMessages;
    }

    public void setLastMessages(List<String> lastMessages) {
        this.lastMessages = lastMessages;
    }

    public Path getDataFolder() {
        return dataFolder;
    }

    public String getRootWebPath() {
        return rootWebPath;
    }

    public Instant getStarted() {
        return started;
    }

    public String getExploraIp() {
        return exploraIp;
    }

    public void setExploraIp(String exploraIp) {
        this.exploraIp = exploraIp;
    }

    public int getExploraPort() {
        return exploraPort;
    }

    public void setExploraPort(int exploraPort) {
        this.exploraPort = exploraPort;
    }

    public Future<Void> getFuture() {
        return future;
    }

    public void setFuture(Future<Void> future) {
        this.future = future;
    }

    public void explore(ExploraCaller caller) {
        exploraResults.add(
                ExploraBox.exploraPool.submit(caller)
        );
    }

    public boolean canLaunch() {
        return (this.future == null) || (this.future != null && this.future.isDone());
    }
}
