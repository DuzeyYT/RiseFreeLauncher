package cc.fish.rfl.api.rise;

import cc.fish.rfl.RflMain;
import cc.fish.rfl.api.utils.EncryptionUtil;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

@ServerEndpoint("/")
public class RiseServerEndpoint {

    @OnMessage
    public void onMessage(String message, Session session) {
        if (RiseServer.encryptionKey == null) {
            RflMain.LOGGER.error(
                    "Cannot read message because encryption key is missing. Restart the client.");
            return;
        }
        String decrypted = EncryptionUtil.decrypt(message, RiseServer.encryptionKey);
        JSONObject jsonObject = new JSONObject(decrypted);
        if (!jsonObject.has("id")) {
            RiseServer.LOGGER.error("Returned because of unknown id");
            return;
        }

        int id = jsonObject.getInt("id");

        RiseServer.LOGGER.info("Received message with id: {}", id);

        if (id > 2) return;

        JSONObject output = new JSONObject();

        output.put("id", id);

        if (id == 1) {
            output.put("a", true);
            output.put("b", Math.PI);
            output.put("c", 90.0f);
            output.put("d", System.currentTimeMillis());
            output.put("e", "Hello, World!");
        }
        if (id == 2) {
            output.put("a", RiseConfigConverter.convert(jsonObject.getString("a")));
        }

        session.getAsyncRemote().sendText(output.toString());
    }
}
