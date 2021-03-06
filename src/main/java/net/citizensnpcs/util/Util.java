package net.citizensnpcs.util;

import java.util.Random;

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_4_5.EntityLiving;
import net.minecraft.server.v1_4_5.Packet;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.google.common.base.Splitter;

public class Util {
    // Static class for small (emphasis small) utility methods
    private Util() {
    }

    private static final Location AT_LOCATION = new Location(null, 0, 0, 0);

    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);

    private static Class<?> RNG_CLASS = null;

    public static void assumePose(org.bukkit.entity.Entity entity, float yaw, float pitch) {
        EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
        NMS.look(handle, yaw, pitch);
    }

    public static void callCollisionEvent(NPC npc, net.minecraft.server.v1_4_5.Entity entity) {
        if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0)
            Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity.getBukkitEntity()));
    }
    public static NPCPushEvent callPushEvent(NPC npc, Vector vector) {
        NPCPushEvent event = new NPCPushEvent(npc, vector);
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void faceEntity(Entity from, Entity at) {
        if (from.getWorld() != at.getWorld())
            return;
        double xDiff, yDiff, zDiff;
        Location atLocation = at.getLocation(AT_LOCATION);
        Location fromLocation = from.getLocation(FROM_LOCATION);
        xDiff = atLocation.getX() - fromLocation.getX();
        yDiff = atLocation.getY() - fromLocation.getY();
        zDiff = atLocation.getZ() - fromLocation.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
        double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90;
        if (zDiff < 0.0)
            yaw += Math.abs(180 - yaw) * 2;

        EntityLiving handle = ((CraftLivingEntity) from).getHandle();
        NMS.look(handle, (float) yaw - 90, (float) pitch);
    }

    public static Random getFastRandom() {
        try {
            return (Random) RNG_CLASS.newInstance();
        } catch (Exception e) {
            return new Random();
        }
    }

    public static String getMinecraftVersion() {
        String raw = Bukkit.getVersion();
        int start = raw.indexOf("MC:");
        if (start == -1)
            return raw;
        start += 4;
        int end = raw.indexOf(')', start);
        return raw.substring(start, end);
    }

    public static boolean isLoaded(Location location) {
        if (location.getWorld() == null)
            return false;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return location.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public static EntityType matchEntityType(String toMatch) {
        EntityType type = EntityType.fromName(toMatch);
        if (type != null)
            return type;
        return matchEnum(EntityType.values(), toMatch);
    }

    public static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        T type = null;
        for (T check : values) {
            String name = check.name();
            if (name.matches(toMatch) || name.equalsIgnoreCase(toMatch)
                    || name.replace("_", "").equalsIgnoreCase(toMatch)
                    || name.replace('_', '-').equalsIgnoreCase(toMatch)
                    || name.replace('_', ' ').equalsIgnoreCase(toMatch) || name.startsWith(toMatch)) {
                type = check;
                break;
            }
        }
        return type;
    }

    public static boolean matchesItemInHand(Player player, String setting) {
        String parts = setting;
        if (parts.contains("*"))
            return true;
        for (String part : Splitter.on(',').split(parts)) {
            if (Material.matchMaterial(part) == player.getItemInHand().getType()) {
                return true;
            }
        }
        return false;
    }

    public static void sendPacketNearby(Location location, Packet packet) {
        sendPacketNearby(location, packet, 64);
    }

    public static void sendPacketNearby(Location location, Packet packet, double radius) {
        radius *= radius;
        final World world = location.getWorld();
        for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
            if (ply == null || world != ply.getWorld()) {
                continue;
            }
            if (location.distanceSquared(ply.getLocation()) > radius) {
                continue;
            }
            NMS.sendPacket(ply, packet);
        }
    }

    public static void sendToOnline(Packet... packets) {
        Validate.notNull(packets, "packets cannot be null");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline())
                continue;
            for (Packet packet : packets) {
                NMS.sendPacket(player, packet);
            }
        }
    }

    static {
        try {
            RNG_CLASS = Class.forName("org.uncommons.maths.random.XORShiftRNG");
        } catch (ClassNotFoundException e) {
        }
    }
}
