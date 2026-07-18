package bm.b0b0b0.SoulBlast.ps.service;

import bm.b0b0b0.SoulBlast.ps.config.PsSettings;
import bm.b0b0b0.SoulBlast.ps.integration.ProtectionStonesBridge;
import bm.b0b0b0.SoulBlast.ps.integration.PsConfiguredBlockInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;

public final class PsDebugLog {

    private final JavaPlugin plugin;
    private final PsSettings settings;

    public PsDebugLog(JavaPlugin plugin, PsSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public boolean enabled() {
        return settings.debug;
    }

    public void line(String message) {
        if (!settings.debug) {
            return;
        }
        plugin.getLogger().info("[ProtectionStones+/debug] " + message);
    }

    public void logReload(
            ProtectionStonesBridge bridge,
            Map<String, ?> yamlTypes,
            int newlyWrittenToFile
    ) {
        if (!settings.debug) {
            return;
        }
        line("--- reload ---");
        if (newlyWrittenToFile > 0) {
            line("в ps/types/ записано новых типов: " + newlyWrittenToFile);
        }
        if (bridge.available()) {
            for (PsConfiguredBlockInfo block : bridge.listConfiguredBlocks()) {
                String material = block.material() == null ? "?" : block.material().name();
                line("PS block: alias=\"" + block.alias() + "\" material=" + material);
            }
        }
        Set<String> keys = yamlTypes.keySet();
        line("типы в ps/types/ (" + keys.size() + "): " + String.join(", ", keys));
        line("редактируй plugins/SoulBlast/ps/types/<alias>.yml — голограмма, прочность, динамиты");
        line("регионы: plugins/SoulBlast/ps/regions/<world>_<x>_<y>_<z>.yml");
        line("--- /reload ---");
    }

}
