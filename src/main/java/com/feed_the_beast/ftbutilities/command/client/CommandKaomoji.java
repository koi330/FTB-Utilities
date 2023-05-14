package com.feed_the_beast.ftbutilities.command.client;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import com.feed_the_beast.ftblib.lib.client.ClientUtils;
import com.feed_the_beast.ftblib.lib.command.CmdBase;
import com.feed_the_beast.ftblib.lib.util.StringJoiner;

/**
 * @author LatvianModder
 */
public class CommandKaomoji extends CmdBase {

    private final String emoji;

    public CommandKaomoji(String n, String e) {
        super(n, Level.ALL);
        emoji = e;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String txt = StringJoiner.with(' ').joinStrings(args);

        if (txt.isEmpty()) {
            ClientUtils.execClientCommand(emoji);
        } else {
            ClientUtils.execClientCommand(txt + " " + emoji);
        }
    }
}
