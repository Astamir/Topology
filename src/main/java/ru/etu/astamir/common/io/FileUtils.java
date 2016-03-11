package ru.etu.astamir.common.io;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A collection of utility methods for files.
 */
public class FileUtils {
    public static final int BUFFER_SIZE = 4096;

    /**
     * Finds a file with given id pattern in all subdirectories from the give base path
     *
     * @return File
     */
    public static File findFile(File root, final Pattern pattern) {
        File[] files = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(pattern.pattern());
            }
        });

        if (files.length > 0) {
            return files[0]; // found something
        } else {
            File[] directories = root.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (directories.length > 0) {
                // lets look in directories
                for (File dir : directories) {
                    File file = findFile(dir, pattern);
                    if (file != null) {
                        return file;
                    }
                }
            }
        }

        return null;
    }


    /**
     * Returns absolute file id from the given parent directory and file id.
     */
    public static String getAbsoluteName(String parent, String file_name) {
        return new File(parent, file_name).getAbsolutePath();
    }

    /**
     * Returns new file id from the <code>file_name</code> which is located in the <code>new_parent</code> directory.
     * For example, <code>getNewPath("/tmp", "/home/my/file.txt")</code> would return "/tmp/file.txt" path.
     */
    public static String getNewPath(String new_parent, String file_name) {
        return getAbsoluteName(new_parent, new File(file_name).getName());
    }

    /**
     * Write the contents of the given input stream to the output stream.
     */
    public static void pipe(File source, File destination) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = buffered(new FileInputStream(source));
            out = buffered(new FileOutputStream(destination));
            pipe(in, out);
        } finally {
            closeSilently(in);
            closeSilently(out);
        }
    }

    /* Returns string read from given input stream. Closes the stream afterward. */
    public static String convertStreamToString(InputStream is) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = buffered(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null)
                result.append(line).append('\n');
        } finally {
            closeSilently(reader);
        }
        return result.toString();
    }

    /**
     * Write the contents of the given input stream to the output stream.
     */
    public static void pipe(InputStream in, OutputStream out) throws IOException {
        pipe(in, out, BUFFER_SIZE);
    }

    public static void pipe(InputStream in, OutputStream out, int buffer_size) throws IOException {
        byte[] buffer = new byte[buffer_size];
        int length;
        while ((length = in.read(buffer)) > -1) {
            out.write(buffer, 0, length);
        }
    }

    public static void closeSilently(Closeable source) {
        if (source != null) {
            try {
                source.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }

    public static void close(Closeable source, Logger log) {
        close(source, log, "Unable to close resource");
    }

    public static void close(Closeable source, Logger log, String message) {
        if (source != null) {
            try {
                source.close();
            } catch (IOException e) {
                log.error(message, e);
            }
        }
    }

    public static boolean copyFile(File file, File new_file) {
        FileChannel fc_in = null;
        FileChannel fc_out = null;
        try {
            if (file.exists()) {
                fc_in = new FileInputStream(file).getChannel();
                fc_out = new FileOutputStream(new_file).getChannel();
                fc_out.transferFrom(fc_in, 0, fc_in.size());
                fc_in.close();
                fc_out.close();
            }
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (fc_in != null)
                    fc_in.close();
                if (fc_out != null)
                    fc_out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean removeDir(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (!removeDir(file)) return false;
            }
        }
        return dir.delete();
    }

    public static BufferedInputStream buffered(InputStream is) {
        return is instanceof BufferedInputStream ? (BufferedInputStream) is : new BufferedInputStream(is, BUFFER_SIZE);
    }

    public static BufferedOutputStream buffered(OutputStream os) {
        return os instanceof BufferedOutputStream ? (BufferedOutputStream) os : new BufferedOutputStream(os, BUFFER_SIZE);
    }

    public static BufferedReader buffered(Reader r) {
        return r instanceof BufferedReader ? (BufferedReader) r : new BufferedReader(r, BUFFER_SIZE);
    }

    public static BufferedWriter buffered(Writer w) {
        return w instanceof BufferedWriter ? (BufferedWriter) w : new BufferedWriter(w, BUFFER_SIZE);
    }


    public static void unzip(File zip, File dest_dir) throws IOException {
        unzip(new ZipFile(zip), dest_dir);
    }

    public static void unzip(ZipFile zip_file, File dest_dir) throws IOException {
        byte[] buffer = new byte[1024];
        try {
            Enumeration<? extends ZipEntry> entries = zip_file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    new File(dest_dir, entry.getName()).mkdirs();
                } else {
                    File f = new File(dest_dir, entry.getName());
                    f.getParentFile().mkdirs();
                    InputStream in = zip_file.getInputStream(entry);
                    OutputStream out = new FileOutputStream(f);
                    try {
                        int length;
                        while ((length = in.read(buffer)) > -1) {
                            out.write(buffer, 0, length);
                        }
                    } finally {
                        closeSilently(in);
                        closeSilently(out);
                    }
                }
            }
        } finally {
            zip_file.close();
        }
    }


    /**
     * Requests deleting directory and all the files within it on JVM exit.
     *
     * @param dir directory or file
     */
    public static void deleteRecursiveOnExit(File dir) {
        dir.deleteOnExit();
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteRecursiveOnExit(file);
                }
            }
        }
    }

    /**
     * Copies content of given files to backup files
     *
     * @param files
     * @throws IOException
     */
    public static void createBackups(File... files) throws IOException {
        for (File file : files) {
            pipe(file, getBackupFile(file));
        }
    }

    /**
     * Returns backup file id for given file
     *
     * @param f
     * @return
     */
    public static File getBackupFile(File f) {
        return new File(f.getParent(), f.getName() + ".bak");
    }

    /**
     * Deletes backups for given files
     *
     * @param files
     * @throws IOException
     */
    public static void deleteBackups(File... files) throws IOException {
        for (File file : files) {
            getBackupFile(file).delete();
        }
    }

    /**
     * Restores content of files from previously saved backup. Does nothing if backup doesn't exist.
     *
     * @param files
     * @throws IOException
     */
    public static void restoreBackups(File... files) throws IOException {
        for (File file : files) {
            File backup = getBackupFile(file);
            if (backup.exists()) {
                pipe(backup, file);
            }
        }
    }
}