package sharpmc.pl.commands.handler;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import org.bukkit.command.CommandSender;
import sharpmc.pl.utils.ChatUtil;

public class InvalidUsageHandler implements dev.rollczi.litecommands.invalidusage.InvalidUsageHandler<CommandSender> {
    @Override
    public void handle(Invocation<CommandSender> invocation, InvalidUsage<CommandSender> commandSenderInvalidUsage, ResultHandlerChain<CommandSender> resultHandlerChain) {
        CommandSender commandSender = invocation.sender();
        Schematic schematic = commandSenderInvalidUsage.getSchematic();

        if (schematic.isOnlyFirst()) {
            commandSender.sendMessage(ChatUtil.coloredHex("&4&l❌ &8× &cPoprawne użycie: &4" + schematic.first()));
            return;
        }

        commandSender.sendMessage(ChatUtil.coloredHex("&4&l❌ &8× &cPoprawne użycie:"));
        for (String scheme : schematic.all())
            commandSender.sendMessage(ChatUtil.coloredHex(" &8- &4" + scheme));
    }
}
