// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.connection;

import com.comphenix.protocol.events.PacketListener;
import de.jpx3.intave.util.objectable.Checkable;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class SkinBlinkCheck extends IntaveCheck
{
    private final IntavePlugin plugin;
    
    public SkinBlinkCheck(final IntavePlugin plugin) {
        super("SkinBlink", CheatCategory.NETWORK);
        this.plugin = plugin;
        final IntavePlugin plugin2 = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, new PacketType[] { PacketType.Play.Client.SETTINGS }) {
            public void onPacketReceiving(final PacketEvent event) {
                if (!SkinBlinkCheck.this.isActivated()) {
                    return;
                }
                try {
                    event.getPlayer().getUniqueId();
                }
                catch (UnsupportedOperationException ex) {
                    return;
                }
                final Player p = event.getPlayer();
                final PacketContainer packet = event.getPacket();
                final int skinChangeValue = packet.getIntegers().getValues().get(1);
                final int lastSkineChangeValue = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastSkinAttributeId;
                final boolean flag = MathHelper.diff((float)skinChangeValue, (float)lastSkineChangeValue) > 0.0f && (p.isSprinting() || p.isSneaking()) && IIUA.getCurrentTimeMillis() - plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getTimedValues().lastMoveEvent < 50L;
                int currentVL = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().skinBlinkVL;
                if (flag && currentVL < 50) {
                    final Checkable.CheckableMeta.ViolationValues vioValues = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues();
                    vioValues.skinBlinkVL += 5;
                }
                else if (plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().skinBlinkVL > 0) {
                    final Checkable.CheckableMeta.ViolationValues vioValues2 = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues();
                    --vioValues2.skinBlinkVL;
                }
                currentVL = plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getVioValues().skinBlinkVL;
                if (flag && currentVL > 15) {
                    plugin2.getRetributionManager().markPlayer(p, 1, "SkinBlink", CheatCategory.NETWORK, "tries to derp his skin");
                }
                if (flag) {
                    event.setCancelled(true);
                }
                plugin2.catchCheckable(IIUA.getUUIDFrom(p)).getMeta().getSyncedValues().lastSkinAttributeId = skinChangeValue;
            }
        });
    }
}
