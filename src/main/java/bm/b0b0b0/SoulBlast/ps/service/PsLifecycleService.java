package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsProtectionTypeDefinition;
import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.integration.LuckPermsBridge;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.integration.PsRegionSnapshot;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockKey;
import bm.b0b0b0.SoulBlast.ps.model.PsBlockState;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockPersistence;
import bm.b0b0b0.SoulBlast.ps.repository.PsBlockStore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

public final class PsLifecycleService {

    private final JavaPlugin plugin;
    private final PsSettings settings;
    private final ProtectionStonesBridge bridge;
    private final LuckPermsBridge luckPerms;
    private final PsTypeRegistry types;
    private final PsBlockStore store;
    private final PsHologramService holograms;
    private final PsDebugLog debug;
    private final PsBlockPersistence persistence;

    public PsLifecycleService(
            JavaPlugin plugin,
            PsSettings settings,
            ProtectionStonesBridge bridge,
            LuckPermsBridge luckPerms,
            PsTypeRegistry types,
            PsBlockStore store,
            PsHologramService holograms,
            PsDebugLog debug,
            PsBlockPersistence persistence
    ) {
        this.plugin = plugin;
        this.settings = settings;
        this.bridge = bridge;
        this.luckPerms = luckPerms;
        this.types = types;
        this.store = store;
        this.holograms = holograms;
        this.debug = debug;
        this.persistence = persistence;
    }

    public void onCreate(Block block, Player player, PsRegionSnapshot snapshot) {
        if (block == null || block.getWorld() == null || snapshot == null) {
            return;
        }
        debug.line("onCreate: block=" + block.getType()
                + " @ " + block.getX() + "," + block.getY() + "," + block.getZ()
                + " alias=\"" + snapshot.alias() + "\"");
        String alias = PsBlockAliasResolver.canonicalForStorage(snapshot.alias(), block, bridge, types);
        Optional<PsProtectionTypeDefinition> type = types.resolve(alias, block);
        if (type.isEmpty()) {
            debug.line("onCreate: тип НЕ найден для alias=\"" + snapshot.alias() + "\"");
            if (!settings.silentStartup && !settings.debug) {
                plugin.getLogger().info(
                        "[ProtectionStones+] Тип привата «" + snapshot.alias()
                                + "» не распознан — включите settings.debug в ps/settings.yml"
                );
            }
            return;
        }
        debug.line("onCreate: тип найден, hologram.enabled=" + type.get().hologram.enabled
                + " durability.max=" + type.get().durability.maximum);
        PsProtectionTypeDefinition definition = type.get();
        int maximum = definition.durability.enabled ? Math.max(1, definition.durability.maximum) : 0;
        int durability = maximum;
        String ownerName = player != null ? player.getName() : snapshot.ownerName();
        UUID ownerId = player != null ? player.getUniqueId() : snapshot.ownerId();
        LuckPermsBridge.ChatMeta meta = ownerId == null
                ? new LuckPermsBridge.ChatMeta("", "")
                : luckPerms.resolve(ownerId).orElse(new LuckPermsBridge.ChatMeta("", ""));
        PsBlockKey key = PsBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        PsBlockState state = new PsBlockState(
                key,
                alias,
                durability,
                maximum,
                ownerName,
                meta.prefix(),
                meta.suffix(),
                ownerId,
                snapshot.radiusX(),
                snapshot.radiusY(),
                snapshot.radiusZ(),
                false
        );
        store.put(state);
        if (persistence != null) {
            persistence.saveState(state);
        }
        World world = block.getWorld();
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        if (definition.lightningStrike.create) {
            world.strikeLightningEffect(center);
        }
        PsSoundPlayer.play(world, center, definition.sound.create);
        holograms.attach(world, state, definition);
        debug.line("onCreate: голограмма установлена, tracked=" + store.size());
    }

    public void onRemove(Block block) {
        if (block == null || block.getWorld() == null) {
            return;
        }
        PsBlockKey key = PsBlockKey.of(block.getWorld(), block.getX(), block.getY(), block.getZ());
        PsBlockState removed = store.remove(key);
        holograms.remove(key);
        if (persistence != null) {
            persistence.removeState(key);
        }
        if (removed == null) {
            return;
        }
        Optional<PsProtectionTypeDefinition> type = types.findAlias(removed.typeAlias());
        if (type.isEmpty()) {
            return;
        }
        PsProtectionTypeDefinition definition = type.get();
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        if (definition.lightningStrike.remove) {
            block.getWorld().strikeLightningEffect(center);
        }
        PsSoundPlayer.play(block.getWorld(), center, definition.sound.remove);
    }

}
