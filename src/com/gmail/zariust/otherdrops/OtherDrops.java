// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.
package com.gmail.zariust.otherdrops;

import static com.gmail.zariust.common.Verbosity.EXTREME;
import static com.gmail.zariust.common.Verbosity.HIGH;
import static com.gmail.zariust.common.Verbosity.HIGHEST;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.DropRunner;
import com.gmail.zariust.otherdrops.event.DropsList;
import com.gmail.zariust.otherdrops.event.GroupDropEvent;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.event.SimpleDrop;
import com.gmail.zariust.otherdrops.listener.OdBlockListener;
import com.gmail.zariust.otherdrops.listener.OdEntityListener;
import com.gmail.zariust.otherdrops.listener.OdFishingListener;
import com.gmail.zariust.otherdrops.listener.OdPlayerJoinListener;
import com.gmail.zariust.otherdrops.listener.OdPlayerListener;
import com.gmail.zariust.otherdrops.listener.OdPlayerRespawnListener;
import com.gmail.zariust.otherdrops.listener.OdRedstoneListener;
import com.gmail.zariust.otherdrops.listener.OdSpawnListener;
import com.gmail.zariust.otherdrops.listener.OdVehicleListener;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Subject.ItemCategory;

public class OtherDrops extends JavaPlugin {

    public static OtherDrops plugin;
    boolean enabled;
    public PluginDescriptionFile info = null;
    static String pluginName;
    static String pluginVersion;
    static Logger log = Logger.getLogger("Minecraft");
    // Global random number generator - used throughout the whole plugin
    public static Random rng = new Random();
    // Config stuff
    public OtherDropsConfig config = null;
    protected boolean enableBlockTo;
    protected boolean disableEntityDrops;

    public OtherDrops() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Set plugin name & version, this must be at the start of onEnable
        // Used in log messages throughout
        pluginName = this.getDescription().getName();
        pluginVersion = this.getDescription().getVersion();

        com.gmail.zariust.otherdrops.parameters.actions.Action.registerDefaultActions();

        // Create the data folder (if not there already) and load the config
        getDataFolder().mkdirs();
        config = new OtherDropsConfig(this);
        config.load(); // load global config, dependencies then scan drops file

        this.getCommand("od").setExecutor(new OtherDropsCommand(this));

