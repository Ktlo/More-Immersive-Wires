package com.tom.morewires.compat.oc2;

import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorBlockEntity;
import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.tile.IConnector;
import li.cil.oc2.api.capabilities.NetworkInterface;
import li.cil.oc2.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OC2ConnectorBlockEntity extends BlockEntity implements IConnector, IEBlockInterfaces.IBlockBounds, NetworkFramePocket {

    protected GlobalWireNetwork globalNet;

    public OC2ConnectorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset) {
        LocalWireNetwork local = this.globalNet.getNullableLocalNet(new ConnectionPoint(this.worldPosition, 0));
        if (local != null && !local.getConnections(this.worldPosition).isEmpty()) {
            return false;
        }
        return cableType == MoreImmersiveWires.OC2_WIRE.wireType;
    }

    @Override
    public void connectCable(WireType wireType, ConnectionPoint connectionPoint, IImmersiveConnectable iImmersiveConnectable, ConnectionPoint connectionPoint1) {

    }

    @Override
    public void removeCable(Connection connection, ConnectionPoint attachedPoint) {
        this.setChanged();
    }

    @Override
    public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
        Direction side = getFacing().getOpposite();

        double lengthFromHalf = 0.5F - type.getRenderDiameter()/2-.5;
        return new Vec3(.5+lengthFromHalf*side.getStepX(),
                .5+lengthFromHalf*side.getStepY(),
                .5+lengthFromHalf*side.getStepZ());
    }

    @Override
    public @Nonnull VoxelShape getBlockBounds(@Nullable CollisionContext ctx) {
        return EnergyConnectorBlockEntity.getConnectorBounds(getFacing(), 0.5F);
    }

    @Override
    public BlockPos getPosition() {
        return worldPosition;
    }

    @Override
    public @Nonnull BlockState getState() {
        return getBlockState();
    }

    @Override
    public Level getLevelNonnull() {
        return level;
    }

    private boolean isUnloaded = false;

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
        isUnloaded = true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        assert level != null;
        ConnectorBlockEntityHelper.onChunkLoad(this, level);
        isUnloaded = false;
    }

    public void setRemovedIE() {
        assert level != null;
        ConnectorBlockEntityHelper.remove(level, this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(!isUnloaded)
            setRemovedIE();
    }

    @Override
    public void setLevel(@Nonnull Level worldIn) {
        super.setLevel(worldIn);
        globalNet = GlobalWireNetwork.getNetwork(worldIn);
    }

    private LazyOptional<NetworkInterface> adjacentInterface = LazyOptional.empty();

    private void resolveLocalInterface() {
        assert level != null;

        adjacentInterface = LazyOptional.empty();

        for (final Direction facing : Direction.values()) {
            final BlockPos sourcePos = getBlockPos().relative(facing);
            final BlockEntity blockEntity = level.getBlockEntity(sourcePos);
            if (blockEntity == null) {
                continue;
            }
            adjacentInterface = blockEntity.getCapability(Capabilities.networkInterface(), facing);
            if (adjacentInterface.isPresent()) {
                break;
            }
        }
    }

    private static class NullNetworkInterface implements NetworkInterface {

        public static final NetworkInterface INSTANCE = new NullNetworkInterface();

        private NullNetworkInterface() {

        }

        @Nullable
        @Override
        public byte[] readEthernetFrame() {
            return null;
        }

        @Override
        public void writeEthernetFrame(@Nonnull final NetworkInterface networkInterface,
                                       @Nonnull  final byte [] bytes,
                                       final int i) {

        }
    }

    private NetworkInterface getAdjacentInterface() {
        return adjacentInterface.orElse(NullNetworkInterface.INSTANCE);
    }

    @Override
    public void initFramePocket() {
        resolveLocalInterface();
    }

    @Nullable
    @Override
    public byte[] extractFrame() {
        return getAdjacentInterface().readEthernetFrame();
    }

    @Override
    public void putFrame(@Nonnull final byte[] frame) {
        getAdjacentInterface().writeEthernetFrame(NullNetworkInterface.INSTANCE, frame, 64);
    }
}