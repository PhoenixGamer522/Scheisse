package com.extrahelden.duelmod.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;

public class DeathHandler {
    private static final File DEATH_POSITIONS_FILE = new File("death_positions.json");

    private static final Type DEATH_POSITIONS_TYPE = (new TypeToken<Map<UUID, List<BlockPos>>>() {

    }).getType();

    private static final Gson GSON = new Gson();

    private static Map<UUID, List<BlockPos>> deathPositions = new HashMap<>();

    public static void load(MinecraftServer server) {
        File file = DEATH_POSITIONS_FILE;
        if (file.exists())
            try {
                FileReader reader = new FileReader(file);
                try {
                    deathPositions = (Map<UUID, List<BlockPos>>)GSON.fromJson(reader, DEATH_POSITIONS_TYPE);
                    reader.close();
                } catch (Throwable throwable) {
                    try {
                        reader.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                    throw throwable;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void save(MinecraftServer server) {
        try {
            FileWriter writer = new FileWriter(DEATH_POSITIONS_FILE);
            try {
                GSON.toJson(deathPositions, writer);
                writer.close();
            } catch (Throwable throwable) {
                try {
                    writer.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addDeathPosition(UUID playerUUID, BlockPos pos) {
        List<BlockPos> positions = deathPositions.computeIfAbsent(playerUUID, k -> new ArrayList());
        positions.add(0, pos);
        if (positions.size() > 3)
            positions.remove(3);
    }

    public static List<BlockPos> getDeathPositions(UUID playerUUID) {
        return deathPositions.getOrDefault(playerUUID, new ArrayList<>());
    }

    public static void removeDeathPosition(BlockPos pos) {
        deathPositions.values().forEach(list -> list.remove(pos));
    }
}
