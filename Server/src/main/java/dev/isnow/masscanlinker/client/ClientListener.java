package dev.isnow.masscanlinker.client;

import com.github.simplenet.Client;
import com.github.simplenet.Server;
import com.github.simplenet.packet.Packet;
import dev.isnow.masscanlinker.MasscanServer;
import dev.isnow.masscanlinker.client.impl.MasscanClient;
import dev.isnow.masscanlinker.express.ApiController;
import dev.isnow.masscanlinker.util.FileUtil;
import lombok.Getter;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ClientListener {

    private final ConcurrentHashMap<String, MasscanClient> clients = new ConcurrentHashMap<>();

    private Server server;


    public ClientListener(int port) {
        checkKeepAlives();
        Thread t = new Thread(() -> {
            new ApiController();
            server = new Server();
            server.onConnect(client -> {
                SocketAddress address = null;
                try {
                    address = client.getChannel().getRemoteAddress();
                } catch (IOException e) {
                    System.out.println("Failed to receive socketadddress from current client!");
                }
                System.out.println("[DEBUG] Client " + address + " has connected!");

                SocketAddress finalAddress1 = address;
                client.postDisconnect(() -> {
                    clients.values().stream().filter(masscanClient1 -> masscanClient1.getIp() == finalAddress1).findFirst().ifPresent(masscanClient1 -> clients.remove(masscanClient1.getName()));
                        System.out.println("[INFO] " + finalAddress1 + " disconnected! Clients Left: " + clients.size());
                    if(MasscanServer.getInstance().getClientListener().getClients().size() == 0) {
                        System.out.println("[INFO] All clients finished scanning, output saved to " + MasscanServer.getInstance().getScanningFile().getAbsolutePath());
                        System.out.println("[INFO] Took " + ((System.currentTimeMillis() - MasscanServer.getInstance().getStartTime() ) / 1000.0) + " seconds to complete every scan!");
                        client.close();
                        server.close();
                        System.exit(0);
                    }
                });
                client.readByteAlways(opcode -> {
                    switch (opcode) {
                        case 0:
                            client.readString(message -> {
                                if (MasscanServer.getInstance().getClientListener().getClients().get(message) != null) {
                                    System.out.println("[INFO] Client with a name " + message + "already exist in the database.");
                                    return;
                                }
                                MasscanServer.getInstance().getClientListener().getClients().put(message, new MasscanClient(client, finalAddress1, message));
                                System.out.println("[INFO] Registered a client with a name " + message);
                            });
                            break;
                        case 1:
                            Optional<MasscanClient> clientQubo = clients.values().stream().filter(masscanClient1 -> masscanClient1.getIp() == finalAddress1).findFirst();
                            if(clientQubo.isEmpty()) {
                                break;
                            }
                            client.readString(message -> {
                                if(message == null) {
                                    return;
                                }
                                try {
                                    System.out.println("[STATUS] " + clientQubo.get().getName() + " -" + message);
                                } catch (Exception ignored) {}
                            });
                            break;
                        case -1:
                            Optional<MasscanClient> clientQubo1 = clients.values().stream().filter(masscanClient1 -> masscanClient1.getIp() == finalAddress1).findFirst();
                            if(clientQubo1.isEmpty()) {
                                break;
                            }
                            clientQubo1.get().setLastKeepaliveTime(System.currentTimeMillis());
                            break;
                        default:
                            System.out.println("[INFO] " + "Received a weird message from a client " + finalAddress1 + ", byte:" + opcode + ", trying to parse string!");
                            try {
                                client.readString(message -> {
                                    System.out.println(message);
                                    FileUtil.writeOutput(message, MasscanServer.getInstance().getScanningFile());
                                });
                            } catch (Exception ignored) {}
                            break;
                    }
                });
            });
            server.bind("0.0.0.0", port);
        });
        t.start();
    }


    public void checkKeepAlives() {
        Thread kThread  = new Thread(() -> {
            while (true) {
                for (MasscanClient client : clients.values()) {
                    Packet.builder().putByte(0).queueAndFlush(client.getClient());
                    if ((client.getLastKeepaliveTime() - System.currentTimeMillis()) > 2500) {
                        System.out.println("[INFO] " + client.getName() + " failed to response to the keepalives [possibly finished]! Clients Left: " + clients.size());
                        clients.remove(client.getName());
                        if (MasscanServer.getInstance().getClientListener().getClients().size() == 0) {
                            System.out.println("[INFO] All clients finished scanning, output saved to " + MasscanServer.getInstance().getScanningFile().getAbsolutePath());
                            System.out.println("[INFO] Took " + ((System.currentTimeMillis() - MasscanServer.getInstance().getStartTime()) / 1000.0) + " seconds to complete every scan!");
                            server.close();
                            System.exit(0);
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        kThread.start();
    }
    public void sendIPRanges(Client client, List<String> ranges, String portrange, String timeout) {
        Packet.builder().putByte(1).putString(String.join(",", ranges) + "|" + timeout + "|" + portrange).queueAndFlush(client);
    }
}
