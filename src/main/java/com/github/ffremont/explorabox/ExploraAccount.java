/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox;

import com.github.ffremont.explorabox.models.DataFolder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author florent
 */
public class ExploraAccount {
    private String user;
    private String pwd;
    
    private List<DataFolder> folders;

    public ExploraAccount(String user, String pwd) {
        this.user = user;
        this.pwd = pwd;
        this.folders = new ArrayList<>();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public List<DataFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<DataFolder> folders) {
        this.folders = folders;
    }
}
