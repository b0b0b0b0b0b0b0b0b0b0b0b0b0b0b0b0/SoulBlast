package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.TsarWarheadSettings;
import bm.b0b0b0.SoulBlast.model.PrimedDynamiteSession;
import bm.b0b0b0.SoulBlast.repository.DynamiteRegistry;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;

public final class TsarWarheadService {

    private final PluginKeys keys;
    private final DynamiteRegistry registry;
    private final PrimedDynamiteService primedService;

    public TsarWarheadService(PluginKeys keys, DynamiteRegistry registry, PrimedDynamiteService primedService) {
        this.keys = keys;
        this.registry = registry;
        this.primedService = primedService;
    }

    public void tickSeparation(PrimedDynamiteSession session, TNTPrimed core) {
        if (!TsarBombRules.isTsar(session.getDefinition()) || session.isWarheadsLaunched()) {
            return;
        }
        TsarWarheadSettings settings = session.getDefinition().explosion.effects.warheads;
        if (!settings.enabled || settings.warheadIds.isEmpty()) {
            return;
        }
        if (core.getFuseTicks() > settings.launchTicksBeforeEnd) {
            return;
        }
        session.setWarheadsLaunched(true);
        launchWarheads(core, settings);
    }

    private void launchWarheads(TNTPrimed core, TsarWarheadSettings settings) {
        Location origin = core.getLocation().clone().add(0, 0.15, 0);
        Entity source = core.getSource();
        List<String> ids = settings.warheadIds;
        int count = ids.size();
        double speed = Math.max(0.2, settings.launchSpeed);
        double lift = Math.max(0.0, settings.upwardBoost);
        int fuse = Math.max(10, settings.warheadFuseTicks);
        for (int index = 0; index < count; index++) {
            Optional<DynamiteDefinition> warheadDef = registry.find(ids.get(index));
            if (warheadDef.isEmpty()) {
                continue;
            }
            double angle = (Math.PI * 2.0 * index) / count;
            Vector velocity = new Vector(
                    Math.cos(angle) * speed,
                    lift,
                    Math.sin(angle) * speed
            );
            TNTPrimed warhead = primedService.spawnWarhead(origin, warheadDef.get(), source, velocity, fuse);
            if (warhead != null) {
                warhead.getPersistentDataContainer().set(keys.tsarWarhead, PersistentDataType.BYTE, (byte) 1);
            }
        }
    }

}
