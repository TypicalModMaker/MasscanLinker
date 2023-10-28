package dev.isnow.masscanlinker.express;

import dev.isnow.masscanlinker.MasscanServer;
import dev.isnow.masscanlinker.util.FileUtil;
import express.Express;
import express.utils.Status;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ApiController {

    public Queue<String> servers = new ConcurrentLinkedQueue<>();
    public ApiController() {
        Express app = new Express("0.0.0.0");

        app.get("/", (req, res) -> res.send("Fuck off you stupid cunt, your ip: " + req.getAddress().getHostAddress()));
        app.post("/hit", (req, res) -> {
            if(req.getHeader("key").get(0).equals("MASSCANLINKER-1337")) {
                String hit = req.getHeader("hit").get(0);
                if(servers.contains(hit)) {
                    res.setStatus(Status._200);
                    res.send("VALID");
                    return;
                }
                System.out.println(hit);
                servers.add(hit);
                FileUtil.writeOutput(hit, MasscanServer.getInstance().getScanningFile());
                res.setStatus(Status._200);
                res.send("VALID");
            } else {
                res.send("Fuck off you stupid cunt, your ip: " + req.getAddress().getHostAddress());
            }
        });

        app.listen(null, 1338);
    }
}
