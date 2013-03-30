package com.gmail.zariust.otherdrops.data.itemmeta;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.subject.Target;

public abstract class OdItemMeta {

    public static OdItemMeta parse(String sub, ItemData.ItemMetaType metaType) {
        switch (metaType) {
            case BOOK:
                return OdBookMeta.parse(sub);
            case LEATHER:
                return OdLeatherArmorMeta.parse(sub);
            case SKULL:
                return OdSkullMeta.parse(sub);
            case ENCHANTED_BOOK:
                return OdEnchantedBookMeta.parse(sub);
            case FIREWORK:
                return OdFireworkMeta.parse(sub);
            default:
                break;

        }
        return null;
    }

    public abstract ItemStack setOn(ItemStack stack, Target source);

    public static Color getColorFrom(String sub) {
        Color color = null;
        if (sub.matches("(?i)RICH.*")) {
            if (sub.equalsIgnoreCase("RICHGREEN")) {
                color = Color.GREEN;
            } else if (sub.equalsIgnoreCase("RICHRED")) {
                color = Color.RED;
            } else if (sub.equalsIgnoreCase("RICHBLUE")) {
                color = Color.BLUE;
            } else if (sub.equalsIgnoreCase("RICHYELLOW")) {
                color = Color.YELLOW;
            }
        } else {
            // FIXME: add ability to use Color values too - they are richer/stronger colors
            color = DyeColor.valueOf(sub.toUpperCase()).getColor();
        }
        return color;
    }
    // TODO:
    // add .matches & .parseFromItem to each class
}
