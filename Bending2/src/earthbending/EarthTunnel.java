package earthbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tools.Tools;

public class EarthTunnel {

	public static ConcurrentHashMap<Player, EarthTunnel> instances = new ConcurrentHashMap<Player, EarthTunnel>();

	private static final double maxradius = 1;
	private static final double range = 10;
	private static final double radiusinc = 0.25;
	// private static final double speed = 10;

	private static final long interval = 30;

	private Player player;
	private Block block;
	private Location origin, location;
	private Vector direction;
	private double depth, radius, angle;
	private long time;

	public EarthTunnel(Player player) {
		this.player = player;
		location = player.getEyeLocation().clone();
		origin = player.getTargetBlock(null, (int) range).getLocation();
		block = origin.getBlock();
		direction = location.getDirection().clone().normalize();
		depth = origin.distance(location) - 1;
		if (depth < 0)
			depth = 0;
		angle = 0;
		radius = radiusinc;
		// ortho = new Vector(direction.getY(), -direction.getX(),
		// 0).normalize();
		// Tools.verbose(ortho.clone().dot(direction));
		time = System.currentTimeMillis();

		instances.put(player, this);
	}

	public boolean progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			// Tools.verbose("progressing");
			if (/*
				 * !player.getEyeLocation().equals(location) ||
				 */!player.isSneaking()) {
				instances.remove(player);
				return false;
			} else {
				while (!Tools.isEarthbendable(block)) {
					// Tools.verbose("going");
					if (!Tools.isTransparentToEarthbending(block)) {
						// Tools.verbose("false! at" + angle + " " + radius +
						// " "
						// + depth);
						instances.remove(player);
						return false;
					}
					if (angle >= 360) {
						angle = 0;
						if (radius >= maxradius) {
							radius = radiusinc;
							if (depth >= range) {
								instances.remove(player);
								return false;
							} else {
								depth += .5;
							}
						} else {
							radius += radiusinc;
						}
					} else {
						angle += 20;
					}
					// block.setType(Material.GLASS);
					Vector vec = Tools.getOrthogonalVector(direction, angle,
							radius);
					block = location.clone()
							.add(direction.clone().normalize().multiply(depth))
							.add(vec).getBlock();
				}

				block.breakNaturally();

				return true;
			}
		} else {
			return false;
		}
	}

	public static boolean progress(Player player) {
		return instances.get(player).progress();
	}

}
