package net.otlg.mcmapper.gui.adapter.container;

import net.otlg.mcmapper.gui.adapter.MojangAPI;

import java.io.IOException;

public class VersionInfo {
    private final String id;
    private final String jsonUrl;

    public VersionInfo(String id, String jsonUrl) {
        this.id = id;
        this.jsonUrl = jsonUrl;
    }

    public String getJsonUrl() {
        return jsonUrl;
    }

    public String getId() {
        return id;
    }

    public VersionDetails fetchDetails() throws IOException {
        return MojangAPI.getDetails(this);
    }

    @Override
    public String toString() {
        return "MCVersion{" +
                "id='" + id + '\'' +
                ", jsonUrl='" + jsonUrl + '\'' +
                '}';
    }
}
