// src/main/java/sharpmc/pl/managers/BossManager.java
package sharpmc.pl.managers;

import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import sharpmc.pl.Main;
import sharpmc.pl.config.PluginConfig;
import sharpmc.pl.objects.bosses.Boss;
import sharpmc.pl.objects.rewards.Reward;
import sharpmc.pl.utils.SchematicUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BossManager {

    private final Main plugin;
    private final PluginConfig pluginConfig;
    private final Map<String, Boss> activeBosses = new ConcurrentHashMap<>();
    private final Map<Location, String> bossBlocks = new ConcurrentHashMap<>();

    // NOWE: Przechowuje sesję edycji z FAWE dla każdego bossa, aby móc cofnąć wklejenie
    private final Map<String, EditSession> bossEditSessions = new ConcurrentHashMap<>();

    private final Random random = new Random();

    public BossManager(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    public void spawn(Location location) {
        String bossId = UUID.randomUUID().toString();
        // Tworzymy bossa z tymczasowym HP, zostanie ono nadpisane po wklejeniu schematu
        Boss boss = new Boss(bossId, location.clone(), 1.0);
        activeBosses.put(bossId, boss);

        Main.getInstance().getLogger().info("Rozpoczynam tworzenie bossa o ID: " + bossId + "...");

        // Używamy zmodyfikowanej metody paste, która przekazuje nam dane z FAWE (clipboard i editSession)
        SchematicUtil.paste(pluginConfig.bossSchematic, location, (clipboard, editSession) -> {
            if (clipboard == null || editSession == null) {
                Main.getInstance().getLogger().severe("Nie udało się wkleić schematu dla bossa: " + bossId);
                activeBosses.remove(bossId); // Anuluj tworzenie bossa, jeśli wklejanie się nie powiodło
                return;
            }

            // Zapisujemy sesję edycji, aby móc później usunąć strukturę
            bossEditSessions.put(bossId, editSession);

            // Rejestrujemy bloki i ustawiamy prawidłowe HP na podstawie danych ze schematu
            registerBossBlocksFromClipboard(boss, clipboard, location);
            startBossBarUpdater(boss);

            Main.getInstance().getLogger().info("Boss " + bossId + " został pomyślnie utworzony!");
        });
    }

    // NOWA METODA: Rejestruje bloki na podstawie danych ze schamtu, a nie przez skanowanie świata
    private void registerBossBlocksFromClipboard(Boss boss, Clipboard clipboard, Location pasteLocation) {
        int registeredBlocks = 0;
        BlockVector3 clipboardOrigin = clipboard.getOrigin();
        BlockVector3 pasteCenter = BlockVector3.at(pasteLocation.getX(), pasteLocation.getY(), pasteLocation.getZ());

        for (BlockVector3 blockPosInClipboard : clipboard.getRegion()) {
            if (!clipboard.getBlock(blockPosInClipboard).getBlockType().equals(BlockTypes.AIR)) {

                BlockVector3 relativePos = blockPosInClipboard.subtract(clipboardOrigin);
                BlockVector3 finalPos = pasteCenter.add(relativePos);

                // OSTATECZNA POPRAWKA: Użycie bezpośredniego dostępu do pól .x, .y, .z
                Location blockLoc = new Location(
                        pasteLocation.getWorld(),
                        finalPos.x(), // Zamiast .getBlockX()
                        finalPos.y(), // Zamiast .getBlockY()
                        finalPos.z()  // Zamiast .getBlockZ()
                );

                bossBlocks.put(blockLoc, boss.getId());
                registeredBlocks++;
            }
        }

        Main.getInstance().getLogger().info("Zarejestrowano " + registeredBlocks + " bloków dla bossa " + boss.getId());

        boss.setMaxHealth(registeredBlocks);
        boss.setCurrentHealth(registeredBlocks);
        boss.updateBossBar();
    }

    public void handleBlockBreak(Player player, Location blockLocation) {
        String bossId = bossBlocks.get(blockLocation);
        if (bossId == null) return;

        Boss boss = activeBosses.get(bossId);
        if (boss == null || !boss.isActive()) return;

        // Nie usuwamy już bloku z `bossBlocks`, ponieważ blok ma być "odnawialny"
        // bossBlocks.remove(blockLocation); // TA LINIA ZOSTAŁA USUNIĘTA

        Main.getInstance().getLogger().info(String.valueOf(boss.getCurrentHealth()));

        boss.damage(player, 1);

        if (boss.getCurrentHealth() <= 0) {
            handleBossDeath(boss);
        }
    }

    private void handleBossDeath(Boss boss) {
        Main.getInstance().getLogger().info("Boss " + boss.getId() + " został pokonany!");

        // Logika nagród
        Map<UUID, Integer> damageMap = boss.getPlayerDamage();
        List<Map.Entry<UUID, Integer>> sortedDamagers = new ArrayList<>(damageMap.entrySet());
        sortedDamagers.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<Reward> rewards = pluginConfig.rewards;
        for (int i = 0; i < sortedDamagers.size() && i < rewards.size(); i++) {
            Map.Entry<UUID, Integer> entry = sortedDamagers.get(i);
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
            }
        }

        // Zniszczenie bossa (w tym usunięcie struktury ze świata)
        destroyBoss(boss.getId());
    }

    public void destroyBoss(String bossId) {
        Boss boss = activeBosses.remove(bossId);
        if (boss != null) {
            boss.destroy();

            // Usuwamy wszystkie bloki powiązane z tym bossem z naszej mapy
            bossBlocks.entrySet().removeIf(entry -> entry.getValue().equals(bossId));

            // NOWE: Cofamy sesję edycji FAWE, co usuwa strukturę ze świata
            EditSession session = bossEditSessions.remove(bossId);
            if (session != null) {
                // Używamy TaskManagera z FAWE, aby operacja co cofnięcia była asynchroniczna i bezpieczna
                TaskManager.taskManager().async(() -> {
                    session.undo(session);
                    Main.getInstance().getLogger().info("Struktura bossa " + bossId + " została usunięta ze świata.");
                });
            }
        }
    }

    private void startBossBarUpdater(Boss boss) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!boss.isActive()) {
                // Anuluj zadanie, gdy boss jest nieaktywny
                return;
            }
            boss.updateBossBar();
        }, 0L, 20L); // Aktualizuj co sekundę
    }

    public boolean isBossBlock(Location location) {
        return bossBlocks.containsKey(location);
    }
}