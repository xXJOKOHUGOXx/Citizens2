package net.citizensnpcs.npc.entity;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.v1_4_5.ItemInWorldManager;
import net.minecraft.server.v1_4_5.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_5.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class HumanController extends AbstractEntityController {
    @Override
    protected LivingEntity createEntity(final Location at, final NPC npc) {
        WorldServer ws = ((CraftWorld) at.getWorld()).getHandle();
        final EntityHumanNPC handle = new EntityHumanNPC(ws.getServer().getServer(), ws,
                StringHelper.parseColors(npc.getFullName()), new ItemInWorldManager(ws), npc);
        handle.getBukkitEntity().teleport(at);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                boolean removeFromPlayerList = Setting.REMOVE_PLAYERS_FROM_PLAYER_LIST.asBoolean();
                NMS.addOrRemoveFromPlayerList(getBukkitEntity(),
                        npc.data().get("removefromplayerlist", removeFromPlayerList));
            }
        }, 1);
        handle.getBukkitEntity().setSleepingIgnored(true);
        return handle.getBukkitEntity();
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) super.getBukkitEntity();
    }
}