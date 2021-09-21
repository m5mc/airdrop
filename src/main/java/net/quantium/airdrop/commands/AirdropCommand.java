package net.quantium.airdrop.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.quantium.airdrop.manager.AirdropHandle;
import net.quantium.airdrop.manager.AirdropSpawner;

public class AirdropCommand extends CommandBase {

    public String getName()
    {
        return "airdrop";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getUsage(ICommandSender sender)
    {
        return "qairdrop.command.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args[0].equalsIgnoreCase("force")) {
            AirdropHandle handle = AirdropSpawner.get().dropNonCalledAirdrop();
            if(handle == null) sender.sendMessage(new TextComponentString("Failed to spawn airdrop here"));
            else sender.sendMessage(new TextComponentString(String.format("Airdrop forced at %.0f %.0f", handle.getPositionX(), handle.getPositionZ())));
        } else if (args[0].equalsIgnoreCase("call")) {
            int x = sender.getPosition().getX();
            int z = sender.getPosition().getZ();
            int level = AirdropSpawner.get().getLootLevel();
            int time = AirdropSpawner.get().getDropTime();

            if(args.length >= 3) {
                x = (int)parseDouble(x, args[1], -30000000, 30000000, true);
                z = (int)parseDouble(z, args[2], -30000000, 30000000, true);
            }

            if(args.length >= 4) {
                level = parseInt(args[3]);
            }

            if(args.length >= 5) {
                time = parseInt(args[4]);
            }

            AirdropHandle handle = AirdropSpawner.get().dropAirdrop((WorldServer) sender.getEntityWorld(), x, z, level, time, true);
            if(handle == null) sender.sendMessage(new TextComponentString("Failed to spawn airdrop here"));
            else sender.sendMessage(new TextComponentString(String.format("Airdrop called at %.0f %.0f", handle.getPositionX(), handle.getPositionZ())));
        }
    }
}
