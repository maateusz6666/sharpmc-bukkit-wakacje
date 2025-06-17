package sharpmc.pl.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import sharpmc.pl.Main;
import sharpmc.pl.config.PluginConfig;
import sharpmc.pl.managers.BossManager;
import sharpmc.pl.objects.bosses.Boss;
import sharpmc.pl.utils.ChatUtil; // Upewnij się, że ten import jest dodany

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.logging.Logger; // Upewnij się, że ten import jest dodany

public class BossScheduler {

    private final Main plugin;
    private final PluginConfig pluginConfig;
    private final BossManager bossManager;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Logger logger = Main.getInstance().getLogger(); // Dodajemy logger do debugowania

    public BossScheduler(Main plugin, PluginConfig pluginConfig, BossManager bossManager) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.bossManager = bossManager;
    }

    public void start() {
        // Główne zadanie do spawnowania i despawnowania bossa
        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndSpawnBoss();
                checkAndDespawnInactiveBosses();
            }
        }.runTaskTimer(plugin, 0L, 20L * 30); // Sprawdzaj co 30 sekund

        startKnockbackTask();
    }

    private void startKnockbackTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!bossManager.isBossActive()) {
                    return; // Jeśli nie ma bossa, nic nie rób
                }

                for (Boss boss : bossManager.getActiveBosses()) {
                    Location bossCenter = boss.getLocation();
                    double knockbackRadius = 30; // Promień odpychania
                    double upwardPower = 1;      // Siła w górę (lekko zwiększona)
                    double horizontalPower = 2;  // Siła w poziomie (lekko zwiększona)

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.getWorld().equals(bossCenter.getWorld())) {
                            continue;
                        }

                        double distance = player.getLocation().distance(bossCenter);
                        if (distance <= knockbackRadius) {
                            // Obliczamy wektor
                            Vector direction = player.getLocation().toVector().subtract(bossCenter.toVector()).normalize();

                            // Zerujemy wektor Y, aby siła w górę była zawsze taka sama
                            direction.setY(0);

                            // Stosujemy siłę w poziomie i dodajemy siłę wertykalną
                            direction.multiply(horizontalPower).add(new Vector(0, upwardPower, 0));

                            // Odepchnij gracza
                            player.setVelocity(direction);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 20 * 20L); // Start po 5s, powtarzaj co 4 sekundy
    }

    // ... (reszta metod bez zmian) ...
    private void checkAndSpawnBoss() {
        if (!bossManager.isBossActive()) {
            String currentTime = LocalTime.now().format(timeFormatter);
            if (pluginConfig.bossTime.contains(currentTime)) {
                plugin.getLogger().info("Nadszedł czas na stworzenie bossa (" + currentTime + ").");
                bossManager.spawn(pluginConfig.bossLocation, pluginConfig.bossHealth);
            }
        }
    }

    private void checkAndDespawnInactiveBosses() {
        if (!bossManager.isBossActive()) {
            return;
        }
        Collection<Boss> activeBosses = bossManager.getActiveBosses();
        long despawnTimeMillis = 3600000;
        for (Boss boss : activeBosses) {
            long timeSinceSpawn = System.currentTimeMillis() - boss.getSpawnTime();
            if (timeSinceSpawn > despawnTimeMillis) {
                plugin.getLogger().info("Boss " + boss.getId() + " był nieaktywny przez ponad godzinę. Usuwanie...");
                bossManager.despawnBoss(boss.getId());
            }
        }
    }
}