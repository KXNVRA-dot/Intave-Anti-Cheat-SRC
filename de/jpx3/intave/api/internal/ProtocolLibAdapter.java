// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.api.internal;

import com.comphenix.protocol.events.PacketListener;
import org.bukkit.entity.Player;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.jpx3.intave.IntavePlugin;

public class ProtocolLibAdapter
{
    private final IntavePlugin plugin;
    private static ProtocolManager protocolManager;
    
    public ProtocolLibAdapter(final IntavePlugin plugin2) {
        this.plugin = plugin2;
        ProtocolLibAdapter.protocolManager = ProtocolLibrary.getProtocolManager();
        this.createCustomPayloadId();
    }
    
    public void createCustomPayloadId() {
        final IntavePlugin plugin1 = this.plugin;
        ProtocolLibAdapter.protocolManager.addPacketListener((PacketListener)new PacketAdapter(this.plugin, new PacketType[] { PacketType.Play.Client.CUSTOM_PAYLOAD }) {
            public void onPacketReceiving(final PacketEvent event) {
                if (event.getPacket().getStrings().getValues().isEmpty()) {
                    return;
                }
                if (event.getPacket().getStrings().getValues().get(0).equalsIgnoreCase("INTAVE|RequestID.qywetfg67utv47wyheyctgxyudgqyufvq")) {
                    final Player p = event.getPlayer();
                    p.sendMessage(plugin1.getIntaveVerificationName() + "-" + plugin1.getVersion());
                    event.setCancelled(true);
                }
            }
        });
    }
    
    public static ProtocolManager getProtocolManager() {
        return ProtocolLibAdapter.protocolManager;
    }
    
    public void close() {
        ProtocolLibAdapter.protocolManager.removePacketListeners((Plugin)this.plugin);
    }
}
