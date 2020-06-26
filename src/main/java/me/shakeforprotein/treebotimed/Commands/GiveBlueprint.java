package me.shakeforprotein.treebotimed.Commands;

import me.shakeforprotein.treebotimed.TreeboTimed;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GiveBlueprint implements CommandExecutor {

    private TreeboTimed pl;

    public GiveBlueprint(TreeboTimed main){
        this.pl = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length < 2){
            sender.sendMessage(pl.badge + ChatColor.RED + "ERROR: " + ChatColor.RESET + "Insufficient Arguments");
            sender.sendMessage(ChatColor.GOLD + "[X]" + ChatColor.RESET + "/giveBlueprint <player> <blueprint>");
        }
        else if (args.length > 2){
            sender.sendMessage(pl.badge + ChatColor.RED + "ERROR: " + ChatColor.RESET + "Too Many Arguments");
            sender.sendMessage(ChatColor.GOLD + "[X]" + ChatColor.RESET + "/giveBlueprint <player> <blueprint>");

        }
        if(args.length == 2) {
            boolean arg0isPlayer = false;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (args[0].equalsIgnoreCase(p.getName())) {
                    arg0isPlayer = true;
                    break;
                }
            }
            if (!arg0isPlayer) {
                sender.sendMessage(pl.badge + ChatColor.RED + "ERROR: " + ChatColor.RESET + "Player '" + args[0] + "' not found");
            } else {
                File detailsFile = new File(pl.getDataFolder() + File.separator + "blueprints", args[1] + ".yml");
                FileConfiguration detailsYml = YamlConfiguration.loadConfiguration(detailsFile);
                ItemStack blueprint = new ItemStack(Material.PAPER, 1);
                if(detailsYml.get("Map") != null){
                    blueprint = new ItemStack(Material.FILLED_MAP, 1);
                }

                ItemMeta itemMeta = blueprint.getItemMeta();
                if(itemMeta instanceof MapMeta){
                    MapMeta mapMeta = (MapMeta) itemMeta;
                    mapMeta.setMapId(detailsYml.getInt("Map"));
                    itemMeta = mapMeta;
                }
                itemMeta.setDisplayName(ChatColor.BLUE + "Blueprint" + ChatColor.GOLD + " " + args[1]);
                List<String> list = new ArrayList<>();
                list.add("Blueprint: " + args[1]);
                itemMeta.setLore(list);
                blueprint.setItemMeta(itemMeta);

                net.minecraft.server.v1_16_R1.ItemStack nmsItem = getNMSItem(blueprint);
                NBTTagCompound nbtCompound = getCompound(nmsItem);
                Set<String> compoundSet = nbtCompound.getKeys();
                StringBuilder sb = new StringBuilder();
                int i = 0;
                nbtCompound.setString("ShakeBlueprint", args[1]);
                nmsItem.setTag(nbtCompound);
                ItemStack newItem = CraftItemStack.asBukkitCopy(nmsItem);

                Bukkit.getPlayer(args[0]).getInventory().addItem(newItem);
            }
        }
        return true;
    }


    public net.minecraft.server.v1_16_R1.ItemStack getNMSItem(ItemStack item) {
        net.minecraft.server.v1_16_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        return nmsItem;
    }

    public NBTTagCompound getCompound(net.minecraft.server.v1_16_R1.ItemStack nmsItem) {
        NBTTagCompound nmsCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
        return nmsCompound;
    }

    public ItemStack getBukkitItem(net.minecraft.server.v1_16_R1.ItemStack nmsItem) {
        ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsItem);
        return bukkitItem;
    }
}
