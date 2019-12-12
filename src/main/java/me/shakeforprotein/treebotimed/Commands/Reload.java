package me.shakeforprotein.treebotimed.Commands;

import me.shakeforprotein.treebotimed.TreeboTimed;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Reload implements CommandExecutor {

    TreeboTimed pl;

    public Reload(TreeboTimed main){
        this.pl = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        pl.reloadConfig();
        pl.setSchedule();
        return true;
    }
}
