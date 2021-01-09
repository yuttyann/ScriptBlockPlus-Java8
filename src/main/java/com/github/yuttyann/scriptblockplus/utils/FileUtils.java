package com.github.yuttyann.scriptblockplus.utils;

import com.google.common.base.Charsets;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * ScriptBlockPlus FileUtils クラス
 * 
 * @author yuttyann44581
 */
public final class FileUtils {

    @Nullable
    public static InputStream getResource(@NotNull Plugin plugin, @NotNull String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            URL url = plugin.getClass().getClassLoader().getResource(filePath);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public static void copyFileFromPlugin(@NotNull Plugin plugin, @NotNull File targetFile, @NotNull String sourceFilePath) throws IOException {
        if (StringUtils.isEmpty(sourceFilePath)) {
            return;
        }
        File parent = targetFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        InputStream resource = getResource(plugin, sourceFilePath);
        if (resource == null) {
            return;
        }
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource, Charsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), Charsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } finally {
            resource.close();
        }
    }

    public static void downloadFile(@NotNull String url, @NotNull File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (InputStream is = getWebFile(url); FileOutputStream fos = new FileOutputStream(file)) {
            if (is == null) {
                return;
            }
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) != -1) {
                fos.write(bytes, 0, length);
            }
        }
    }

    @Nullable
    public static InputStream getWebFile(@NotNull String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setAllowUserInteraction(false);
        connection.setInstanceFollowRedirects(true);
        connection.connect();
        int httpStatusCode = connection.getResponseCode();
        if (httpStatusCode == HttpURLConnection.HTTP_OK) {
            return connection.getInputStream();
        }
        connection.disconnect();
        return null;
    }

    public static boolean isEmpty(@NotNull File file) {
        String[] array = file.list();
        return array == null || array.length == 0;
    }
}