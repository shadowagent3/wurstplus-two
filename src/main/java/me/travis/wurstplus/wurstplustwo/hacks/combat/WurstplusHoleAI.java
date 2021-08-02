package me.travis.wurstplus.wurstplustwo.hacks.combat;

import me.travis.wurstplus.wurstplustwo.guiscreen.settings.WurstplusSetting;
import me.travis.wurstplus.wurstplustwo.hacks.WurstplusCategory;
import me.travis.wurstplus.wurstplustwo.hacks.WurstplusHack;
import me.travis.wurstplus.wurstplustwo.util.WurstplusBlockInteractHelper;
import me.travis.wurstplus.wurstplustwo.util.WurstplusBlockUtil;
import me.travis.wurstplus.wurstplustwo.util.WurstplusFriendUtil;
import me.travis.wurstplus.wurstplustwo.util.WurstplusHoleUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class WurstplusHoleAI extends WurstplusHack {
    public WurstplusHoleAI(){
        super(WurstplusCategory.WURSTPLUS_COMBAT);

        this.name        = "Hole AI";
        this.tag         = "HoleAi";
        this.description = "Trips opponents";
    }
    WurstplusSetting hole_toggle = create("Toggle", "HoleFillToggle", true);

    WurstplusSetting switch_mode = create("Block Select", "BlockSelect", "Select", combobox("Obsidian", "Web", "Bedrock"));
    WurstplusSetting slot_back = create("Back Slot", "BackSlot", true);

    WurstplusSetting maxrange = create("Max Distance", "MaximumDistance", 4, 0, 6);
    WurstplusSetting minrange = create("Min Distance", "MinimumDistance", 2, 0, 6);

    WurstplusSetting holeslength = create("Fill Holes", "FillHoles", 3, 1, 7);
    WurstplusSetting enemysearch = create("Enemy Search", "EnemySearch", 3.0f, 1.0f, 6.0f);

    WurstplusSetting swing = create("Swing", "HoleFillSwing", "Mainhand", combobox("Mainhand", "Offhand", "Both", "None"));
    WurstplusSetting hole_rotate = create("Rotate", "HoleFillRotate", true);


    private BlockPos[] holes;

    //to make the user know he will need to have the right item in hotbar
    @Override
    public void enable() {
        if (find_in_hotbar() == -1) {
            this.set_disable();
        }
    }

    @Override
    public void disable() { holes = null; }

    private void init_array(){
        holes = new BlockPos[holeslength.get_value(1)];

        //same as holes.clear()
        for(int i = 0; i < holeslength.get_value(1); i++)
        {
            holes[i] = null;
        }
    }

    @Override
    public void update(){
        if(mc.player == null || mc.world == null) return;

        int hotbar_pos = find_in_hotbar();
        int slot_selected = mc.player.inventory.currentItem;

        if (hotbar_pos == -1) {
            return;
        }

        Vec3d mcp = new Vec3d(mc.player.posX, mc.player.posY, mc.player.posZ);

        short tracker = 0;
        init_array();

        for(EntityPlayer p : mc.world.playerEntities){

            if (WurstplusFriendUtil.isFriend(p.getName()) || mc.player == p) continue;

            double dist = Math.sqrt(mcp.squareDistanceTo(p.posX,p.posY,p.posZ));

            if(dist > maxrange.get_value(1) || dist < minrange.get_value(1)){
                continue;
            }

            BlockPos Ppos = new BlockPos(Math.floor(p.posX), Math.floor(p.posY), Math.floor(p.posZ));


            for(BlockPos Bpos : WurstplusBlockInteractHelper.getSphere(Ppos, enemysearch.get_value(1), enemysearch.get_value(1),false, true, 0)){
                if(Bpos.equals(Ppos))
                {
                    continue;
                }

                WurstplusHoleUtil.Hole Bpostype = WurstplusHoleUtil.getHoleType(Bpos);
                if(Bpostype != WurstplusHoleUtil.Hole.None)
                {
                    holes[tracker] = Bpos;
                    if(tracker == holeslength.get_value(1)-1)
                        tracker = 0;
                    else
                        tracker++;
                }
            }

            for(int i = 0; i < holeslength.get_value(1); i++){
                if(holes[i] == null)
                    continue;
                WurstplusBlockUtil.placeBlock(holes[i], hotbar_pos, hole_rotate.get_value(true), hole_rotate.get_value(true), swing);


            }
        }

        if(slot_back.get_value(true)) {
            mc.player.inventory.currentItem = slot_selected;
        }

        if(!hole_toggle.get_value(true))
        {
            this.set_disable();
        }
    }

    private int find_in_hotbar() {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();


                if (switch_mode.get_current_value().equals("Obsidian") && block instanceof BlockObsidian) {

                    return i;
                }

                if (switch_mode.get_current_value().equals("Web") && block instanceof BlockWeb) {

                    return i;
                }

                if (switch_mode.get_current_value().equals("Bedrock") && block.equals(Block.getBlockById(7))) {

                    return i;
                }
            }
        }
        return -1;
    }
}
