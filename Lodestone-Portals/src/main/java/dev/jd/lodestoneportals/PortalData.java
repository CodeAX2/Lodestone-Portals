package dev.jd.lodestoneportals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PortalData {

    private String fileName;
    private File portalFile;
    private FileConfiguration portalConfig;

    private App plugin;

    private List<Portal> portals;
    private List<PortalLink> links;

    private int nextPortalID = 0;

    public PortalData(String fileName, App plugin) {
        this.fileName = fileName;
        this.plugin = plugin;

        portals = new ArrayList<>();
        links = new ArrayList<>();

        createConfig();
    }

    /**
     * Creates the config file for the portal data
     */
    private void createConfig() {
        portalFile = new File(plugin.getDataFolder(), fileName);

        if (!portalFile.exists()) {
            portalFile.getParentFile().mkdirs();
            try {
                portalFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Unable to create file for portal data: " + fileName, e);
            }
        }

        portalConfig = new YamlConfiguration();
        try {
            portalConfig.load(portalFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Unable to load config for portal data: " + fileName, e);
        }
    }

    /**
     * Loads the portals and portal links into memory
     */
    public void loadPortals() {

        // First load all the portals
        if (portalConfig.getConfigurationSection("portals") == null)
            return;

        for (String portalIdString : portalConfig.getConfigurationSection("portals")
                .getKeys(false)) {
            // Get the portal data
            int id = Integer.parseInt(portalIdString);
            PortalLocation location = portalConfig.getObject(
                    "portals." + portalIdString + ".location",
                    PortalLocation.class);

            // Check a portal exists there
            Block portalBlock = location.world.getBlockAt(location.x, location.y, location.z);
            if (portalBlock.getType().equals(plugin.getPluginConfig().getPortalMaterial())) {
                // Correct type, add the portal
                Portal p = new Portal(id, location);
                portals.add(p);

                // Increment portal id
                if (id + 1 > nextPortalID)
                    nextPortalID = id + 1;
            }
        }

        // Load all the links between portals

        if (portalConfig.getConfigurationSection("links") == null)
            return;

        for (String linkIndex : portalConfig.getConfigurationSection("links").getKeys(false)) {
            int portalAId = portalConfig.getInt("links." + linkIndex + ".portalA");
            int portalBId = portalConfig.getInt("links." + linkIndex + ".portalB");
            double charge = portalConfig.getDouble("links." + linkIndex + ".charge");

            // Get the two portals to link together
            Portal portalA = getPortalFromID(portalAId);
            Portal portalB = getPortalFromID(portalBId);

            if (portalA != null && portalB != null) {

                // Link the portals and set the charge
                PortalLink link = portalA.linkToPortal(portalB,
                        plugin.getPluginConfig().allowInterdimensional());
                // Check if we successfully linked the portals
                if (link == null)
                    continue;
                link.setCharge(charge);

                links.add(link);
            }
        }

    }

    /**
     * Saves the portal data to the config file
     */
    public void savePortals() {
        // Reset the config
        for (String key : portalConfig.getKeys(false)) {
            portalConfig.set(key, null);
        }

        // Save the portal data
        for (Portal p : portals) {
            String portalIdString = p.getPortalID() + "";
            portalConfig.set("portals." + portalIdString + ".location", p.getPortalLocation());
        }

        // Save the link data
        int i = 0;
        for (PortalLink l : links) {
            portalConfig.set("links." + i + ".portalA", l.getFirstPortal().getPortalID());
            portalConfig.set("links." + i + ".portalB", l.getSecondPortal().getPortalID());
            portalConfig.set("links." + i + ".charge", l.getCharge());
            i++;
        }
        try {
            portalConfig.save(portalFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Unable to save portal data: " + fileName, e);
        }
    }

    /**
     * Get a portal from a given id
     * 
     * @param id
     *            the id of the portal
     * @return the portal with the specified id, or null if none exists
     */
    public Portal getPortalFromID(final int id) {
        for (Portal p : portals) {
            if (p.getPortalID() == id)
                return p;
        }
        return null;
    }

    /**
     * Gets the portal located at a given location
     * 
     * @param location
     *            the location of the wanted portal
     * @return the portal if one exists at the location, or null otherwise
     */
    public Portal getPortalAt(PortalLocation location) {
        for (Portal p : portals) {
            if (p.getPortalLocation().equals(location)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Removes a portal from the list of registered portals
     * 
     * @param p
     *            the portal to be removed
     * @return true if the portal was a registered portal, false otherwise
     */
    public boolean removePortal(Portal p) {
        if (!portals.contains(p))
            return false;

        PortalLink link = p.unLink();
        if (link != null) {
            links.remove(link);
        }
        portals.remove(p);
        return true;
    }

    /**
     * Attempts to create a new portal at a given portal location
     * 
     * @param loc
     *            the location to create the new portal
     * @return the created portal if successfully created, null otherwise
     */
    public Portal createPortal(PortalLocation loc) {
        if (getPortalAt(loc) == null) {

            Portal p = new Portal(nextPortalID, loc);
            portals.add(p);
            nextPortalID++;
            return p;
        }
        return null;
    }

    public List<Portal> getAllPortals() {
        return portals;
    }

    public void addLink(PortalLink link) {
        if (!links.contains(link))
            links.add(link);
    }

    public void removeLink(PortalLink link) {
        links.remove(link);
    }

}
