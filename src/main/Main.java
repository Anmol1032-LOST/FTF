package main;

import main.gui.MainGui;

import java.io.*;
import java.util.*;

public class Main {
    public final static File dataFile = new File("data.txt");
    private final static File sharedDataFile = new File("shared.txt");
    private final static HashSet<String> shared = new HashSet<>();
    private final static ArrayList<String> newlyShared = new ArrayList<>();
    private final MainGui mainGui;
    public static HashMap<String, File> locations = new LinkedHashMap<>();
    public static final String separator = "=";

    public Main(MainGui mainGui) {
        this.mainGui = mainGui;

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            private static final Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                mainGui.loge("Error: " + e.getMessage());
                e.printStackTrace(System.err);
                save();
                if (handler != null)
                    handler.uncaughtException(t, e);
            }
        });
    }

    public void readData() {
        if (!sharedDataFile.exists()) try {
            if (!sharedDataFile.createNewFile()) {
                throw new IOException("Cannot create " + sharedDataFile + " file.");
            }
        } catch (IOException e) {
            mainGui.loge(e.getMessage());
        }

        if (!dataFile.exists()) try {
            if (!dataFile.createNewFile()) {
                throw new IOException("Cannot create " + dataFile + " file.");
            }
        } catch (IOException e) {
            mainGui.loge(e.getMessage());
        }


        shared.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(sharedDataFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                shared.add(line);
            }
        } catch (IOException e) {
            mainGui.loge(e.getMessage());
            e.printStackTrace(System.err);
        }


        locations.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            String[] split;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                split = line.split(separator, 2);
                String key = split[0].trim();
                String value = split[1].trim();
                locations.put(key, new File(value));
            }
        } catch (IOException e) {
            mainGui.loge(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sharedDataFile, true))) {
            for (String string : newlyShared) {
                writer.write(string);
                writer.newLine();
            }
        } catch (IOException e) {
            mainGui.loge(e.getMessage());
            e.printStackTrace(System.err);
        }
        newlyShared.clear();
    }

    public void start(File src) {
        readData();

        for (File f : locations.values()) {
            boolean ignored = f.mkdirs();
        }

        if (!src.exists()) {
            mainGui.loge("Cannot File Source File");
            return;
        }
        search(src);

        save();
        mainGui.logDone();
    }

    private void search(File src) {
        for (File file : Objects.requireNonNull(src.listFiles())) {

            /// uncomment this for recursive search (but the file location data will be lost)
//            if (file.isDirectory()) {
//                search(src);
//                if (Objects.requireNonNull(src.listFiles()).length == 0) {
//                    boolean ignored = src.delete();
//                }
//            }

            boolean found = false;
            for (Map.Entry<String, File> entry : locations.entrySet()) {
                String s = entry.getKey();
                File f = entry.getValue();
                if (file.getName().endsWith(s)) {
                    found = true;
                    copy(file, f);
                    break;
                }
            }
            if (!found) {
                mainGui.logw("Cannot identify: " + file.getName());
            }
        }
    }

    private void copy(File file, File location) {
        String name = file.getName();
        if (shared.contains(file.getAbsolutePath())) return;

        File dstFile = new File(location, name);

        mainGui.logd("Coping " + file.getAbsolutePath() + " to " + dstFile.getAbsolutePath());

        try {
            CopyHelper.copyDirectory(file, dstFile, mainGui, true);
            newlyShared.add(file.getAbsolutePath());
        } catch (IOException e) {
            mainGui.loge(e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}