/*
 * Village Defense 3 - Protect villagers from hordes of zombies
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.villagedefense3.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;

import pl.plajer.villagedefense3.Main;
import pl.plajer.villagedefense3.handlers.ConfigurationManager;

/**
 * Created by Tom on 29/07/2014.
 */
public class Utils {

  private static Main plugin;

  public Utils(Main plugin) {
    Utils.plugin = plugin;
  }

  public static void addLore(ItemStack itemStack, String string) {
    ItemMeta meta = itemStack.getItemMeta();
    List<String> lore = new ArrayList<>();
    if (meta != null && meta.hasLore()) {
      lore.addAll(meta.getLore());
    }
    lore.add(string);
    meta.setLore(lore);
    itemStack.setItemMeta(meta);
  }

  public static Queue<Block> getLineOfSight(LivingEntity entity, HashSet<Byte> transparent, int maxDistance, int maxLength) {
    if (maxDistance > 120) {
      maxDistance = 120;
    }

    Queue<Block> blocks = new LinkedList<>();
    Iterator<Block> itr = new BlockIterator(entity, maxDistance);
    while (itr.hasNext()) {
      Block block = itr.next();
      blocks.add(block);

      if (maxLength != 0 && blocks.size() > maxLength) {
        blocks.remove(0);
      }
      //todo block id!
      int id = block.getTypeId();
      if (transparent == null) {
        if (id != 0 && id != 50 && id != 59 && id != 31 && id != 175 && id != 38 && id != 37 && id != 6 && id != 106) {
          break;
        }
      } else {
        if (!transparent.contains((byte) id)) {
          break;
        }
      }
    }
    return blocks;
  }

  public static Entity[] getNearbyEntities(Location l, int radius) {
    int chunkRadius = radius < 16 ? 1 : radius / 16;
    HashSet<Entity> radiusEntities = new HashSet<>();
    for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
      for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
        int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
        for (Entity e : new Location(l.getWorld(), x + chX * 16, y, z + chZ * 16).getChunk().getEntities()) {
          if (!(l.getWorld().getName().equalsIgnoreCase(e.getWorld().getName()))) {
            continue;
          }
          if (e.getLocation().distanceSquared(l) <= radius * radius && e.getLocation().getBlock() != l
                  .getBlock()) {
            radiusEntities.add(e);
          }
        }
      }
    }
    return radiusEntities.toArray(new Entity[0]);
  }

  public static String formatIntoMMSS(int secsIn) {
    int minutes = secsIn / 60,
            seconds = secsIn % 60;
    return ((minutes < 10 ? "0" : "") + minutes
            + ":" + (seconds < 10 ? "0" : "") + seconds);
  }

  public static double round(double value, int places) {
    if (places < 0) {
      return value;
    }
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  public static int serializeInt(Integer i) {
    if ((i % 9) == 0) {
      return i;
    } else {
      return (int) ((Math.ceil(i / 9) * 9) + 9);
    }
  }

  public static String locationToString(Location location) {
    return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
  }

  public static void spawnRandomFirework(Location location) {
    Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
    FireworkMeta fwm = fw.getFireworkMeta();

    //Our random generator
    Random r = new Random();

    //Get the type
    int rt = r.nextInt(4) + 1;
    FireworkEffect.Type type;
    switch (rt) {
      case 1:
        type = FireworkEffect.Type.BALL;
        break;
      case 2:
        type = FireworkEffect.Type.BALL_LARGE;
        break;
      case 3:
        type = FireworkEffect.Type.BURST;
        break;
      case 4:
        type = FireworkEffect.Type.CREEPER;
        break;
      case 5:
        type = FireworkEffect.Type.STAR;
        break;
      default:
        type = FireworkEffect.Type.BALL;
        break;
    }

    //Get our random colours
    int r1i = r.nextInt(250) + 1;
    int r2i = r.nextInt(250) + 1;
    Color c1 = Color.fromBGR(r1i);
    Color c2 = Color.fromBGR(r2i);

    //Create our effect with this
    FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

    //Then apply the effect to the meta
    fwm.addEffect(effect);

    //Generate some random power and set it
    int rp = r.nextInt(2) + 1;
    fwm.setPower(rp);
    fw.setFireworkMeta(fwm);
  }


  public static List<String> splitString(String string, int max) {
    List<String> matchList = new ArrayList<>();
    Pattern regex = Pattern.compile(".{1," + max + "}(?:\\s|$)", Pattern.DOTALL);
    Matcher regexMatcher = regex.matcher(string);
    while (regexMatcher.find()) {
      matchList.add(ChatColor.translateAlternateColorCodes('&', "&7") + regexMatcher.group());
    }
    return matchList;
  }

  public static void saveLoc(String path, Location loc, boolean inConfig) {
    String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    if (inConfig) {
      plugin.getConfig().set(path, location);
      plugin.saveConfig();
    } else {
      FileConfiguration config = ConfigurationManager.getConfig("arenas");
      config.set(path, location);
      ConfigurationManager.saveConfig(config, "arenas");
    }
  }

  public static Location getLocation(boolean configUsage, String path) {
    String[] loc;
    if (configUsage) {
      loc = plugin.getConfig().getString(path).split(",");
    } else {
      loc = path.split(",");
    }
    plugin.getServer().createWorld(new WorldCreator(loc[0]));
    World w = plugin.getServer().getWorld(loc[0]);
    Double x = Double.parseDouble(loc[1]);
    Double y = Double.parseDouble(loc[2]);
    Double z = Double.parseDouble(loc[3]);
    float yaw = Float.parseFloat(loc[4]);
    float pitch = Float.parseFloat(loc[5]);
    return new Location(w, x, y, z, yaw, pitch);
  }

  public static String getProgressBar(int current, int max, int totalBars, String symbol, String completedColor, String notCompletedColor) {

    float percent = (float) current / max;

    int progressBars = (int) (totalBars * percent);

    int leftOver = (totalBars - progressBars);

    StringBuilder sb = new StringBuilder();
    sb.append(ChatColor.translateAlternateColorCodes('&', completedColor));
    for (int i = 0; i < progressBars; i++) {
      sb.append(symbol);
    }
    sb.append(ChatColor.translateAlternateColorCodes('&', notCompletedColor));
    for (int i = 0; i < leftOver; i++) {
      sb.append(symbol);
    }
    return sb.toString();
  }

  public static ItemStack getPotion(PotionType type, int tier, boolean splash, int amount) {
    ItemStack potion;
    if (!splash) {
      potion = new ItemStack(Material.POTION, 1);
    } else {
      potion = new ItemStack(Material.SPLASH_POTION, 1);
    }

    PotionMeta meta = (PotionMeta) potion.getItemMeta();
    if (tier >= 2 && !splash) {
      meta.setBasePotionData(new PotionData(type, false, true));
    } else {
      meta.setBasePotionData(new PotionData(type, false, false));
    }
    potion.setItemMeta(meta);
    return potion;
  }

  public static void sendSound(Player p, String oldSound, String newSound) {
    if (plugin.is1_9_R1() || plugin.is1_10_R1() || plugin.is1_11_R1() || plugin.is1_12_R1() || plugin.is1_13_R1()) {
      p.playSound(p.getLocation(), Sound.valueOf(oldSound), 1, 1);
    } else {
      p.playSound(p.getLocation(), Sound.valueOf(newSound), 1, 1);
    }
  }

}
