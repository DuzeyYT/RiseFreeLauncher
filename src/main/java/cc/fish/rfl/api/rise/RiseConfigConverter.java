package cc.fish.rfl.api.rise;

import lombok.experimental.UtilityClass;
import org.json.JSONObject;

@UtilityClass
public class RiseConfigConverter {

    public String convert(String config) {
        JSONObject jsonObject = new JSONObject(config);

        String theme = jsonObject.getString("theme");
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("th_").append(theme).append("\n");
        for (String module : jsonObject.keySet()) {
            if (!module.contains(".")) continue;

            JSONObject moduleObject = jsonObject.getJSONObject(module);

            if (moduleObject.has("state"))
                stringBuilder.append(module).append("_").append("e1_").append(moduleObject.getBoolean("state")).append("\n");
            if (moduleObject.has("keyCode"))
                stringBuilder.append(module).append("_").append("kc_").append(moduleObject.getInt("keyCode")).append("\n");

            // settings
            for (String setting : moduleObject.keySet()) {
                if (setting.equals("state") || setting.equals("keyCode")) continue;

                Object value = moduleObject.get(setting);
                if (value instanceof JSONObject jsonObject1) {
                    for (String jsonObject1Key : jsonObject1.keySet()) {
                        stringBuilder.append(module).append("_").append(setting).append("_").append(jsonObject1Key)
                                .append("_").append(jsonObject1.get(jsonObject1Key)
                                .toString().replaceAll("\"", "")).append("\n");
                    }
                }
            }
        }

        return stringBuilder.toString();
    }
}