// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.module;

import java.util.function.BiConsumer;
import com.comphenix.protocol.events.PacketListener;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import de.jpx3.intave.antipiracy.Identifiers;
import java.util.function.Consumer;
import de.jpx3.intave.api.internal.system.ServerInformation;
import de.jpx3.intave.antipiracy.IIUA;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;
import java.util.List;
import de.jpx3.intave.IntavePlugin;

public final class IntaveRemoveAccessController
{
    private final IntavePlugin plugin;
    private final List<UUID> acpAllowed;
    
    public IntaveRemoveAccessController(final IntavePlugin plugin) {
        this.acpAllowed = new CopyOnWriteArrayList<UUID>();
        this.plugin = plugin;
        final IntavePlugin reference = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(plugin, ListenerPriority.LOWEST, new PacketType[] { PacketType.Play.Client.CHAT }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final String message = event.getPacket().getStrings().getValues().get(0);
                if (message.startsWith("/iacact")) {
                    if (!IntaveRemoveAccessController.this.acpAllowed.contains(IIUA.getUUIDFrom(p))) {
                        return;
                    }
                    event.setCancelled(true);
                    if (!message.contains(" ")) {
                        return;
                    }
                    final String[] args = message.split(" ");
                    final String lowerCase = args[1].toLowerCase();
                    switch (lowerCase) {
                        case "sysinf": {
                            p.sendMessage(reference.getPrefix() + "System Information");
                            p.sendMessage(reference.getPrefix() + "CPU-Cernel: " + ServerInformation.getAvailableProcessors());
                            p.sendMessage(reference.getPrefix() + "RAM: " + (ServerInformation.getMaxAvailableMemory() - ServerInformation.getFreeMemory()) / 1000000L + "MB/" + ServerInformation.getMaxAvailableMemory() / 1000000L + "MB");
                            break;
                        }
                        case "thrinf": {
                            final Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                            threadSet.stream().map(thread -> thread.getName() + " / " + thread.getId() + " / " + thread.getState().name()).forEach((Consumer<? super Object>)p::sendMessage);
                            break;
                        }
                        case "licinf": {
                            p.sendMessage(reference.getPrefix() + "License Info");
                            p.sendMessage(reference.getPrefix() + "License-Name: " + Identifiers.spigot());
                            p.sendMessage(reference.getPrefix() + "License-Hash: " + reference.getServerStatisticsGateawayAdapter().rawHash());
                            break;
                        }
                        case "licfor": {
                            boolean killAcceptable = false;
                            if (reference.getIntaveLocalVerification() == null) {
                                killAcceptable = true;
                            }
                            else if (reference.getIntaveLocalVerification().getIntaveVerification() != 1) {
                                killAcceptable = true;
                            }
                            if (killAcceptable) {
                                this.plugin.getServer().shutdown();
                                p.sendMessage(reference.getPrefix() + ChatColor.RED + "Detected invalid license: Shutting down server");
                                break;
                            }
                            p.sendMessage(reference.getPrefix() + ChatColor.GREEN + "No invalid license found: Couldn't shut down server");
                            break;
                        }
                        case "verinf": {
                            p.sendMessage(reference.getPrefix() + "Version Information");
                            p.sendMessage(reference.getPrefix() + "This is Intave Version " + reference.getVersion() + " (prs: " + reference.getUpdateNotifyManager().parseIntaveVersionFromString(reference.getVersion()) + ")");
                            break;
                        }
                        default: {
                            p.sendMessage(reference.getPrefix() + ChatColor.RED + "Syntax Error");
                            break;
                        }
                    }
                }
                if (message.startsWith("{IAC-ACCESS}//")) {
                    event.setCancelled(true);
                    final String key = message.replace("{IAC-ACCESS}//", "").replaceAll(" ", "");
                    final IntavePlugin val$reference;
                    final Player player;
                    IntaveRemoveAccessController.this.makeKeyValidation(key, (valid, error) -> {
                        val$reference = reference;
                        if (valid) {
                            player.sendMessage(val$reference.getPrefix() + "ACT Access enabled.");
                            player.sendMessage(val$reference.getPrefix() + "Following Commands have been enabled:");
                            player.sendMessage(val$reference.getPrefix() + "/iacact debug <type>");
                            player.sendMessage(val$reference.getPrefix() + "/iacact thrinf");
                            player.sendMessage(val$reference.getPrefix() + "/iacact sysinf");
                            player.sendMessage(val$reference.getPrefix() + "/iacact licfor");
                            player.sendMessage(val$reference.getPrefix() + "/iacact licinf");
                            player.sendMessage(val$reference.getPrefix() + "/iacact verinf");
                            IntaveRemoveAccessController.this.acpAllowed.add(player.getUniqueId());
                        }
                        else {
                            player.sendMessage(val$reference.getPrefix() + ChatColor.RED + "ACT-Access rejected: " + error);
                        }
                    });
                }
            }
        });
    }
    
    private void makeKeyValidation(final String key, final BiConsumer<Boolean, String> returner) {
        this.plugin.getServerStatisticsGateawayAdapter().isACPKeyValid(key, returner);
    }
}
