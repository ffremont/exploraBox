/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox.models;

import java.nio.file.Path;

/**
 *
 * @author florent
 */
public class DataFolder {
    private Path source;
    private String folderTargetName;

    public DataFolder(Path source, String folderTargetName) {
        this.source = source;
        this.folderTargetName = folderTargetName;
    }
    
    public Path getSource() {
        return source;
    }

    public void setSource(Path source) {
        this.source = source;
    }

    public String getFolderTargetName() {
        return folderTargetName;
    }

    public void setFolderTargetName(String folderTargetName) {
        this.folderTargetName = folderTargetName;
    }
    
    
}
