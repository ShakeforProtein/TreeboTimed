package me.shakeforprotein.treebotimed.Commands;

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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class PasteAtOrigin implements CommandExecutor {

    TreeboTimed pl;

    public PasteAtOrigin(TreeboTimed main) {
        this.pl = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
     /*   if (args.length == 1 ||
                (args.length == 2 && (args[1].toLowerCase().startsWith("exclude:") || args[1].toLowerCase().startsWith("replace:")))){
            //sender.sendMessage("Running task Asynchronously to reduce lag.");
            Bukkit.getScheduler().runTask(pl, new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> excludeList = new ArrayList();
                    ArrayList<String> replaceList = new ArrayList();
                    if (args.length == 2 && args[1].toLowerCase().startsWith("exclude:")) {
                        for (String item : args[1].split(":")[1].split(",")) {
                            excludeList.add(item);
                        }
                    }
                    else if (args.length == 2 && args[1].toLowerCase().startsWith("replace:")) {
                        for (String item : args[1].split(":")[1].split(",")) {
                            excludeList.add(item);
                        }
                    }
                    File schematicFile = new File(pl.getDataFolder() + File.separator + "schematics", args[0] + ".scm");
                    FileConfiguration schematicYml = YamlConfiguration.loadConfiguration(schematicFile);
                    int i = 0;
                    int j = 0;
                    sender.sendMessage("Preparing to paste blocks");
                    for (i = 0; i < schematicYml.getConfigurationSection("schematic.blocks").getKeys(false).size(); i++) {
                        Location loc = (Location) schematicYml.get("schematic.blocks.B" + i + ".location");
                        Material blockType = Material.valueOf(schematicYml.getString("schematic.blocks.B" + i + ".type"));
                        boolean placeBlock = true;
                        if (!excludeList.isEmpty()) {
                            for (j = 0; j < excludeList.size(); j++) {
                                if (blockType.name().equalsIgnoreCase(excludeList.get(j))) {
                                    placeBlock = false;
                                }
                            }
                        }
                        if(placeBlock) {
                            loc.getBlock().setType(blockType);

                            if (schematicYml.get("schematic.blocks.B" + i + ".blockData") != null) {
                                BlockData blockData = Bukkit.createBlockData(schematicYml.getString("schematic.blocks.B" + i + ".blockData"));
                                loc.getBlock().setBlockData(blockData);
                            } else {
                                sender.sendMessage("NULL");
                            }
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.sendBlockChange(loc, blockType, loc.getBlock().getData());
                            }

                           // sender.sendMessage("Block " + i + "(" + blockType.name() + ") pasted.");
                        }
                        else{
                            //sender.sendMessage("Block " + i + "(" + blockType.name() + ") skipped due to exclude list");
                        }
                    }
                }
            });

        } else {
            sender.sendMessage("Insufficient Arguments");
        }

      */


        File detailsFile = new File(pl.getDataFolder() + File.separator + "schematics", args[0] + ".yml");
        FileConfiguration detailsYml = YamlConfiguration.loadConfiguration(detailsFile);

        World bukkitWorld = Bukkit.getWorld(detailsYml.getString("World"));

        double x = detailsYml.getDouble("X");
        double y = detailsYml.getDouble("Y");
        double z = detailsYml.getDouble("Z");
        com.sk89q.worldedit.world.World worldEditWorld = new BukkitWorld(bukkitWorld);
        File schematicFile = new File(pl.getDataFolder() + File.separator + "schematics", args[0] + ".scm");
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
    }
}
