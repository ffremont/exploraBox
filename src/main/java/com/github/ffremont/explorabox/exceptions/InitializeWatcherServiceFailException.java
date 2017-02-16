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
public class InitializeWatcherServiceFailException extends Exception {

    public InitializeWatcherServiceFailException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InitializeWatcherServiceFailException(String msg) {
        super(msg);
    }
    
}
