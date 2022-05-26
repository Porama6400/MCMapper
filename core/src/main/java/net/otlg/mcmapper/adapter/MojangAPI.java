package net.otlg.mcmapper.adapter;

import com.google.gson.*;
import net.otlg.mcmapper.adapter.container.DownloadEntry;
import net.otlg.mcmapper.adapter.container.VersionDetails;
import net.otlg.mcmapper.adapter.container.VersionInfo;
import net.otlg.mcmapper.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MojangAPI {
    public static final Gson GSON = new GsonBuilder().create();

    public static String fetchString(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream stream = (urlConnection.getInputStream());
        return IOUtils.streamToString(stream);
    }

    public static HashMap<String, VersionInfo> fetchVersionList() throws IOException {
        String jsonString = fetchString(new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"));
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonArray versionsJsonArray = jsonObject.getAsJsonArray("versions");
        HashMap<String, VersionInfo> versionMap = new HashMap<>();

        int size = versionsJsonArray.size();
        for (int i = 0; i < size; i++) {

            JsonObject element = versionsJsonArray.get(i).getAsJsonObject();
            VersionInfo versionInfo = new VersionInfo(element.get("id").getAsString(), element.get("url").getAsString());
            versionMap.put(versionInfo.getId(), versionInfo);

            if (versionInfo.getId().equals("19w36a")) break; // first version with mapping
        }

        return versionMap;
    }

    public static VersionDetails getDetails(VersionInfo info) throws IOException {
        String jsonString = fetchString(new URL(info.getJsonUrl()));
        JsonObject downloads = JsonParser.parseString(jsonString)
                .getAsJsonObject()
                .getAsJsonObject("downloads");

        VersionDetails detailedData = new VersionDetails(info);
        detailedData.setClient(DownloadEntry.fromJsonString(downloads.get("client").toString()));
        detailedData.setClientMap(DownloadEntry.fromJsonString(downloads.get("client_mappings").toString()));
        detailedData.setServer(DownloadEntry.fromJsonString(downloads.get("server").toString()));
        detailedData.setServerMap(DownloadEntry.fromJsonString(downloads.get("server_mappings").toString()));

        return detailedData;
    }
}
