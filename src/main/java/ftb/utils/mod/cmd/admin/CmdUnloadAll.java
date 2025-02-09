package ftb.utils.mod.cmd.admin;

import ftb.lib.api.cmd.*;
import ftb.utils.world.*;
import ftb.utils.world.claims.ClaimedChunk;
import net.minecraft.command.*;
import net.minecraft.util.*;

public class CmdUnloadAll extends CommandLM {
    public CmdUnloadAll() {
        super("unload_all", CommandLevel.OP);
    }

    public String getCommandUsage(ICommandSender ics) {
        return '/' + commandName + " <player | @a>";
    }

    public Boolean getUsername(String[] args, int i) {
        return (i == 0) ? Boolean.FALSE : null;
    }

    public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException {
        checkArgs(args, 1);

        if (args[0].equals("@a")) {
            for (ClaimedChunk c : LMWorldServer.inst.claimedChunks.getAllChunks()) c.isChunkloaded = false;
            for (LMPlayer p : LMWorldServer.inst.getAllOnlinePlayers())
                p.toPlayerMP().sendUpdate();
            return new ChatComponentText("Unloaded all chunks");
        }

        LMPlayerServer p = LMPlayerServer.get(args[0]);
        for (ClaimedChunk c : LMWorldServer.inst.claimedChunks.getChunks(p, null)) c.isChunkloaded = false;
        if (p.isOnline()) p.sendUpdate();
        return new ChatComponentText("Unloaded all " + p.getProfile().getName() + "'s chunks");
    }
}
