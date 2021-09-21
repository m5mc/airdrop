package net.quantium.airdrop.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.quantium.airdrop.ModProvider;

import java.util.Random;

public class AirdropLevelCondition implements LootCondition
{
    private static int level;
    public static void setLevel(int level) {
        AirdropLevelCondition.level = level;
    }

    private final int minLevel;
    private final int maxLevel;

    public AirdropLevelCondition(int minLevel, int maxLevel)
    {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public boolean testCondition(Random rand, LootContext context)
    {
        return level >= this.minLevel && level <= this.maxLevel;
    }

    public static class Serializer extends LootCondition.Serializer<AirdropLevelCondition>
    {
        public Serializer()
        {
            super(new ResourceLocation(ModProvider.MODID, "airdrop_level"), AirdropLevelCondition.class);
        }

        public void serialize(JsonObject json, AirdropLevelCondition value, JsonSerializationContext context)
        {
            json.addProperty("min", value.minLevel);
            json.addProperty("max", value.maxLevel);
        }

        public AirdropLevelCondition deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return new AirdropLevelCondition(JsonUtils.getInt(json, "min", Integer.MIN_VALUE), JsonUtils.getInt(json, "max", Integer.MAX_VALUE));
        }
    }
}
