 package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.airbending.AirMethods;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class HealingWaters {

	private static final boolean shift = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.HealingWaters.ShiftRequired");
	private static final double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.HealingWaters.Radius");
	private static final long interval = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.HealingWaters.Interval");
	private static final int power = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.HealingWaters.Power");

	private static long time = 0;

	public static void heal(Server server) {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			for (Player player : server.getOnlinePlayers()) {
				if (GeneralMethods.getBoundAbility(player) != null) {
					if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("HealingWaters") && GeneralMethods.canBend(player.getName(), "HealingWaters")) {
						heal(player);
					}
				}
			}
		}
	}

	private static void heal(Player player) {
		if (inWater(player)) {
			Entity entity = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (entity instanceof LivingEntity && inWater(entity) && player.isSneaking()) {
				giveHPToEntity((LivingEntity) entity);
			} else if ((player.isSneaking() && shift) || !shift){
				giveHP(player);
			}
		}
	}


	private static void giveHPToEntity(LivingEntity le) {
		if (!le.isDead() && le.getHealth() < le.getMaxHealth()) {
			applyHealingToEntity(le);
		}
		for (PotionEffect effect : le.getActivePotionEffects()) {
			if (WaterMethods.isNegativeEffect(effect.getType())) {
				le.removePotionEffect(effect.getType());
			}
		}
	}

	private static void giveHP(Player player) {
		if (!player.isDead() && player.getHealth() < 20) {
			applyHealing(player);
		}
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (WaterMethods.isNegativeEffect(effect.getType())) {
				if ((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.blinded.containsKey(player.getName())) {
					return;
				}
				player.removePotionEffect(effect.getType());
			}
		}
	}

	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
		if (WaterMethods.isWater(block) && !TempBlock.isTempBlock(block))
			return true;
		return false;
	}

	private static void applyHealing(Player player) {
		if (!GeneralMethods.isRegionProtectedFromBuild(player, "HealingWaters", player.getLocation()))
			if (player.getHealth() < player.getMaxHealth()) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, power));
				AirMethods.breakBreathbendingHold(player);
			}
		//			for(PotionEffect effect : player.getActivePotionEffects()) {
		//				if(Methods.isNegativeEffect(effect.getType())) {
		//					player.removePotionEffect(effect.getType());
		//				}
		//			}
	}

	private static void applyHealingToEntity(LivingEntity le) {
		if (le.getHealth() < le.getMaxHealth()) {
			le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1));
			AirMethods.breakBreathbendingHold(le);
		}
		//		for(PotionEffect effect : le.getActivePotionEffects()) {
		//			if(Methods.isNegativeEffect(effect.getType())) {
		//				le.removePotionEffect(effect.getType());
		//			}
		//		}
	}

	public static String getDescription() {
		return "To use, the bender must be at least partially submerged in water. " + "If the user is not sneaking, this ability will automatically begin " + "working provided the user has it selected. If the user is sneaking, " + "he/she is channeling the healing to their target in front of them. " + "In order for this channel to be successful, the user and the target must " + "be at least partially submerged in water. This ability will heal the user or target, and it will also remove any negative potion effects the user or target has.";
	}
}
