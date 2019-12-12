package me.shakeforprotein.treebotimed.Commands;

import me.shakeforprotein.treebotimed.TreeboTimed;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPos2 implements CommandExecutor {

    TreeboTimed pl;

    public TPos2(TreeboTimed main){
        this.pl = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        Location loc = ((Player) sender).getLocation();
        pl.loc2Hash.put((Player) sender, loc);
        sender.sendMessage("Set Pos 2 - " + loc.getBlockX() + " " + loc.getY() + " " + loc.getBlockZ());
        return true;
    }
}
