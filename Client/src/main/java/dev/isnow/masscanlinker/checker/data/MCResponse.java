package dev.isnow.masscanlinker.checker.data;

import dev.isnow.masscanlinker.checker.rawData.Version;
import com.google.gson.annotations.SerializedName;
import dev.isnow.masscanlinker.checker.rawData.Players;
import lombok.Getter;
import lombok.Setter;

class MCResponse
{
    @Setter
    @SerializedName("players")
    Players players;

    @Setter
    @SerializedName("version")
    Version version;
    @SerializedName("favicon")
    String favicon;
}
