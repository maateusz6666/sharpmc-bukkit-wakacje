package sharpmc.pl.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent; // Dodajemy nowy import
import org.bukkit.event.player.PlayerQuitEvent;
import sharpmc.pl.Main;
import sharpmc.pl.managers.BossManager;
import sharpmc.pl.objects.bosses.Boss;

import java.util.HashSet;
import java.util.Set;

public class BossListener implements Listener {

    private final BossManager bossManager = BossManager.getInstance();
    //private final Set<Location> damagedBlocksCooldown = new HashSet<>();

    //@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    //public void onBlockDamage(BlockDamageEvent event) {
        //Block block = event.getBlock();
        //Location loc = block.getLocation();

        //if (bossManager.isBossBlock(loc) && !damagedBlocksCooldown.contains(loc)) {
            //bossManager.handleBlockDamage(event.getPlayer(), loc);

            //damagedBlocksCooldown.add(loc);
            //Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), () -> {
                //damagedBlocksCooldown.remove(loc);
            //}, 4L);
        //}
    //}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (this.bossManager.isBossBlock(block.getLocation())) {
            event.setCancelled(true);
            this.bossManager.handleBlockDamage(event.getPlayer(), block.getLocation());
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (this.bossManager.isBossBlock(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (Boss boss : this.bossManager.getActiveBosses()) {
            boss.removePlayerFromBossBar(event.getPlayer());
        }
    }
}