package dev.isnow.masscanlinker;

import com.github.simplenet.packet.Packet;
import dev.isnow.masscanlinker.client.ClientListener;
import dev.isnow.masscanlinker.client.impl.MasscanClient;
import dev.isnow.masscanlinker.util.*;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class MasscanServer {
    private final ClientListener clientListener;
    private final File scanningFile;
    private static MasscanServer instance;
    private final File outputFolder;

    private long startTime;
    public MasscanServer() {
        instance = this;
        System.out.println("  _____________\n" +
                "< MASSCANLINKER >\n" +
                "  -------------\n" +
                "         \\   ^__^ \n" +
                "          \\  (oo)\\_______\n" +
                "             (__)\\       )\\/\\\n" +
                "                 ||----w |\n" +
                "                 ||     ||\n" +
                "    ");
        System.out.println("A Private project made with " + Color.ANSI_RED + "❤" + Color.ANSI_RESET + " by Isnow");
        System.out.println("Checking license...");
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {}
        System.out.println("License check passed, Welcome ISNOW|COW|SPOOKY|LUCA");
        System.out.println("Todays MOTD: " + FileUtil.getMOTD());
        System.out.println("Creating outputs directory.");

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        outputFolder = new File(s, "outputs/");

        if(!outputFolder.exists()) {
            boolean done = outputFolder.mkdir();
            if(done) {
                System.out.println("Finished creating output directory.");
            }
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        System.out.println("Is there a scan in-progress?");
        String inProgress = "NO";
        try {
            inProgress = reader.readLine();
        } catch (IOException e) {
            System.out.println("INVALID RESPONSE");
            System.exit(0);
        }

        if(inProgress.equalsIgnoreCase("yes")) {
            scanningFile = new File(outputFolder, "masscanlinker-recovered.txt");
            System.out.println("Starting ClientListener...");
            clientListener = new ClientListener(1340);
            startTime = System.currentTimeMillis();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.equals("status")) {
                        System.out.println("[Info] Requesting status...");
                        clientListener.getClients().values().forEach(masscanClient -> Packet.builder().putByte(2).putString("MASSCANLINKER-1337").queueAndFlush(masscanClient.getClient()));
                    } else if (line.equals("stop")) {
                        System.out.println("[Info] Stopping all scans...");
                        clientListener.getClients().values().forEach(masscanClient -> Packet.builder().putByte(3).putString("MASSCANLINKER-1337").queueAndFlush(masscanClient.getClient()));
                    }
                }
            } catch (IOException ignored) {}
        } else {
            System.out.println("What IP range(s) would you like to scan?");

            StringBuilder ipRangeInput = new StringBuilder("1.1.1.1");
            try {
                ipRangeInput = new StringBuilder(reader.readLine());
            } catch (IOException e) {
                System.out.println("Invalid IpRange");
                System.exit(0);
            }

            if (ipRangeInput.toString().contains(" ")) {
                System.out.println("IPRange cant contain any spaces!");
                System.exit(0);
            }
            boolean singleShit = false;
            if (ipRangeInput.toString().contains("/")) {
                if (ipRangeInput.toString().contains(",")) {
                    List<String> split = new ArrayList<>(Arrays.asList(ipRangeInput.toString().split(",")));
                    ipRangeInput = new StringBuilder();
                    for(String cidr : split) {
                        try {
                            CIDRUtils converted = new CIDRUtils(cidr);
                            String convertedSting = converted.getNetworkAddress() + "-" + converted.getBroadcastAddress();
                            ipRangeInput.append(convertedSting).append(",");
                        } catch (UnknownHostException e) {
                            System.out.println("INVALID CIDR");
                            System.exit(0);
                        }
                    }
                } else {
                    try {
                        CIDRUtils converted = new CIDRUtils(ipRangeInput.toString());
                        ipRangeInput = new StringBuilder(converted.getNetworkAddress() + "-" + converted.getBroadcastAddress());
                        singleShit = true;
                    } catch (UnknownHostException exception) {
                        System.out.println("INVALID CIDR");
                        System.exit(0);
                    }
                }
            }

            if(!singleShit) {
                ipRangeInput.setLength(ipRangeInput.length() - 1);
            }
            System.out.println("[DEBUG] Converted to " + ipRangeInput);
            ArrayList<String> scanips = new ArrayList<>();
            if (!ipRangeInput.toString().contains(",")) {
                scanips.add(ipRangeInput.toString());
            } else {
                scanips.addAll(Arrays.asList(ipRangeInput.toString().split(",")));
            }

            if(singleShit) {
                scanningFile = new File("outputs/masscanlinker-" + scanips.get(0).split("-")[0] + "-" + scanips.get(scanips.size() - 1).split("-")[1]);
            } else {
                if(scanips.size() == 1) {
                    scanningFile = new File("outputs/masscanlinker-" + scanips.get(0));
                } else {
                    scanningFile = new File("outputs/masscanlinker-" + scanips.get(0).split("-")[0] + "-" + scanips.get(scanips.size() - 1).split("-")[1]);
                }
            }
            try {
                boolean deleted = scanningFile.delete();
                boolean newFile = scanningFile.createNewFile();
            } catch (IOException e) {
                System.out.println("Couldn't create new scanning file.");
                System.exit(0);
            }
            FileUtil.writeSplash(scanningFile);

            System.out.println("What port range would you like to use?");
            String portrange = "1-65535";

            try {
                portrange = reader.readLine();
            } catch (IOException e) {
                System.out.println("Invalid port range");
                System.exit(0);
            }
            if (portrange.equals("")) {
                System.out.println("PortRange cant be empty");
                System.exit(0);
            }
            if (portrange.contains(" ")) {
                System.out.println("PortRange cant contain any spaces!");
                System.exit(0);
            }

            System.out.println("What rate would you like to use? [PER CLIENT]");
            String rate = "1000";

            try {
                rate = reader.readLine();
            } catch (IOException e) {
                System.out.println("Invalid rate amount");
                System.exit(0);
            }
            if (rate.equals("")) {
                System.out.println("Rate amount cannot be empty!");
                System.exit(0);
            }
            System.out.println("Do you wish to autoConnect VPS'es?");

            String ready = "NO";
            try {
                ready = reader.readLine();
            } catch (IOException e) {
                System.exit(0);
            }
            ArrayList<InetAddress> ips = null;
            if (ready.equalsIgnoreCase("yes")) {
                System.out.println("Reading vpses.txt...");
                ips = FileUtil.getVPSList();
            }

            System.out.println("Starting ClientListener...");
            clientListener = new ClientListener(1337);
            System.out.println("Waiting for clients to connect...");

            if (ips != null) {
                System.out.println("[DEBUG] Connecting to the vpses...");
                int count = 1;
                String masterIP = IPUtils.getIP();
                for (InetAddress ip : ips) {
                    int finalCount = count;
                    ProcessBuilder ps = new ProcessBuilder("ssh", "-o", "StrictHostKeyChecking=no", ip.getHostAddress(), "-i", "/root/.ssh/id_rsa", "-t", "screen", "-d", "-m", "java", "-jar", "ClientMASSCAN.jar", masterIP, "VPS" + finalCount);
                    try {
                        ps.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
//                    Thread clientThread = new Thread(() -> {
////                        com.github.simplenet.Client client = new com.github.simplenet.Client();
////                        client.onConnect(() -> {
////                            try {
////                                Thread.sleep(10);
////                            } catch (InterruptedException ignored) {}
////                            Packet.builder().putString("QUBOLINKER-AUTHSTRING-01").queueAndFlush(client);
////
////                            client.postDisconnect(() -> Thread.currentThread().interrupt());
////                        });
////
////                        client.connect(ip.getHostAddress(), 1337);
//                        ProcessBuilder ps = new ProcessBuilder("ssh", "-o", "StrictHostKeyChecking=no", "root@" + ip.getHostAddress(), "-i", "/root/.ssh/id_rsa", "-t", "screen", "-d", "-m", "java", "-jar", "Client.jar", masterIP, "VPS" + String.valueOf(finalCount));
//                        try {
//                            ps.start();
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    });
//                    clientThread.start();
                    count++;
                }

            }

            String finalPortrange = portrange;
            String finalTimeout = rate;

            Thread scanThread = new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                int clients = getClientListener().getClients().size();
                System.out.println("Splitting the IP Range to " + getClientListener().getClients().size());
                try {
                    if (clients == 0) {
                        System.out.println("No clients connected!");
                        System.exit(0);
                    }
                    List<String> splitted = NewIPRangeSplitter.splitIpRanges(scanips, clients);
                    List<List<String>> perClient = NewIPRangeSplitter.splitIpRangesToClients(splitted, clients);

                    int index = 0;
                    for (String key : clientListener.getClients().keySet()) {
                        MasscanClient client = clientListener.getClients().get(key);
                        List<String> list = perClient.get(index);
                        client.setCurrentScanningIpRanges(list);
                        System.out.println("Broadcasting " + String.join(",", list) + " to " + client.getName());
                        clientListener.sendIPRanges(client.getClient(), list, finalPortrange, finalTimeout);
                        ++index;
                    }

                    startTime = System.currentTimeMillis();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equalsIgnoreCase("status")) {
                            System.out.println("[Info] Requesting status...");
                            clientListener.getClients().values().forEach(masscanClient -> Packet.builder().putByte(2).putString("MASSCANLINKER-1337").queueAndFlush(masscanClient.getClient()));
                        } else if (line.equalsIgnoreCase("stop")) {
                            System.out.println("[Info] Stopping all scans...");
                            clientListener.getClients().values().forEach(masscanClient -> Packet.builder().putByte(3).putString("MASSCANLINKER-1337").queueAndFlush(masscanClient.getClient()));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Failed to split the IP Range.");
                    System.exit(0);
                }
            });
            scanThread.start();
        }
    }

    public static MasscanServer getInstance() {
        return instance;
    }
}
