// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.check.other;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import java.util.Iterator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.ChatColor;
import de.jpx3.intave.util.calc.MathHelper;
import de.jpx3.intave.util.calc.analysis.StringUtils;
import de.jpx3.intave.antipiracy.IIUA;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.entity.Player;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.PacketType;
import de.jpx3.intave.api.internal.ProtocolLibAdapter;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import de.jpx3.intave.util.enums.CheatCategory;
import de.jpx3.intave.util.objectable.BadWord;
import java.util.List;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.event.Listener;
import de.jpx3.intave.util.objectable.IntaveCheck;

public final class ChatCheck extends IntaveCheck implements Listener
{
    private final IntavePlugin plugin;
    private final List<BadWord> bad_words;
    
    public ChatCheck(final IntavePlugin plugin) {
        super("Chat", CheatCategory.OTHER);
        this.bad_words = new ArrayList<BadWord>();
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
        final IntavePlugin reference = plugin;
        ProtocolLibAdapter.getProtocolManager().addPacketListener((PacketListener)new PacketAdapter(reference, new PacketType[] { PacketType.Play.Client.TAB_COMPLETE }) {
            public void onPacketReceiving(final PacketEvent event) {
                final Player p = event.getPlayer();
                final String text = event.getPacket().getStrings().getValues().get(0);
                if (!ChatCheck.this.isActivated()) {
                    return;
                }
                if (text.equalsIgnoreCase("/") || text.equalsIgnoreCase("//")) {
                    event.setCancelled(true);
                }
            }
        });
        this.loadBadWords();
    }
    
