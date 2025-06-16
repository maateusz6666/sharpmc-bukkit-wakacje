package sharpmc.pl.objects.bosses;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import sharpmc.pl.Main;
import sharpmc.pl.config.PluginConfig;
import sharpmc.pl.utils.ChatUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Boss {
    private final PluginConfig pluginConfig = Main.getInstance().getPluginConfig();

    private String id;
    private Location location;
    private double maxHealth;
    private double currentHealth;
    private BossBar bossBar;
    private Map<UUID, Integer> playerDamage;
    private boolean isActive;
    private long spawnTime;

    public Boss(String id, Location location, double maxHealth) {
        this.id = id;
        this.location = location;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.playerDamage = new HashMap<>();
        this.isActive = true;
        this.spawnTime = System.currentTimeMillis();

        this.bossBar = Bukkit.createBossBar(
                ChatUtil.coloredHex(
                        pluginConfig.bossBar.title
                                .replace("{current_health}", String.format("%.0f", currentHealth))
                                .replace("{max_health}", String.format("%.0f", maxHealth))
                ),
                pluginConfig.bossBar.color,
                pluginConfig.bossBar.style
        );
        this.bossBar.setProgress(1.0);
    }

    public void damage(Player player, int amount) {
        if (!isActive) return;

        // Zapisz obrażenia gracza
        playerDamage.put(player.getUniqueId(), playerDamage.getOrDefault(player.getUniqueId(), 0) + amount);

        // POPRAWKA: Odejmij HP od bossa
        currentHealth = Math.max(0, currentHealth - amount);

        // Aktualizuj boss bar
        updateBossBar();

        // Log dla debugowania
        Main.getInstance().getLogger().info("Boss " + id + " otrzymał " + amount + " obrażeń od " + player.getName() +
                ". HP: " + currentHealth + "/" + maxHealth);

        // Sprawdź czy boss umarł
        if (currentHealth <= 0) {
            isActive = false;
            Main.getInstance().getLogger().info("Boss " + id + " został pokonany!");
        }
    }

    public void updateBossBar() {
        // POPRAWKA: Poprawne obliczenie progresu
        double progress = maxHealth > 0 ? Math.max(0.0, Math.min(1.0, currentHealth / maxHealth)) : 0.0;
        bossBar.setProgress(progress);

        // Aktualizuj tytuł z aktualnym HP
        bossBar.setTitle(
                ChatUtil.coloredHex(
                        pluginConfig.bossBar.title
                                .replace("{current_health}", String.format("%.0f", currentHealth))
                                .replace("{max_health}", String.format("%.0f", maxHealth))
                )
        );
    }

    public void addPlayerToBossBar(Player player) {
        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }
    }

    public void removePlayerFromBossBar(Player player) {
        bossBar.removePlayer(player);
    }

    public void destroy() {
        isActive = false;
        bossBar.removeAll();
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    public int getTotalDamageDealt() {
        return playerDamage.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double getPlayerContribution(UUID uniqueId) {
        int playerDmg = playerDamage.getOrDefault(uniqueId, 0);
        int totalDmg = getTotalDamageDealt();
        return totalDmg > 0 ? (double) playerDmg / totalDmg : 0.0;
    }

    // Getter dla obrażeń graczy
    public int getPlayerDamage(UUID playerId) {
        return playerDamage.getOrDefault(playerId, 0);
    }

    // Dodane settery dla maxHealth i currentHealth
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
        updateBossBar(); // Aktualizuj boss bar po zmianie maxHealth
    }

    public void setCurrentHealth(double currentHealth) {
        this.currentHealth = currentHealth;
        updateBossBar(); // Aktualizuj boss bar po zmianie currentHealth
    }
}