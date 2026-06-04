package bm.b0b0b0.SoulBlast.decay.config;

import java.util.LinkedHashMap;

public final class DecayBlockTypeCopier {

    private DecayBlockTypeCopier() {
    }

    public static DecayBlockTypeDefinition copy(DecayBlockTypeDefinition source) {
        DecayBlockTypeDefinition target = new DecayBlockTypeDefinition();
        target.resistance = source.resistance;
        target.regeneration.every = source.regeneration.every;
        target.regeneration.materials = new LinkedHashMap<>(source.regeneration.materials);
        target.options.canPlayerBreak = source.options.canPlayerBreak;
        target.options.canPlayerInteract = source.options.canPlayerInteract;
        target.options.canPistonMove = source.options.canPistonMove;
        target.variables = new LinkedHashMap<>(source.variables);
        return target;
    }

}
