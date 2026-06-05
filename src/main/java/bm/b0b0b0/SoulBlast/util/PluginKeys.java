package bm.b0b0b0.SoulBlast.util;

import bm.b0b0b0.SoulBlast.SoulBlast;
import org.bukkit.NamespacedKey;

public final class PluginKeys {

    public static final String NAMESPACE = "soulblast";

    public final NamespacedKey dynamiteId;
    public final NamespacedKey primedDynamiteId;
    public final NamespacedKey primedPlacerId;
    public final NamespacedKey menuIconType;
    public final NamespacedKey menuDynamiteId;
    public final NamespacedKey tsarWarhead;
    public final NamespacedKey tsarDecoy;
    public final NamespacedKey primedDud;

    public PluginKeys(SoulBlast plugin) {
        dynamiteId = new NamespacedKey(plugin, "dynamite_id");
        primedDynamiteId = new NamespacedKey(plugin, "primed_dynamite_id");
        primedPlacerId = new NamespacedKey(plugin, "primed_placer_id");
        menuIconType = new NamespacedKey(plugin, "menu_icon_type");
        menuDynamiteId = new NamespacedKey(plugin, "menu_dynamite_id");
        tsarWarhead = new NamespacedKey(plugin, "tsar_warhead");
        tsarDecoy = new NamespacedKey(plugin, "tsar_decoy");
        primedDud = new NamespacedKey(plugin, "primed_dud");
    }

}
