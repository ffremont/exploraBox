/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox;

import com.github.ffremont.explorabox.exceptions.InstallationFailException;
import com.github.ffremont.explorabox.models.DataFolder;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class Installation {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Installation.class);

    private Path confFolder;

    public Installation(Path confFolder) {
        this.confFolder = confFolder;
    }

    public void make() throws InstallationFailException {
        System.out.println("Procédure d'installation");
        System.out.println("");

        Console console = System.console();
        if (console == null) {
            throw new InstallationFailException("Console insdiponible");
        }
        Path defaultDataFolder = Paths.get(System.getProperty("user.home"), ExploraBox.DEFAULT_SYNC_FOLDER);

        if (!Files.exists(confFolder)) {
            try {
                Files.createDirectory(confFolder);
            } catch (IOException ex) {
                throw new InstallationFailException("Création du répertoire data impossible", ex);
            }
        }

        InputStreamReader cin = new InputStreamReader(System.in);
        String user = console.readLine("Veuillez saisir votre identifiant Explora ? ");
        String pwd = new String(console.readPassword("Veuillez saisir votre mot de passe Explora ? "));
        ExploraAccount account = new ExploraAccount(user, pwd);

        String yOrNDefaultDataFolder = console.readLine("Voulez-vous partager le répertoire " + defaultDataFolder.toAbsolutePath() + " (o/n) ?");
        if ("o".equals(yOrNDefaultDataFolder)) {
            account.getFolders().add(new DataFolder(defaultDataFolder, "explorabox"));
        }

        String yOrNOtherDataFolder = console.readLine("Avez-vous d'autres répertoire à partager (o/n) ?");
        if ("o".equals(yOrNOtherDataFolder)) {
            String dataFolderList = console.readLine("Veuillez saisir les chemins des répertoires à partager (séparés par des virgules) ?");
            if (!dataFolderList.isEmpty()) {
                account.getFolders().addAll((Collection<? extends DataFolder>) Arrays.asList(dataFolderList.split(","))
                        .stream()
                        .map(mapper -> {
                            Path dataFolder = Paths.get(mapper);

                            return new DataFolder(dataFolder, dataFolder.toFile().getName());
                        }));
            }
        }

        System.out.println("Installation en cours...");
        for (DataFolder dataFolder : account.getFolders()) {
            if (!Files.exists(dataFolder.getSource())) {
                try {
                    Files.createDirectory(dataFolder.getSource());
                } catch (IOException ex) {
                    throw new InstallationFailException("Création du répertoire impossible", ex);
                }
            }
        }
        try {
            ExploraConf.updateAccount(confFolder, new ExploraAccount(user, pwd));
        } catch (IOException ex) {
            throw new InstallationFailException("Stockage de explora account impossible", ex);
        }
        System.out.println("");
        System.out.println("Installation terminée");

    }

}
