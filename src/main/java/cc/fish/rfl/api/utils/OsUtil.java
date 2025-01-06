package cc.fish.rfl.api.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OsUtil {

    public OS getOs() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return OS.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux")) {
            return OS.LINUX;
        } else if (os.contains("mac")) {
            return OS.MAC;
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    public enum OS {
        WINDOWS("dll"),
        LINUX("so"),
        MAC("dylib");

        private final String nativeExtension;
    }

}
