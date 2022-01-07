package dev.jd.lodestoneportals;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class LSPListener implements Listener {

    private App plugin;

    private HashMap<Player, Portal> configuringMap;

    public LSPListener(App plugin) {
        this.plugin = plugin;
        configuringMap = new HashMap<>();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType()
                .equals(plugin.getPluginConfig().getPortalMaterial())) {

            PortalLocation loc = new PortalLocation();
            loc.x = e.getBlock().getLocation().getBlockX();
            loc.y = e.getBlock().getLocation().getBlockY();
            loc.z = e.getBlock().getLocation().getBlockZ();
            loc.world = e.getBlock().getLocation().getWorld();

            Portal possiblePortal = plugin.getPortalData().getPortalAt(loc);
            if (possiblePortal != null) {
                double remainingCharge = 0;
                PortalLink link = possiblePortal.getLink();
                if (link != null) {
                    remainingCharge = link.getCharge();
                }
                plugin.getPortalData().removePortal(possiblePortal);

                // Determine how many charge items to drop
                double lowChargeValue = plugin.getPluginConfig().getLowChargeValue();
                double highChargeValue = plugin.getPluginConfig().getHighChargeValue();

                int numHigh = 0;
                int numLow = 0;

                while (remainingCharge >= lowChargeValue) {
                    if (remainingCharge >= highChargeValue && highChargeValue != 0) {
                        remainingCharge -= highChargeValue;
                        numHigh++;
                    } else {
                        remainingCharge -= lowChargeValue;
                        numLow++;
                    }
                }

                // Drop the low and high charge items
                while (numLow != 0) {
                    int amountToDrop = Math.min(numLow, 64);
                    ItemStack lowItemStack = new ItemStack(
                            plugin.getPluginConfig().getLowChargeMaterial(), amountToDrop);

                    e.getBlock().getLocation().getWorld().dropItemNaturally(
                            e.getBlock().getLocation(),
                            lowItemStack);
                    numLow -= amountToDrop;
                }

                while (numHigh != 0) {
                    int amountToDrop = Math.min(numHigh, 64);
                    ItemStack highItemStack = new ItemStack(
                            plugin.getPluginConfig().getHighChargeMaterial(), amountToDrop);

                    e.getBlock().getLocation().getWorld().dropItemNaturally(
                            e.getBlock().getLocation(),
                            highItemStack);
                    numHigh -= amountToDrop;
                }

            }

        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        // Check that we right clicked a portal block
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK
                || e.getClickedBlock().getType() != plugin.getPluginConfig().getPortalMaterial())
            return;

        Material clickedHandMaterial = e.getPlayer().getInventory().getItem(e.getHand()).getType();
        Material otherHandMaterial;

        if (e.getHand() == EquipmentSlot.HAND) {
            otherHandMaterial = e.getPlayer().getInventory().getItem(EquipmentSlot.OFF_HAND)
                    .getType();
        } else {
            otherHandMaterial = e.getPlayer().getInventory().getItem(EquipmentSlot.HAND).getType();
        }

        // If either item is a compass, don't do anything
        if (clickedHandMaterial == Material.COMPASS || otherHandMaterial == Material.COMPASS)
            return;

        PortalLocation pLoc = new PortalLocation();
        pLoc.x = e.getClickedBlock().getLocation().getBlockX();
        pLoc.y = e.getClickedBlock().getLocation().getBlockY();
        pLoc.z = e.getClickedBlock().getLocation().getBlockZ();
        pLoc.world = e.getClickedBlock().getWorld();

        // Get or create the portal
        Portal interactingPortal = plugin.getPortalData().getPortalAt(pLoc);
        if (interactingPortal == null)
            interactingPortal = plugin.getPortalData().createPortal(pLoc);

        // Check for configuration item
        if (clickedHandMaterial == plugin.getPluginConfig().getPortalConfigMaterial()) {
            // Check for config item in both hands to prevent double events
            if (otherHandMaterial == plugin.getPluginConfig().getPortalConfigMaterial()) {
                // Only allow if this is the main hand
                if (e.getHand() == EquipmentSlot.OFF_HAND)
                    return;
            }
            // Already began linking portals, continue linking process
            if (configuringMap.containsKey(e.getPlayer())) {
                if (interactingPortal.getLink() != null) {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(
                                    ChatColor.RED + "This portal is already linked!"));
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_ANVIL_PLACE,
                            10, 0.9f);
                } else {
                    // Ensure no linking to self
                    if (interactingPortal == configuringMap.get(e.getPlayer())) {
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent(
                                        ChatColor.RED + "You cannot link a portal to itself!"));
                        e.getPlayer().playSound(e.getPlayer().getLocation(),
                                Sound.BLOCK_ANVIL_PLACE,
                                10, 0.9f);
                    } else {
                        PortalLink newLink = interactingPortal.linkToPortal(
                                configuringMap.get(e.getPlayer()),
                                plugin.getPluginConfig().allowInterdimensional());
                        if (newLink == null) {
                            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    new TextComponent(
                                            ChatColor.RED + "You cannot link these two portals!"));
                            e.getPlayer().playSound(e.getPlayer().getLocation(),
                                    Sound.BLOCK_ANVIL_PLACE,
                                    10, 0.9f);
                        } else {
                            plugin.getPortalData().addLink(newLink);
                            configuringMap.remove(e.getPlayer());
                            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                    new TextComponent(
                                            ChatColor.GREEN + "Successfully linked portals!"));
                            e.getPlayer().playSound(e.getPlayer().getLocation(),
                                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                    10, 1f);
                        }
                    }
                }
            } else {
                // Attempt to begin linking process
                if (interactingPortal.getLink() != null) {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(
                                    ChatColor.RED + "This portal is already linked!"));
                    e.getPlayer().playSound(e.getPlayer().getLocation(),
                            Sound.BLOCK_ANVIL_PLACE,
                            10, 0.9f);
                } else {
                    configuringMap.put(e.getPlayer(), interactingPortal);
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(
                                    ChatColor.GREEN + "Successfully began linking portals!"));
                    e.getPlayer().playSound(e.getPlayer().getLocation(),
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                            10, 1f);
                }
            }
            e.setCancelled(true);
            return;
        }

        // No more config items past here
        if (otherHandMaterial == plugin.getPluginConfig().getPortalConfigMaterial())
            return;

        // Check for fuel item
        if (clickedHandMaterial == plugin.getPluginConfig().getHighChargeMaterial() ||
                clickedHandMaterial == plugin.getPluginConfig().getLowChargeMaterial()) {

            // Check for fuel item in both hands to prevent double events
            if (otherHandMaterial == plugin.getPluginConfig().getHighChargeMaterial() ||
                    otherHandMaterial == plugin.getPluginConfig().getLowChargeMaterial()) {
                // Only allow if this is the main hand
                if (e.getHand() == EquipmentSlot.OFF_HAND)
                    return;
            }

            int amountToInput = 1;
            if (e.getPlayer().isSneaking()) {
                amountToInput = e.getPlayer().getInventory().getItem(e.getHand()).getAmount();
            }

            double chargeIncreaseAmount = 0;
            boolean usingHighMaterial = false;
            if (clickedHandMaterial == plugin.getPluginConfig().getHighChargeMaterial()) {
                chargeIncreaseAmount = plugin.getPluginConfig().getHighChargeValue();
                usingHighMaterial = true;
            } else {
                chargeIncreaseAmount = plugin.getPluginConfig().getLowChargeValue();
            }

            // If using high charge, ensure amount is not 0
            if (!usingHighMaterial || plugin.getPluginConfig().getHighChargeValue() != 0) {

                // Check the portal has a link
                if (interactingPortal.getLink() == null) {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(
                                    ChatColor.RED + "This portal is not linked!"));
                    e.getPlayer().playSound(e.getPlayer().getLocation(),
                            Sound.BLOCK_ANVIL_PLACE,
                            10, 0.9f);
                } else {

                    double curCharge = interactingPortal.getLink().getCharge();

                    // Ensure we are not at the max charge
                    if (curCharge == plugin.getPluginConfig().getMaxFuel()) {
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent(
                                        ChatColor.RED
                                                + "This portal is already at maximum charge!"));
                        e.getPlayer().playSound(e.getPlayer().getLocation(),
                                Sound.BLOCK_ANVIL_PLACE,
                                10, 0.9f);

                    } else {

                        // Lower the amount we input to avoid over charging
                        while (curCharge + chargeIncreaseAmount * (amountToInput - 1) >= plugin
                                .getPluginConfig().getMaxFuel()) {

                            amountToInput--;
                        }

                        // Update the charge
                        double newCharge = curCharge + chargeIncreaseAmount * amountToInput;
                        if (newCharge > plugin.getPluginConfig().getMaxFuel()) {
                            newCharge = plugin.getPluginConfig().getMaxFuel();
                        }

                        interactingPortal.getLink().setCharge(newCharge);

                        // Remove item from hand
                        ItemStack handItemStack = e.getPlayer().getInventory().getItem(e.getHand());
                        handItemStack.setAmount(handItemStack.getAmount() - amountToInput);
                        e.getPlayer().getInventory().setItem(e.getHand(), handItemStack);

                        String chargeAmountColor;
                        double percentageOfMax = newCharge / plugin.getPluginConfig().getMaxFuel();
                        if (percentageOfMax <= 0.25) {
                            chargeAmountColor = ChatColor.RED.toString();
                        } else if (percentageOfMax <= 0.50) {
                            chargeAmountColor = ChatColor.GOLD.toString();
                        } else if (percentageOfMax <= 0.75) {
                            chargeAmountColor = ChatColor.YELLOW.toString();
                        } else {
                            chargeAmountColor = ChatColor.GREEN.toString();
                        }

                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent(
                                        ChatColor.LIGHT_PURPLE + "Portal Charge: "
                                                + chargeAmountColor
                                                + String.format("%.2f", newCharge) + "/"
                                                + String.format("%.2f",
                                                        plugin.getPluginConfig().getMaxFuel())));
                        e.getPlayer().playSound(e.getPlayer().getLocation(),
                                Sound.BLOCK_AMETHYST_BLOCK_PLACE,
                                10, 1f);
                    }
                }
                e.setCancelled(true);
                return;
            }
        }

        // No more fuel items past here
        if ((otherHandMaterial == plugin.getPluginConfig().getHighChargeMaterial()
                && plugin.getPluginConfig().getHighChargeValue() != 0) ||
                otherHandMaterial == plugin.getPluginConfig().getLowChargeMaterial())
            return;

        // Only allow interaction with main hand
        if (e.getHand() != EquipmentSlot.HAND)
            return;

        Portal linkedPortal = interactingPortal.getLinkedPortal();
        if (linkedPortal != null) {
            String message = "Linked to:" + ChatColor.YELLOW + " ("
                    + linkedPortal.getPortalLocation().x + ", "
                    + linkedPortal.getPortalLocation().y + ", " + linkedPortal.getPortalLocation().z
                    + ", " + linkedPortal.getPortalLocation().world.getName() + ")"
                    + ChatColor.WHITE + " Charge: " + ChatColor.LIGHT_PURPLE
                    + String.format("%.2f", interactingPortal.getLink().getCharge());
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(
                            ChatColor.WHITE
                                    + message));
        }

    }

    @EventHandler
    public void onPlayerCrouch(PlayerToggleSneakEvent e) {
        if (e.isSneaking()) {
            // Check for portal
            Location belowPlayer = e.getPlayer().getLocation().subtract(0, 1, 0);

            if (belowPlayer.getBlock().getType()
                    .equals(plugin.getPluginConfig().getPortalMaterial())) {

                PortalLocation portalLoc = new PortalLocation();
                portalLoc.x = belowPlayer.getBlockX();
                portalLoc.y = belowPlayer.getBlockY();
                portalLoc.z = belowPlayer.getBlockZ();
                portalLoc.world = belowPlayer.getWorld();

                Portal p = plugin.getPortalData().getPortalAt(portalLoc);
                if (p != null) {
                    Portal other = p.getLinkedPortal();
                    PortalLink link = p.getLink();

                    // Calculate cost to teleport
                    double totalCost = plugin.getPluginConfig().getBaseCostPerUse();

                    totalCost += plugin.getPluginConfig().getCostPerBlock()
                            * link.getLinkDistance();

                    if (link.isInterdimensional()) {
                        totalCost += plugin.getPluginConfig().getCostForDimensional();
                    }

                    // Check if there is enough charge
                    if (totalCost > link.getCharge()) {
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new TextComponent(
                                        ChatColor.RED
                                                + "This portal is out of fuel!"));
                        e.getPlayer().playSound(e.getPlayer().getLocation(),
                                Sound.BLOCK_REDSTONE_TORCH_BURNOUT,
                                10, 1f);
                    } else {
                        // Teleport and remove charge
                        link.setCharge(link.getCharge() - totalCost);
                        Location tpLoc = other.getTeleportLocation();
                        tpLoc.setDirection(e.getPlayer().getLocation().getDirection());
                        e.getPlayer().teleport(tpLoc);
                        e.getPlayer().playSound(e.getPlayer().getLocation(),
                                Sound.ENTITY_ENDERMAN_TELEPORT,
                                10, 1.3f);
                    }
                }
            }
        }
    }

}
