package com.extrahelden.duelmod.handler;

import com.extrahelden.duelmod.DuelMod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Verwaltet die letzten Todespositionen pro Spieler und speichert sie in einer JSON-Datei.
 */
@Mod.EventBusSubscriber(modid = DuelMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeathHandler {

    private static File deathFile;
    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<Map<UUID, List<BlockPos>>>() {}.getType();
    private static Map<UUID, List<BlockPos>> deathPositions = new HashMap<>();

    /**
     * Initialisiert und lädt die gespeicherten Todespositionen.
     * Die Datei wird im Server-Root unter "death_positions.json" abgelegt.
     */
    public static void load(MinecraftServer server) {
        deathFile = new File(server.getServerDirectory(), "death_positions.json");
        if (!deathFile.exists()) {
            deathPositions = new HashMap<>();
            return;
        }
        try (FileReader reader = new FileReader(deathFile)) {
            Map<UUID, List<BlockPos>> loaded = GSON.fromJson(reader, TYPE);
            deathPositions = (loaded != null) ? loaded : new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
            deathPositions = new HashMap<>();
        }
    }

    /**
     * Speichert die aktuellen Todespositionen in die JSON-Datei.
     */
    public static void save(MinecraftServer server) {
        if (deathFile == null) {
            deathFile = new File(server.getServerDirectory(), "death_positions.json");
        }
        try (FileWriter writer = new FileWriter(deathFile)) {
            GSON.toJson(deathPositions, TYPE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fügt eine neue Todesposition ganz vorne in die Liste ein.
     * Begrenzt die Liste auf maximal 3 Einträge.
     */
    public static void addDeathPosition(UUID playerUUID, BlockPos pos) {
        List<BlockPos> list = deathPositions.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        list.add(0, pos);
        if (list.size() > 3) {
            list.remove(list.size() - 1);
        }
    }

    /**
     * Gibt die zuletzt gespeicherten Todespositionen (max. 3) für den Spieler zurück.
     */
    public static List<BlockPos> getDeathPositions(UUID playerUUID) {
        return Collections.unmodifiableList(
                deathPositions.getOrDefault(playerUUID, Collections.emptyList())
        );
    }

    /**
     * Entfernt eine gegebene Position aus allen Spieler-Listen.
     */
    public static void removeDeathPosition(BlockPos pos) {
        for (List<BlockPos> list : deathPositions.values()) {
            list.remove(pos);
        }
    }
}
