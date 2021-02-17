/**
 * ScriptBlockPlus - Allow you to add script to any blocks.
 * Copyright (C) 2021 yuttyann44581
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.yuttyann.scriptblockplus;

import com.github.yuttyann.scriptblockplus.file.SBFile;
import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.utils.FileUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ScriptBlockPlus Updater クラス
 * @author yuttyann44581
 */
public final class Updater {

    private final Plugin plugin;
    private final String pluginName;
    private final String pluginVersion;

    private String latest;
    private String downloadURL;
    private String changeLogURL;

    private boolean update;
    private List<String> details;

    public Updater(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.pluginName = plugin.getName();
        this.pluginVersion = plugin.getDescription().getVersion();
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    public String getJarName() {
        return pluginName + " v" + latest + ".jar";
    }

    public void load() throws Exception {
        if(!SBConfig.UPDATE_CHECKER.getValue()){
            update = false;
            return;
        }
        update = false;
        details = null;
        latest = downloadURL = changeLogURL = null;
        NodeList rootChildren = getDocument(pluginName).getDocumentElement().getChildNodes();
        for (int i = 0; i < rootChildren.getLength(); i++) {
            Node updateNode = rootChildren.item(i);
            if (updateNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (updateNode.getNodeName().equals("update")) {
                this.latest = ((Element) updateNode).getAttribute("version");
            }
            NodeList updateChildren = updateNode.getChildNodes();
            for (int j = 0; j < updateChildren.getLength(); j++) {
                Node node = updateChildren.item(j);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                switch (node.getNodeName()) {
                case "download":
                    this.downloadURL = ((Element) node).getAttribute("url");
                    break;
                case "changelog":
                    this.changeLogURL = ((Element) node).getAttribute("url");
                    break;
                case "details":
                    NodeList detailsChildren = node.getChildNodes();
                    this.details = new ArrayList<>(detailsChildren.getLength());
                    for (int k = 0; k < detailsChildren.getLength(); k++) {
                        Node detailsNode = detailsChildren.item(k);
                        if (detailsNode.getNodeType() == Node.ELEMENT_NODE) {
                            details.add(((Element) detailsNode).getAttribute("info"));
                        }
                    }
                }
            }
        }
        this.update = Utils.getVersionInt(latest) > Utils.getVersionInt(pluginVersion);
    }

    public boolean run(@NotNull CommandSender sender) {
        if (!SBConfig.UPDATE_CHECKER.getValue() || !update) {
            return false;
        }
        File data = plugin.getDataFolder();
        SBFile logFile = new SBFile(data, "update/ChangeLog.txt");
        boolean sameLogs = !logFile.exists() || !logEquals(changeLogURL, logFile), failure = false;
        SBConfig.UPDATE_CHECK.replace(pluginName, latest, details).send(sender);
        if (SBConfig.AUTO_DOWNLOAD.getValue()) {
            SBFile jarFile = new SBFile(data, "update/jar/" + getJarName());
            try {
                SBConfig.UPDATE_DOWNLOAD_START.send(sender);
                FileUtils.downloadFile(changeLogURL, logFile);
                FileUtils.downloadFile(downloadURL, jarFile);
            } catch (IOException e) {
                failure = true;
                SBConfig.ERROR_UPDATE.send(sender);
            } finally {
                if (jarFile.exists() && !failure) {
                    String fileName = jarFile.getName();
                    String filePath = jarFile.getPath().replace(File.separatorChar, '/');
                    SBConfig.UPDATE_DOWNLOAD_END.replace(fileName, filePath, getSize(jarFile.length())).send(sender);
                }
            }
        }
        if (SBConfig.OPEN_CHANGE_LOG.getValue() && sameLogs && !failure) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private String getSize(long length) {
        double b = 1024D;
        if (b > length) {
            return length + " Byte";
        }
        String unit = b * b > length ? " KB" : " MB";
        double size = unit.endsWith("KB") ? length / b : length / b / b;
        return new BigDecimal(size).setScale(1, RoundingMode.HALF_UP).doubleValue() + unit;
    }

    private boolean logEquals(@NotNull String url, @NotNull File file) {
        if (!file.exists()) {
            return false;
        }
        try (BufferedReader reader1 = FileUtils.newBufferedReader(file); BufferedReader reader2 = FileUtils.newBufferedReader(new URL(url).openStream())) {
            while (reader1.ready() && reader2.ready()) {
                if (!reader1.readLine().equals(reader2.readLine())) {
                    return false;
                }
            }
            return !(reader1.ready() || reader2.ready());
        } catch (IOException e) {
            return false;
        }
    }

    @NotNull
    private Document getDocument(@NotNull String name) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(FileUtils.getWebFile("https://xml.yuttyann44581.net/uploads/" + name + ".xml"));
    }
}