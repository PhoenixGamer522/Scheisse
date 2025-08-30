package com.extrahelden.duelmod.command;

import com.extrahelden.duelmod.handler.ServerEventHandler;
import com.extrahelden.duelmod.helper.Helper;
import com.extrahelden.duelmod.network.NetworkHandler;
import com.extrahelden.duelmod.perms.Permissions; // <--- NEU
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Mth;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class ShowLivesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("mylives")
                // Root: braucht Grundrecht auf den Befehl
                .requires(src -> Permissions.check(src, Permissions.MYLIVES_BASE))

                // /mylives set <targets> <lives>
                .then(Commands.literal("set")
                        .requires(src -> Permissions.check(src, Permissions.MYLIVES_SET))
                        .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                .then(Commands.argument("lives", IntegerArgumentType.integer(0, 99))
                                        .executes(ctx -> {
                                            CommandSourceStack src = ctx.getSource();
                                            MinecraftServer server = src.getServer();
                                            GameProfileCache cache  = server.getProfileCache();

                                            Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(ctx, "targets");
                                            int inVal = IntegerArgumentType.getInteger(ctx, "lives");
                                            int lives = Mth.clamp(inVal, 0, 99);

                                            int ok = 0, fail = 0;

                                            for (GameProfile gpIn : targets) {
                                                GameProfile gp = gpIn;

                                                // Falls nur Name ankommt → über Cache vollständiges Profil besorgen
                                                if ((gp.getId() == null || gp.getName() == null) && gpIn.getName() != null) {
                                                    Optional<GameProfile> fromCache = cache.get(gpIn.getName());
                                                    if (fromCache.isPresent()) gp = fromCache.get();
                                                }

                                                String name = gp.getName() != null ? gp.getName() : "(unbekannt)";
                                                UUID uuid = gp.getId();

                                                if (uuid == null) {
                                                    // Spieler nie gejoint → keine bekannte UUID im Cache
                                                    fail++;
                                                    src.sendFailure(Component.literal(
                                                            Helper.getPrefix() + " §cSpieler §e" + name +
                                                                    "§c ist offline und nicht im Server-Cache (muss mindestens einmal gejoint haben)."
                                                    ));
                                                    continue;
                                                }

                                                // 1) Persistent speichern (wirkt auch für Offline-Spieler)
                                                ServerEventHandler.saveMyLivesData(uuid, lives);

                                                // 2) Wenn online → NBT setzen + Sync mit Owner-Infos
                                                ServerPlayer online = server.getPlayerList().getPlayer(uuid);
                                                if (online != null) {
                                                    // Leben im NBT aktualisieren
                                                    online.getPersistentData().putInt("MyLives", lives);

                                                    // Owner-Name/UUID aus NBT lesen oder sinnvolle Defaults bestimmen
                                                    OwnerPair owner = readOrResolveOwner(online, server);

                                                    // Client synchronisieren (neues 4-Parameter-Paket)
                                                    ///NetworkHandler.syncLives(online, lives, owner.name, owner.uuid);

                                                    // Feedback
                                                    online.sendSystemMessage(Component.literal(
                                                            Helper.getPrefix() + " §7Deine Leben wurden auf §b" + lives + "§7 gesetzt."
                                                    ));
                                                }

                                                ok++;
                                                src.sendSuccess(() -> Component.literal(
                                                        Helper.getPrefix() + " §aLeben von §e" + name + " §a→ §b" + lives
                                                ), true);
                                            }

                                            return ok; // Anzahl erfolgreich gesetzter Ziele
                                        })
                                )
                        )
                )

                // /mylives setname <name> <lives>  (Fallback rein über Namen)
                .then(Commands.literal("setname")
                        .requires(src -> Permissions.check(src, Permissions.MYLIVES_SETNAME))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .then(Commands.argument("lives", IntegerArgumentType.integer(0, 99))
                                        .executes(ctx -> {
                                            CommandSourceStack src = ctx.getSource();
                                            MinecraftServer server = src.getServer();
                                            GameProfileCache cache  = server.getProfileCache();

                                            String name  = StringArgumentType.getString(ctx, "name");
                                            int inVal    = IntegerArgumentType.getInteger(ctx, "lives");
                                            int lives    = Mth.clamp(inVal, 0, 99);

                                            Optional<GameProfile> profOpt = cache.get(name);
                                            if (profOpt.isEmpty()) {
                                                src.sendFailure(Component.literal(
                                                        Helper.getPrefix() + " §cSpieler §e" + name +
                                                                "§c ist nicht im Server-Cache (muss mindestens einmal gejoint haben)."
                                                ));
                                                return 0;
                                            }

                                            GameProfile prof = profOpt.get();
                                            UUID uuid = prof.getId();

                                            // 1) Persistente Speicherung (wirkt auch offline)
                                            ServerEventHandler.saveMyLivesData(uuid, lives);

                                            // 2) Online? → NBT + Sync + Info
                                            ServerPlayer online = server.getPlayerList().getPlayer(uuid);
                                            if (online != null) {
                                                online.getPersistentData().putInt("MyLives", lives);

                                                OwnerPair owner = readOrResolveOwner(online, server);
                                                 NetworkHandler.syncLives(online, lives, owner.name, owner.uuid);

                                                online.sendSystemMessage(Component.literal(
                                                        Helper.getPrefix() + " §7Deine Leben wurden auf §b" + lives + "§7 gesetzt."
                                                ));
                                            }

                                            src.sendSuccess(() -> Component.literal(
                                                    Helper.getPrefix() + " §aLeben von §e" + name + " §a→ §b" + lives
                                            ), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    // --- kleine Hilfsstruktur + Resolver ---

    private record OwnerPair(String name, String uuid) {}

    /** Liest Owner-Infos aus NBT, oder bestimmt sie (Owner=Spieler selbst; UUID via ProfileCache → Fallback Offline-UUID). */
    private static OwnerPair readOrResolveOwner(ServerPlayer player, MinecraftServer server) {
        var data = player.getPersistentData();

        String ownerName = data.getString("LinkedHeartOwner");
        if (ownerName == null || ownerName.isBlank()) {
            ownerName = player.getGameProfile().getName();
            data.putString("LinkedHeartOwner", ownerName);
        }

        String ownerUuid = data.getString("LinkedHeartOwnerUUID");
        if (ownerUuid == null || ownerUuid.isBlank()) {
            ownerUuid = resolveUuidByName(server, ownerName);
            data.putString("LinkedHeartOwnerUUID", ownerUuid);
        }

        return new OwnerPair(ownerName, ownerUuid);
    }

    /** Bestimmt UUID zu einem Namen über Server-ProfileCache; Fallback: Offline-UUID wie bei Player-Heads. */
    private static String resolveUuidByName(MinecraftServer server, String name) {
        if (name == null || name.isBlank()) return "";
        if (server != null) {
            var opt = server.getProfileCache().get(name);
            if (opt.isPresent() && opt.get().getId() != null) {
                return opt.get().getId().toString();
            }
        }
        UUID off = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name)
                .getBytes(StandardCharsets.UTF_8));
        return off.toString();
    }
}
