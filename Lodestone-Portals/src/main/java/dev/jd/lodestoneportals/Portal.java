package dev.jd.lodestoneportals;

import org.bukkit.Location;

public class Portal {

    private final int portalId;

    private PortalLink link;
    private final PortalLocation location;

    public Portal(int id, PortalLocation location) {
        this.portalId = id;
        this.location = location;
    }

    /**
     * Links the portal with another portal
     * 
     * @param other
     *            the other portal to be linked with the current
     *            one
     * @param allowInterdimensional
     *            determines if two portals in different
     *            dimensions can be linked together
     * @return the created PortalLink if successful, or null if unsuccessful
     */
    public PortalLink linkToPortal(Portal other, boolean allowInterdimensional) {
        if (link == null) {
            if (!allowInterdimensional && !this.location.world.equals(other.location.world))
                return null;
            link = new PortalLink(this, other);
            other.link = link;
            return link;
        }
        return null;
    }

    /**
     * Gets the link of this portal
     * 
     * @return the PortalLink associated with this portal
     */
    public PortalLink getLink() {
        return link;
    }

    /**
     * Unlinks the current portal
     * 
     * @return the link that was removed
     */
    public PortalLink unLink() {
        PortalLink curLink = link;
        if (curLink != null) {
            curLink.getFirstPortal().link = null;
            curLink.getSecondPortal().link = null;
        }
        return curLink;
    }

    /**
     * 
     * @return the ID of the portal
     */
    public final int getPortalID() {
        return portalId;
    }

    /**
     * 
     * @return the location of the portal
     */
    public final PortalLocation getPortalLocation() {
        return location;
    }

    /**
     * Get the portal linked to the current one
     * 
     * @return the other portal that is linked to the current one, or null if not
     *         linked
     */
    public Portal getLinkedPortal() {
        if (link != null) {
            if (link.getFirstPortal() == this)
                return link.getSecondPortal();
            return link.getFirstPortal();
        }
        return null;
    }


    /**
     * Gets the location to teleport to for this portal
     * @return Location element on top of the given portal's coordinates
     */
    public Location getTeleportLocation() {
        Location tpLoc = new Location(location.world, location.x + 0.5, location.y + 1.05, location.z + 0.5);
        return tpLoc;
    }

}
