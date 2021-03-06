package com.projectkorra.projectkorra.firebending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;

public class Illumination implements ConfigLoadable {
	
	public static ConcurrentHashMap<Player, Illumination> instances = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Block, Player> blocks = new ConcurrentHashMap<Block, Player>();

	private static int range = config.get().getInt("Abilities.Fire.Illumination.Range");

	private Player player;
	private Block block;
	private Material normaltype;
	private byte normaldata;

	public Illumination(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("Illumination"))
			return;
		/* End Initial Checks */

		if (instances.containsKey(player)) {
			instances.get(player).remove();
		} else {
			// reloadVariables();
			this.player = player;
			set();
			instances.put(player, this);
			bPlayer.addCooldown("Illumination", GeneralMethods.getGlobalCooldown());
		}
	}

	public static String getDescription() {
		return "This ability gives firebenders a means of illuminating the area. It is a toggle - clicking "
				+ "will create a torch that follows you around. The torch will only appear on objects that are "
				+ "ignitable and can hold a torch (e.g. not leaves or ice). If you get too far away from the torch, "
				+ "it will disappear, but will reappear when you get on another ignitable block. Clicking again "
				+ "dismisses this torch.";
	}

	public static void revert(Block block) {
		Player player = blocks.get(block);
		instances.get(player).revert();
	}

	// public static void manage(Server server) {
	// for (Player player : server.getOnlinePlayers()) {
	// if (instances.containsKey(player)) {
	// if (!GeneralMethods.canBend(player.getName(), "Illumination")) {
	// instances.get(player).revert();
	// instances.remove(player);
	// } else {
	// instances.get(player).set();
	// }
	// }
	// }
	//
	// for (Player player : instances.keySet()) {
	// if (!player.isOnline() || player.isDead()) {
	// instances.get(player).revert();
	// instances.remove(player);
	// }
	// }
	// }

	public boolean progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return false;
		}
		if (!GeneralMethods.canBend(player.getName(), "Illumination")) {
			remove();
			return false;
		} else {
			set();
		}
		return true;
	}

	public static void progressAll() {
		for (Illumination ability : instances.values()) {
			ability.progress();
		}
	}

	@Override
	public void reloadVariables() {
		range = config.get().getInt("Abilities.Fire.Illumination.Range");
	}

	public void remove() {
		revert();
		instances.remove(player);
	}

	public static void removeAll() {
		for (Illumination ability : instances.values()) {
			ability.remove();
		}
	}

	@SuppressWarnings("deprecation")
	private void revert() {
		if (block != null) {
			blocks.remove(block);
			block.setType(normaltype);
			block.setData(normaldata);
		}
	}

	@SuppressWarnings("deprecation")
	private void set() {
		Block standingblock = player.getLocation().getBlock();
		Block standblock = standingblock.getRelative(BlockFace.DOWN);
		if (standblock.getType() == Material.GLOWSTONE) {
			revert();
		} else if ((FireStream.isIgnitable(player, standingblock) && standblock.getType() != Material.LEAVES && standblock
				.getType() != Material.LEAVES_2) && block == null && !blocks.containsKey(standblock)) {
			block = standingblock;
			normaltype = block.getType();
			normaldata = block.getData();
			block.setType(Material.TORCH);
			blocks.put(block, player);
		} else if ((FireStream.isIgnitable(player, standingblock) && standblock.getType() != Material.LEAVES && standblock
				.getType() != Material.LEAVES_2)
				&& !block.equals(standblock)
				&& !blocks.containsKey(standblock)
				&& GeneralMethods.isSolid(standblock)) {
			revert();
			block = standingblock;
			normaltype = block.getType();
			normaldata = block.getData();
			block.setType(Material.TORCH);
			blocks.put(block, player);
		} else if (block == null) {
			return;
		} else if (!player.getWorld().equals(block.getWorld())) {
			revert();
		} else if (player.getLocation().distance(block.getLocation()) > FireMethods.getFirebendingDayAugment(range,
				player.getWorld())) {
			revert();
		}
	}

}
