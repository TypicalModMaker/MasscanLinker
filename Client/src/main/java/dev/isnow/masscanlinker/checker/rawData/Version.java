package dev.isnow.masscanlinker.checker.rawData;

import com.google.gson.annotations.SerializedName;

public class Version
{
    @SerializedName("name")
    private String name;
    @SerializedName("protocol")
    private int protocol;
    
    public void setName(final String a) {
        this.name = a;
    }
    
    public String getName() {
        return this.name;
    }

    public Version(String name, int protocol) {
        this.name = name;
        this.protocol = protocol;
    }
    public Version() {
        this.name = "NoName";
        this.protocol = 0;
    }
}
