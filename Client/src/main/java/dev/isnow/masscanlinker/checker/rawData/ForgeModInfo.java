package dev.isnow.masscanlinker.checker.rawData;

import com.google.gson.annotations.SerializedName;

public class ForgeModInfo
{
    @SerializedName("type")
    private String type;
    @SerializedName("modList")
    private ForgeModListItem[] modList;
    
    public int getNMods() {
        return this.modList.length;
    }
}
