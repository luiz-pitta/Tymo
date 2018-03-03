package io.development.tymo.model_server;


import java.io.Serializable;

public class FlagWrapper implements Serializable {
    private static final long serialVersionUID = 3L;
    private FlagServer flagServer;

    public FlagWrapper(FlagServer flagServer) {
        this.flagServer = flagServer;
    }

    public FlagServer getFlagServer() {
        return flagServer;
    }

}