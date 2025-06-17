package sharpmc.pl.utils;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class SchematicUtil {

    private SchematicUtil() {
    }

    public static void paste(String schematicName, Location pasteLocation, Consumer<List<Location>> callback) {
        File schematicFile = new File(Main.getInstance().getDataFolder(), "schematics/" + schematicName);
        if (!schematicFile.exists()) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Plik schematu nie został znaleziony: " + schematicName);
            callback.accept(new ArrayList<>());
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Nieznany format schematu: " + schematicName);
            callback.accept(new ArrayList<>());
            return;
        }

        // Normalizujemy lokalizację wklejenia do koordynatów bloku, aby uniknąć błędów precyzji
        Location blockPasteLoc = pasteLocation.getBlock().getLocation();
        World world = BukkitAdapter.adapt(Objects.requireNonNull(blockPasteLoc.getWorld()));

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();
            List<Location> blockLocations = new ArrayList<>();

            // Pobieramy punkt, w którym stał gracz podczas kopiowania (klucz do sukcesu!)
            BlockVector3 clipboardOrigin = clipboard.getOrigin();

            // Obliczamy finalne pozycje bloków, opierając się na punkcie 'origin'
            for (BlockVector3 pt : clipboard.getRegion()) {
                if (!clipboard.getBlock(pt).getBlockType().getMaterial().isAir()) {
                    // Wektor przesunięcia od punktu 'origin' do danego bloku w schemacie
                    BlockVector3 offset = pt.subtract(clipboardOrigin);
                    // Dodajemy to przesunięcie do miejsca wklejenia
                    Location finalLoc = blockPasteLoc.clone().add(offset.getX(), offset.getY(), offset.getZ());
                    blockLocations.add(finalLoc);
                }
            }

            // Wklejamy schemat - WorldEdit wewnętrznie robi dokładnie to samo, co obliczyliśmy wyżej
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(blockPasteLoc.getX(), blockPasteLoc.getY(), blockPasteLoc.getZ()))
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);
            }

            // Uruchamiamy callback z poprawnie obliczoną listą lokalizacji
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> callback.accept(blockLocations));

        } catch (IOException e) {
            e.printStackTrace();
            callback.accept(new ArrayList<>());
        }
    }
}