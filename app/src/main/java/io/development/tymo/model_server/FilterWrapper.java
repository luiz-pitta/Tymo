package io.development.tymo.model_server;


import java.io.Serializable;

public class FilterWrapper implements Serializable {
    private static final long serialVersionUID = 10L;
    private FilterServer filterServer;

    public FilterWrapper(FilterServer filterServer) {
        this.filterServer = filterServer;
    }

    public FilterServer getFilterServer() {
        return filterServer;
    }

}