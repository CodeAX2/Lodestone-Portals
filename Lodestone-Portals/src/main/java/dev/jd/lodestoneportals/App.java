package dev.jd.lodestoneportals;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {

	static {
        ConfigurationSerialization.registerClass(PortalLocation.class, "PortalLocation");
    }

	private LSPListener listener;
	private PortalData portalData;
	private LSPConfig lspConfig;

	@Override
	public void onEnable() {
		listener = new LSPListener(this);
		getServer().getPluginManager().registerEvents(listener, this);

		saveDefaultConfig();
		lspConfig = new LSPConfig(getConfig());

		portalData = new PortalData("portalMain.yml", this);
		portalData.loadPortals();
	}

	@Override
	public void onDisable() {
		portalData.savePortals();
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		return false;
	}

	public PortalData getPortalData() {
		return portalData;
	}

	public LSPConfig getPluginConfig() {
		return lspConfig;
	}
}