        Log.logInfo("OtherDrops loaded.");

    }

    @Override
    public void onDisable() {
        log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
    }

    public static void enableOtherDrops() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        String registered = "Loaded listeners: ";

        if (OtherDropsConfig.dropForBlocks) {
            registered += "Block, ";
            pm.registerEvents(new OdBlockListener(plugin), plugin);
            //registered += "PistonListener, ";
            //pm.registerEvents(new OdPistonListener(plugin), plugin);

        }
        if (OtherDropsConfig.dropForCreatures) {
            registered += "Entity, ";
            pm.registerEvents(new OdEntityListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForClick) {
            registered += "Player (left/rightclick), ";
            pm.registerEvents(new OdPlayerListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForFishing) {
            registered += "Fishing, ";
            pm.registerEvents(new OdFishingListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForSpawned) {
            registered += "MobSpawn, ";
            pm.registerEvents(new OdSpawnListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForRedstoneTrigger) {
            registered += "Redstone, ";
            pm.registerEvents(new OdRedstoneListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForPlayerJoin) {
            registered += "PlayerJoin, ";
            pm.registerEvents(new OdPlayerJoinListener(plugin), plugin);
        }
        if (OtherDropsConfig.dropForPlayerRespawn) {
            registered += "PlayerRespawn, ";
            pm.registerEvents(new OdPlayerRespawnListener(plugin), plugin);
        }
        registered += "Vechicle.";
        pm.registerEvents(new OdVehicleListener(plugin), plugin);

        // BlockTo seems to trigger quite often, leaving off unless explicitly enabled for now
        if (OtherDropsConfig.enableBlockTo) {
            //pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, config.priority, this);
        }

        plugin.enabled = true;
        Log.logInfo("Register listeners: " + registered, Verbosity.HIGH);
    }

    public static void disableOtherDrops() {
        HandlerList.unregisterAll(plugin);
        plugin.enabled = false;
    }

    /**
     * Matches an actual drop against the configuration and runs any configured
     * drops that are found.
     *
     * @param occurence The actual drop.
     */
    public void performDrop(OccurredEvent occurence) {
        DropsList customDrops = config.blocksHash.getList(occurence.getAction(), occurence.getTarget());
        if (customDrops == null) {
            if (OtherDropsConfig.verbosity.exceeds(HIGH)) { // check verbosity outside logInfo so that "toString()" functions are processed otherwise
                // set spawn event log message to extreme as otherwise too common
                if (occurence.getEvent() instanceof CreatureSpawnEvent) {
                    Log.logInfo("PerformDrop (" + (occurence.getAction() == null ? "" : occurence.getAction().toString()) + ", " + (occurence.getTarget() == null ? "" : occurence.getTarget().toString()) + " w/ " + (occurence.getTool() == null ? "" : occurence.getTool().toString()) + ") no potential drops found", EXTREME);
                } else {
                    Log.logInfo("PerformDrop (" + (occurence.getAction() == null ? "" : occurence.getAction().toString()) + ", " + (occurence.getTarget() == null ? "" : occurence.getTarget().toString()) + " w/ " + (occurence.getTool() == null ? "" : occurence.getTool().toString()) + ") no potential drops found", HIGHEST);
                }
            }
            return;  // TODO: if no drops, just return - is this right?
        }
        // TODO: return a list of drops found? difficult due to multi-classes?
        if (OtherDropsConfig.verbosity.exceeds(HIGH)) {
            Log.logInfo("PerformDrop - potential drops found: " + customDrops.toString() + " tool: " + (occurence.getTool() == null ? "" : occurence.getTool().toString()), HIGH);
        }

        // check if block is excepted (for any)
        for (CustomDrop drop : customDrops) {
            if (drop.getTarget() instanceof BlockTarget) {
                BlockTarget any = (BlockTarget) drop.getTarget();
                if (any.except != null) {
                    Material compareTo = null;
                    if (occurence.getEvent() instanceof BlockBreakEvent) {
                        compareTo = ((BlockBreakEvent) occurence.getEvent()).getBlock().getType();
                    } else if (occurence.getEvent() instanceof PlayerInteractEvent) {
                        compareTo = null;
                        PlayerInteractEvent pie = (PlayerInteractEvent) occurence.getEvent();
                        if (pie.getPlayer() != null) {
                            compareTo = pie.getPlayer().getItemInHand().getType();
                        }
                    }

                    if (any.except.contains(compareTo)) {
                        return;
                    }
                }
            }
        }

        // Loop through the drops and check for a match, process uniques, etc	
        List<SimpleDrop> scheduledDrops = gatherDrops(customDrops, occurence);
        if (OtherDropsConfig.verbosity.exceeds(HIGHEST)) {
            Log.logInfo("PerformDrop: scheduled drops=" + scheduledDrops.toString(), HIGHEST);
        }

        // check for any DEFAULT drops
        boolean defaultDrop = false;
        int dropCount = 0;
        for (SimpleDrop simpleDrop : scheduledDrops) {
            if (simpleDrop.getDropped() != null) //if (!simpleDrop.getDropped().toString().equalsIgnoreCase("AIR")) // skip drops that don't actually drop anything
            {
                dropCount++;
            }
            if (simpleDrop.isDefault()) {
                defaultDrop = true;
                occurence.setOverrideDefault(false); // DEFAULT drop
            }
            if (simpleDrop.getDropped() != null && simpleDrop.getDropped().toString().equalsIgnoreCase("AIR")) {
                occurence.setOverrideDefault(true); // NOTHING drop
            }
        }


        for (SimpleDrop simpleDrop : scheduledDrops) {
            Log.logInfo("PerformDrop: scheduling " + simpleDrop.getDropName(), HIGH);
            scheduleDrop(occurence, simpleDrop, defaultDrop);
        }

        if (occurence.isOverrideEquipment() && occurence.getRealEvent() instanceof EntityDeathEvent) {
            EntityDeathEvent evt = (EntityDeathEvent) occurence.getRealEvent();
            if (!(evt.getEntity() instanceof Player)) {
                clearMobEquipment(evt.getEntity());
            }
        }
        // Cancel event, if applicable
        if (occurence.isOverrideDefault() && !defaultDrop) {
            clearDrops(occurence, dropCount);
        } else {
            occurence.setCancelled(false);
        }
        if (occurence.getRealEvent() != null) {
            if (occurence.getRealEvent() instanceof EntityDeathEvent) {
                EntityDeathEvent evt = (EntityDeathEvent) occurence.getRealEvent();
                if (occurence.isOverrideDefaultXp()) {
                    Log.logInfo("PerformDrop: entitydeath - isOverrideDefaultXP=true, clearing xp drop.", HIGH);
                    evt.setDroppedExp(0);
                }
            }
        }

        if (occurence.getReplaceBlockWith() != null) {
            occurence.getTarget().setTo(occurence.getReplaceBlockWith());
        }

        if (occurence.isDenied()) {
            occurence.setCancelled(true);
        }

        // Make sure explosion events are not cancelled (as this will cancel the whole explosion
        // Individual blocks are prevented (if DENY is set) in the entity listener
        if (occurence.getEvent() instanceof EntityExplodeEvent) {
            occurence.setCancelled(false);
        }
        Log.logInfo("PerformDrop: finished. defaultdrop=" + defaultDrop + " dropcount=" + dropCount + " cancelled=" + occurence.isCancelled() + " denied=" + occurence.isDenied(), HIGH);
    }

    /**
     * @param occurence
     * @param dropCount
     */
    private void clearDrops(OccurredEvent occurence, int dropCount) {
        if (occurence.getEvent() instanceof LeavesDecayEvent) {
            occurence.setCancelled(true);
            ((LeavesDecayEvent) occurence.getEvent()).getBlock().setType(Material.AIR);
            return;
        }

        if (occurence.getEvent() instanceof BlockBreakEvent || occurence.getEvent() instanceof PlayerFishEvent) {
            if (occurence.getTool().getType() != ItemCategory.EXPLOSION) {

                Log.logInfo("PerformDrop: blockbreak or fishing - not default drop - cancelling event (dropcount=" + dropCount + ").", HIGH);
                occurence.setCancelled(true);
                // Process action through logging plugins, if any - this is only because we generally cancel the break event
                if (occurence.getTarget() instanceof BlockTarget && occurence.getAction() == Action.BREAK) {
                    Block block = occurence.getLocation().getBlock();
                    String playerName = "(unknown)";
                    if (occurence.getTool() instanceof PlayerSubject) {
                        playerName = ((PlayerSubject) occurence.getTool()).getPlayer().getName();
                    }
                    Dependencies.queueBlockBreak(playerName, block);
                }
            }
        } else if (occurence.getRealEvent() != null) {
            if (occurence.getRealEvent() instanceof EntityDeathEvent) {
                EntityDeathEvent evt = (EntityDeathEvent) occurence.getRealEvent();
                if ((evt.getEntity() instanceof Player) && !(occurence.isDenied())) {
                    Log.logInfo("Player death - not clearing.");
                } else {
                    Log.logInfo("PerformDrop: entitydeath - clearing drops.", HIGHEST);
                    evt.getDrops().clear();
                    if (!(evt.getEntity() instanceof Player)) {
                        clearMobEquipment(evt.getEntity());

                        // and if denied just remove the entity to stop animation (as we cannot cancel the event)
                        if (occurence.isDenied()) {
                            evt.getEntity().remove();
                        }
                    }
                }
                if (OtherDropsConfig.disableXpOnNonDefault) {
                    Log.logInfo("PerformDrop: entitydeath - no default drop, clearing xp drop.", HIGH);
                    evt.setDroppedExp(0);
                }
            }
        }
    }

    private void clearMobEquipment(LivingEntity entity) {
        EntityEquipment eq = entity.getEquipment();
        if (eq != null) {
            eq.setHelmetDropChance(0);
            eq.setChestplateDropChance(0);
            eq.setLeggingsDropChance(0);
            eq.setBootsDropChance(0);
            eq.setItemInHandDropChance(0);
        }

    }

    // For testing only, so far
    public void dropCreatureEquipment(LivingEntity le) {
        //Log.dMsg(String.valueOf(le.getEquipment().getBootsDropChance()));	
        //Log.dMsg(le.getEquipment().toString());

        /*
         if (le.getEquipment().getBoots() != null) {
         if OtherDrops.rng.nextFloat() >
         }
         DropType.parse(le.getEquipment().getBoots()+"/"+le.getEquipment().getBootsDropChance(), "");
         Log.dMsg(String.valueOf(le.getEquipment().getBootsDropChance()));	
         Log.dMsg(le.getEquipment().toString());*/
    }

    private List<SimpleDrop> gatherDrops(DropsList customDrops, OccurredEvent occurence) {
//		OtherDrops.logInfo("Gatherdrops start.", HIGHEST);

        List<CustomDrop> matchedDrops = new ArrayList<CustomDrop>(); // rename to matchedDrops
        List<CustomDrop> uniqueList = new ArrayList<CustomDrop>();

        // First, loop through all drops and gather successful & unique ones into two lists
        // Note: since we don't know if this drop will be cleared by uniques, don't do any events in here
        for (CustomDrop customDrop : customDrops) {
            if (customDrop instanceof GroupDropEvent) {
                GroupDropEvent groupCustomDrop = (GroupDropEvent) customDrop;
                if (groupCustomDrop.matches(occurence)) { // FIXME: include chance check at top of matches                        
                    //OtherDrops.logInfo("PerformDrop: found group ("+groupCustomDrop.getGroupsString()+")", HIGHEST);
                    matchedDrops.add(groupCustomDrop);
                    if (!groupCustomDrop.getFlagState().continueDropping) {  // This means a unique flag found
                        //OtherDrops.logInfo("PerformDrop: group ("+groupCustomDrop.getName()+") is UNIQUE.", HIGHEST);
                        uniqueList.add(groupCustomDrop);
                    }

                } else {
                    //OtherDrops.logInfo("PerformDrop: Dropgroup ("+groupCustomDrop.getLogMessage()+") did not match ("+occurence.getLogMessage()+").", HIGHEST);
                    continue;
                }
            } else { // SimpleDrop - so add to a list
                if (customDrop.matches(occurence)) {
                    matchedDrops.add(customDrop);
                    if (!customDrop.getFlagState().continueDropping) {  // This means a unique flag found
                        uniqueList.add(customDrop);
                    }
                } else {
                    //OtherDrops.logInfo("PerformDrop: Drop ("+occurence.getLogMessage()+") did not match ("+customDrop.getLogMessage()+").", HIGHEST);
                }
            }
        }

        // If there were unique, pick a random one and clear the rest
        if (!uniqueList.isEmpty()) {
            matchedDrops.clear();
            matchedDrops.add(getSingleRandomUnique(uniqueList));
        }


        // Loop through what's left and check for groups that need to be recursed into, otherwise add to final list and return
        List<SimpleDrop> finalDrops = new ArrayList<SimpleDrop>();
        for (CustomDrop customDrop : matchedDrops) {
            if (customDrop instanceof GroupDropEvent) {
                GroupDropEvent groupCustomDrop = (GroupDropEvent) customDrop;
                // Process dropGroup events here...
                // Display dropgroup "message:"
                String message = MessageAction.getRandomMessage(customDrop, occurence, customDrop.getMessages());
                if (message != null && (!message.isEmpty()) && (occurence.getTool() instanceof PlayerSubject)) {
                    ((PlayerSubject) occurence.getTool()).getPlayer().sendMessage(message);
                }

                finalDrops.addAll(gatherDrops(groupCustomDrop.getDrops(), occurence));
            } else {
                //OtherDrops.logInfo("PerformDrop: adding " + customDrop.getDropName(), HIGHEST);
                finalDrops.add((SimpleDrop) customDrop);
            }
        }

        //OtherDrops.logInfo("Gatherdrops end... finaldrops: "+finalDrops.toString(), HIGHEST);
        return finalDrops;

    }

    public CustomDrop getSingleRandomUnique(List<CustomDrop> uniqueList) {
        CustomDrop random = uniqueList.get(rng.nextInt(uniqueList.size()));
        Log.logInfo("PerformDrop: getunique, selecting: " + random.getDropName(), HIGHEST);
        return random;
    }

    public void scheduleDrop(OccurredEvent evt, CustomDrop customDrop, boolean defaultDrop) {

        int schedule = customDrop.getRandomDelay();
//		if(schedule > 0.0) Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OtherDrops.plugin, this, schedule);
        //	else run();


        Location playerLoc = null;
        Player player = null; // FIXME: need to get player early - in event
        //if (evt.player != null) playerLoc = player.getLocation();
        DropRunner dropRunner = new DropRunner(OtherDrops.plugin, evt, customDrop, player, playerLoc, defaultDrop);

        // schedule the task - NOTE: this must be a sync task due to the changes made in the performActualDrop function
        if (schedule > 0.0) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OtherDrops.plugin, dropRunner, schedule);
        } else {
            dropRunner.run();
        }
        //}
    }

    public List<String> getGroups(Player player) {
        List<String> foundGroups = new ArrayList<String>();
        Set<PermissionAttachmentInfo> permissions = player.getEffectivePermissions();
        for (PermissionAttachmentInfo perm : permissions) {
            String groupPerm = perm.getPermission();
            if (groupPerm.startsWith("group.")) {
                foundGroups.add(groupPerm.substring(6));
            } else if (groupPerm.startsWith("groups.")) {
                foundGroups.add(groupPerm.substring(7));
            }
        }
        return foundGroups;
    }

    public static boolean inGroup(Player agent, String group) {
        return agent.hasPermission("group." + group) || agent.hasPermission("groups." + group);
    }

    // TODO: This is only for temporary debug purposes.
    public static void stackTrace() {
        if (OtherDropsConfig.verbosity.exceeds(EXTREME)) {
            Thread.dumpStack();
        }
    }
}
