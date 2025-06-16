package sharpmc.pl.config.factory;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerdesRegistry;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.configs.yaml.bukkit.serdes.serializer.ItemStackSerializer;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class ConfigurationFactory {

    private final File defaultDir;

    public ConfigurationFactory(File defaultDir) {
        this.defaultDir = defaultDir;
    }

    public <T extends OkaeriConfig> T produce(Class<T> clazz, String file, ObjectSerializer<?>... serializers) {
        return this.produce(clazz, new File(this.defaultDir, file), serializers);
    }

    public <T extends OkaeriConfig> T produce(Class<T> clazz, File folderName, String file, ObjectSerializer<?>... serializers) {
        return this.produce(clazz, new File(folderName, file), serializers);
    }

    private void registerSerializers(SerdesRegistry registry, ObjectSerializer<?>... serializers) {
        registry.register(new SerdesBukkit());
        registry.registerExclusive(ItemStack.class, new ItemStackSerializer(true));
        for (ObjectSerializer<?> serializer : serializers) {
            registry.register(serializer);
        }
    }

    public <T extends OkaeriConfig> T produce(Class<T> clazz, File file, ObjectSerializer<?>... serializers) {
        return ConfigManager.create(clazz, it -> it
                .withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit(), registry -> {
                    registerSerializers(registry, serializers);
                })
                .withBindFile(file)
                .saveDefaults()
                .load(true)
        );
    }
}
