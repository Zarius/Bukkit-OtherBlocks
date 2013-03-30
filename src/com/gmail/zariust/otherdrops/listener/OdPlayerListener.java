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
package com.gmail.zariust.otherdrops.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.GameMode;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class OdPlayerListener implements Listener {

    private OtherDrops parent;

    public OdPlayerListener(OtherDrops instance) {
        parent = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getClickedBlock() == null) {
            Log.logWarning("onPlayerInteract: getClickedBlock() is null, skipping. Player=" + event.getPlayer().getName(), Verbosity.HIGH);
            return;
        }
        if (event.getPlayer() != null) {
            if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                // skip drops for creative mode - TODO: make this configurable?
            } else {
                OccurredEvent drop = new OccurredEvent(event);
                parent.performDrop(drop);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getPlayer() != null) {
            if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                // skip drops for creative mode - TODO: make this configurable?
            } else {
                OccurredEvent drop = new OccurredEvent(event);
                parent.performDrop(drop);
            }
        }
    }
}
