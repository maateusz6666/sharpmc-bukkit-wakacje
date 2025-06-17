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
    private final PluginConfig pluginConfig = Main.getInstance().getPluginConfig();
    private final BossManager bossManager = BossManager.getInstance();

    @Execute(name = "reload")
    public void reload(@Context Player player) {
        Main.getInstance().getPluginConfig().load();
        ChatUtil.successMessage(player, "Konfiguracja została pomyślnie przeładowana.");
    }

    @Execute(name = "spawn")
    public void spawn(@Context Player player) {
        this.bossManager.spawn(pluginConfig.bossLocation, pluginConfig.bossHealth);
        ChatUtil.successMessage(player, "Ręcznie stworzono bossa w lokalizacji z konfiguracji.");
    }

    @Execute(name = "location")
    public void location(@Context Player player) {
        pluginConfig.bossLocation = player.getLocation();
        pluginConfig.save();
        pluginConfig.load();

        ChatUtil.successMessage(player, "Ustawiono nową lokalizację bossa w konfiguracji.");
    }

    @Execute(name = "visualize")
    public void visualize(@Context Player player) {
        this.bossManager.visualizeBossBlocks(player);
    }
}