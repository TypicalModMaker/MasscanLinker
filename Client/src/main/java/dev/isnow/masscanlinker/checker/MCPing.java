package dev.isnow.masscanlinker.checker;

import java.io.IOException;
import dev.isnow.masscanlinker.checker.data.OldResponse;
import dev.isnow.masscanlinker.checker.data.NewResponse;
import dev.isnow.masscanlinker.checker.data.ExtraResponse;
import dev.isnow.masscanlinker.checker.data.ForgeResponseOld;
import dev.isnow.masscanlinker.checker.data.ForgeResponse;
import dev.isnow.masscanlinker.checker.data.ForgeResponseTranslate;
import java.net.InetSocketAddress;
import com.google.gson.Gson;
import dev.isnow.masscanlinker.checker.data.FinalResponse;

public class MCPing
{
    public FinalResponse getPing(final PingOptions options) throws IOException {
        final Gson gson = new Gson();
        final Pinger a = new Pinger();
        a.setAddress(new InetSocketAddress(options.getHostname(), options.getPort()));
        a.setTimeout(options.getTimeout());
        final String json = a.fetchData();
        if (json == null || !json.contains("{")) {
            return null;
        }
        if (json.contains("\"modid\"") && json.contains("\"translate\"")) {
            return gson.fromJson(json, ForgeResponseTranslate.class).toFinalResponse();
        }
        if (json.contains("\"modid\"") && json.contains("\"text\"")) {
            return gson.fromJson(json, ForgeResponse.class).toFinalResponse();
        }
        if (json.contains("\"modid\"")) {
            return gson.fromJson(json, ForgeResponseOld.class).toFinalResponse();
        }
        if (json.contains("\"extra\"")) {
            return gson.fromJson(json, ExtraResponse.class).toFinalResponse();
        }
        if (json.contains("\"text\"")) {
            return gson.fromJson(json, NewResponse.class).toFinalResponse();
        }
        return gson.fromJson(json, OldResponse.class).toFinalResponse();
    }
}
