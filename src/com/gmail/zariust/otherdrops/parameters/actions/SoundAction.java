package com.gmail.zariust.otherdrops.parameters.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

public class SoundAction extends Action {
    // "potioneffect: "
    // message.player, message.radius@<r>, message.world, message.server

    public enum SoundLocation {

        ATTACKER, VICTIM, RADIUS, WORLD, SERVER, TOOL
    }
    static Map<String, SoundLocation> matches = new HashMap<String, SoundLocation>();

    static {
        String name = "sound";
        matches.put(name, SoundLocation.VICTIM);  // default for a sound makes sense to be the target
        matches.put(name + ".attacker", SoundLocation.ATTACKER);
        matches.put(name + ".target", SoundLocation.VICTIM);
        matches.put(name + ".victim", SoundLocation.VICTIM);
        matches.put(name + ".server", SoundLocation.SERVER);
        matches.put(name + ".world", SoundLocation.WORLD);
        matches.put(name + ".global", SoundLocation.SERVER);
        matches.put(name + ".all", SoundLocation.SERVER);
        matches.put(name + ".radius", SoundLocation.RADIUS);


        // Can't do tooldamage yet - need a way to damage tools by "1" if a block break
        // event and this condition hasn't run.

        //matches.put("damage.tool", DamageActionType.TOOL);
        //matches.put("damagetool", DamageActionType.TOOL);
    }
    protected SoundLocation damageActionType;
    protected double radius = 10;
    private final List<ODSound> sounds; // this can contain variables, parse at runtime

    private class ODSound {

        public ODSound(Sound sound2, Float volume2, Float pitch2) {
            this.sound = sound2;
            this.volume = volume2;
            this.pitch = pitch2;
        }
        Sound sound;
        Float volume;
        Float pitch;
    }

    public SoundAction(Object object, SoundLocation damageEffectType2) {
        damageActionType = damageEffectType2;
        sounds = new ArrayList<SoundAction.ODSound>();

        if (object instanceof List) {
            // TODO: support lists?
            @SuppressWarnings("unchecked")
            List<Object> stringList = (List<Object>) object;
            for (Object sub : stringList) {
                if (sub instanceof String) {
                    parseValue((String) sub);
                } else if (sub instanceof Integer) {
                    parseValue(String.valueOf(sub));
                }
            }
        } else if (object instanceof String) {
            parseValue((String) object);
        }
    }

    private void parseValue(String sub) {
        Sound sound = null;
        Float pitch = null;
        Float volume = null;

        // split out sound/volume <#v>/pitch <#p>
        String[] split = sub.split("/");
        for (String value : split) {

            if (value.matches("[0-9.]*v")) {
                volume = Float.parseFloat(value.substring(0, value.length() - 1));
            } else if (value.matches("[0-9.]*p")) {
                pitch = Float.parseFloat(value.substring(0, value.length() - 1));
            } else {
                for (Sound loopValue : Sound.values()) {
                    if (CommonMaterial.fuzzyMatchString(value, loopValue.toString())) {
                        Log.logInfo("Matched sound " + loopValue.toString() + " = " + value, Verbosity.HIGHEST);
                        sound = loopValue;
                    }
                }

            }
        }

        sounds.add(new ODSound(sound, volume, pitch));
    }

    @Override
    public boolean act(CustomDrop drop, OccurredEvent occurence) {
        if (sounds != null) {
            for (ODSound key : sounds) {
                process(drop, occurence, key);
            }
        }

        return false;
    }

    private void process(CustomDrop drop, OccurredEvent occurence, ODSound sound) {

        switch (damageActionType) {
            case ATTACKER:
                if (occurence.getPlayerAttacker() != null) {
                    playSound(sound, occurence.getPlayerAttacker().getLocation());
                }
                break;
            case VICTIM:  // and target
                playSound(sound, occurence.getLocation());
                break;
            case RADIUS:
                // occurence.getLocation().getRadiusPlayers()? - how do we get players around radius without an entity?
                Location loc = occurence.getLocation();
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().getX() > (loc.getX() - radius)
                            || player.getLocation().getX() < (loc.getX() + radius)) {
                        if (player.getLocation().getY() > (loc.getY() - radius)
                                || player.getLocation().getY() < (loc.getY() + radius)) {
                            if (player.getLocation().getZ() > (loc.getZ() - radius)
                                    || player.getLocation().getZ() < (loc.getZ() + radius)) {
                                playSound(sound, player.getLocation());
                            }
                        }
                    }
                }

                break;
            case SERVER:
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    playSound(sound, player.getLocation());
                }
                break;
            case WORLD:
                for (Player player : occurence.getLocation().getWorld().getPlayers()) {
                    playSound(sound, player.getLocation());
                }
                break;
            case TOOL:
                // not yet supported, as default damage of 1 needs to be done in the main DropRunner.run() method
                break;
            default:
                break;
        }

    }

    private void playSound(ODSound sound, Location location) {
        float volume = 1, pitch = 1;
        if (sound.volume != null) {
            volume = sound.volume;
        }
        if (sound.pitch != null) {
            pitch = sound.pitch;
        }

        Log.dMsg("Playing sound '" + sound.sound.toString() + "'/" + volume + "/" + pitch + " at location: " + location.toString());

        location.getWorld().playSound(location, sound.sound, volume, pitch);
    }

    //@Override
    @Override
    public List<Action> parse(ConfigurationNode parseMe) {
        List<Action> actions = new ArrayList<Action>();

        for (String key : matches.keySet()) {
            if (parseMe.get(key) != null) {
                actions.add(new SoundAction(parseMe.get(key), matches.get(key)));
            }
        }


        return actions;
    }
}
