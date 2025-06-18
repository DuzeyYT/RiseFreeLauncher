package cc.fish.rfl.api.utils;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.URI;
import java.net.URL;

@UtilityClass
public class DownloadUtil {

    public String readFromWeb(String url) {
        StringBuilder content = new StringBuilder();
        try {
            URL urlObj = URI.create(url).toURL();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlObj.openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();
            return content.toString();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    public void downloadFile(String url, String destination) {
        try {
            File file = new File(destination);
            boolean created = file.getParentFile().mkdirs();

            BufferedInputStream in = new BufferedInputStream(URI.create(url).toURL().openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream out = new BufferedOutputStream(fileOutputStream, 1024);
            byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
