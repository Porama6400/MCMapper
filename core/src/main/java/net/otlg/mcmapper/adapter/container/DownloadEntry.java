package net.otlg.mcmapper.adapter.container;

import com.google.gson.annotations.SerializedName;
import net.otlg.mcmapper.adapter.MojangAPI;
import net.otlg.mcmapper.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadEntry {
    @SerializedName("sha1")
    String hash;
    @SerializedName("size")
    int size;
    @SerializedName("url")
    String urlString;

    public static DownloadEntry fromJsonString(String jsonString) {
        return MojangAPI.GSON.fromJson(jsonString, DownloadEntry.class);
    }

    public String getHash() {
        return hash;
    }

    public int getSize() {
        return size;
    }

    public String getUrlString() {
        return urlString;
    }

    public void download(File output) throws IOException {
        FileOutputStream fileOutputStream = null;
        InputStream urlInputStream = null;
        try {
            fileOutputStream = new FileOutputStream(output);

            URL url = new URL(this.urlString);
            URLConnection connection = url.openConnection();

            urlInputStream = connection.getInputStream();

            IOUtils.copyStream(urlInputStream,fileOutputStream);
        } finally {
            if (fileOutputStream != null) fileOutputStream.close();
            if (urlInputStream != null) urlInputStream.close();
        }
    }
}
