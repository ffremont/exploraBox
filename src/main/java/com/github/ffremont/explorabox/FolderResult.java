/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox;

import com.github.ffremont.explorabox.models.SimFile;

/**
 *
 * @author florent
 */
public class FolderResult {
    private String errorMsg;
    private SimFile[] files;
    private String webPath;

    private FolderResult(String msg) {
        this.errorMsg = msg;
    }
    private FolderResult(String webPath, SimFile[] files) {
        this.files = files;
        this.webPath = webPath;
    }
    
    public static FolderResult ok(String webPath, SimFile[] files){
        return new FolderResult(webPath, files);
    }

    public static FolderResult error(String msg){
        return new FolderResult(msg);
    }

    public String getWebPath() {
        return webPath;
    }

    public void setWebPath(String webPath) {
        this.webPath = webPath;
    }
    
    private FolderResult(SimFile[] files) {
        this.files = files;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public SimFile[] getFiles() {
        return files;
    }
    
    public boolean isOk(){
        return errorMsg == null;
    }
    
}
