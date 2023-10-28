package dev.isnow.masscanlinker.checker.data;

import dev.isnow.masscanlinker.checker.rawData.ForgeModInfo;
import dev.isnow.masscanlinker.checker.rawData.Version;
import dev.isnow.masscanlinker.checker.rawData.Players;
import com.google.gson.annotations.SerializedName;
import dev.isnow.masscanlinker.checker.rawData.ForgeDescriptionTranslate;

public class ForgeResponseTranslate
{
    @SerializedName("description")
    private ForgeDescriptionTranslate description;
    @SerializedName("players")
    private Players players;
    @SerializedName("version")
    private Version version;
    @SerializedName("modinfo")
    private ForgeModInfo modinfo;
    
    public FinalResponse toFinalResponse() {
        this.version.setName(this.version.getName() + " FML with " + this.modinfo.getNMods() + " mods");
        return new FinalResponse(this.players, this.version, "", this.description.getTranslate());
    }
}
