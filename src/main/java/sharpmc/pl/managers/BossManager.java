package sharpmc.pl.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import sharpmc.pl.Main;
import sharpmc.pl.config.PluginConfig;
import sharpmc.pl.objects.bosses.Boss;
import sharpmc.pl.objects.rewards.Reward;
import sharpmc.pl.utils.SchematicUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BossManager {
    private final PluginConfig pluginConfig = Main.getInstance().getPluginConfig();
    private final Map<String, Boss> activeBosses = new ConcurrentHashMap<>();
    private final Map<Location, String> bossBlocks = new ConcurrentHashMap<>(); // Lokacja bloku -> ID bossa
    private final Random random = new Random();

    private static BossManager instance;

    private BossManager() {}

    public static BossManager getInstance() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public void spawn(Location location) {
        String bossId = UUID.randomUUID().toString();

        Boss boss = new Boss(bossId, location.clone(), 1000.0);
        activeBosses.put(bossId, boss);

        Main.getInstance().getLogger().info("Rozpoczynam tworzenie bossa o ID: " + bossId + " w lokacji: " +
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());

        // Callback po wklejeniu schematyki
        SchematicUtil.paste(pluginConfig.bossSchematic, location, () -> {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                registerBossBlocks(boss, location);
                startBossBarUpdater(boss);
                Main.getInstance().getLogger().info("Boss " + bossId + " został pomyślnie utworzony!");
            });
        });
    }

    private void registerBossBlocks(Boss boss, Location centerLocation) {
        // Skanuj obszar wokół lokacji spawnu bossa (np. 50x50x20)
        int radius = 25;
        int height = 10;

        int registeredBlocks = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -height; y <= height; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location blockLoc = centerLocation.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();

                    // Rejestruj tylko bloki które nie są powietrzem i można je wykopać
                    if (block.getType() != Material.AIR &&
                            block.getType().isBlock() &&
                            block.getType().getHardness() >= 0) { // Hardness >= 0 oznacza że można zniszczyć

                        bossBlocks.put(blockLoc, boss.getId());
                        registeredBlocks++;
                    }
                }
            }
        }

        Main.getInstance().getLogger().info("Zarejestrowano " + registeredBlocks + " bloków dla bossa " + boss.getId());

        // POPRAWKA: Ustaw HP bossa na podstawie liczby bloków
        // Każdy blok = 1 HP, ale wyświetlaj jako procenty w boss barze
        boss.setMaxHealth(registeredBlocks);
        boss.setCurrentHealth(registeredBlocks);
        boss.updateBossBar();
    }

    private void startBossBarUpdater(Boss boss) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isActive() || boss.isDead()) {
                    this.cancel();
                    return;
                }

                // Sprawdź graczy w pobliżu (50 bloków)
                List<Player> nearbyPlayers = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(boss.getLocation().getWorld()) &&
                            player.getLocation().distance(boss.getLocation()) <= 50) {
                        nearbyPlayers.add(player);
                        boss.addPlayerToBossBar(player);
                    } else {
                        boss.removePlayerFromBossBar(player);
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L); // Co sekundę
    }

    public boolean isBossBlock(Location location) {
        return bossBlocks.containsKey(location);
    }

    public void handleBlockBreak(Player player, Location blockLocation) {
        String bossId = bossBlocks.get(blockLocation);
        if (bossId == null) return;

        Boss boss = activeBosses.get(bossId);
        if (boss == null || !boss.isActive()) return;

        // POPRAWKA: Usuń blok z mapy PRZED zadaniem obrażeń
        bossBlocks.remove(blockLocation);

        // Zadaj obrażenia bossowi (1 blok = 1 HP)
        boss.damage(player, 1);

        Main.getInstance().getLogger().info("Gracz " + player.getName() + " zniszczył blok bossa. " +
                "Obecne HP: " + boss.getCurrentHealth() + "/" + boss.getMaxHealth());

        // Sprawdź czy boss nie umarł
        if (boss.isDead()) {
            handleBossDeath(boss);
        }
    }

    private void handleBossDeath(Boss boss) {
        Main.getInstance().getLogger().info("Boss " + boss.getId() + " został pokonany!");

        // Rozdaj nagrody
        distributeRewards(boss);

        // Usuń bossa
        destroyBoss(boss.getId());

        // Wyślij wiadomość wszystkim graczom
        Bukkit.broadcastMessage("§c§lBOSS ZOSTAŁ POKONANY!");
    }

    private void distributeRewards(Boss boss) {
        List<Reward> rewards = Main.getInstance().getPluginConfig().rewards;

        for (Map.Entry<UUID, Integer> entry : boss.getPlayerDamage().entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;

            double contribution = boss.getPlayerContribution(entry.getKey());
            int damageDealt = entry.getValue();

            player.sendMessage("§aWykopałeś §e" + damageDealt + " §abloków (§e" +
                    String.format("%.1f", contribution * 100) + "%§a wkładu)");

            // Losuj nagrody na podstawie wkładu
            giveRewardsToPlayer(player, rewards, contribution);
        }
    }

    private void giveRewardsToPlayer(Player player, List<Reward> rewards, double contribution) {
        for (Reward reward : rewards) {
            // Zwiększ szansę na nagrodę w zależności od wkładu gracza
            double adjustedChance = reward.getChance() * (contribution + 0.1); // Minimum 10% szansy

            if (random.nextDouble() * 100 < adjustedChance) {
                player.getInventory().addItem(reward.getItemStack().clone());
                player.sendMessage("§aOtrzymałeś nagrodę: §e" + reward.getName());
            }
        }
    }

    public void destroyBoss(String bossId) {
        Boss boss = activeBosses.remove(bossId);
        if (boss != null) {
            boss.destroy();

            // Usuń wszystkie bloki tego bossa
            bossBlocks.entrySet().removeIf(entry -> entry.getValue().equals(bossId));
        }
    }

    public void destroyAllBosses() {
        for (String bossId : new ArrayList<>(activeBosses.keySet())) {
            destroyBoss(bossId);
        }
    }

    public Collection<Boss> getActiveBosses() {
        return activeBosses.values();
    }

    public Boss getBoss(String id) {
        return activeBosses.get(id);
    }
}