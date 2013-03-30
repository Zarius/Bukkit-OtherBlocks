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
package com.gmail.zariust.otherdrops.drop;

import static com.gmail.zariust.common.Verbosity.EXTREME;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

public class CreatureDrop extends DropType {

    private final EntityType type;
    private Data data;
    private final IntRange quantity;
    private int rolledQuantity;
    private CreatureDrop passenger;

    public CreatureDrop(EntityType mob) {
        this(new IntRange(1), mob, 0);
    }

    public CreatureDrop(EntityType mob, double percent) {
        this(new IntRange(1), mob, 0, percent);
    }

    public CreatureDrop(IntRange amount, EntityType mob) {
        this(amount, mob, 0);
    }

    public CreatureDrop(IntRange amount, EntityType mob, double percent) {
        this(amount, mob, 0, percent);
    }

    public CreatureDrop(EntityType mob, int mobData) {
        this(new IntRange(1), mob, mobData);
    }

    public CreatureDrop(EntityType mob, int mobData, double percent) {
        this(new IntRange(1), mob, mobData, percent);
    }

    public CreatureDrop(IntRange amount, EntityType mob, int mobData) {
        this(amount, mob, mobData, 100.0);
    }

    public CreatureDrop(IntRange amount, EntityType mob, int mobData, double percent) {
        this(amount, mob, new CreatureData(mobData), percent, null);
    }

    public CreatureDrop(EntityType mob, Data mobData) {
        this(new IntRange(1), mob, mobData);
    }

    public CreatureDrop(EntityType mob, Data mobData, double percent) {
        this(new IntRange(1), mob, mobData, percent, null);
    }

    public CreatureDrop(IntRange amount, EntityType mob, Data mobData) {
        this(amount, mob, mobData, 100.0, null);
    }

    public CreatureDrop(IntRange amount, EntityType mob, Data mobData, double percent, CreatureDrop passenger) { // Rome
        super(DropCategory.CREATURE, percent);
        type = mob;
        data = mobData;
        quantity = amount;
        this.setPassenger(passenger);
    }

    public EntityType getCreature() {
        return type;
    }

    public int getCreatureData() {
        return data.getData();
    }

    @Override
    protected DropResult performDrop(Target source, Location where, DropFlags flags) {
        DropResult dropResult = DropResult.fromOverride(this.overrideDefault);
        rolledQuantity = quantity.getRandomIn(flags.rng);
        int amount = rolledQuantity;
        while (amount-- > 0) {
            dropResult.addWithoutOverride(dropCreatureWithRider(where, flags.recipient, type, data, this.getPassenger(), null, flags.getEvent()));
        }
        return dropResult;
    }

    public static DropType parse(String drop, String state, IntRange amount, double chance) {
        CreatureDrop passenger = null;

        if (!drop.startsWith("^")) {
            String[] passengerSplit = drop.split("\\^", 2);
            drop = passengerSplit[0];
            if (passengerSplit.length > 1) {
                passenger = (CreatureDrop) CreatureDrop.parse(passengerSplit[1], "", new IntRange(1), 100.0);
            }
        }

        drop = drop.replace("(?i)(CREATURE_|MOB_)", "");

        String[] split = null;
        if (drop.matches("\\w+:.*")) {
            split = drop.split(":", 2);
        } else {
            split = drop.split("@", 2);
        }
        if (split.length > 1) {
            state = split[1];
        }
        String name = split[0].toUpperCase();
        // TODO: Is there a way to detect non-vanilla creatures?
        EntityType creature = CommonEntity.getCreatureEntityType(name.split("@")[0]);
//		EntityType creature = enumValue(EntityType.class, name);
        // Log the name being parsed rather than creature.toString() to avoid NullPointerException
        Log.logInfo("Parsing the creature drop... creature=" + name, EXTREME);
        if (creature == null) {
            if (name.startsWith("^")) {
                name = name.substring(1);
                CreatureGroup group = CreatureGroup.get(name);
                if (group == null) {
                    return null;
                }
                return new DropListInclusive(group.creatures(), amount, chance);
            } else {
                CreatureGroup group = CreatureGroup.get(name);
                if (group == null) {
                    return null;
                }
                return new DropListExclusive(group.creatures(), amount, chance);
            }
        }
        Data data = CreatureData.parse(creature, state);
        Log.logInfo("Parsing the creature drop... creature=" + creature.toString() + " data=" + data.toString(), EXTREME);
        return new CreatureDrop(amount, creature, data, chance, passenger);
    }

    @Override
    public String getName() {
        String ret = "CREATURE_" + type;
        // TODO: Will data ever be null, or will it just be 0?
        if (data != null) {
            ret += "@" + data.get(type);
        }
        return ret;
    }

    @Override
    public double getAmount() {
        return rolledQuantity;
    }

    @Override
    public DoubleRange getAmountRange() {
        return quantity.toDoubleRange();
    }

    public CreatureDrop getPassenger() {
        return passenger;
    }

    public void setPassenger(CreatureDrop passenger) {
        this.passenger = passenger;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
