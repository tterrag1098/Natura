package mods.natura.blocks.trees;

import java.util.List;
import java.util.Random;

import mods.natura.common.NContent;
import mods.natura.common.NaturaTab;
import mods.natura.common.PHNatura;
import mods.natura.worldgen.BloodTreeLargeGen;
import mods.natura.worldgen.BushTreeGen;
import mods.natura.worldgen.DarkwoodGen;
import mods.natura.worldgen.EucalyptusTreeGenShort;
import mods.natura.worldgen.FusewoodGen;
import mods.natura.worldgen.RedwoodTreeGen;
import mods.natura.worldgen.SakuraTreeGen;
import mods.natura.worldgen.WhiteTreeGen;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NSaplingBlock extends BlockSapling
{
    public Icon[] icons;
    public String[] textureNames = new String[] { "redwood", "eucalyptus", "hopseed", "sakura", "ghostwood", "bloodwood", "darkwood", "fusewood" };

    public NSaplingBlock(int id)
    {
        super(id);
        float f = 0.4F;
        setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
        this.setHardness(0.0F);
        this.setStepSound(Block.soundGrassFootstep);
        this.setCreativeTab(NaturaTab.tab);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons (IconRegister iconRegister)
    {
        this.icons = new Icon[textureNames.length];

        for (int i = 0; i < this.icons.length; ++i)
        {
            this.icons[i] = iconRegister.registerIcon("natura:" + textureNames[i] + "_sapling");
        }
    }

    public boolean canPlaceBlockAt (World world, int x, int y, int z)
    {
        int blockID = world.getBlockId(x, y, z);
        Block block = Block.blocksList[blockID];
        if (block == null || block.isBlockReplaceable(world, x, y, z))
        {
            int lowerID = world.getBlockId(x, y - 1, z);
            //return canThisPlantGrowOnThisBlockID(lowerID);
            if (!canThisPlantGrowOnThisBlockID(lowerID))
            {
                int upperID = world.getBlockId(x, y + 1, z);
                return canThisPlantGrowOnThisBlockID(upperID);
            }
            else
                return true;
        }
        return false;
    }

    public boolean canThisPlantGrowOnThisBlockID (int id)
    {
        return id == Block.grass.blockID || id == Block.dirt.blockID || id == Block.slowSand.blockID || id == Block.netherrack.blockID || id == NContent.taintedSoil.blockID;
    }

    @Override
    public boolean canBlockStay (World world, int x, int y, int z)
    {
        int meta = world.getBlockMetadata(x, y, z) % 8;
        switch (meta)
        {
        case 0:
        case 1:
        case 2:
        case 3:
            int blockID = world.getBlockId(x, y - 1, z);
            Block soil = blocksList[blockID];
            return (world.getFullBlockLightValue(x, y, z) >= 8 || world.canBlockSeeTheSky(x, y, z)) && (soil != null && soil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this));
        case 4:
        case 6:
        case 7:
            int belowID = world.getBlockId(x, y - 1, z);
            Block netherSoil = blocksList[belowID];
            return netherSoil != null && (netherSoil == Block.netherrack || netherSoil.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, this));
        case 5:
            int aboveID = world.getBlockId(x, y + 1, z);
            Block nSoil = blocksList[aboveID];
            return nSoil != null && (nSoil == Block.netherrack || nSoil == Block.slowSand || nSoil == NContent.taintedSoil);
        default:
            return true;
        }
    }

    @Override
    public EnumPlantType getPlantType (World world, int x, int y, int z)
    {
        int meta = world.getBlockMetadata(x, y, z) % 8;
        if (meta <= 3)
            return EnumPlantType.Plains;
        else
            return EnumPlantType.Nether;
    }

    public void updateTick (World world, int x, int y, int z, Random random)
    {
        if (world.isRemote)
        {
            return;
        }
        super.updateTick(world, x, y, z, random);
        int md = world.getBlockMetadata(x, y, z);
        if (md % 8 == 0)
        {
            if (world.getBlockLightValue(x, y + 1, z) >= 9 && random.nextInt(120) == 0)
            {
                if ((md & 8) == 0)
                    world.setBlockMetadataWithNotify(x, y, z, md | 8, 4);

                else
                {
                    int numSaplings = 0;
                    for (int xPos = -3; xPos <= 3; xPos++)
                    {
                        for (int zPos = -3; zPos <= 3; zPos++)
                        {
                            int ecks = x + xPos, zee = z + zPos;
                            if (world.getBlockId(x + xPos, y, z + zPos) == this.blockID && world.getBlockMetadata(x + xPos, y, z + zPos) % 8 == 0)
                            {
                                numSaplings++;
                            }
                        }
                    }

                    if (numSaplings >= 40)
                    {
                        for (int xPos = -4; xPos <= 4; xPos++)
                        {
                            for (int zPos = -4; zPos <= 4; zPos++)
                            {
                                int ecks = x + xPos, zee = z + zPos;
                                if (world.getBlockId(ecks, y, zee) == this.blockID && world.getBlockMetadata(ecks, y, zee) % 8 == 0)
                                {
                                    world.setBlock(ecks, y, zee, 0, 0, 4);
                                }
                            }
                        }
                        growTree(world, x, y, z, random);
                    }
                }
            }
        }
        else if (md % 8 <= 3)
        {
            if (random.nextInt(10) == 0 && world.getBlockLightValue(x, y + 1, z) >= 9)//&& random.nextInt(120) == 0)
            {
                if ((md & 8) == 0)
                    world.setBlockMetadataWithNotify(x, y, z, md | 8, 4);

                else
                    growTree(world, x, y, z, random);
            }
        }
        else if (random.nextInt(10) == 0)
        {
            if ((md & 8) == 0)
                world.setBlockMetadataWithNotify(x, y, z, md | 8, 4);

            else
                growTree(world, x, y, z, random);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon (int side, int meta)
    {
        return icons[meta % 8];
    }

    @Override
    public void markOrGrowMarked (World world, int x, int y, int z, Random random)
    {
        boneFertilize(world, x, y, z, random);
    }

    public boolean boneFertilize (World world, int x, int y, int z, Random random)
    {
        int meta = world.getBlockMetadata(x, y, z);

        /*if (meta % 8 == 0)
            return false;*/

        if ((meta & 8) == 0)
        {
            world.setBlockMetadataWithNotify(x, y, z, meta | 8, 4);
        }
        else
        {
            this.growTree(world, x, y, z, random);
        }

        return true;
    }

    public void growTree (World world, int x, int y, int z, Random random)
    {
        int md = world.getBlockMetadata(x, y, z) % 8;
        world.setBlock(x, y, z, 0);
        WorldGenerator obj = null;

        if (md == 1)
            obj = new EucalyptusTreeGenShort(0, 1);

        else if (md == 2)
            obj = new BushTreeGen(true, 2, 3, 2);

        else if (md == 3)
            obj = new SakuraTreeGen(true, 1, 0);

        else if (md == 4)
            obj = new WhiteTreeGen(true, 2, 1);

        else if (md == 5)
            obj = new BloodTreeLargeGen(3, 2);

        else if (md == 6)
            obj = new DarkwoodGen(true, 3, 0);

        else if (md == 7)
            obj = new FusewoodGen(true, 3, 1);

        else
            obj = new RedwoodTreeGen(true, PHNatura.redwoodID);

        if (!(obj.generate(world, random, x, y, z)))
            world.setBlock(x, y, z, blockID, md + 8, 3);
    }

    public int damageDropped (int i)
    {
        return i % 8;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks (int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int i = 0; i < 8; i++)
            par3List.add(new ItemStack(par1, 1, i));
    }
}
