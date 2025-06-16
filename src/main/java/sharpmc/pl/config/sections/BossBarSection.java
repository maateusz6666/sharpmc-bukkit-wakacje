package sharpmc.pl.config.sections;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class BossBarSection extends OkaeriConfig {
    public String title = "{current_health}/{max_health}";
    public BarColor color = BarColor.RED;
    public BarStyle style = BarStyle.SOLID;
}
