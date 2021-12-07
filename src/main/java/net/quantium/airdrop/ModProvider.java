package net.quantium.airdrop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.airdrop.client.gui.GuiAirdrop;
import net.quantium.airdrop.client.render.RenderAirdrop;
import net.quantium.airdrop.client.render.RenderSupplyGrenade;
import net.quantium.airdrop.commands.AirdropCommand;
import net.quantium.airdrop.entity.EntityAirdrop;
import net.quantium.airdrop.entity.EntitySupplyGrenade;
import net.quantium.airdrop.inventory.ContainerAirdrop;
import net.quantium.airdrop.item.ItemSupplyGrenade;
import net.quantium.airdrop.loot.AirdropLevelCondition;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

@Mod(modid = ModProvider.MODID, name = ModProvider.NAME, version = ModProvider.VERSION)
public class ModProvider {
    public static final String MODID = "qairdrop";
    public static final String NAME = "AirDrop Mod";
    public static final String VERSION = "1.0";

    public static final Item SUPPLY_GRENADE = new ItemSupplyGrenade();

    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(MODID, "airdrop");

    @Mod.Instance(MODID)
    private static ModProvider instance;
    private Logger logger;
    private ModConfig config;

    public static ModProvider instance() {
        return instance;
    }

    public static Logger logger() {
        return instance.logger;
    }

    public static ModConfig config() {
        return instance.config;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = ModConfig.config(new Configuration(event.getSuggestedConfigurationFile()));
        logger = event.getModLog();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(Events.class);
        logger().info("Da airdropes loaderd :DDDDD");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LootTableList.register(LOOT_TABLE);
        LootConditionManager.registerCondition(new AirdropLevelCondition.Serializer());
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new AirdropCommand());
    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void preInitClient(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityAirdrop.class, RenderAirdrop::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySupplyGrenade.class, RenderSupplyGrenade::new);
    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(SUPPLY_GRENADE, 0, new ModelResourceLocation(MODID + ":supply_grenade", "inventory"));
    }

    private static class Events {
        @SubscribeEvent
        public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
            event.getRegistry().register(
                    EntityEntryBuilder.create()
                            .entity(EntityAirdrop.class)
                            .id(new ResourceLocation(MODID, "airdrop"), 1)
                            .name(MODID + ".airdrop")
                            .tracker(512, 4, true)
                            .build());

            event.getRegistry().register(
                    EntityEntryBuilder.create()
                            .entity(EntitySupplyGrenade.class)
                            .id(new ResourceLocation(MODID, "supply_grenade"), 2)
                            .name(MODID + ".supply")
                            .tracker(64, 4, true)
                            .build());
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(SUPPLY_GRENADE);
        }
    }

    private static class GuiHandler implements IGuiHandler {

        @Override @Nullable
        public ContainerAirdrop getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            Entity entity = world.getEntityByID(id);
            if (entity instanceof EntityAirdrop) {
                return new ContainerAirdrop((EntityAirdrop) entity, player);
            }

            return null;
        }

        @Override @Nullable
        public GuiAirdrop getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            ContainerAirdrop container = getServerGuiElement(id, player, world, x, y, z);
            if(container != null) {
                return new GuiAirdrop(container);
            }
            return null;
        }
    }
}
