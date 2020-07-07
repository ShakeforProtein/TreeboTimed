package me.shakeforprotein.treebotimed;

import com.sk89q.worldedit.WorldEdit;
import me.shakeforprotein.treebotimed.Commands.*;
import me.shakeforprotein.treebotimed.Listeners.PlaceSchem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Random;


public final class TreeboTimed extends JavaPlugin {

    public HashMap<Player, Location> loc1Hash = new HashMap<>();
    public HashMap<Player, Location> loc2Hash = new HashMap<>();
    public HashMap<Integer, Location> blockLocHash = new HashMap<>();
    public HashMap<Integer, Material> possibilityHash = new HashMap<>();
    public String badge = ChatColor.translateAlternateColorCodes('&', getConfig().getString("badge"));
    public WorldEdit worldEdit = WorldEdit.getInstance();
    public static TreeboTimed pl;


    @Override
    public void onEnable() {
        // Plugin startup logic
        pl = this;
        getConfig().options().copyDefaults(true);
        getConfig().set("version", this.getDescription().getVersion());
        saveConfig();
        this.getCommand("pasteatorigin").setExecutor(new PasteAtOrigin(this));
        this.getCommand("copyfromorigin").setExecutor(new CopyFromOrigin(this));
        this.getCommand("tpos1").setExecutor(new TPos1(this));
        this.getCommand("tpos2").setExecutor(new TPos2(this));
        this.getCommand("giveblueprint").setExecutor(new GiveBlueprint(this));
        this.getCommand("copyStone").setExecutor(new CopyStone());
        this.getCommand("CopyIslandFormat").setExecutor(new CopyIslandFormat());
        Bukkit.getPluginManager().registerEvents(new PlaceSchem(this), this);
        if(getConfig().getBoolean("allowGenerateOres")) {
            setStoneQue();
        }
        if (getConfig().getBoolean("automaticPasting")) {
            setSchedule();
        }
        if(getConfig().get("bstatsIntegration") != null) {
            if (getConfig().getBoolean("bstatsIntegration")) {
                //Metrics metrics = new Metrics(this);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        pl = null;
    }


    public void setSchedule() {
        File sourceDir = new File(getDataFolder().getAbsolutePath() + File.separator + "schematics");
        File[] files = sourceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".scm");
            }
        });
        if (files != null && files.length > 0) {
            int totalFiles = files.length;
            int count = 1;
            long timespan = getConfig().getInt("totalInterval") * 20 * 60;
            if (totalFiles > 0) {
                long interval = timespan / totalFiles;
                for (File schematic : files) {
                    System.out.println("Scheduling pasting schematic - " + schematic.getName().replace(".scm", "") + " - in " + interval * count + " ticks, repeating every " + timespan + " ticks");
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                        @Override
                        public void run() {
                            //System.out.println(schematic.getAbsolutePath().replace(".scm", ""));
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pasteatorigin " + schematic.getName().replace(".scm", ""));
                        }
                    }, (interval * count), timespan);
                    count++;

                    Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                        @Override
                        public void run() {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                int x, y, z = 0;
                                FileConfiguration yaml = new YamlConfiguration().loadConfiguration(new File(schematic.getName().replace(".scm", ".yml")));
                                x = yaml.getInt("X");
                                y = yaml.getInt("Y");
                                z = yaml.getInt("Z");
                                if (p.getLocation().getBlockX() > (x - 50) && p.getLocation().getBlockX() < (x + 50)) {
                                    if (p.getLocation().getBlockZ() > (z - 50) && p.getLocation().getBlockZ() < (z + 50)) {
                                        if (p.getLocation().getBlockY() > (y - 15) && p.getLocation().getBlockY() < (y + 50)) {
                                            //p.sendMessage(badge + " Resetting farm " + ChatColor.GOLD + schematic.getName().replace(".scm", "") + ChatColor.RESET + " in " + ChatColor.RED + ChatColor.BOLD + "ONE" + ChatColor.RESET + " Minute");
                                        }
                                    }
                                }
                            }
                            //Bukkit.broadcastMessage(badge + " Resetting farm " + ChatColor.GOLD + schematic.getName().replace(".scm", "") + ChatColor.RESET + " in " + ChatColor.RED + ChatColor.BOLD + "ONE" + ChatColor.RESET + " Minute");
                        }
                    }, (interval * count) - 1200, timespan);
                }
            }
        }
    }

    public void setStoneQue() {
        int m = 0;
        int n = 0;
        for (String key : getConfig().getConfigurationSection("oreWeights").getKeys(false)) {
            for (m = 0; m < getConfig().getInt("oreWeights." + key); m++) {
                possibilityHash.put(n, Material.valueOf(key));
                n++;
            }
        }

        File sourceDir = new File(getDataFolder().getAbsolutePath() + File.separator + "stoneFiles");
        File[] files = sourceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yml");
            }
        });
        int totalBlocks = 0;
        if (files != null && files.length > 0) {
            for (File file : files) {
                FileConfiguration blockFile = new YamlConfiguration().loadConfiguration(file);
                for (String key : blockFile.getConfigurationSection("Blocks").getKeys(false)) {
                    int x, y, z;
                    String world = blockFile.getString("Blocks." + key + ".W");
                    x = blockFile.getInt("Blocks." + key + ".X");
                    y = blockFile.getInt("Blocks." + key + ".Y");
                    z = blockFile.getInt("Blocks." + key + ".Z");
                    Location tempLocation = new Location(Bukkit.getWorld(world), x, y, z);
                    blockLocHash.putIfAbsent(totalBlocks, tempLocation);
                    totalBlocks++;
                }
            }

            Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    Random r = new Random();
                    int low = 0;
                    int high = blockLocHash.keySet().size();
                    int result = r.nextInt(high - low) + low;
                    //getLogger().info(high + " " + low + " " + result);
                    Block targetBlock = blockLocHash.get(result).getBlock();
                    if (targetBlock.getType() == Material.AIR) {
                        if (Math.floor(r.nextInt(100)) > 80) {
                            targetBlock.setType(Material.STONE);
                        }
                    } else if (targetBlock.getType() == Material.STONE) {
                        if (r.nextInt(100) > 60) {
                            targetBlock.setType(possibilityHash.get((int) Math.floor(r.nextInt(possibilityHash.size()))));
                        }
                    } else if (targetBlock.getType() == Material.DIAMOND_ORE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.DIAMOND_BLOCK);
                        }
                    } else if (targetBlock.getType() == Material.EMERALD_ORE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.EMERALD_BLOCK);
                        }
                    } else if (targetBlock.getType() == Material.GOLD_ORE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.GOLD_BLOCK);
                        }
                    } else if (targetBlock.getType() == Material.IRON_ORE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.IRON_BLOCK);
                        }
                    } else if (targetBlock.getType() == Material.COAL_ORE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.COAL_BLOCK);
                        }
                    } else if (targetBlock.getType() == Material.REDSTONE_ORE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.REDSTONE_BLOCK);
                        }
                    } else if (targetBlock.getType() == Material.LAPIS_ORE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.LAPIS_BLOCK);
                        }
                    } else if (targetBlock.getType() == Material.NETHER_QUARTZ_ORE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.QUARTZ_BLOCK);
                        }
                    } else if (targetBlock.getType() == Material.GLOWSTONE) {
                        if ((int) Math.floor(r.nextInt(100)) <= getConfig().getInt("growSolidPercent")) {
                            targetBlock.setType(Material.OBSIDIAN);
                        }
                    }
                }
            }, 100L, getConfig().getInt("stoneRegenModifier"));
            getLogger().info("Stone Generator found " + files.length + " files to load, with a total of " + totalBlocks + "Blocks to que");
        }
    }
}
