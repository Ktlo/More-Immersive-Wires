package com.tom.morewires.item;

import static blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils.hasWireLink;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.tom.morewires.MoreImmersiveWires;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.IWireCoil;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import blusunrize.immersiveengineering.common.items.IEBaseItem;

public class WireCoilItem extends IEBaseItem implements IWireCoil {
	@Nonnull
	private final WireType type;

	public WireCoilItem(@Nonnull WireType type) {
		super(new Properties(), MoreImmersiveWires.MOD_TAB);
		this.type = type;
	}

	@Override
	public WireType getWireType(ItemStack stack) {
		return type;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
		if(hasWireLink(stack)) {
			WireLink link = WireLink.readFromItem(stack);
			list.add(new TranslatableComponent(Lib.DESC_INFO+"attachedToDim", link.cp.getX(),
					link.cp.getY(), link.cp.getZ(), link.dimension));
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		return WirecoilUtils.doCoilUse(this, ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), ctx.getHand(), ctx.getClickedFace(),
				(float)ctx.getClickLocation().x, (float)ctx.getClickLocation().y, (float)ctx.getClickLocation().z);
	}
}