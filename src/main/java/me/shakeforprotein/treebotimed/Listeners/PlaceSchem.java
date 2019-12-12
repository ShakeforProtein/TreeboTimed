package me.shakeforprotein.treebotimed.Listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.shakeforprotein.treebotimed.TreeboTimed;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PlaceSchem implements Listener {

    private TreeboTimed pl;

    public PlaceSchem(TreeboTimed main) {
        this.pl = main;
    }

    @EventHandler
    private void placeSchem(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null && (e.getItem().getType() == Material.PAPER || e.getItem().getType() == Material.FILLED_MAP)) {
                if (e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName() && ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName().toLowerCase()).startsWith("blueprint")) {
                    ItemStack blueprint = e.getItem();
                    net.minecraft.server.v1_15_R1.ItemStack nmsBlueprint = getNMSItem(blueprint);
                    NBTTagCompound nbtBlueprint = getCompound(nmsBlueprint);
                    if (nbtBlueprint.hasKey("ShakeBlueprint")) {
                        String scmName = nbtBlueprint.getString("ShakeBlueprint");
                        if (placeAtPlayer(e.getPlayer(), scmName)) {
                            blueprint.setAmount(blueprint.getAmount() - 1);
                        }
                    }
                }
            }
        }
    }


    public net.minecraft.server.v1_15_R1.ItemStack getNMSItem(ItemStack item) {
        net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        return nmsItem;
    }

    public NBTTagCompound getCompound(net.minecraft.server.v1_15_R1.ItemStack nmsItem) {
        NBTTagCompound nmsCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
        return nmsCompound;
    }

    public ItemStack getBukkitItem(net.minecraft.server.v1_15_R1.ItemStack nmsItem) {
        ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsItem);
        return bukkitItem;
    }

    public boolean placeAtPlayer(Player p, String nbtString) {

        File detailsFile = new File(pl.getDataFolder() + File.separator + "blueprints", nbtString + ".yml");
        FileConfiguration detailsYml = YamlConfiguration.loadConfiguration(detailsFile);

        Boolean hasResources = true;
        ItemStack[] pInv = p.getInventory().getContents();
        for (String key : detailsYml.getConfigurationSection("Materials").getKeys(false)) {
            boolean hasItem = false;
            for (ItemStack item : pInv) {
                if (item != null) {
                        if (item.getType() != Material.AIR) {
                            if (item.getType().toString().equalsIgnoreCase(key)) {
                                if (item.getAmount() >= detailsYml.getInt("Materials." + key)) {
                                    hasItem = true;
                                }
                            }
                        }

                }
            }
            if (!hasItem) {
                hasResources = false;
                break;
            }
        }

        if (hasResources || p.getGameMode() == GameMode.CREATIVE) {
            for (String key : detailsYml.getConfigurationSection("Materials").getKeys(false)) {
                int counter = 0;
                int required = detailsYml.getInt("Materials." + key);
                for(counter = 0; counter < required; counter++){
                    for(ItemStack item : pInv){
                        if(required > 0){
                        if(item != null) {
                            if (item.getType().toString().equalsIgnoreCase(key)) {
                                if (item.getAmount() >= required && required > 0) {
                                    item.setAmount(item.getAmount() - required);
                                    required = 0;
                                } else {
                                    required = required - item.getAmount();
                                    item.setAmount(0);
                                }
                            }
                            }
                        }
                    }
                }
                p.getInventory().remove(new ItemStack(Material.valueOf(key), detailsYml.getInt("Materials." + key)));
            }
            World bukkitWorld = Bukkit.getWorld(detailsYml.getString("World"));

            double x = p.getLocation().getBlockX();
            double y = p.getLocation().getBlockY();
            double z = p.getLocation().getBlockZ();
            com.sk89q.worldedit.world.World worldEditWorld = new BukkitWorld(bukkitWorld);
            File schematicFile = new File(pl.getDataFolder() + File.separator + "blueprints", nbtString + ".scm");
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();
                try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(worldEditWorld, -1)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(x, y, z))
                            .ignoreAirBlocks(false)
                            .build();
                    Operations.complete(operation);

                } catch (WorldEditException e) {
                    System.out.println(e);
                }
            } catch (IOException e) {
                System.out.println(e);
            }
            return true;
        } else {
            p.sendMessage(pl.badge + ChatColor.RED + "ERROR: Resources Missing" + ChatColor.RESET + " - You require the following resources");
            for (String key : detailsYml.getConfigurationSection("Materials").getKeys(false)) {
                p.sendMessage(detailsYml.getInt("Materials." + key) + " x " + ChatColor.GOLD + key);
            }
            return false;
        }
    }
}
