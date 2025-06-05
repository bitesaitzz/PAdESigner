package com.padesigner.ui;

import java.io.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class for detecting hardware, specifically USB drive paths.
 * It uses OS-specific commands to find connected USB drives.
 */
public class HardwareDetector {
    /**
     * Retrieves a list of paths for connected USB drives based on the operating
     * system.
     *
     * @return A List of strings, where each string is the path to a USB drive.
     * @throws IOException                   If an I/O error occurs while executing
     *                                       system commands.
     * @throws UnsupportedOperationException If the current operating system is not
     *                                       supported.
     */
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

    /**
     * Retrieves USB drive paths on Windows using PowerShell.
     *
     * @return A List of strings, where each string is the path to a USB drive
     *         (e.g., "D:\\").
     * @throws IOException If an I/O error occurs while executing the PowerShell
     *                     command.
     */
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

    /**
     * Retrieves USB drive paths on Linux and macOS using system commands (`bash` or
     * `lsblk`).
     *
     * @return A List of strings, where each string is the mount point path to a USB
     *         drive.
     * @throws IOException If an I/O error occurs while executing the system
     *                     command.
     */
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

    /**
     * Recursively checks a device and its children (partitions) to identify USB
     * drives
     * based on the 'rm' (removable) and 'tran' (transport) properties from `lsblk`
     * output.
     * Adds the mount point to the list if it's identified as a removable USB device
     * with a mount point.
     *
     * @param device The JSONObject representing a block device from `lsblk` output.
     * @param drives The List to add the found USB drive paths to.
     */
    private static void checkUsbDrive(org.json.JSONObject device, List<String> drives) {
        boolean isRemovable = device.optBoolean("rm", false);
        String transport = device.optString("tran", "");

        if (device.has("children")) {
            JSONArray children = device.getJSONArray("children");
            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.getJSONObject(i);
                String mountpoint = child.optString("mountpoint", null);

                if (isRemovable && "usb".equalsIgnoreCase(transport) && mountpoint != null
                        && !mountpoint.equals("null")) {
                    drives.add(mountpoint + "/");
                }
            }
        }
    }
}