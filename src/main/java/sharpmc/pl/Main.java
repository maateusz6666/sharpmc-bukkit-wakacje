package sharpmc.pl;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import sharpmc.pl.commands.command.BossCommand;
import sharpmc.pl.commands.handler.InvalidUsageHandler;
import sharpmc.pl.commands.handler.MissingPermissionHandler;
import sharpmc.pl.config.PluginConfig;
import sharpmc.pl.config.factory.ConfigurationFactory;
import sharpmc.pl.listeners.BossListener;
import sharpmc.pl.objects.rewards.RewardSerializer;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    @Getter
    private PluginConfig pluginConfig;
    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        instance = this;

        ConfigurationFactory configurationFactory = new ConfigurationFactory(getDataFolder());
        pluginConfig = configurationFactory.produce(PluginConfig.class, "config.yml", new RewardSerializer());

        getServer().getPluginManager().registerEvents(new BossListener(), this);

        liteCommands = LiteBukkitFactory.builder()
                .settings(setings -> setings.fallbackPrefix("sharpmc").nativePermissions(false))
                .commands(
                        new BossCommand()
                )
                .message(LiteBukkitMessages.PLAYER_NOT_FOUND, input -> "&4&l❌ &8× &cGracz &4" + input + " &cnie został odnaleziony!")
                .invalidUsage(new InvalidUsageHandler())
                .missingPermission(new MissingPermissionHandler())
                .build();
    }

    @Override
    public void onDisable() {
        if (liteCommands != null) liteCommands.unregister();
    }
}
