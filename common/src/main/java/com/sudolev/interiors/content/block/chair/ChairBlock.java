package com.sudolev.interiors.content.block.chair;

import com.sudolev.interiors.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.utility.Lang;

import static com.sudolev.interiors.content.block.chair.ChairBlock.ArmrestConfiguration.*;

public abstract class ChairBlock extends DirectionalSeatBlock implements ProperWaterloggedBlock {
	public static final EnumProperty<ArmrestConfiguration> ARMRESTS = EnumProperty.create("armrests", ArmrestConfiguration.class);
	public static final BooleanProperty CROPPED_BACK = BooleanProperty.create("cropped_back");
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	protected final DyeColor color;

	public ChairBlock(Properties properties, DyeColor color) {
		super(properties, color);
		this.color = color;
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(ARMRESTS, DEFAULT).setValue(CROPPED_BACK, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(ARMRESTS).add(CROPPED_BACK));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	public abstract VoxelShape shape();

	@Override
	public void updateEntityAfterFallOn(BlockGetter reader, Entity entity) {
		BlockPos pos = entity.blockPosition();
		if(entity instanceof Player || !(entity instanceof LivingEntity) || !canBePickedUp(entity) || isSeatOccupied(entity.level(), pos)) {
			if(entity.isSuppressingBounce()) {
				super.updateEntityAfterFallOn(reader, entity);
				return;
			}

			Vec3 vec3 = entity.getDeltaMovement();
			if(vec3.y < 0) {
				double d0 = entity instanceof LivingEntity ? 1 : 0.8;
				entity.setDeltaMovement(vec3.x, -vec3.y * 0.66 * d0, vec3.z);
			}

			return;
		}
		if(reader.getBlockState(pos).getBlock() != this) return;
		sitDown(entity.level(), pos, entity);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();

		Vec3 clickPos = pos.getCenter().subtract(context.getClickLocation());

		state = switch(state.getValue(FACING)) {
			case NORTH -> clickPos.x > 0 ? toggleLeft(state) : toggleRight(state);
			case SOUTH -> clickPos.x < 0 ? toggleLeft(state) : toggleRight(state);
			case WEST -> clickPos.z < 0 ? toggleLeft(state) : toggleRight(state);
			case EAST -> clickPos.z > 0 ? toggleLeft(state) : toggleRight(state);
			default -> state;
		};

		if(!world.isClientSide) {
			world.setBlock(pos, state, 3);
		}

		return InteractionResult.SUCCESS;
	}

	private BlockState toggleBackCrop(BlockState state) {
		boolean currentValue = state.getValue(CROPPED_BACK);
		return state.setValue(CROPPED_BACK, !currentValue);
	}

	private BlockState toggleLeft(BlockState state) {
		return state.setValue(ARMRESTS, switch(state.getValue(ARMRESTS)) {
			case BOTH -> RIGHT;
			case NONE -> LEFT;
			case LEFT -> NONE;
			case RIGHT -> BOTH;
		});
	}

	private BlockState toggleRight(BlockState state) {
		return state.setValue(ARMRESTS, switch(state.getValue(ARMRESTS)) {
			case BOTH -> LEFT;
			case NONE -> RIGHT;
			case LEFT -> BOTH;
			case RIGHT -> NONE;
		});
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();

		if(!world.isClientSide) {
			world.setBlock(pos, toggleBackCrop(state), 3);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch(state.getValue(FACING)) {
			case NORTH -> shape();
			case SOUTH -> Utils.rotateShape(Direction.NORTH, Direction.WEST, shape());
			case WEST -> Utils.rotateShape(Direction.NORTH, Direction.EAST, shape());
			default -> Utils.rotateShape(Direction.NORTH, Direction.SOUTH, shape());
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getShape(state, level, pos, context);
	}

	public enum ArmrestConfiguration implements StringRepresentable {
		BOTH, NONE, LEFT, RIGHT;

		public static final ArmrestConfiguration DEFAULT = BOTH;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

}
