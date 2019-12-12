package me.shakeforprotein.treebotimed.Commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.shakeforprotein.treebotimed.TreeboTimed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class CopyFromOrigin implements CommandExecutor {

    TreeboTimed pl;

    public CopyFromOrigin(TreeboTimed main) {
        this.pl = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 || args.length == 2) {

            File schematicFile = new File(pl.getDataFolder() + File.separator + "schematics", args[0] + ".scm");
            File detailsFile = new File(pl.getDataFolder() + File.separator + "schematics", args[0] + ".yml");

            if(args.length == 2) {
                schematicFile = new File(pl.getDataFolder() + File.separator + "blueprints", args[0] + ".scm");
                detailsFile = new File(pl.getDataFolder() + File.separator + "blueprints", args[0] + ".yml");
            }

            FileConfiguration detailsYml = YamlConfiguration.loadConfiguration(detailsFile);

            Location loc1 = pl.loc1Hash.get((Player) sender);
            Location loc2 = pl.loc2Hash.get((Player) sender);

            int lowX = 0;
            int highX = 0;
            int lowY = 255;
            int highY = 255;
            int lowZ = 0;
            int highZ = 0;
            int i = 0;

            if (loc2.getX() >= loc1.getX()) {
                lowX = (int) loc1.getX();
                highX = (int) loc2.getX();
            } else {
                lowX = (int) loc2.getX();
                highX = (int) loc1.getX();
            }

            if (loc2.getY() >= loc1.getY()) {
                lowY = (int) loc1.getY();
                highY = (int) loc2.getY();
            } else {
                lowY = (int) loc2.getY();
                highY = (int) loc1.getY();
            }

            if (loc2.getZ() >= loc1.getZ()) {
                lowZ = (int) loc1.getZ();
                highZ = (int) loc2.getZ();
            } else {
                lowZ = (int) loc2.getZ();
                highZ = (int) loc1.getZ();
            }

            HashMap<Material, Integer> regionBlocks = new HashMap<>();
            int ix, iy, iz;
            for(ix = lowX; ix < highX+1; ix++){
                for(iy = lowY; iy < highY+1; iy++){
                    for(iz = lowZ; iz < highZ+1; iz++){
                        Block block = new Location(((Player) sender).getLocation().getWorld(), ix, iy, iz).getBlock();
                        if(regionBlocks.containsKey(block.getType())){
                            regionBlocks.replace(block.getType(), regionBlocks.get(block.getType()) +1);
                        }
                        else{
                            regionBlocks.putIfAbsent(block.getType(), 1);
                        }
                    }
                }
            }

            for(Material material : regionBlocks.keySet()){
                if(material != null && !material.equals(Material.AIR)){
                    detailsYml.set("Materials." + material.toString(), regionBlocks.get(material));
                }
            }


            com.sk89q.worldedit.world.World worldEditWorld = new BukkitWorld(((Player) sender).getWorld());

            CuboidRegion cuboidRegion = new CuboidRegion(worldEditWorld, BlockVector3.at(lowX,lowY,lowZ),BlockVector3.at(highX,highY,highZ));
            BlockArrayClipboard clipboard = new BlockArrayClipboard(cuboidRegion);
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(cuboidRegion.getWorld(), -1);
            BlockVector3 origin = clipboard.getOrigin();
            String world = ((Player) sender).getWorld().getName();

            detailsYml.set("World", world);
            detailsYml.set("X", origin.getBlockX());
            detailsYml.set("Y", origin.getBlockY());
            detailsYml.set("Z", origin.getBlockZ());


            try{detailsYml.save(detailsFile);}
            catch (IOException e){
                System.out.println(e);
            }


            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, cuboidRegion, clipboard, cuboidRegion.getMinimumPoint());
            forwardExtentCopy.setCopyingEntities(true);
            try {
                Operations.complete(forwardExtentCopy);
            }
            catch(WorldEditException e){
                System.out.println(e);
            }

            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schematicFile))) {
                writer.write(clipboard);
            }
            catch(IOException e){
                System.out.println(e);
            }
        }

        return true;
    }
}
