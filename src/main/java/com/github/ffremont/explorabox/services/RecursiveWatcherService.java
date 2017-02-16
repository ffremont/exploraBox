/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox.services;

import com.github.ffremont.explorabox.exceptions.InitializeWatcherServiceFailException;
import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class RecursiveWatcherService {

    private static final Logger LOG = LoggerFactory.getLogger(RecursiveWatcherService.class);

    private File rootFolder;

    private WatchService watcher;

    private ExecutorService executor;
    
    private ConcurrentLinkedQueue<Path> changes = new ConcurrentLinkedQueue<>();

    public RecursiveWatcherService(File rootFolder) {
        this.rootFolder = rootFolder;
    }

    public RecursiveWatcherService start() throws InitializeWatcherServiceFailException {
        try {
            if(!Files.exists(rootFolder.toPath())){
                throw new InitializeWatcherServiceFailException("Répertoire inexistant");
            }
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            throw new InitializeWatcherServiceFailException("Création du watch service impossible", ex);
        }
        executor = Executors.newSingleThreadExecutor();
        startRecursiveWatcher();
        
        return this;
    }

    public void stop() {
        try {
            watcher.close();
        } catch (IOException e) {
            LOG.error("Error closing watcher service", e);
        }
        executor.shutdown();
    }

    private void startRecursiveWatcher() {
        LOG.info("Starting Recursive Watcher");

        final Map<WatchKey, Path> keys = new HashMap<>();

        Consumer<Path> register = p -> {
            if (!p.toFile().exists() || !p.toFile().isDirectory()) {
                throw new RuntimeException("folder " + p + " does not exist or is not a directory");
            }
            try {
                Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        LOG.info("registering " + dir + " in watcher service");
                        WatchKey watchKey = dir.register(watcher, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
                        keys.put(watchKey, dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Error registering path " + p);
            }
        };

        register.accept(rootFolder.toPath());

        executor.submit(() -> {
            while (true) {
                final WatchKey key;
                try {
                    key = watcher.take(); // wait for a key to be available
                } catch (InterruptedException ex) {
                    return;
                }

                final Path dir = keys.get(key);
                if (dir == null) {
                    LOG.error("WatchKey " + key + " not recognized!");
                    continue;
                }

                key.pollEvents().stream()
                        .filter(e -> (e.kind() != OVERFLOW))
                        .forEach(e -> {
                            Path p = ((WatchEvent<Path>) e).context();
                            final Path absPath = dir.resolve(p);
                            if (absPath.toFile().isDirectory()) {
                                LOG.debug("Changements dans "+absPath.getParent().toAbsolutePath());
                                changes.add(absPath.getParent());
                                register.accept(absPath);
                            } else {
                                changes.add(absPath.getParent());
                                LOG.debug("Changements dans "+absPath.getParent().toAbsolutePath());
                                
                            }
                        });

                boolean valid = key.reset(); // IMPORTANT: The key must be reset after processed
                if (!valid) {
                    break;
                }
            }
        });
    }
    
    public ConcurrentLinkedQueue<Path> getChanges(){
        return changes;
    }
    
    public void purgeChanges(){
        changes.clear();
    }

}
