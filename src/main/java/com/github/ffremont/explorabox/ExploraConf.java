/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox;

import com.github.ffremont.explorabox.exceptions.ExploraBoxInternalException;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class ExploraConf {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExploraConf.class);
    
    public static final String CONF = "explorabox.json";
    public static final String IGNORE_FILE = ".explora-ignore";
    public static final String TPL_URL_EXPLORA = "http://{host}:{port}/files";
    
    private static final Gson gson = new Gson();
    
    private ExploraConf(){}
    
    public static ExploraAccount getAccount(Path ConfigFolder){
        Path confFile = Paths.get(ConfigFolder.toAbsolutePath().toString(), CONF);
        try {
            String access = new String(Files.readAllBytes(confFile), StandardCharsets.UTF_8);
            if(access.isEmpty()){
                throw new ExploraBoxInternalException("Le fichier de configuration ne doit pas être vide");
            }
            String[] userPwd = access.split(":");
            
            return new ExploraAccount(userPwd[0], userPwd[1]);
        } catch (IOException ex) {
            throw new ExploraBoxInternalException("Lecture du fichier de configuration impossible", ex);
        }
    }
    
    public static List<String> getExcludesSync(Path dataFolder){
        try {
            return Files.readAllLines(dataFolder);
        } catch (IOException ex) {
            LOG.warn("lecture du fichier "+IGNORE_FILE+" impossible", ex);
            return new ArrayList<>();
        }
    }
    
    /**
     * 
     * @param configFolder
     * @param account
     * @param force
     * @throws IOException 
     */
    public static void updateAccount(Path configFolder, ExploraAccount account) throws IOException{
        Path confFile = Paths.get(configFolder.toAbsolutePath().toString(), CONF);
        Files.deleteIfExists(confFile);
        Files.write(confFile, gson.toJson(account).getBytes(StandardCharsets.UTF_8));
    }
}
