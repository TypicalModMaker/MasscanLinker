package dev.isnow.masscanlinker.checker.rawData;

import com.google.gson.annotations.SerializedName;

public class Description
{
    @SerializedName("text")
    private String text;
    
    public String getText() {
        return this.text;
    }
}
