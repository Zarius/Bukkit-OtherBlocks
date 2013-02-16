package com.gmail.zariust.otherdrops.parameters.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.actions.MessageAction.MessageType;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;

public class PotionAction extends Action {
	// "potioneffect: "
	// message.player, message.radius@<r>, message.world, message.server
	public enum PotionEffectActionType {
		ATTACKER, VICTIM, RADIUS, WORLD, SERVER
	}

	static Map<String, PotionEffectActionType> matches = new HashMap<String, PotionEffectActionType>();
	static {
		matches.put("potioneffect", PotionEffectActionType.ATTACKER);
		matches.put("potioneffect.attacker", PotionEffectActionType.ATTACKER);
		matches.put("potioneffect.victim", PotionEffectActionType.VICTIM);
		matches.put("potioneffect.target", PotionEffectActionType.VICTIM);
		matches.put("potioneffect.server", PotionEffectActionType.SERVER);
		matches.put("potioneffect.world", PotionEffectActionType.WORLD);
		matches.put("potioneffect.global", PotionEffectActionType.SERVER);
		matches.put("potioneffect.all", PotionEffectActionType.SERVER);
		matches.put("potioneffects.radius", PotionEffectActionType.RADIUS);


		matches.put("potioneffects", PotionEffectActionType.ATTACKER);
		matches.put("potioneffects.attacker", PotionEffectActionType.ATTACKER);
		matches.put("potioneffects.victim", PotionEffectActionType.VICTIM);
		matches.put("potioneffects.target", PotionEffectActionType.VICTIM);
		matches.put("potioneffects.server", PotionEffectActionType.SERVER);
		matches.put("potioneffects.world", PotionEffectActionType.WORLD);
		matches.put("potioneffects.global", PotionEffectActionType.SERVER);
		matches.put("potioneffects.all", PotionEffectActionType.SERVER);
		matches.put("potioneffects.radius", PotionEffectActionType.RADIUS);
		
	}

	protected PotionEffectActionType potionEffectActionType;
	protected double radius = 10; // default to 10 blocks

	private Collection<PotionEffect> effects = new ArrayList<PotionEffect>();

	public PotionAction(Collection<PotionEffect> effectsList) {
		this.effects = effectsList;
	}

	public PotionAction(Object object, PotionEffectActionType potionEffectType2) {
		potionEffectActionType = potionEffectType2;
		
		if (object instanceof List) {
			List<String> stringList = (List<String>)object;
			for (String effect : stringList) {
				PotionEffect singleEffect = getEffect(effect);
				if (singleEffect != null) effects.add(singleEffect);
			}
		} else if (object instanceof String) {
			PotionEffect singleEffect = getEffect((String)object);
			if (singleEffect != null) effects.add(singleEffect);
		}
	}

	@Override
	public boolean act(CustomDrop drop, OccurredEvent occurence) {
		switch (potionEffectActionType) {
		case ATTACKER:
			if (occurence.getPlayerAttacker() != null & this.effects != null)
				occurence.getPlayerAttacker().addPotionEffects(this.effects);
			return false;
		case VICTIM:
			if (occurence.getPlayerVictim() != null & this.effects != null)
				occurence.getPlayerVictim().addPotionEffects(this.effects);
			else if (occurence.getTarget() instanceof CreatureSubject) {
				Entity ent = ((CreatureSubject)occurence.getTarget()).getEntity();
				if (ent instanceof LivingEntity) {
					((LivingEntity)ent).addPotionEffects(this.effects);
				}
			}
			
			return false;
			
		case RADIUS:
			// occurence.getLocation().getRadiusPlayers()? - how do we get players around radius without an entity?
			Location loc = occurence.getLocation();
			for (Player player : loc.getWorld().getPlayers()) {
				if (player.getLocation().getX() > (loc.getX() - radius) ||
						player.getLocation().getX() < (loc.getX() + radius))
					if (player.getLocation().getY() > (loc.getY() - radius) ||
							player.getLocation().getY() < (loc.getY() + radius))
						if (player.getLocation().getZ() > (loc.getZ() - radius) ||
								player.getLocation().getZ() < (loc.getZ() + radius))
									player.addPotionEffects(this.effects);
			}
			
			break;
		case SERVER:
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				player.addPotionEffects(this.effects);
			}
			break;
		case WORLD:
			for (Player player : occurence.getLocation().getWorld().getPlayers()) {
				player.addPotionEffects(this.effects);
			}
			break;
		}
		
		return false;
	}

	//@Override
	static public List<Action> parse(ConfigurationNode parseMe) {
		List<Action> actions = new ArrayList<Action>();

		for (String key : matches.keySet()) {
			if (parseMe.get(key) != null)
				actions.add(new PotionAction(parseMe.get(key), matches.get(key)));
		}
		
		return actions;
	}

	private static PotionEffect getEffect(String effects) {			
		String[] split = effects.split("@");
		int duration = 100;
		int strength = 5;

		if (split.length > 1)
			duration = Integer.parseInt(split[1]);
		if (split.length > 2)
			strength = Integer.parseInt(split[2]);

		PotionEffectType effect = PotionEffectType.getByName(split[0]);
		if (effect == null) {
			Log.logInfo("PotionEffect: INVALID effect ("+split[0]+")", Verbosity.NORMAL);
			return null;
		}
		Log.logInfo("PotionEffect: adding effect ("+split[0]+", duration: "+duration+", strength: "+strength+")", Verbosity.HIGH);

		// FIXME: parse time and modifier
		return new PotionEffect(effect, duration, strength);
	}

}
