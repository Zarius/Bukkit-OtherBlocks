// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Zarius Tularial
// Copyright (C) 2011 Robert Sargant
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

package com.gmail.zariust.bukkit.otherblocks.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.*;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

public class ObBlockListener extends BlockListener
{
	private OtherBlocks parent;

	public ObBlockListener(OtherBlocks instance) {
		parent = instance;
	}

	public Boolean checkWorldguardLeafDecayPermission(Block block) {
		if (OtherBlocks.worldguardPlugin != null) {
			// WORLDGUARD: check to see if leaf decay is allowed...
			// Need to convert the block (it's location) to a WorldGuard Vector
			//Vector pt = com.sk89q.worldguard.bukkit.BukkitUtil.toVector(block); // Don't use this - fails if WorldEdit plugin not installed
			Location loc = block.getLocation();
			Vector pt = new Vector(loc.getX(), loc.getY(), loc.getZ());

			// Get the region manager for this world
			RegionManager regionManager = OtherBlocks.worldguardPlugin.getGlobalRegionManager().get(block.getWorld());
			// Get the "set" for this location
			ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
			// If leaf decay is not allowed, just exit this function
			if (!set.allows(DefaultFlag.LEAF_DECAY)) {
				OtherBlocks.logInfo("Leaf decay denied - worldguard protected region.",4);
				return false;
			}
		}
		OtherBlocks.logInfo("Leaf decay allowed.",4);
		return true;
	}
	
	@Override
	public void onLeavesDecay(LeavesDecayEvent event) {
		if (event.isCancelled()) return;
		if (!parent.config.dropForBlocks) return;
		// TODO: Um, this profiling code should not be here; it's now in SimpleDrop,
		// so leaf decays are being profiled twice
		long startTime = 0; 
		if (parent.config.profiling) startTime = System.currentTimeMillis();
		
		if (!checkWorldguardLeafDecayPermission(event.getBlock())) return;

		OccurredDrop drop = new OccurredDrop(event);
		parent.performDrop(drop);		

		if (parent.config.profiling) {
			OtherBlocks.logInfo("Leafdecay took "+(System.currentTimeMillis()-startTime)+" milliseconds.",4);
			OtherBlocks.plugin.profileMap.get("LEAFDECAY").add(System.currentTimeMillis()-startTime);
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!parent.config.dropForBlocks) return;
		// Again, duplicate profiling code
		Long currentTime = null; 
		if (parent.config.profiling) currentTime = System.currentTimeMillis();

		OccurredDrop drop = new OccurredDrop(event);
		parent.performDrop(drop);
		
		if (currentTime != null) {
			OtherBlocks.logInfo("Blockbreak start: "+currentTime+" end: "+System.currentTimeMillis()+" total: "+(System.currentTimeMillis()-currentTime)+" milliseconds.");
			OtherBlocks.plugin.profileMap.get("BLOCKBREAK").add(System.currentTimeMillis()-currentTime);
		}
	}
	
	@Override
	public void onBlockFromTo(BlockFromToEvent event) { // TODO: Stuff here
/*//temp disabled - not working anyway
		if (event.isCancelled()) return;
		if(event.getBlock().getType() != Material.WATER && event.getBlock().getType() != Material.STATIONARY_WATER)
			return;
		if(event.getToBlock().getType() == Material.AIR) return;

		Block target  = event.getToBlock();
		Integer maxDamage = 0;
		boolean successfulComparison = false;
		boolean doDefaultDrop = false;

		for(OB_Drop obc : parent.transformList) {
			
			if(!obc.compareTo(
					event.getBlock().getType().toString(),
					(short) event.getBlock().getData(),
					"DAMAGE_WATER", 
					target.getWorld(),
					null,
					parent.permissionHandler)) {
				
				continue;
			}

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

			// At this point, the tool and the target block match
			successfulComparison = true;
			if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;
			OtherBlocks.performDrop(target.getLocation(), obc, null);
			maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
		}

		if(successfulComparison && !doDefaultDrop) {

			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);
		}*/
	}
}

