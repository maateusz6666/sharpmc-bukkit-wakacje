package sharpmc.pl.commands.handler;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.permission.MissingPermissions;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sharpmc.pl.utils.ChatUtil;

public class MissingPermissionHandler implements dev.rollczi.litecommands.permission.MissingPermissionsHandler<CommandSender> {
    @Override
    public void handle(Invocation<CommandSender> invocation, MissingPermissions missingPermissions, ResultHandlerChain<CommandSender> resultHandlerChain) {
        CommandSender commandSender = invocation.sender();
        String permissions = missingPermissions.asJoinedText();

        Player player = (Player) commandSender;

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        commandSender.sendMessage(ChatUtil.coloredHex("&4&l❌ &8× &cNie posiadasz permisji (&4" + permissions + "&c) do tej komendy."));
    }
}
