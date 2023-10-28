package dev.isnow.masscanlinker.checker.data;

import dev.isnow.masscanlinker.checker.rawData.Version;
import dev.isnow.masscanlinker.checker.rawData.Players;
import com.google.gson.annotations.SerializedName;
import dev.isnow.masscanlinker.checker.rawData.Description;

public class NewResponse extends MCResponse
{
    @SerializedName("description")
    private final Description description;
    
    public void setVersion(final String a) {
        this.version.setName(a);
    }
    
    public NewResponse() {
        this.description = new Description();
        this.players = new Players();
        this.version = new Version();
    }
    
    public Description getDescription() {
        return this.description;
    }
    
    public FinalResponse toFinalResponse() {
        return new FinalResponse(this.players, this.version, this.favicon, this.description.getText());
    }
}
