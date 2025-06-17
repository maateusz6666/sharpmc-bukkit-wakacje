package sharpmc.pl.config.sections;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import sharpmc.pl.utils.builders.ListBuilder;

import java.util.List;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class MessagesSection extends OkaeriConfig {
    public List<String> spawnBoss = new ListBuilder<String>()
            .put("Boss sie pojawił")
            .build();
    public List<String> despawnBoss = new ListBuilder<String>()
            .put("Mineło zbyt dużo czasu nikt go nie zajebał wiec znikł")
            .build();
    public List<String> rewardBoss = new ListBuilder<String>()
            .put("ZADANE OBRAZENIA")
            .put("1. {top1_name} - {top1_value}")
            .put("2. {top2_name} - {top2_value}")
            .put("3. {top3_name} - {top3_value}")
            .build();
}
