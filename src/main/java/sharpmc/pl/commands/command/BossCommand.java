package sharpmc.pl.commands.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;
import sharpmc.pl.Main;
import sharpmc.pl.config.PluginConfig;
import sharpmc.pl.managers.BossManager;
import sharpmc.pl.utils.ChatUtil;

@Command(name = "boss")
@Permission("sharpmc.bukkit.boss")
public class BossCommand {
    private final BossManager bossManager = BossManager.getInstance();
    private final PluginConfig pluginConfig = Main.getInstance().getPluginConfig();

    @Execute(name = "reload")
    public void reload(@Context Player player) {
        pluginConfig.load();

        ChatUtil.successMessage(player, "Konfiguracja została pomyślnie przeładowana.");
    }

    @Execute(name = "spawn")
    public void spawn(@Context Player player) {
        bossManager.spawn(player.getLocation().clone());
    }
}
