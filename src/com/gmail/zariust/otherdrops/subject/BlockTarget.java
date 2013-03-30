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
package com.gmail.zariust.otherdrops.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingSand;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.MaterialGroup;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.ContainerData;
import com.gmail.zariust.otherdrops.data.NoteData;
import com.gmail.zariust.otherdrops.data.RecordData;
import com.gmail.zariust.otherdrops.data.SimpleData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.SpawnerData;

public class BlockTarget implements Target {

    private Material id;
    private Data data;
    private Block bl;
    public List<Material> except;

    public BlockTarget() {
        this(null, null);
    }

    public BlockTarget(Material block) {
        this(block, null);  // note: leave as null for "wildcard" to match block with any data
    }

    public BlockTarget(Material block, byte d) {
        this(block, new SimpleData(d));
    }

    public BlockTarget(Material block, int d) {
        this(block, (byte) d);
    }

    public BlockTarget(Block block) {
        this(block == null ? Material.AIR : block.getType(), getData(block));
        bl = block;
    }

    public BlockTarget(Material mat, Data d) { // The Rome constructor
        id = mat;
        if (mat == Material.LEAVES && d != null) {
            d.setData((byte) ((0x3) & d.getData()));
        }
        data = d;
    }

    public BlockTarget(FallingSand what) {
        // TODO: Get the type of falling block rather than assuming it's sand
        this(Material.SAND, 0);
    }

    public BlockTarget(List<Material> except2) {
        this(null, null);
        except = except2;
    }

    private static Data getData(Block block) {
        if (block == null) {
            return new SimpleData();
        }
        switch (block.getType()) {
            case FURNACE:
            case BURNING_FURNACE:
            case DISPENSER:
            case CHEST:
                return new ContainerData(block.getState());
            case MOB_SPAWNER:
                return new SpawnerData(block.getState());
            case NOTE_BLOCK:
                return new NoteData(block.getState());
            case JUKEBOX:
                return new RecordData(block.getState());
            default:
                return new SimpleData(block.getData());
        }
    }

    public Material getMaterial() {
        return id;
    }

    public int getId() {
        return id.getId();
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BlockTarget)) {
            return false;
        }
        BlockTarget targ = (BlockTarget) other;
        return id == targ.id && data.equals(targ.data);
    }

    @Override
    public int hashCode() {
        return new HashCode(this).get(id);
    }

    @Override
    public boolean overrideOn100Percent() {
        return true;
    }

    @Override
    public ItemCategory getType() {
        return ItemCategory.BLOCK;
    }

    @Override
    public boolean matches(Subject block) {
        if (!(block instanceof BlockTarget)) {
            return false;
        }
        BlockTarget targ = (BlockTarget) block;

        Boolean match = false;
        if (id == targ.id) {
            match = true;
        }
        if (data == null) {
            match = true;
        } else {
            match = data.matches(targ.data);
        }
        return match;
    }

    public static Target parse(String name, String state) {
        name = name.toUpperCase();
        state = state.toUpperCase();
        Material mat = null;
        try {
            int id = Integer.parseInt(name);
            // TODO: Need a way to accept non-standard items, but Material is an enum...
            // Waiting on change to Bukkit API.
            mat = Material.getMaterial(id);
        } catch (NumberFormatException x) {
            mat = CommonMaterial.matchMaterial(name);
        }
        if (mat == null) {
            return null;
        }
        if (!mat.isBlock()) {
            // Only a very select few non-blocks are permitted as a target
            if (mat != Material.PAINTING && mat != Material.BOAT && mat != Material.MINECART
                    && mat != Material.POWERED_MINECART && mat != Material.STORAGE_MINECART) {
                return null;
            } else {
                return VehicleTarget.parse(mat, state);
            }
        }
        try {
            int val = Integer.parseInt(state);
            return new BlockTarget(mat, val);
        } catch (NumberFormatException e) {
        }
        Data data = null;
        try {
            data = SimpleData.parse(mat, state);
        } catch (IllegalArgumentException e) {
            Log.logWarning(e.getMessage());
            return null;
        }
        if (data != null) {
            return new BlockTarget(mat, data);
        }
        return new BlockTarget(mat);
    }

    @Override
    public String toString() {
        if (id == null) {
            return "ANY_BLOCK";
        }
        if (data == null) {
            return id.toString();
        }
        return id + "@" + data.get(id);
    }

    @Override
    public List<Target> canMatch() {
        if (id == null) {
            return new BlocksTarget(MaterialGroup.ANY_BLOCK).canMatch();
        }
        return Collections.singletonList((Target) this);
    }

    @Override
    public String getKey() {
        return id.toString();
    }

    @Override
    public void setTo(BlockTarget replacement) {
        bl.setType(replacement.getMaterial());
        BlockState state = bl.getState();
        if (replacement.data != null) {
            replacement.data.setOn(state);
        }
        state.update(true);
    }

    @Override
    public Location getLocation() {
        if (bl != null) {
            return bl.getLocation();
        }
        return null;
    }

    public Block getBlock() {
        return bl;
    }

    @Override
    public String getReadableName() {
        String readableName = bl.getType().toString().toLowerCase().replaceAll("[-_]", " ");
        return readableName;
    }
}
