package cc.fish.rfl.api.utils;

import java.util.ArrayList;
import java.util.List;

public class OptionParser {
    private final List<String> options;

    public OptionParser(String[] args) {
        this.options = new ArrayList<>();

        for (String arg : args) {
            if (arg.startsWith("--")) {
                this.options.add(arg.substring(2));
            }
        }
    }

    public boolean isEnabled(String name) {
        return this.options.contains(name);
    }
}
