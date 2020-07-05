package com.pkuhelperhelper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigManager {
    private static AtomicBoolean longClickToCopy = new AtomicBoolean(true);
    private static AtomicBoolean longClickToSavePicture = new AtomicBoolean(true);

    public static boolean getLongClickToCopy() {
        return longClickToCopy.get();
    }

    public static boolean getLongClickToSavePicture() {
        return longClickToSavePicture.get();
    }

    public static void set(Map<String, ?> data) {
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            switch (key) {
                case "long_click_to_copy":
                    longClickToCopy.set((Boolean) value);
                    break;
                case "long_click_to_save_picture":
                    longClickToSavePicture.set((Boolean) value);
                    break;
            }
        }
    }
}