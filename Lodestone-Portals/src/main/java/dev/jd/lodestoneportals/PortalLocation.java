package dev.jd.lodestoneportals;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("PortalLocation")
public class PortalLocation implements ConfigurationSerializable {

    public int x;
    public int y;
    public int z;
    public World world;

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof PortalLocation))
            return false;
        PortalLocation obj = (PortalLocation) other;
        return x == obj.x && y == obj.y && z == obj.z && world.equals(world);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("x", Integer.valueOf(x));
        result.put("y", Integer.valueOf(y));
        result.put("z", Integer.valueOf(z));
        result.put("world", world.getName());

        return result;
    }

    public static PortalLocation deserialize(Map<String, Object> args) {
        PortalLocation loc = new PortalLocation();

        if (args.containsKey("x"))
            loc.x = ((Integer) args.get("x")).intValue();

        if (args.containsKey("y"))
            loc.y = ((Integer) args.get("y")).intValue();

        if (args.containsKey("z"))
            loc.z = ((Integer) args.get("z")).intValue();

        if (args.containsKey("world"))
            loc.world = Bukkit.getWorld((String) args.get("world"));

        return loc;
    }

}
