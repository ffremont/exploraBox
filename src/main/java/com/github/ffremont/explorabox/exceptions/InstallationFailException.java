/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox.exceptions;

/**
 *
 * @author florent
 */
public class InstallationFailException extends Exception{
    
    public InstallationFailException(String msg, Throwable cause){
        super(msg, cause);
    }

    public InstallationFailException(String msg) {
        super(msg);
    }
    
}
