package ftb.utils.mod.cmd.admin;

import ftb.lib.api.cmd.*;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;

public class CmdInvseeEnderchest extends CommandLM {
    public CmdInvseeEnderchest() {
        super("invsee_enderchest", CommandLevel.OP);
    }

    public String getCommandUsage(ICommandSender ics) {
        return '/' + commandName + " <player>";
    }

    public Boolean getUsername(String[] args, int i) {
        return (i == 0) ? Boolean.TRUE : null;
    }

    public IChatComponent onCommand(ICommandSender ics, String[] args) throws CommandException {
        checkArgs(args, 1);
        EntityPlayerMP ep0 = getCommandSenderAsPlayer(ics);
        EntityPlayerMP ep = getPlayer(ics, args[0]);
        ep0.displayGUIChest(ep.getInventoryEnderChest());
        return null;
    }
}
