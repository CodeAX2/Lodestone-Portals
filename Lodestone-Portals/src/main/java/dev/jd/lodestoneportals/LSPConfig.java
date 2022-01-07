package dev.jd.lodestoneportals;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class LSPConfig {

	private FileConfiguration config;

	public LSPConfig(FileConfiguration config) {
		this.config = config;
	}

	public Material getPortalMaterial() {
		return Material.getMaterial(config.getString("portalMaterial"));
	}

	public Material getPortalConfigMaterial() {
		return Material.getMaterial(config.getString("portalConfigMaterial"));
	}

	public Material getLowChargeMaterial() {
		return Material.getMaterial(config.getString("lowChargeMaterial"));
	}

	public Material getHighChargeMaterial() {
		return Material.getMaterial(config.getString("highChargeMaterial"));
	}

	public double getLowChargeValue() {
		return config.getDouble("lowChargeValue");
	}

	public double getHighChargeValue() {
		return config.getDouble("highChargeValue");
	}

	public double getBaseCostPerUse() {
		return config.getDouble("baseCostPerUse");
	}

	public double getCostPerBlock() {
		return config.getDouble("costPerBlock");
	}

	public double getCostForDimensional() {
		return config.getDouble("costForDimensional");
	}

	public double getMaxFuel() {
		return config.getDouble("maxFuel");
	}

	public boolean allowInterdimensional() {
		return config.getBoolean("allowInterdimensional");
	}

}
