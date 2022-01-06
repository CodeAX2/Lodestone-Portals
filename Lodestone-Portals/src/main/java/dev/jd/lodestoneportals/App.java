package dev.jd.lodestoneportals;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {

	private LSPListener listener;

	@Override
	public void onEnable() {
		listener = new LSPListener(this);
		getServer().getPluginManager().registerEvents(listener, this);

	}

	@Override
	public void onDisable() {
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		return false;
	}
}
