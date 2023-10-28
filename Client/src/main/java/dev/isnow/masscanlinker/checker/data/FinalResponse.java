package dev.isnow.masscanlinker.checker.data;

import dev.isnow.masscanlinker.checker.rawData.Version;
import dev.isnow.masscanlinker.checker.rawData.Players;

public class FinalResponse extends MCResponse
{
    private final String description;
    
    public FinalResponse(final Players players, final Version version, final String favicon, final String description) {
        this.description = description;
        this.favicon = favicon;
        this.players = players;
        this.version = version;
    }
    
    public Players getPlayers() {
        return this.players;
    }
    
    public Version getVersion() {
        return this.version;
    }
    
    public String getDescription() {
        return this.description;
    }
}
