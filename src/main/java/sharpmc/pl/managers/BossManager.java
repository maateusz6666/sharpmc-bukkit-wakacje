package sharpmc.pl.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import sharpmc.pl.Main;
import sharpmc.pl.config.PluginConfig;
import sharpmc.pl.config.sections.MessagesSection;
import sharpmc.pl.objects.bosses.Boss;
import sharpmc.pl.objects.rewards.Reward;
import sharpmc.pl.utils.ChatUtil;
import sharpmc.pl.utils.SchematicUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BossManager {

    private static BossManager instance;

    private final PluginConfig pluginConfig = Main.getInstance().getPluginConfig();
    private final MessagesSection messages = pluginConfig.messages;
    private final Map<String, Boss> activeBosses = new ConcurrentHashMap<>();
    private final Map<String, String> bossBlocks = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private BossManager() {}

    public static BossManager getInstance() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    private String locationToKey(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    public void spawn(Location location, int health) {
        String bossId = UUID.randomUUID().toString();

        SchematicUtil.paste(this.pluginConfig.bossSchematic, location, blockLocations -> {
            if (blockLocations.isEmpty()) {
                Main.getInstance().getLogger().log(Level.WARNING, "Schemat jest pusty. Anulowano tworzenie bossa.");
                return;
            }

            Boss boss = new Boss(bossId, location.clone(), blockLocations);
            this.activeBosses.put(bossId, boss);

            for (Location blockLoc : blockLocations) {
                this.bossBlocks.put(locationToKey(blockLoc), boss.getId());
            }

            boss.setMaxHealth(health);
            boss.setCurrentHealth(health);
            startBossBarUpdater(boss);

            // Wiadomość o pojawieniu się bossa
            for (String line : this.messages.spawnBoss) {
                Bukkit.broadcastMessage(ChatUtil.coloredHex(line));
            }
        });
    }

    private void handleBossDeath(Boss boss) {
        Main.getInstance().getLogger().info("Boss " + boss.getId() + " został pokonany!");

        broadcastTopDamagers(boss);
        distributeRewards(boss);
        clearBossBlocks(boss.getAllBlockLocations());
        destroyBoss(boss.getId());
    }

    private void broadcastTopDamagers(Boss boss) {
        // Sortujemy graczy po zadanych obrażeniach (malejąco)
        List<Map.Entry<UUID, Integer>> topPlayers = boss.getPlayerDamage().entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Przygotowujemy dane do placeholderów
        String top1_name = "Brak", top2_name = "Brak", top3_name = "Brak";
        String top1_value = "0", top2_value = "0", top3_value = "0";

        if (topPlayers.size() > 0) {
            top1_name = Bukkit.getOfflinePlayer(topPlayers.get(0).getKey()).getName();
            top1_value = topPlayers.get(0).getValue().toString();
        }
        if (topPlayers.size() > 1) {
            top2_name = Bukkit.getOfflinePlayer(topPlayers.get(1).getKey()).getName();
            top2_value = topPlayers.get(1).getValue().toString();
        }
        if (topPlayers.size() > 2) {
            top3_name = Bukkit.getOfflinePlayer(topPlayers.get(2).getKey()).getName();
            top3_value = topPlayers.get(2).getValue().toString();
        }

        // Wysyłamy wiadomości, zamieniając placeholdery
        for (String line : this.messages.rewardBoss) {
            String formattedLine = line
                    .replace("{top1_name}", top1_name)
                    .replace("{top1_value}", top1_value)
                    .replace("{top2_name}", top2_name)
                    .replace("{top2_value}", top2_value)
                    .replace("{top3_name}", top3_name)
                    .replace("{top3_value}", top3_value);
            Bukkit.broadcastMessage(ChatUtil.coloredHex(formattedLine));
        }
    }

    public void despawnBoss(String bossId) {
        Boss boss = this.activeBosses.get(bossId);
        if (boss != null) {
            clearBossBlocks(boss.getAllBlockLocations());
            destroyBoss(bossId);

            // Wiadomość o zniknięciu bossa
            for (String line : this.messages.despawnBoss) {
                Bukkit.broadcastMessage(ChatUtil.coloredHex(line));
            }
        }
    }

    private void distributeRewards(Boss boss) {
        List<Reward> rewards = this.pluginConfig.rewards;
        for (Map.Entry<UUID, Integer> entry : boss.getPlayerDamage().entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;

            // Tutaj możesz dodać logikę wkładu, jeśli chcesz, aby szansa na nagrodę była od niego zależna
            // double contribution = boss.getPlayerContribution(entry.getKey());

            giveRewardsToPlayer(player, rewards);
        }
    }

    private void giveRewardsToPlayer(Player player, List<Reward> rewards) {
        for (Reward reward : rewards) {
            // Szansa na nagrodę
            if (this.random.nextDouble() * 100 < reward.getChance()) {
                for (String command : reward.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
                }
            }
        }
    }

    // ... reszta metod bez zmian ...
    private void clearBossBlocks(List<Location> locations) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Location loc : locations) {
                    if (loc.isWorldLoaded() && loc.getChunk().isLoaded()) {
                        loc.getBlock().setType(Material.AIR, false);
                    }
                }
                Main.getInstance().getLogger().info("Usunięto strukturę pokonanego bossa z " + locations.size() + " bloków.");
            }
        }.runTask(Main.getInstance());
    }

    public boolean isBossBlock(Location location) {
        return this.bossBlocks.containsKey(locationToKey(location));
    }

    public void handleBlockDamage(Player player, Location blockLocation) {
        String bossId = this.bossBlocks.get(locationToKey(blockLocation));
        if (bossId == null) return;
        Boss boss = this.activeBosses.get(bossId);
        if (boss == null || !boss.isActive() || boss.isDead()) return;
        boss.damage(player, 1);
        if (boss.isDead()) {
            handleBossDeath(boss);
        }
    }

    public void visualizeBossBlocks(Player player) {
        if (bossBlocks.isEmpty()) {
            player.sendMessage(ChatUtil.coloredHex("&c&lBOSS &8× &7Brak aktywnych bloków do zwizualizowania."));
            return;
        }
        final Map<Location, BlockData> originalBossBlocks = new HashMap<>();
        player.sendMessage(ChatUtil.coloredHex("&a&lBOSS &8× &7Wizualizacja aktywnych bloków..."));
        for (String key : this.bossBlocks.keySet()) {
            try {
                String[] parts = key.split(";");
                World world = Bukkit.getWorld(parts[0]);
                if (world == null) continue;
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                Location loc = new Location(world, x, y, z);
                if (!loc.isWorldLoaded() || !loc.getChunk().isLoaded()) continue;
                originalBossBlocks.put(loc, loc.getBlock().getBlockData().clone());
                loc.getBlock().setType(Material.GLASS, false);
            } catch (Exception e) {
                Main.getInstance().getLogger().warning("Błąd podczas parsowania klucza lokalizacji dla wizualizacji: " + key);
            }
        }
        player.sendMessage(ChatUtil.coloredHex("&a&lBOSS &8× &7Bloki bossa zostały tymczasowo zamienione w szkło. Zostaną przywrócone za &e15 sekund&7."));
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, BlockData> entry : originalBossBlocks.entrySet()) {
                    entry.getKey().getBlock().setBlockData(entry.getValue(), false);
                }
                player.sendMessage(ChatUtil.coloredHex("&a&lBOSS &8× &7Przywrócono oryginalne bloki."));
            }
        }.runTaskLater(Main.getInstance(), 15 * 20L);
    }

    private void startBossBarUpdater(Boss boss) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isActive() || boss.isDead()) {
                    //boss.getBossBar().getPlayers().forEach(player -> {
                        //if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                            //player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                        //}
                    //});
                    this.cancel();
                    return;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(boss.getLocation().getWorld()) && player.getLocation().distance(boss.getLocation()) <= 50) {
                        boss.addPlayerToBossBar(player);
                        //player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0, true, false));
                    } else {
                        boss.removePlayerFromBossBar(player);
                        //if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                            //player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                        //}
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    public void destroyBoss(String bossId) {
        Boss boss = this.activeBosses.remove(bossId);
        if (boss != null) {
            //boss.getBossBar().getPlayers().forEach(player -> {
                //if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                    //player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                //}
            //});
            boss.destroy();
            this.bossBlocks.entrySet().removeIf(entry -> entry.getValue().equals(bossId));
        }
    }

    public void destroyAllBosses() {
        new ArrayList<>(this.activeBosses.keySet()).forEach(this::destroyBoss);
    }

    public Collection<Boss> getActiveBosses() {
        return this.activeBosses.values();
    }

    public boolean isBossActive() {
        return !this.activeBosses.isEmpty();
    }

    public Boss getBoss(String id) {
        return this.activeBosses.get(id);
    }
}