// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.movement;

import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.plugin.Plugin;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class CorruptPacket extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    
    public CorruptPacket(final IntavePlugin plugin) {
        super("CorruptPacket", CheatCategory.NETWORK);
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        final IntavePlugin plugin2 = this.plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(this.plugin, new PacketType[] { PacketType.Play.Client.ABILITIES }) {
            public void onPacketReceiving(final PacketEvent event) {
                if (!CorruptPacket.this.isActivated()) {
                    return;
                }
                final Player p = event.getPlayer();
                final PacketContainer packet = event.getPacket();
                final boolean claimsFlying = !packet.getBooleans().getValues().get(1);
                if (!p.getAllowFlight() && claimsFlying) {
                    plugin2.getRetributionManager().markPlayer(p, 1, "CorruptPacket", CheatCategory.NETWORK, "sent an invalid ability packet");
                }
            }
        });
    }
}
