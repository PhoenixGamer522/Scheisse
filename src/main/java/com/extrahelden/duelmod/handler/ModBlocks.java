package com.extrahelden.duelmod.handler;

import com.extrahelden.duelmod.DuelMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    private ModBlocks() {
    }

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, DuelMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, DuelMod.MOD_ID);

    public static final RegistryObject<Block> CLAN_CHEST = BLOCKS.register(
            "clan_chest",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS))
    );

    public static final RegistryObject<Item> CLAN_CHEST_ITEM = ITEMS.register(
            "clan_chest",
            () -> new BlockItem(CLAN_CHEST.get(), new Item.Properties())
    );

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
