package me.travis.wurstplus.wurstplustwo.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class WurstplusHoleUtil {
    public static final Minecraft mc = Minecraft.getMinecraft();

    private static byte safe_sides;

    public enum Hole{
        None,
        //Weak, any 4 connected non air blocks on horizontal plane
        Partial,
        Bedrock,
        PartialDual,
        BedrockDual
    }

    public static boolean checkAny(BlockPos input){
            return mc.world.getBlockState(input.east()).getBlock() != Blocks.AIR
                    && mc.world.getBlockState(input.west()).getBlock() != Blocks.AIR
                    && mc.world.getBlockState(input.north()).getBlock() != Blocks.AIR
                    && mc.world.getBlockState(input.south()).getBlock() != Blocks.AIR;
    }

    public static Hole getHoleType(BlockPos input){
        if (!mc.world.getBlockState(input).getBlock().equals(Blocks.AIR)) {
            return Hole.None;
        }

        if (!mc.world.getBlockState(input.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
            return Hole.None;
        }

        /*if (!mc.world.getBlockState(input.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
            return Hole.None;
        }*/
        boolean potential_dual = !mc.world.getBlockState(input.add(0,2,0)).getBlock().equals(Blocks.AIR);

        boolean single_hole = true; //was possible

        safe_sides = 0;

        byte air_orient = -1;
        byte dual_possible_at = 0; //was counter

        for (BlockPos seems_blocks : new BlockPos[]{
                new BlockPos( 0,-1, 0),
                new BlockPos( 0, 0,-1),
                new BlockPos( 1, 0, 0),
                new BlockPos( 0, 0, 1),
                new BlockPos(-1, 0, 0)
        }) {
            Block block = mc.world.getBlockState(input.add(seems_blocks)).getBlock();

            if (block != Blocks.BEDROCK && block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST
                    && block != Blocks.ANVIL) {
                single_hole = false;

                if (dual_possible_at == 0) break;

                if (air_orient != -1) {
                    air_orient = -1;
                    break;
                }

                if (block.equals(Blocks.AIR)) {
                    air_orient = dual_possible_at;
                } else {
                    break;
                }
            }

            if (block == Blocks.BEDROCK) {
                safe_sides++;

            }
            dual_possible_at++;
        }

        if(single_hole && !potential_dual){
            if(safe_sides == 5){
                return Hole.Bedrock;
            }

            return  Hole.Partial;
        }

        if(air_orient == -1) return Hole.None;

        BlockPos second_pos = input.add(orientConv(air_orient));

        if(checkDual(second_pos, air_orient)){
            boolean low_ceiling_second_hole
                    = mc.world.getBlockState(second_pos.add(0,1,0)).getBlock().equals(Blocks.AIR) &&
                    !mc.world.getBlockState(second_pos.add(0,2,0)).getBlock().equals(Blocks.AIR);

            if(potential_dual && low_ceiling_second_hole) return Hole.None;

            if(safe_sides == 8){
                return Hole.BedrockDual;
            }
            else{
                return Hole.PartialDual;
            }
        }

        return Hole.None;
    }

    //tools
    private static BlockPos orientConv(byte orient_count) {
        BlockPos converted = null;

        switch(orient_count) {
            case 0:
                //return EnumFacing.DOWN.getDirectionVec();
                converted = new BlockPos( 0, -1,  0);
                break;
            case 1:
                //return EnumFacing.NORTH.getDirectionVec();
                converted = new BlockPos( 0,  0, -1);
                break;
            case 2:
                //return EnumFacing.EAST.getDirectionVec();
                converted = new BlockPos( 1,  0,  0);
                break;
            case 3:
                //return EnumFacing.SOUTH.getDirectionVec();
                converted = new BlockPos( 0,  0,  1);
                break;
            case 4:
                //return EnumFacing.WEST.getDirectionVec();
                converted = new BlockPos(-1,  0,  0);
                break;
            case 5:
                converted = new BlockPos(0,  1,  0);
                break;
        }
        return converted;
    }

    private static byte oppositeIntOrient(byte orient_count) {

        byte opposite = 0;

        switch(orient_count)
        {
            case 0:
                opposite = 5;
                break;
            case 1:
                opposite = 3;
                break;
            case 2:
                opposite = 4;
                break;
            case 3:
                opposite = 1;
                break;
            case 4:
                opposite = 2;
                break;
        }
        return opposite;
    }

    private static boolean checkDual(BlockPos second_block, byte counter) {
        byte i = -1;

		/*
			lets check down from second block to not have esp of a dual hole of one space
			missing a bottom block
		*/
        for (BlockPos seems_blocks : new BlockPos[] {
                new BlockPos( 0,  -1, 0), //Down
                new BlockPos( 0,  0, -1), //N
                new BlockPos( 1,  0,  0), //E
                new BlockPos( 0,  0,  1), //S
                new BlockPos(-1,  0,  0)  //W

        }) {
            i++;
            //skips opposite direction check, since its air

            if(counter == oppositeIntOrient(i)) {
                continue;
            }

            Block block = mc.world.getBlockState(second_block.add(seems_blocks)).getBlock();
            if (block == Blocks.BEDROCK) {
                safe_sides++;
            }
            else if (block != Blocks.OBSIDIAN && block != Blocks.ENDER_CHEST && block != Blocks.ANVIL) {
                return false;
            }
        }
        return true;
    }
}