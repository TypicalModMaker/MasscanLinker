package dev.isnow.masscanlinker.checker.data;

import dev.isnow.masscanlinker.checker.rawData.Version;
import com.google.gson.annotations.SerializedName;
import dev.isnow.masscanlinker.checker.rawData.Players;

class MCResponse
{
    @SerializedName("players")
    Players players;
    @SerializedName("version")
    Version version;
    @SerializedName("favicon")
    String favicon;
}
