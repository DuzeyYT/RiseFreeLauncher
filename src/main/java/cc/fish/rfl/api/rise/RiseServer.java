package cc.fish.rfl.api.rise;

import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.tyrus.server.Server;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("LoggingSimilarMessage")
@Getter
@Setter
public class RiseServer {

    public static final Logger LOGGER = LogManager.getLogger("Rise Server");
    public static final int PORT = 8443;

    public static String encryptionKey;
    public static boolean loggedIn;
    public static long lastKeepAlive;

    public void startServer() {
        // create temporary socket for the agent to give us the encryption key
        new Thread(
                        () -> {
                            try (ServerSocket serverSocket = new ServerSocket(8444)) {
                                Socket agentSocket = serverSocket.accept();
                                LOGGER.info("Agent connected...");

                                DataInputStream in =
                                        new DataInputStream(agentSocket.getInputStream());
                                encryptionKey = in.readUTF();
                                LOGGER.info("Received key: {}", encryptionKey);

                                agentSocket.close();
                            } catch (Exception e) {
                                LOGGER.error("Failed to start server: {}", e.getMessage());
                            }
                        })
                .start();

        try {
            LOGGER.info("Starting backend server...");
            Server server = new Server("localhost", PORT, "/", null, RiseServerEndpoint.class);
            server.start();
            LOGGER.info("backend running on port {}", PORT);

            // keep the retarded server alive
            while (!loggedIn || (System.currentTimeMillis() - lastKeepAlive) < 1000 * 20) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
