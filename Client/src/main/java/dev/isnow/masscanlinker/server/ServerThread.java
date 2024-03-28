package dev.isnow.masscanlinker.server;

import com.github.simplenet.Client;
import com.github.simplenet.packet.Packet;
import dev.isnow.masscanlinker.MasscanClient;
import dev.isnow.masscanlinker.checker.ServerChecker;
import dev.isnow.masscanlinker.process.ProcessOutput;
import lombok.Getter;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Getter
public class ServerThread {
    private Client client;

    private float lastKeepAlive = System.currentTimeMillis();

    private final String masterIP;

    public ServerThread(String ip, int port) {
        masterIP = ip;
        Thread t = new Thread(() -> {
            client = new Client();
            client.onConnect(() -> {
              checkKeepAlives();
              System.out.println("[INFO] Connected to the server!");
              if(!MasscanClient.getInstance().getQueuedHits().isEmpty()) {
                  sendHit(Objects.requireNonNull(MasscanClient.getInstance().getQueuedHits().element()));
              }
              Packet.builder().putByte(0).putString(MasscanClient.getInstance().getClientName()).queueAndFlush(client);
              client.readByteAlways(opcode -> {
                  switch (opcode) {
                      case 0:
                          lastKeepAlive = System.currentTimeMillis();
                          break;
                      case 1:
                          client.readString(message -> {
                              if (message == null) {
                                  return;
                              }
                              String[] split = message.split("\\|");
                              String ipRanges = split[0];
                              String rate = split[1];
                              String portrange = split[2];
                              if (MasscanClient.getInstance().getProcessOutput() != null) {
                                  MasscanClient.getInstance().getProcessOutput().stopProcess();
                              }

                              System.out.println("[INFO] Received a scan request, " + ipRanges + " Ports: " + portrange + " Timeout: " + rate);
                              MasscanClient.getInstance().setProcessOutput(new ProcessOutput(ipRanges, portrange, rate));
                              Thread t2 = new Thread(() -> MasscanClient.getInstance().getProcessOutput().run());
                              t2.start();
                          });
                          break;
                      case 2:
                          client.readString(message -> {
                              if (message == null) {
                                  return;
                              }
                              if (!message.equals("MASSCANLINKER-1337")) {
                                  return;
                              }
                              if (MasscanClient.getInstance().getProcessOutput() != null && MasscanClient.getInstance().getProcessOutput().process.isAlive()) {
                                  MasscanClient.getInstance().getProcessOutput().sendStatus();
                              } else {
                                  Packet.builder().putByte(1).putString("N/A " + MasscanClient.getInstance().getProcessOutput().isFinishing()).queueAndFlush(client);
                              }
                          });
                          break;
                      case 3:
                          client.readString(message -> {
                              if (message == null) {
                                  return;
                              }
                              if(!message.equals("MASSCANLINKER-1337")) {
                                  return;
                              }
                              System.out.println("[INFO] Received a STOP request.");
                              if (MasscanClient.getInstance().getProcessOutput() != null) {
                                  MasscanClient.getInstance().getProcessOutput().stopProcess();
                              }
                              MasscanClient.getInstance().setReconnect(false);
                              client.close();
                              System.exit(0);
                          });
                          break;
                  }
                });
                client.postDisconnect(() -> {
                    System.out.println("[INFO] Lost Connection to the server...");
                    if(MasscanClient.getInstance().getProcessOutput() == null) {
                        MasscanClient.getInstance().setReconnect(false);
                        client.close();
                        System.exit(0);
                        return;
                    }
                    if (MasscanClient.getInstance().isReconnect())  {
                        MasscanClient.getInstance().setServerThread(new ServerThread(ip ,port));
                        client.close();
                    }
                });
            });

            client.connect(ip, port, 5, TimeUnit.SECONDS, () -> {
                if (MasscanClient.getInstance().isReconnect())   {
                    MasscanClient.getInstance().setServerThread(new ServerThread(ip ,port));
                    client.close();
                }
            });
        });
        t.start();
    }

    public void sendStatus(final String line) {
        Packet.builder().putByte(1).putString(line).queueAndFlush(client);
    }

    public void checkKeepAlives() {
        Thread kThread  = new Thread(() -> {
            while (true) {
                if(client != null) {
                    Packet.builder().putByte(-1).queueAndFlush(client);
                    if ((lastKeepAlive - System.currentTimeMillis()) > 2500) {
                        System.out.println("[INFO] failed to recieve a keepalive [server possibly died]!"); // IDGAF about reconnecting.
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

    public void sendHit(final String output) {
        Thread t1 = new Thread(() -> {
            String ip = output.split("on ")[1].split(" ")[0];
            int port = Integer.parseInt(output.split("port ")[1].split("/")[0]);
            String line = ServerChecker.check(ip, port);
            if(line.equals("FAILED")) {
                return;
            }

            if(line.contains("FalixNodes.net/start") || line.contains("www.MineHost.pl") || line.contains("Ochrona DDoS:") || line.contains("Craftserve.pl - wydajny hosting Minecraft!Testuj za darmo przez 24h!") || line.contains("Serwer jest wylaczony") || line.contains("start.falix.cc") || line.contains("start.Falix.cc") || line.contains("Powered by FalixNodes.net") || line.contains("Ochrona DDoS") || line.contains("Blad pobierania statusu. Polacz sie bezposrednio") || line.contains("Please refer to our documentation at docs.tcpshield.com")) {
                return;
            }

            Thread t = new Thread(() -> {
                HttpClient httpclient = HttpClients.createDefault();
                try {
                    URI address = new URI("http", null, masterIP, 1338, "/hit", null, null);
                    HttpPost httppost = new HttpPost(address);
                    httppost.setHeader("key", "MASSCANLINKER-1337");
                    httppost.setHeader("hit", line);
                    httpclient.execute(httppost);
                    MasscanClient.getInstance().getQueuedHits().remove();
                    if(!MasscanClient.getInstance().getQueuedHits().isEmpty() && MasscanClient.getInstance().getQueuedHits().element() == null) {
                        sendHit(MasscanClient.getInstance().getQueuedHits().element() );
                    } else if(MasscanClient.getInstance().getProcessOutput().isFinishing()) {
                        System.exit(0);
                    }
                } catch (URISyntaxException | IOException e) {
                    System.out.println("[INFO] Failed to send hit to the Master Server! [1]");
                    if(!MasscanClient.getInstance().getQueuedHits().contains(line)) {
                        MasscanClient.getInstance().getQueuedHits().add(line);
                    }
                } catch (NoSuchElementException ignored) {}
            });
            t.start();
        });
        t1.start();
    }
}
