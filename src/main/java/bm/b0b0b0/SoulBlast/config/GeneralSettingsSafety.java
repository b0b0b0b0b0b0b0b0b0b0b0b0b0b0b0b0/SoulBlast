package bm.b0b0b0.SoulBlast.config;

import org.bukkit.plugin.java.JavaPlugin;

public final class GeneralSettingsSafety {

    private static final int SAFE_EXPLOSION_BLOCKS = 3200;
    private static final int SAFE_TICK_BLOCKS = 450;
    private static final int WARN_EXPLOSION_BLOCKS = 12000;
    private static final int WARN_TICK_BLOCKS = 700;

    private GeneralSettingsSafety() {
    }

    public static void apply(JavaPlugin plugin, GeneralSettings general) {
        if (!"GRIEF".equalsIgnoreCase(general.destructionMode) || general.griefUnlimitedBlocks) {
            return;
        }
        if (general.griefMaxBlocksPerExplosion > WARN_EXPLOSION_BLOCKS) {
            plugin.getLogger().warning(
                    "grief-max-blocks-per-explosion="
                            + general.griefMaxBlocksPerExplosion
                            + " — слишком много для клиентов, снижено до "
                            + SAFE_EXPLOSION_BLOCKS
                            + " (grief-unlimited-blocks: true чтобы отключить лимит)"
            );
            general.griefMaxBlocksPerExplosion = SAFE_EXPLOSION_BLOCKS;
        }
        if (general.griefMaxBlocksPerExplosionTick > WARN_TICK_BLOCKS) {
            plugin.getLogger().warning(
                    "grief-max-blocks-per-explosion-tick="
                            + general.griefMaxBlocksPerExplosionTick
                            + " — слишком много для клиентов, снижено до "
                            + SAFE_TICK_BLOCKS
                            + " (grief-unlimited-blocks: true чтобы отключить лимит)"
            );
            general.griefMaxBlocksPerExplosionTick = SAFE_TICK_BLOCKS;
        }
    }

}
