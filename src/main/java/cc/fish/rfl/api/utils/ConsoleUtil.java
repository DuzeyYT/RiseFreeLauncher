package cc.fish.rfl.api.utils;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

@UtilityClass
public class ConsoleUtil {

    public void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows"))
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            else
                new ProcessBuilder("clear").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    public void emptyLine() {
        System.out.println();
    }

    public String getCustomArt(String text, String font) {
        if (font == null || font.isEmpty()) font = "ogre";

        try {
            HttpURLConnection connection =
                    (HttpURLConnection)
                            URI.create(
                                            String.format(
                                                    "http://www.network-science.de/ascii/ascii.php?TEXT=%s&x=31&y=5&FONT=%s&RICH=no&FORM=left&STRE=no&WIDT=2000",
                                                    text.replace(" ", "+"), font))
                                    .toURL()
                                    .openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            String response = DownloadUtil.readInputStream(connection.getInputStream());

            return response.substring(
                    response.indexOf("<TR><TD><PRE>") + 13,
                    response.indexOf("</PRE><!-- white text and background :) /!-->"));
        } catch (IOException e) {
            return "Error while getting custom ASCII art, maybe the font is not supported?";
        }
    }
}
