package ecb.ajneb97.spigot;

import ecb.ajneb97.core.managers.CommandsManager;
import ecb.ajneb97.core.managers.ConfigManager;
import ecb.ajneb97.core.managers.UpdateCheckerManager;
/*import ecb.ajneb97.core.model.internal.UpdateCheckerResult;*/
import ecb.ajneb97.spigot.listeners.PlayerListener;
import ecb.ajneb97.spigot.listeners.PlayerListenerNew;
import ecb.ajneb97.spigot.managers.BungeeMessagingManager;
import ecb.ajneb97.spigot.managers.ProtocolLibManager;
import ecb.ajneb97.spigot.managers.ViaVersionManager;
import ecb.ajneb97.spigot.utils.MessagesUtils;
import ecb.ajneb97.spigot.utils.OtherUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyCommandBlocker extends JavaPlugin {
    public static EasyCommandBlocker plugin;
    public static String prefix = "&8[&bE&9CB&8]";
    private PluginDescriptionFile pdfFile = getDescription();
    public static String uuid = "12e8653c7fcf4847b33d70a2a3697305";
    public String version = pdfFile.getVersion();
    private ProtocolLibManager protocolLibManager;
    private ViaVersionManager viaVersionManager;
    private BungeeMessagingManager bungeeMessagingManager;
    private CommandsManager commandsManager;
    private ConfigManager configManager;
    private UpdateCheckerManager updateCheckerManager;
    public void onEnable(){
        plugin = this;
        this.configManager = new ConfigManager(this.getDataFolder().toPath(),"config.yml","config.yml");
        this.configManager.registerConfig();
        this.configManager.checkMessagesUpdate();
        commandsManager = new CommandsManager(configManager.getConfig());
        registerCommands();
        registerEvents();
        bungeeMessagingManager = new BungeeMessagingManager(this);
        protocolLibManager = new ProtocolLibManager(this);
        viaVersionManager = new ViaVersionManager(this);

        Bukkit.getConsoleSender().sendMessage(MessagesUtils.getColoredMessage(prefix+" &eHas been enabled! &fVersion: 2.0"));

        updateCheckerManager = new UpdateCheckerManager(version);
        //updateMessage(updateCheckerManager.check());
    }

    public void onDisable(){
        Bukkit.getConsoleSender().sendMessage(MessagesUtils.getColoredMessage(prefix+" &eHas been disabled! &fVersion: 2.0"));
    }

    public void customReload(){
        configManager.registerConfig();
        this.commandsManager.load(configManager.getConfig());
    }

    public void registerCommands(){
        this.getCommand("ecb").setExecutor(new MainCommand(this));
    }

    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        if(!OtherUtils.serverIsLegacy()){
            pm.registerEvents(new PlayerListenerNew(this), this);
        }
    }

    public ProtocolLibManager getProtocolLibManager() {
        return protocolLibManager;
    }

    public ViaVersionManager getViaVersionManager() {
        return viaVersionManager;
    }

    public BungeeMessagingManager getBungeeMessagingManager() {
        return bungeeMessagingManager;
    }

    public UpdateCheckerManager getUpdateCheckerManager() {
        return updateCheckerManager;
    }

    public CommandsManager getCommandsManager() {
        return commandsManager;
    }
    public static String getId() { return "#TPGinc#"; }
/*    public void updateMessage(UpdateCheckerResult result){
        if(!result.isError()){
            String latestVersion = result.getLatestVersion();
            if(latestVersion != null){
                Bukkit.getConsoleSender().sendMessage(MessagesUtils.getColoredMessage("&cThere is a new version available. &e(&7"+latestVersion+"&e)"));
                Bukkit.getConsoleSender().sendMessage(MessagesUtils.getColoredMessage("&cYou can download it at: &fhttps://www.spigotmc.org/resources/101752/"));
            }
        }else{
            Bukkit.getConsoleSender().sendMessage(MessagesUtils.getColoredMessage(prefix+" &cError while checking update."));
        }
    }*/
}
