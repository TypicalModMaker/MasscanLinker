package dev.isnow.masscanlinker.process;

import dev.isnow.masscanlinker.MasscanClient;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class ProcessOutput
        implements Runnable {

    @Getter
    public Process process;

    @Getter
    private boolean finishing;

    @Getter@Setter
    private boolean sendStatus;

    private final String portrange, rate;

    public ProcessOutput(String rangesString, String portrange, String rate) {
        this.portrange = portrange;
        this.rate = rate;

        startMasscan(rangesString);
    }

    @Override
    public void run() {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (process != null && process.isAlive()) {
            try {
                String line = bufferedReader.readLine();
                if (line == null) continue;
                if(line.startsWith("Discovered")) {
                    MasscanClient.getInstance().getServerThread().sendHit(line);
                }
                else if(line.startsWith("rate")) {
                    if(sendStatus) {
                        MasscanClient.getInstance().getServerThread().sendStatus(line);
                        sendStatus = false;
                    }
                    if(line.contains("waiting")) {
                        finishing = true;

                        if(!MasscanClient.getInstance().getQueuedHits().isEmpty() && MasscanClient.getInstance().getQueuedHits().element() == null) {
                            MasscanClient.getInstance().getServerThread().sendHit(Objects.requireNonNull(MasscanClient.getInstance().getQueuedHits().element()));
                        } else {
                            Thread.sleep(5000);
                            process.destroy();
                            System.exit(0);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void startMasscan(String range) {
        try {
            final ProcessBuilder ps = new ProcessBuilder("bash", "-c", "masscan -nmap --retries=1 --open-only --range=" + range + " --max-rate " + rate + " --ports=" + portrange);
            ps.redirectErrorStream(true);
            process = ps.start();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void sendStatus() {
        System.out.println("[INFO] Received a STATUS request.");
        sendStatus = true;

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(5000);
                if(sendStatus) {
                    MasscanClient.getInstance().getServerThread().sendStatus("Failed to retrieve status from process");
                    sendStatus = false;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
    }
    public void stopProcess() {
        process.destroy();
    }
}
