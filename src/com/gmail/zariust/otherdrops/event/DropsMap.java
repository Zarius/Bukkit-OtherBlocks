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
package com.gmail.zariust.otherdrops.event;

import java.util.HashMap;
import java.util.Map;

import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.Target;

public class DropsMap {

    private Map<Action, Map<String, DropsList>> blocksHash = new HashMap<Action, Map<String, DropsList>>();

    public void addDrop(CustomDrop drop) {
        if (!blocksHash.containsKey(drop.getAction())) {
            blocksHash.put(drop.getAction(), new HashMap<String, DropsList>());
        }
        Map<String, DropsList> actionHash = blocksHash.get(drop.getAction());
        for (Target target : drop.getTarget().canMatch()) {
            String key = target.getKey();
            if (key == null) {
                continue; // shouldn't happen though...?
            }
            if (!actionHash.containsKey(key)) {
                actionHash.put(key, new DropsList());
            }
            DropsList drops = actionHash.get(key);
            drops.add(drop);
        }
    }

    public DropsList getList(Action action, Target target) {
        if (!blocksHash.containsKey(action)) {
            return null;
        }
        if (target == null) {
            return null;
        }
        return blocksHash.get(action).get(target.getKey());
    }

    public void clear() {
        blocksHash.clear();
    }

    @Override
    public String toString() {
        return blocksHash.toString();
    }

    @Override
    public int hashCode() {
        return blocksHash.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DropsMap)) {
            return false;
        }
        return blocksHash.equals(((DropsMap) other).blocksHash);
    }

    public void applySorting() {
        for (Action action : blocksHash.keySet()) {
            for (DropsList list : blocksHash.get(action).values()) {
                list.sort();
            }
        }
    }
}
