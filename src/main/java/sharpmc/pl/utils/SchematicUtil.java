// src/main/java/sharpmc/pl/utils/SchematicUtil.java
package sharpmc.pl.utils;

import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import sharpmc.pl.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.BiConsumer;

public class SchematicUtil {

    // ZMIANA: Zmieniliśmy typ callbacku, aby przyjmował dwa argumenty: Clipboard i EditSession
    public static void paste(String schematicName, Location location, BiConsumer<Clipboard, EditSession> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            File schematicFile = new File(Main.getInstance().getDataFolder(), "schematics/" + schematicName);
            if (!schematicFile.exists()) {
                Main.getInstance().getLogger().severe("Nie znaleziono pliku schematu: " + schematicName);
                if (callback != null) {
                    // Wywołujemy callback z nullami, aby poinformować o błędzie
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> callback.accept(null, null));
                }
                return;
            }

            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                Main.getInstance().getLogger().severe("Nie rozpoznano formatu schematu: " + schematicName);
                if (callback != null) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> callback.accept(null, null));
                }
                return;
            }

            try (FileInputStream fis = new FileInputStream(schematicFile);
                 ClipboardReader reader = format.getReader(fis)) {

                Clipboard clipboard = reader.read();
                World weWorld = BukkitAdapter.adapt(location.getWorld());

                // Tworzymy nową sesję edycji dla tej operacji
                EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld);

                // Wykonujemy wklejanie w asynchronicznym wątku FAWE
                TaskManager.taskManager().async(() -> {
                    try {
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                                .ignoreAirBlocks(false)
                                .build();
                        Operations.complete(operation);
                        editSession.flushQueue();

                        if (callback != null) {
                            // Po udanym wklejeniu, przekazujemy obiekt clipboard i editSession do naszego BossManagera
                            Bukkit.getScheduler().runTask(Main.getInstance(), () -> callback.accept(clipboard, editSession));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (callback != null) {
                            Bukkit.getScheduler().runTask(Main.getInstance(), () -> callback.accept(null, null));
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                if (callback != null) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> callback.accept(null, null));
                }
            }
        });
    }
}