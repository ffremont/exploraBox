/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox.services;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class TrayService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TrayService.class);

    public static final String TRAY_OK = "ok_.png";
    public static final String TRAY_SYNC = "sync_.png";
    public static final String TRAY_INIT = "warning_.png";
    public static final String TRAY_KO = "error_.png";

    private String icon;
    private String title;
    private TrayIcon trayIcon;
    private String tooltip;

    public TrayService(String title, String tooltip) {
        this.icon = TRAY_INIT;
        this.title = title;
        this.tooltip = tooltip;
    }

    public static SystemTray getSystemTray() {
        return SystemTray.getSystemTray();
    }

    public void start() {
        if (!SystemTray.isSupported()) {
            LOG.info("SystemTray is not supported");
            return;
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(icon));
        } catch (IOException ex) {
            LOG.error("oups", ex);
        }

        trayIcon = new TrayIcon(image, title);
        trayIcon.setToolTip(tooltip);
        trayIcon.setImageAutoSize(true);
        try {
            getSystemTray().add(trayIcon);
        } catch (AWTException ex) {
            LOG.error("tray impossible à ajouter", ex);
        }
    }

    public void changeState(String tooltip, String icon) {
        LOG.info("changement d'état  {}", tooltip);
        trayIcon.setToolTip(tooltip);
        try {
            trayIcon.setImage(ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(icon)));
            LOG.debug("changement de l'icon");
        } catch (IOException ex) {
            LOG.error("oups", ex);
        }
    }
    
    public void stop(){
        getSystemTray().remove(trayIcon);
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    
}
