package dev.jd.lodestoneportals;

public class PortalLink {

    private Portal portalA, portalB;
    private double charge;

    public PortalLink(Portal portalA, Portal portalB) {
        this.portalA = portalA;
        this.portalB = portalB;
        this.charge = 0;
    }

    public Portal getFirstPortal() {
        return portalA;
    }

    public Portal getSecondPortal() {
        return portalB;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double newCharge) {
        charge = newCharge;
    }

    public double getLinkDistance() {

        PortalLocation locA = portalA.getPortalLocation();
        PortalLocation locB = portalB.getPortalLocation();

        double dx = locA.x - locB.x;
        double dy = locA.y - locB.y;
        double dz = locA.z - locB.z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);

    }

    public boolean isInterdimensional() {
        return !portalA.getPortalLocation().world.equals(portalB.getPortalLocation().world);
    }

}
