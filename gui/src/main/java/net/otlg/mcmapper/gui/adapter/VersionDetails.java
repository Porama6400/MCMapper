package net.otlg.mcmapper.gui.adapter;

public class VersionDetails {

    private final VersionInfo info;

    private DownloadEntry client;
    private DownloadEntry clientMap;

    private DownloadEntry server;
    private DownloadEntry serverMap;

    public VersionDetails(VersionInfo info) {
        this.info = info;
    }

    public VersionInfo getInfo() {
        return info;
    }

    public DownloadEntry getClient() {
        return client;
    }

    public void setClient(DownloadEntry client) {
        this.client = client;
    }

    public DownloadEntry getClientMap() {
        return clientMap;
    }

    public void setClientMap(DownloadEntry clientMap) {
        this.clientMap = clientMap;
    }

    public DownloadEntry getServer() {
        return server;
    }

    public void setServer(DownloadEntry server) {
        this.server = server;
    }

    public DownloadEntry getServerMap() {
        return serverMap;
    }

    public void setServerMap(DownloadEntry serverMap) {
        this.serverMap = serverMap;
    }
}
