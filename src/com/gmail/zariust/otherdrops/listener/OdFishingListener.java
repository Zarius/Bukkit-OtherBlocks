package com.gmail.zariust.otherdrops.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class OdFishingListener implements Listener {

    private OtherDrops parent;

    public OdFishingListener(OtherDrops instance) {
        parent = instance;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Log.logInfo("Fishing - state: " + event.getState() + ", caught: " + event.getCaught(), Verbosity.EXTREME);
        if (event.getState() == State.CAUGHT_FISH) {
            OccurredEvent drop = new OccurredEvent(event);
            parent.performDrop(drop);
        } else if (event.getState() == State.FAILED_ATTEMPT) {
            OccurredEvent drop = new OccurredEvent(event, "FAILED");
            parent.performDrop(drop);
        }
    }
}