    private void loadBadWords() {
        this.addBadWord(new BadWord("hitler", false, 3));
        this.addBadWord(new BadWord("sieg heil", false, 4));
        this.addBadWord(new BadWord("f\u00fchrer", false, 1));
        this.addBadWord(new BadWord("heil", false, 1));
        this.addBadWord(new BadWord("nsdap", false, 1));
        this.addBadWord(new BadWord("nazi", true, 1));
        this.addBadWord(new BadWord("hh", false, 1, true));
        this.addBadWord(new BadWord("eZ", false, 3, true));
        this.addBadWord(new BadWord("easy", false, 3));
        this.addBadWord(new BadWord("wixxer", false, 3));
        this.addBadWord(new BadWord("wichser", false, 3));
        this.addBadWord(new BadWord("spastiker", false, 3));
        this.addBadWord(new BadWord("spasti", false, 3));
        this.addBadWord(new BadWord("spast", false, 3));
        this.addBadWord(new BadWord("spasst", false, 3));
        this.addBadWord(new BadWord("hs", false, 4, true));
        this.addBadWord(new BadWord("sad", true, 4));
        this.addBadWord(new BadWord("huso", false, 4));
        this.addBadWord(new BadWord("arschloch", false, 3));
        this.addBadWord(new BadWord("huan", false, 3));
        this.addBadWord(new BadWord("wixer", false, 3));
        this.addBadWord(new BadWord("wixa", false, 3));
        this.addBadWord(new BadWord("wichsa", false, 3));
        this.addBadWord(new BadWord("wixxa", false, 3));
        this.addBadWord(new BadWord("hurensohn", false, 3));
        this.addBadWord(new BadWord("hurenson", false, 3));
        this.addBadWord(new BadWord("hurrnson", false, 3));
        this.addBadWord(new BadWord("hurrnsohn", false, 3));
        this.addBadWord(new BadWord("hurnsohn", false, 3));
        this.addBadWord(new BadWord("Hundesohn", false, 3));
        this.addBadWord(new BadWord("bitch", false, 3));
        this.addBadWord(new BadWord("nutte", false, 3));
        this.addBadWord(new BadWord("schlampe", false, 3));
        this.addBadWord(new BadWord("hure", false, 3));
        this.addBadWord(new BadWord("opfa", false, 3));
        this.addBadWord(new BadWord("opfer", false, 3));
        this.addBadWord(new BadWord("looser", false, 3));
        this.addBadWord(new BadWord("kak", true, 3, true));
        this.addBadWord(new BadWord("kack", true, 3, true));
        this.addBadWord(new BadWord("kek", true, 3, true));
        this.addBadWord(new BadWord("big l", true, 3));
        this.addBadWord(new BadWord("fuckr", false, 3));
        this.addBadWord(new BadWord("fucker", false, 3));
        this.addBadWord(new BadWord("fuck", false, 3));
        this.addBadWord(new BadWord("fickt", false, 3));
        this.addBadWord(new BadWord("fick", false, 3));
        this.addBadWord(new BadWord("ficken", false, 3));
        this.addBadWord(new BadWord("ficker", false, 3));
        this.addBadWord(new BadWord("arsch", false, 3, true));
        this.addBadWord(new BadWord("idiot", false, 3));
        this.addBadWord(new BadWord("low", true, 3, true));
        this.addBadWord(new BadWord("behindert", false, 3));
        this.addBadWord(new BadWord("fratze", false, 3));
        this.addBadWord(new BadWord("fotze", false, 3));
        this.addBadWord(new BadWord("bastard", false, 3));
        this.addBadWord(new BadWord("amk", false, 3));
        this.addBadWord(new BadWord("cock", false, 3));
        this.addBadWord(new BadWord("dick", false, 3));
        this.addBadWord(new BadWord("noob", false, 3));
        this.addBadWord(new BadWord("penner", false, 3));
        this.addBadWord(new BadWord("missgeburt", false, 3));
        this.addBadWord(new BadWord("mistgeburt", false, 3));
        this.addBadWord(new BadWord("miss geburt", false, 3));
        this.addBadWord(new BadWord("drecks", false, 3));
        this.addBadWord(new BadWord("schei\u00df", false, 3));
        this.addBadWord(new BadWord("scheiss", false, 3));
        this.addBadWord(new BadWord("kid", false, 3));
        this.addBadWord(new BadWord("lappen", false, 3));
        this.addBadWord(new BadWord("schwuchtel", false, 3));
        this.addBadWord(new BadWord("hdm", false, 3, true));
        this.addBadWord(new BadWord("hdfm", false, 3, true));
        this.addBadWord(new BadWord("hdf", false, 3, true));
        this.addBadWord(new BadWord("fresser", false, 3));
        this.addBadWord(new BadWord("fettsack", false, 3));
        this.addBadWord(new BadWord("fetsack", false, 3));
        this.addBadWord(new BadWord("halt dein maul", false, 3));
        this.addBadWord(new BadWord("halt deine fresse", false, 3));
        this.addBadWord(new BadWord("maul", false, 3));
        this.addBadWord(new BadWord("pedo", false, 3));
        this.addBadWord(new BadWord("zigeuner", false, 3));
        this.addBadWord(new BadWord("pisser", false, 3));
        this.addBadWord(new BadWord("spacko", false, 3));
        this.addBadWord(new BadWord("trottel", false, 3));
        this.addBadWord(new BadWord("fresse", false, 3));
        this.addBadWord(new BadWord("stinkt", false, 3));
        this.addBadWord(new BadWord("stinkt", false, 3));
        this.addBadWord(new BadWord("schwanz", false, 3, true));
        this.addBadWord(new BadWord("tittn", false, 3));
        this.addBadWord(new BadWord("titten", false, 3));
        this.addBadWord(new BadWord("penis", false, 3));
        this.addBadWord(new BadWord("pimmel", false, 3));
        this.addBadWord(new BadWord("wixxe", false, 3, true));
        this.addBadWord(new BadWord("shut up", true, 3, true));
        this.addBadWord(new BadWord("is bad", false, 3));
        this.addBadWord(new BadWord("is shit", false, 3));
    }
    
