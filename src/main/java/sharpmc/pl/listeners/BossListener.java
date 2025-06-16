package sharpmc.pl.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sharpmc.pl.managers.BossManager;
import sharpmc.pl.objects.bosses.Boss;

public class BossListener implements Listener {
    private final BossManager bossManager = BossManager.getInstance();

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        // Sprawdź czy to blok bossa
        Bukkit.getLogger().info("zniszyczłem blok");
        if (bossManager.isBossBlock(event.getBlock().getLocation())) {
            // Anuluj normalne dropowanie przedmiotów
            event.setDropItems(false);

            Bukkit.getLogger().info("zniszyczłem blok2");
            // Obsłuż zniszczenie bloku bossa
            bossManager.handleBlockBreak(event.getPlayer(), event.getBlock().getLocation());

            // Blok zostanie zniszczony normalnie, ale bez dropów
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Usuń gracza z wszystkich boss barów
        for (Boss boss : bossManager.getActiveBosses()) {
            boss.removePlayerFromBossBar(event.getPlayer());
        }
    }
}