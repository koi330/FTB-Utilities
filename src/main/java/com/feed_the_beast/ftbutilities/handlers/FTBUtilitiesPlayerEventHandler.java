package com.feed_the_beast.ftbutilities.handlers;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import com.feed_the_beast.ftblib.events.player.ForgePlayerConfigEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerDataEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedInEvent;
import com.feed_the_beast.ftblib.events.player.ForgePlayerLoggedOutEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.BlockDimPos;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftblib.lib.util.InvUtils;
import com.feed_the_beast.ftblib.lib.util.ServerUtils;
import com.feed_the_beast.ftblib.lib.util.StringUtils;
import com.feed_the_beast.ftblib.lib.util.permission.PermissionAPI;
import com.feed_the_beast.ftbutilities.FTBUtilitiesConfig;
import com.feed_the_beast.ftbutilities.FTBUtilitiesNotifications;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesPlayerData;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesUniverseData;
import com.feed_the_beast.ftbutilities.net.MessageUpdateTabName;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class FTBUtilitiesPlayerEventHandler {

    public static final FTBUtilitiesPlayerEventHandler INST = new FTBUtilitiesPlayerEventHandler();

    @SubscribeEvent
    public void registerPlayerData(ForgePlayerDataEvent event) {
        event.register(new FTBUtilitiesPlayerData(event.getPlayer()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLoggedIn(ForgePlayerLoggedInEvent event) {
        EntityPlayerMP player = event.getPlayer().getPlayer();

        if (ServerUtils.isFirstLogin(player, "ftbutilities_starting_items")) {
            if (FTBUtilitiesConfig.login.enable_starting_items) {
                InvUtils.dropAllItems(
                        player.getEntityWorld(),
                        player.posX,
                        player.posY,
                        player.posZ,
                        FTBUtilitiesConfig.login.getStartingItems());
            }
        }

        if (FTBUtilitiesConfig.login.enable_motd) {
            for (IChatComponent t : FTBUtilitiesConfig.login.getMOTD()) {
                player.addChatMessage(t);
            }
        }

        if (ClaimedChunks.isActive()) {
            ClaimedChunks.instance.markDirty();
        }

        if (FTBUtilitiesConfig.chat.replace_tab_names) {
            new MessageUpdateTabName(player).sendToAll();

            for (EntityPlayerMP player1 : (List<EntityPlayerMP>) player.mcServer
                    .getConfigurationManager().playerEntityList) {
                if (player1 != player) {
                    new MessageUpdateTabName(player1).sendTo(player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(ForgePlayerLoggedOutEvent event) {
        EntityPlayerMP player = event.getPlayer().getPlayer();

        if (ClaimedChunks.isActive()) {
            ClaimedChunks.instance.markDirty();
        }

        FTBUtilitiesUniverseData.updateBadge(player.getUniqueID());
        player.getEntityData().removeTag(FTBUtilitiesPlayerData.TAG_LAST_CHUNK);
    }

    @SubscribeEvent
    public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        event.entityPlayer.getEntityData().removeTag(FTBUtilitiesPlayerData.TAG_LAST_CHUNK);
    }

    @SubscribeEvent
    public void getPlayerSettings(ForgePlayerConfigEvent event) {
        FTBUtilitiesPlayerData.get(event.getPlayer()).addConfig(event.getConfig());
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.entityLiving;
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) entity;
            FTBUtilitiesPlayerData data = FTBUtilitiesPlayerData.get(Universe.get().getPlayer(entityPlayerMP));
            data.setLastDeath(new BlockDimPos(entity));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChunkChanged(EntityEvent.EnteringChunk event) {
        if (event.entity.worldObj.isRemote || !(event.entity instanceof EntityPlayerMP) || !Universe.loaded()) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.entity;
        player.func_143004_u();
        ForgePlayer p = Universe.get().getPlayer(player.getGameProfile());

        if (p == null || p.isFake()) {
            return;
        }

        FTBUtilitiesPlayerData.get(p).setLastSafePos(new BlockDimPos((ICommandSender) player));
        FTBUtilitiesNotifications
                .updateChunkMessage(player, new ChunkDimPos(event.newChunkX, event.newChunkZ, player.dimension));
    }

    @SubscribeEvent
    public void onEntityDamage(LivingAttackEvent event) {
        if (FTBUtilitiesConfig.world.disable_player_suffocation_damage && event.entity instanceof EntityPlayer
                && (event.source == DamageSource.inWall)) {
            // event.ammount = 0;
            event.setCanceled(true);
        }
    }

    // TODO: I am registering the event handlers because ftbutil data was not working!!!

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityAttacked(AttackEntityEvent event) {
        if (!ClaimedChunks.canAttackEntity(event.entityPlayer, event.target)) {
            InvUtils.forceUpdate(event.entityPlayer);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (FTBUtilitiesConfig.world.isItemRightClickDisabled(event.entityPlayer.getItemInUse())) {
            event.setCanceled(true);

            if (!event.world.isRemote) {
                event.entityPlayer.addChatComponentMessage(new ChatComponentText("Item disabled!"));
            }

            return;
        }

        if (ClaimedChunks.blockBlockInteractions(event.entityPlayer, event.x, event.y, event.z, 0)) {
            InvUtils.forceUpdate(event.entityPlayer);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRightClickItem(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (FTBUtilitiesConfig.world.isItemRightClickDisabled(event.entityPlayer.getItemInUse())) {
            event.setCanceled(true);

            if (!event.world.isRemote) {
                event.entityPlayer.addChatComponentMessage(new ChatComponentText("Item disabled!"));
            }

            return;
        }

        if (ClaimedChunks.blockItemUse(event.entityPlayer, event.x, event.y, event.z)) {
            InvUtils.forceUpdate(event.entityPlayer);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (ClaimedChunks.blockBlockEditing(event.getPlayer(), event.x, event.y, event.z, 0)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (ClaimedChunks.blockBlockEditing(event.player, event.x, event.y, event.z, 0)) {
            InvUtils.forceUpdate(event.player);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockLeftClick(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (ClaimedChunks.blockBlockEditing(event.entityPlayer, event.x, event.y, event.z, 0)) {
            event.setCanceled(true);
        }
    }

    /*
     * @SubscribeEvent(priority = EventPriority.HIGH) public static void onItemPickup(EntityItemPickupEvent event) { }
     */

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onNameFormat(PlayerEvent.NameFormat event) {
        if (FTBUtilitiesConfig.commands.nick && Universe.loaded() && event.entityPlayer instanceof EntityPlayerMP) {
            ForgePlayer p = Universe.get().getPlayer(event.entityPlayer.getGameProfile());

            if (p != null) {
                FTBUtilitiesPlayerData data = FTBUtilitiesPlayerData.get(p);

                if (!data.getNickname().isEmpty()
                        && PermissionAPI.hasPermission(event.entityPlayer, FTBUtilitiesPermissions.CHAT_NICKNAME_SET)) {
                    String name = StringUtils.addFormatting(data.getNickname());

                    if (!p.hasPermission(FTBUtilitiesPermissions.CHAT_NICKNAME_COLORS)) {
                        name = StringUtils.unformatted(name);
                    } else if (name.indexOf(StringUtils.FORMATTING_CHAR) != -1) {
                        name += EnumChatFormatting.RESET;
                    }

                    if (FTBUtilitiesConfig.chat.add_nickname_tilde) {
                        name = "~" + name;
                    }

                    event.displayname = name;
                }
            }
        }
    }

    private static String getStateName(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        Block block = world.getBlock(x, y, z);
        return block.getLocalizedName() + ":" + meta;
    }

    private static String getDim(EntityPlayer player) {
        return ServerUtils.getDimensionName(player.dimension).getUnformattedText();
    }

    private static String getPos(int x, int y, int z) {
        return String.format("[%d, %d, %d]", x, y, z);
    }

    @SubscribeEvent
    public void onBlockBreakLog(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();

        if (FTBUtilitiesConfig.world.logging.block_broken && player instanceof EntityPlayerMP
                && FTBUtilitiesConfig.world.logging.log((EntityPlayerMP) player)) {
            FTBUtilitiesUniverseData.worldLog(
                    String.format(
                            "%s broke %s at %s in %s",
                            player.getDisplayName(),
                            getStateName(event.world, event.x, event.y, event.z),
                            getPos(event.x, event.y, event.z),
                            getDim(player)));
        }
    }

    @SubscribeEvent
    public void onBlockPlaceLog(BlockEvent.PlaceEvent event) {
        EntityPlayer player = event.player;

        if (FTBUtilitiesConfig.world.logging.block_placed && player instanceof EntityPlayerMP
                && FTBUtilitiesConfig.world.logging.log((EntityPlayerMP) player)) {
            FTBUtilitiesUniverseData.worldLog(
                    String.format(
                            "%s placed %s at %s in %s",
                            player.getDisplayName(),
                            getStateName(event.world, event.x, event.y, event.z),
                            getPos(event.x, event.y, event.z),
                            getDim(player)));
        }
    }

    @SubscribeEvent
    public void onRightClickItemLog(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            return;
        }
        EntityPlayer player = event.entityPlayer;

        if (FTBUtilitiesConfig.world.logging.item_clicked_in_air && player instanceof EntityPlayerMP
                && FTBUtilitiesConfig.world.logging.log((EntityPlayerMP) player)) {
            FTBUtilitiesUniverseData.worldLog(
                    String.format(
                            "%s clicked %s in air at %s in %s",
                            player.getDisplayName(),
                            event.entityPlayer.getItemInUse().getItem()
                                    .getItemStackDisplayName(event.entityPlayer.getItemInUse()),
                            getPos(event.x, event.y, event.z),
                            getDim(player)));
        }
    }
}
