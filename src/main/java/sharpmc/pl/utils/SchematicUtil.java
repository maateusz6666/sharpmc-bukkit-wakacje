package sharpmc.pl.utils;

import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import sharpmc.pl.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class SchematicUtil {
    private static final Logger logger = Main.getInstance().getLogger();

    public static void paste(String schematicName, Location location) {
        paste(schematicName, location, null);
    }

    public static void paste(String schematicName, Location location, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            File schematicFile = new File(Main.getInstance().getDataFolder(), "schematics/" + schematicName);

            if (!schematicFile.exists()) {
                logger.warning("Schematic file not found: " + schematicFile.getPath());
                if (callback != null) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), callback);
                }
                return;
            }

            try (FileInputStream fis = new FileInputStream(schematicFile);
                 ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(fis)) {

                Clipboard clipboard = reader.read();
                com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(location.getWorld().getName());

                if (weWorld == null) {
                    logger.severe("FAWE World is null!");
                    if (callback != null) {
                        Bukkit.getScheduler().runTask(Main.getInstance(), callback);
                    }
                    return;
                }

                TaskManager.taskManager().async(() -> {
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                        Operations.complete(
                                new ClipboardHolder(clipboard)
                                        .createPaste(editSession)
                                        .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                                        .ignoreAirBlocks(false)
                                        .build()
                        );

                        editSession.flushQueue();

                        // Wykonaj callback po pomyślnym wklejeniu
                        if (callback != null) {
                            Bukkit.getScheduler().runTask(Main.getInstance(), callback);
                        }

                        logger.info("Schematyka została pomyślnie wklejona: " + schematicName);

                    } catch (Exception e) {
                        logger.severe("Error pasting schematic: " + e.getMessage());
                        e.printStackTrace();

                        // Wykonaj callback nawet w przypadku błędu
                        if (callback != null) {
                            Bukkit.getScheduler().runTask(Main.getInstance(), callback);
                        }
                    }
                });
            } catch (IOException e) {
                logger.severe("Error loading schematic: " + e.getMessage());
                e.printStackTrace();

                // Wykonaj callback w przypadku błędu
                if (callback != null) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), callback);
                }
            }
        });
    }
}