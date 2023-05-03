package com.tom.morewires.compat.oc2;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class OC2ConnectorBlock extends ConnectorBlock<OC2ConnectorBlockEntity> {

    public OC2ConnectorBlock(RegistryObject<BlockEntityType<OC2ConnectorBlockEntity>> entityType) {
        super(ConnectorBlock.PROPERTIES.get(), entityType);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
    }
}
