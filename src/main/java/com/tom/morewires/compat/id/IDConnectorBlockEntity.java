package com.tom.morewires.compat.id;

import java.util.Set;

import org.cyclops.cyclopscore.blockentity.BlockEntityTickerDelayed;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableTile;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementTile;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.util.LazyOptional;

import com.google.common.collect.Sets;

import com.tom.morewires.MoreImmersiveWires;
import com.tom.morewires.tile.IOnCableConnector;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.ConnectorBlockEntityHelper;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;

public class IDConnectorBlockEntity extends CyclopsBlockEntity implements IOnCableConnector {
	private final ICable cable;
	private IDNetworkHandler handler;
	protected GlobalWireNetwork globalNet;

	public IDConnectorBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
		super(p_155228_, p_155229_, p_155230_);

		cable = new CableTile<>(this) {

			@Override
			protected boolean isForceDisconnectable() {
				return false;
			}

			@Override
			protected boolean isForceDisconnected(Direction side) {
				return getFacing() != side;
			}

			@Override
			protected EnumFacingMap<Boolean> getForceDisconnected() {
				return null;
			}

			@Override
			protected EnumFacingMap<Boolean> getConnected() {
				return EnumFacingMap.newMap();
			}

			@Override
			public boolean isConnected(Direction side) {
				return getFacing() == side && CableHelpers.canCableConnectTo(level, getPos(), side, this);
			}

			@Override
			public void updateConnections() {
			}

			@Override
			public ItemStack getItemStack() {
				return new ItemStack(MoreImmersiveWires.ID_WIRE.CONNECTOR.get());
			}
		};
		addCapabilityInternal(CableConfig.CAPABILITY, LazyOptional.of(() -> cable));
		addCapabilityInternal(NetworkCarrierConfig.CAPABILITY, LazyOptional.of(NetworkCarrierDefault::new));
		addCapabilityInternal(PathElementConfig.CAPABILITY, LazyOptional.of(() -> new PathElementTile<>(this, cable) {
			@Override
			public Set<ISidedPathElement> getReachableElements() {
				Set<ISidedPathElement> elements = Sets.newHashSet();
				Direction facing = getFacing();
				IPathElement pathElement = BlockEntityHelpers.getCapability(level, worldPosition.relative(facing), facing.getOpposite(), PathElementConfig.CAPABILITY).orElse(null);
				if(pathElement != null) {
					elements.add(SidedPathElement.of(pathElement, facing.getOpposite()));
				}
				if(handler != null) {
					handler.visitAll(elements);
				}
				return elements;
			}
		}));
	}

	@Override
	public BlockState getState() {
		return getBlockState();
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
		return cableType == MoreImmersiveWires.ID_WIRE.wireType;
	}

	@Override
	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other,
			ConnectionPoint otherTarget) {

	}

	@Override
	public void removeCable(Connection connection, ConnectionPoint attachedPoint) {
		setChanged();
	}

	@Override
	public BlockPos getPosition() {
		return worldPosition;
	}

	@Override
	public Level getLevelNonnull() {
		return level;
	}

	public ICable getCable() {
		return cable;
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (getLevel() != null && !getLevel().isClientSide) {
			NetworkHelpers.invalidateNetworkElements(getLevel(), getBlockPos(), this);
		}
		ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
		isUnloaded = true;
	}

	public static class Ticker extends BlockEntityTickerDelayed<IDConnectorBlockEntity> {
		@Override
		protected void update(Level level, BlockPos pos, BlockState blockState, IDConnectorBlockEntity blockEntity) {
			super.update(level, pos, blockState, blockEntity);

			NetworkHelpers.revalidateNetworkElements(level, pos);
		}
	}

	public void setNetworkHandler(IDNetworkHandler handler) {
		this.handler = handler;
		if(!remove)NetworkHelpers.initNetwork(level, worldPosition, null);
	}

	@Override
	public void setLevel(Level worldIn) {
		super.setLevel(worldIn);
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}

	private boolean isUnloaded = false;

	@Override
	public void onLoad() {
		super.onLoad();
		if(level != null) {
			ConnectorBlockEntityHelper.onChunkLoad(this, level);
			isUnloaded = false;
		}
	}

	public void setRemovedIE() {
		ConnectorBlockEntityHelper.remove(level, this);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(!isUnloaded)
			setRemovedIE();
	}
}
