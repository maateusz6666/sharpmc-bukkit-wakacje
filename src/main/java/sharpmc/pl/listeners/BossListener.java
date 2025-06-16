package sharpmc.pl.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import sharpmc.pl.Main;
import sharpmc.pl.managers.BossManager;
import sharpmc.pl.objects.bosses.Boss;

public class BossListener implements Listener {
    private final BossManager bossManager = new BossManager(Main.getInstance());

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        // Sprawdź czy to blok bossa
        if (bossManager.isBossBlock(event.getBlock().getLocation())) {
            // ANULUJ zdarzenie, aby blok się nie zniszczył
            event.setCancelled(true);

            // Reszta logiki pozostaje bez zmian - nadal zadajemy obrażenia
            bossManager.handleBlockBreak(event.getPlayer(), event.getBlock().getLocation());
        }
    }
}