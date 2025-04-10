package com.padesigner.ui;

import java.io.*;
import java.util.*;

public class HardwareDetector {
    public static List<String> getUsbDrivePaths() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return getUsbDrivesWindows();
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return getUsbDrivesLinuxMac();
        } else {
            throw new UnsupportedOperationException("Os is not supported: " + os);
        }
    }

    private static List<String> getUsbDrivesWindows() throws IOException {
        List<String> drives = new ArrayList<>();
        String command = "powershell Get-WmiObject Win32_LogicalDisk | Where-Object { $_.DriveType -eq 2 }";
        Process process = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP866"));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("DeviceID")) {
                String driveLetter = line.split(":")[1].trim();
                drives.add(driveLetter + "\\");
            }
        }
        return drives;
    }

    private static List<String> getUsbDrivesLinuxMac() throws IOException {
        List<String> drives = new ArrayList<>();
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            // MacOS
            String[] command = { "bash", "-c", "diskutil list" };
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("/Volumes/")) {
                    String mountPoint = line.split("\\s+")[0]; // Получаем путь к диску
                    drives.add(mountPoint);
                }
            }
        } else {
            // Linux
            String[] command = { "bash", "-c", "lsblk -o NAME,MOUNTPOINT,RM,TRAN -J" };
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder jsonOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }

            org.json.JSONObject json = new org.json.JSONObject(jsonOutput.toString());
            org.json.JSONArray devices = json.getJSONArray("blockdevices");

            for (int i = 0; i < devices.length(); i++) {
                org.json.JSONObject device = devices.getJSONObject(i);
                checkUsbDrive(device, drives);
            }
        }

        return drives;
    }

    private static void checkUsbDrive(org.json.JSONObject device, List<String> drives) {
        boolean isRemovable = device.optBoolean("rm", false);
        String transport = device.optString("tran", "");

        if (device.has("children")) {
            org.json.JSONArray children = device.getJSONArray("children");
            for (int i = 0; i < children.length(); i++) {
                org.json.JSONObject child = children.getJSONObject(i);
                String mountpoint = child.optString("mountpoint", null);

                if (isRemovable && "usb".equalsIgnoreCase(transport) && mountpoint != null
                        && !mountpoint.equals("null")) {
                    drives.add(mountpoint+"/");
                }
            }
        }
    }
}