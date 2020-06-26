package me.shakeforprotein.treebotimed.Commands;

import me.shakeforprotein.treebotimed.TreeboTimed;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class CopyIslandFormat implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        TreeboTimed pl = TreeboTimed.pl;
        if(sender instanceof Player){
            if(args.length == 4) {
                File actualFile = new File(pl.getDataFolder() + File.separator + "islandFiles", args[0] + ".yml");
                FileConfiguration stoneYml = new YamlConfiguration().loadConfiguration(actualFile);
                Location loc1 = pl.loc1Hash.get(sender);
                Location loc2 = pl.loc2Hash.get(sender);

                int lx, hx, ly, hy, lz, hz = 0;
                lx = Math.min(loc1.getBlockX(), loc2.getBlockX());
                hx = Math.max(loc1.getBlockX(), loc2.getBlockX());
                ly = Math.min(loc1.getBlockY(), loc2.getBlockY());
                hy = Math.max(loc1.getBlockY(), loc2.getBlockY());
                lz = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
                hz = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
                int i, j, k = 0;
                for (i = lx; i < hx + 1; i++) {
                    for (j = lz; j < hz + 1; j++) {
                        for (k = ly; k < hy + 1; k++) {
                            Location targetLoc = new Location(((Player) sender).getWorld(), i, k, j);
                            sender.sendMessage(targetLoc.getBlock().getLocation() + "");
                            sender.sendMessage(targetLoc.getBlock().getType() + "");
                            if (targetLoc.getBlock().getType() != Material.AIR) {
                                stoneYml.set("Blocks.X" + targetLoc.getBlockX() + "Y" + targetLoc.getBlockY() + "Z" + targetLoc.getBlockZ() + ".X", targetLoc.getBlockX() - Integer.parseInt(args[1]));
                                stoneYml.set("Blocks.X" + targetLoc.getBlockX() + "Y" + targetLoc.getBlockY() + "Z" + targetLoc.getBlockZ() + ".Y", targetLoc.getBlockY() - Integer.parseInt(args[2]));
                                stoneYml.set("Blocks.X" + targetLoc.getBlockX() + "Y" + targetLoc.getBlockY() + "Z" + targetLoc.getBlockZ() + ".Z", targetLoc.getBlockZ() - Integer.parseInt(args[3]));
                                stoneYml.set("Blocks.X" + targetLoc.getBlockX() + "Y" + targetLoc.getBlockY() + "Z" + targetLoc.getBlockZ() + ".D0", targetLoc.getBlock().getBlockData().getAsString());
                                stoneYml.set("Blocks.X" + targetLoc.getBlockX() + "Y" + targetLoc.getBlockY() + "Z" + targetLoc.getBlockZ() + ".M", targetLoc.getBlock().getType().name());
                                stoneYml.set("Blocks.X" + targetLoc.getBlockX() + "Y" + targetLoc.getBlockY() + "Z" + targetLoc.getBlockZ() + ".D1", targetLoc.getBlock().getState().getData());
                                stoneYml.set("Blocks.X" + targetLoc.getBlockX() + "Y" + targetLoc.getBlockY() + "Z" + targetLoc.getBlockZ() + ".S", targetLoc.getBlock().getState());
                            }
                        }
                    }
                }
                try {
                    stoneYml.save(actualFile);
                }
                catch(IOException e){pl.getLogger().warning(e.getCause() + "");}
            }
            else{
                sender.sendMessage(pl.badge + ChatColor.RED + "ERROR:" + ChatColor.RESET + "This command requires a file name argument.");
            }
        }
        else{
            sender.sendMessage(pl.badge + " Given the need for multiple coordinate inputs, this command has only been programmed to work when run as a player.");
        }
        return true;
    }
}
