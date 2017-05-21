package io.development.tymo.model_server;


import java.io.Serializable;

public class AppInfoWrapper implements Serializable {
    private static final long serialVersionUID = 53L;
    private AppInfoServer appInfoServer;

    public AppInfoWrapper(AppInfoServer appInfoServer) {
        this.appInfoServer = appInfoServer;
    }

    public AppInfoServer getAppInfoServer() {
        return appInfoServer;
    }

}