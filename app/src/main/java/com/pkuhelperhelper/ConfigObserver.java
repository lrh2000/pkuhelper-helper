package com.pkuhelperhelper;

import android.os.FileObserver;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;

public class ConfigObserver extends FileObserver {
    File confFile;
    long lastModifiedTime;

    private void refresh() {
        if (confFile.lastModified() == lastModifiedTime)
            return;
        lastModifiedTime = confFile.lastModified();

        try {
            FileInputStream fis = new FileInputStream(confFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            Object obj = ois.readObject();
            ConfigManager.set((Map<String, ?>) obj);

            XposedBridge.log("Preferences have been refreshed.");
        } catch (Throwable e) {
            XposedBridge.log("Failed to refresh preferences.");
            XposedBridge.log(e);
        }
    }

    public ConfigObserver(File dir) {
        super(dir, MOVED_TO);
        confFile = new File(dir, "pkuhelperhelper.conf");
        lastModifiedTime = 0;

        startWatching();
        refresh();
    }

    @Override
    public void onEvent(int i, @Nullable String s) {
        if (s != null && s.equals("pkuhelperhelper.conf"))
            refresh();
    }
}