    private void addBadWord(final BadWord badWord) {
        this.bad_words.add(badWord);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void on(final AsyncPlayerChatEvent e) {
        if (!this.isActivated()) {
            return;
        }
        boolean contains_playername = false;
        for (final Player p : this.plugin.getServer().getOnlinePlayers()) {
            if (e.getMessage().trim().replace(" ", "").toLowerCase().contains(p.getName().toLowerCase())) {
                contains_playername = true;
            }
        }
        final Player p2 = e.getPlayer();
        final String message = e.getMessage().trim();
        final long diffLast = IIUA.getCurrentTimeMillis() - this.plugin.catchCheckable(IIUA.getUUIDFrom(p2)).getMeta().getTimedValues().lastTimeChatMessageSent;
        final double equalisationVL = StringUtils.equalization(message.toLowerCase(), this.plugin.catchCheckable(IIUA.getUUIDFrom(p2)).getMeta().getSyncedValues().lastChatMessage);
        final int sDL = (int)MathHelper.diff((float)message.length(), (float)this.plugin.catchCheckable(IIUA.getUUIDFrom(p2)).getMeta().getSyncedValues().lastChatMessage.length());
        boolean illegal = false;
        for (final char q : e.getMessage().toCharArray()) {
            if (q >= '\u03e8' && (q < '\u2764' || q > '\u2765')) {
                illegal = true;
            }
        }
        if (illegal) {
            p2.sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Your chatmessage contains illegal characters");
            e.setCancelled(true);
            return;
        }
        if (sDL < 4 && equalisationVL > this.plugin.getConfig().getDouble(this.getConfigPath() + ".max_similarity") && this.plugin.getRetributionManager().markPlayer(p2, 2, "Chat", CheatCategory.OTHER, "sent a chatmessage, that was too similar to the last one (" + Math.round(equalisationVL * 100.0) + "%)")) {
            e.getPlayer().sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Please refrain from repeating yourself");
            e.setCancelled(true);
            return;
        }
        final double minMsPerCharacter = diffLast / (double)message.length();
        if (message.length() > 10 && minMsPerCharacter < 150.0) {
            final int vl = (int)(10.0 - MathHelper.map(minMsPerCharacter, 0.0, 150.0, 0.0, 6.0));
            if (this.plugin.getRetributionManager().markPlayer(p2, vl, "Chat", CheatCategory.OTHER, "sent suspicious message")) {
                e.getPlayer().sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Please do not spam");
                e.setCancelled(true);
                return;
            }
        }
        if (diffLast < this.plugin.getConfig().getInt(this.getConfigPath() + ".min_delay") && this.plugin.getRetributionManager().markPlayer(p2, 1, "Chat", CheatCategory.OTHER, "sent message too fast")) {
            e.getPlayer().sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Please wait before typing again");
            e.setCancelled(true);
            return;
        }
        final String filtered_message = e.getMessage().replaceAll("1", "i").replaceAll("3", "e").replaceAll("4", "a").replaceAll("0", "o").toLowerCase();
        for (final BadWord badWord : this.bad_words) {
            final int index = filtered_message.indexOf(badWord.getBadWord().toLowerCase());
            if (index < 0) {
                continue;
            }
            if (!contains_playername && badWord.isVerballyAbusedNeeded()) {
                continue;
            }
            if (index > 0 && Character.isLetter(e.getMessage().toCharArray()[index - 1]) && badWord.isSpaceBeforeNeeded()) {
                continue;
            }
            final int bound = badWord.getBadWord().length();
            final BadWord badWord2;
            final String censored = IntStream.range(0, bound).mapToObj(i -> badWord2.getBadWord().equalsIgnoreCase("ez") ? "g" : ((badWord2.getBadWord().toCharArray()[i] == ' ') ? " " : "*")).collect((Collector<? super Object, ?, String>)Collectors.joining());
            if (!this.plugin.getRetributionManager().markPlayer(p2, badWord.getVLSummant(), "Chat", CheatCategory.OTHER, "tried to write " + badWord.getBadWord() + " in public chat")) {
                continue;
            }
            final String newMessage = e.getMessage().substring(0, index) + censored + e.getMessage().substring(index + badWord.getBadWord().length(), e.getMessage().length());
            e.setMessage(newMessage);
        }
        final String onlyLetterMessage = StringUtils.getOnlyChars(message);
        final double capsVL = StringUtils.equalization(onlyLetterMessage, onlyLetterMessage.toUpperCase());
        if (onlyLetterMessage.length() > 3) {
            if (capsVL > this.plugin.getConfig().getDouble(this.getConfigPath() + ".max_caps_percentage_s") && onlyLetterMessage.length() <= 5) {
                if (this.plugin.getRetributionManager().markPlayer(p2, 2, "Chat", CheatCategory.OTHER, "used caps too often in a message (" + Math.round(capsVL * 100.0) + "%)")) {
                    e.getPlayer().sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Please mind your caps");
                    e.setMessage(e.getMessage().toLowerCase());
                    return;
                }
            }
            else if (capsVL > this.plugin.getConfig().getDouble(this.getConfigPath() + ".max_caps_percentage_l") && this.plugin.getRetributionManager().markPlayer(p2, 1, "Chat", CheatCategory.OTHER, "used caps too often in a message (" + Math.round(capsVL * 100.0) + "%)")) {
                e.getPlayer().sendMessage(this.plugin.getPrefix() + ChatColor.RED + "Please mind your caps");
                e.setMessage(e.getMessage().toLowerCase());
                return;
            }
        }
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p2)).getMeta().getTimedValues().lastTimeChatMessageSent = IIUA.getCurrentTimeMillis();
        this.plugin.catchCheckable(IIUA.getUUIDFrom(p2)).getMeta().getSyncedValues().lastChatMessage = message;
    }
}
