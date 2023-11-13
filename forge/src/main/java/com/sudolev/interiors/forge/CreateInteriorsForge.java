package com.sudolev.interiors.forge;

import net.minecraft.world.item.DyeColor;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.sudolev.interiors.CreateInteriors;
import com.sudolev.interiors.content.registry.CIBlocks;

@Mod(CreateInteriors.ID)
public class CreateInteriorsForge {
	public CreateInteriorsForge() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

		forgeEventBus.register(Events.ClientModBusEvents.class);
		modEventBus.addListener(Events.ClientModBusEvents::onLoadComplete);

		CreateInteriors.init();
		CreateInteriors.REGISTRATE.registerEventListeners(modEventBus);
	}
}