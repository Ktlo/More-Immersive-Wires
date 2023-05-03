package com.tom.morewires.compat.oc2;

import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import com.google.common.collect.ImmutableList;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.WireTypeDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;

public class OC2WireDefinition implements WireTypeDefinition<OC2ConnectorBlockEntity> {

    public static final ResourceLocation NET_ID = new ResourceLocation(MoreImmersiveWires.modid, "oc2_network");

    @Override
    public OC2ConnectorBlockEntity createBE(BlockPos pos, BlockState state) {
        return new OC2ConnectorBlockEntity(MoreImmersiveWires.OC2_WIRE.CONNECTOR_ENTITY.get(), pos, state);
    }

    @Override
    public boolean isCable(BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public Block makeBlock0(RegistryObject<BlockEntityType<OC2ConnectorBlockEntity>> type) {
        return new OC2ConnectorBlock(type);
    }

    @Override
    public void init() {
        LocalNetworkHandler.register(NET_ID, OC2NetworkHandler::new);
    }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return ImmutableList.of(NET_ID);
    }
}
