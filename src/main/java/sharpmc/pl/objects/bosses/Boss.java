package sharpmc.pl.objects.bosses;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import sharpmc.pl.Main;
import sharpmc.pl.config.sections.BossBarSection;

import java.util.HashMap;
import java.util.List; // Dodajemy import
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Boss {

    private final String id;
    private final Location location;
    private final List<Location> allBlockLocations; // NOWE POLE: przechowuje wszystkie bloki bossa
    private double maxHealth;
    private double currentHealth;
    private final BossBar bossBar;
    private final Map<UUID, Integer> playerDamage;
    private boolean isActive;
    private final long spawnTime;

    /**
     * Zaktualizowany konstruktor, który przyjmuje listę bloków.
     */
    public Boss(String id, Location location, List<Location> allBlockLocations) {
        this.id = id;
        this.location = location;
        this.allBlockLocations = allBlockLocations; // Przypisujemy listę do nowego pola
        this.playerDamage = new HashMap<>();
        this.isActive = true;
        this.spawnTime = System.currentTimeMillis();

        BossBarSection bossBarConfig = Main.getInstance().getPluginConfig().bossBar;
        this.bossBar = Bukkit.createBossBar(
                formatBossBarTitle(),
                bossBarConfig.color,
                bossBarConfig.style
        );
    }

    public void damage(Player player, int amount) {
        if (!this.isActive) return;

        this.playerDamage.put(player.getUniqueId(), this.playerDamage.getOrDefault(player.getUniqueId(), 0) + amount);
        this.currentHealth = Math.max(0, this.currentHealth - amount);

        updateBossBar();
    }

    public void updateBossBar() {
        double progress = this.maxHealth > 0 ? Math.max(0.0, Math.min(1.0, this.currentHealth / this.maxHealth)) : 0.0;
        this.bossBar.setProgress(progress);
        this.bossBar.setTitle(formatBossBarTitle());
    }

    private String formatBossBarTitle() {
        BossBarSection bossBarConfig = Main.getInstance().getPluginConfig().bossBar;
        return sharpmc.pl.utils.ChatUtil.coloredHex(bossBarConfig.title
                .replace("{current_health}", String.format("%.0f", this.currentHealth))
                .replace("{max_health}", String.format("%.0f", this.maxHealth))
        );
    }

    public void addPlayerToBossBar(Player player) {
        if (!this.bossBar.getPlayers().contains(player)) {
            this.bossBar.addPlayer(player);
        }
    }

    public void removePlayerFromBossBar(Player player) {
        this.bossBar.removePlayer(player);
    }

    public void destroy() {
        this.isActive = false;
        this.bossBar.removeAll();
    }

    public boolean isDead() {
        return this.currentHealth <= 0;
    }

    public int getTotalDamageDealt() {
        return this.playerDamage.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double getPlayerContribution(UUID uniqueId) {
        int totalDmg = getTotalDamageDealt();
        return totalDmg > 0 ? (double) this.playerDamage.getOrDefault(uniqueId, 0) / totalDmg : 0.0;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
        updateBossBar();
    }

    public void setCurrentHealth(double currentHealth) {
        this.currentHealth = currentHealth;
        updateBossBar();
    }
}