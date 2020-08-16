import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ReplayDownloader {
    private String url;

    public ReplayDownloader(String url) {
        this.url = url;
    }

    public CompressorInputStream run() throws IOException, CompressorException {
        BufferedInputStream bis = new BufferedInputStream(new URL(url).openStream());
        return new CompressorStreamFactory().createCompressorInputStream(bis);
    }
}
