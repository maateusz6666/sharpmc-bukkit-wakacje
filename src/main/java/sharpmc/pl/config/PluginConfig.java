package sharpmc.pl.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import org.bukkit.Material;
import org.bukkit.boss.BossBar;
import sharpmc.pl.config.sections.BossBarSection;
import sharpmc.pl.objects.rewards.Reward;
import sharpmc.pl.utils.builders.ItemBuilder;
import sharpmc.pl.utils.builders.ListBuilder;

import java.util.List;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class PluginConfig extends OkaeriConfig {

    @Comment("Tutaj znajduje się cała konfiguracja odnosząca się do bosów.")
    public String bossSchematic = "boss.schem";
    public List<String> bossTime = new ListBuilder<String>()
            .put("17:00")
            .put("18:00")
            .put("19:00")
            .build();
    public BossBarSection bossBar = new BossBarSection();

    @Comment({"", "Tutaj znajdują się przedmioty które możę wylosować gracz podczas niszczenia bossa."})
    public List<Reward> rewards = new ListBuilder<Reward>()
            .put(
                    new Reward(
                            "test",
                            100.0,
                            new ItemBuilder(Material.DIRT).toItemStack()
                    )
            )
            .build();
}
