package main;

import main.gui.MainGui;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

public class CopyHelper {
    private static long progress = 0;

    public static boolean canCopy = true;

    public static boolean isParentDirectory(File parentDirPath, File childPath) {
        Path parentPath = parentDirPath.toPath().toAbsolutePath();
        Path childPathObj = childPath.toPath().toAbsolutePath();

        if (parentPath.equals(childPathObj)) {
            return true;
        }

        return childPathObj.startsWith(parentPath);
    }

    public static void copyDirectory(File sourceLocation, File targetLocation, MainGui mainGui, boolean isRoot) throws IOException {
        if (!canCopy) {
            mainGui.loge("Stopping coping on file: " + sourceLocation);
            throw new RuntimeException("Stopping coping on file: " + sourceLocation);
        }

        if (isRoot) {
            if (isParentDirectory(sourceLocation, targetLocation)) {
                mainGui.loge("Destination " + targetLocation + " is contained in source " + sourceLocation + ", will cause recursion if tried to copy.");
                return;
            }

                mainGui.setFileProgressBarMax(getDirectorySize(sourceLocation));
            progress = 0;
        }

        if (sourceLocation.isDirectory()) {
            // Create the destination directory if it doesn't exist
            if (!targetLocation.exists()) {
                if (!targetLocation.mkdirs()) {
                    throw new IOException("Could not create target directory: " + targetLocation);
                }
            }

            // Recursively copy all files and subdirectories
            for (String file : Objects.requireNonNull(sourceLocation.list())) {
                File sourceFile = new File(sourceLocation, file);
                File destFile = new File(targetLocation, file);
                copyDirectory(sourceFile, destFile, mainGui, false);
            }
        } else {
            // Copy the file

            if (targetLocation.exists()) {
                mainGui.logs("File " + targetLocation + " already exists");
                return;
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buffer = new byte[16392];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
                progress += length;
                mainGui.setFileProgressBarProgesss(inKb(progress));
            }

            in.close();
            out.close();
        }
    }

    public static void rawCopyFile(InputStream src, File dst) throws IOException {
        if (dst.exists()) {
            throw new RuntimeException("File already exists: " + dst);
        }

        OutputStream out = new FileOutputStream(dst);

        byte[] buffer = new byte[16392];
        int length;
        while ((length = src.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        src.close();
        out.close();
    }

    public static int getDirectorySize(File directory) throws IOException {
        long size = 0;
        if (directory.isDirectory()) {
            try (Stream<Path> stream = Files.walk(Paths.get(directory.getAbsolutePath()))) {
                size = stream
                        .filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                e.printStackTrace(System.err);
                                return 0;
                            }
                        })
                        .sum();
            }
        }
        return inKb(size);
    }

    private static int inKb(long size) {
        return size / 1024 < Integer.MAX_VALUE ? (int) (size / 1024) : Integer.MAX_VALUE;
    }
}