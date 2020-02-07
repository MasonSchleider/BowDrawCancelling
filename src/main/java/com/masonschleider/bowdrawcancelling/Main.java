package com.masonschleider.bowdrawcancelling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Main {
    public static final String MOD_ID = "bowdrawcancelling";
    
    static final Logger LOGGER = LogManager.getLogger(Main.MOD_ID);
    
    private static Item activeItem = null;
    private static boolean cancelPending = false;
    
    @SubscribeEvent
    static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
    
        Minecraft minecraft = Minecraft.getInstance();
        KeyBinding keyBindUseItem = minecraft.gameSettings.keyBindUseItem;
        
        if (keyBindUseItem.isKeyDown()) {
            KeyBinding keyBindAttack = minecraft.gameSettings.keyBindAttack;
            
            if (activeItem == Items.BOW && keyBindAttack.isKeyDown())
                cancelPending = true;
        } else {
            activeItem = null;
            cancelPending = false;
        }
    }
    
    @SubscribeEvent
    static void onPlayerInteract(PlayerInteractEvent event) {
        if (cancelPending && event.isCancelable()) {
            event.setCanceled(true);
        } else if (event instanceof PlayerInteractEvent.RightClickItem) {
            Item item = event.getItemStack().getItem();
            
            if (activeItem != null && activeItem != Items.BOW && item == Items.BOW)
                event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity().world.isRemote)
            activeItem = event.getItem().getItem();
    }
    
    @SubscribeEvent
    public static void onUseItemTick(LivingEntityUseItemEvent.Tick event) {
        if (cancelPending)
            event.setCanceled(true);
    }
}
