package bm.b0b0b0.SoulBlast.service;

import bm.b0b0b0.SoulBlast.config.DynamiteDefinition;
import bm.b0b0b0.SoulBlast.config.FuseRecallSettings;
import bm.b0b0b0.SoulBlast.message.MessageService;
import bm.b0b0b0.SoulBlast.model.PrimedDynamiteSession;
import bm.b0b0b0.SoulBlast.util.PluginKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PrimedDynamiteRecallService {

    public enum RecallResult {
        SUCCESS,
        DISABLED,
        NOT_PRIMED,
        WARHEAD,
        NO_OWNER,
        NOT_OWNER
    }

    private final FuseRecallSettings settings;
    private final PrimedDynamiteService primedService;
    private final PrimedDynamiteMisfireService misfireService;
    private final DynamiteItemFactory itemFactory;
    private final MessageService messages;
    private final PluginKeys keys;

    public PrimedDynamiteRecallService(
            FuseRecallSettings settings,
            PrimedDynamiteService primedService,
            PrimedDynamiteMisfireService misfireService,
            DynamiteItemFactory itemFactory,
            MessageService messages,
            PluginKeys keys
    ) {
        this.settings = settings;
        this.primedService = primedService;
        this.misfireService = misfireService;
        this.itemFactory = itemFactory;
        this.messages = messages;
        this.keys = keys;
    }

    public RecallResult tryRecall(Player player, TNTPrimed primed) {
        if (!settings.enabled) {
            return RecallResult.DISABLED;
        }
        if (primed.getPersistentDataContainer().has(keys.tsarWarhead, PersistentDataType.BYTE)) {
            return RecallResult.WARHEAD;
        }
        Optional<DynamiteDefinition> definition = primedService.resolvePrimed(primed);
        if (definition.isEmpty()) {
            return RecallResult.NOT_PRIMED;
        }
        Optional<PrimedDynamiteSession> session = primedService.findSessionForRecall(primed);
        UUID placerId = resolvePlacerId(primed, session);
        if (placerId == null) {
            return RecallResult.NO_OWNER;
        }
        if (!placerId.equals(player.getUniqueId())) {
            return RecallResult.NOT_OWNER;
        }
        session.ifPresent(misfireService::disposeDud);
        removeRecallEntities(primed, session);
        returnItem(player, primed.getLocation(), definition.get());
        return RecallResult.SUCCESS;
    }

    public void notify(Player player, RecallResult result, DynamiteDefinition definition) {
        switch (result) {
            case SUCCESS -> messages.send(player, "fuse-recall-success", Map.of(
                    "display", displayName(definition)
            ));
            case NOT_OWNER -> messages.send(player, "fuse-recall-not-owner");
            case NO_OWNER -> messages.send(player, "fuse-recall-no-owner");
            case WARHEAD -> messages.send(player, "fuse-recall-warhead");
            case DISABLED -> messages.send(player, "fuse-recall-disabled");
            default -> {
            }
        }
    }

    private void returnItem(Player player, org.bukkit.Location dropAt, DynamiteDefinition definition) {
        ItemStack stack = itemFactory.create(definition, 1);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                dropAt.getWorld().dropItemNaturally(dropAt, item);
            }
        }
    }

    private void removeRecallEntities(TNTPrimed clicked, Optional<PrimedDynamiteSession> session) {
        session.ifPresent(primedService::disposeSession);
        UUID clickedId = clicked.getUniqueId();
        if (!clicked.isDead()) {
            clicked.remove();
        }
        Entity trackedEntity = session
                .map(PrimedDynamiteSession::getEntityId)
                .map(id -> clicked.getServer().getEntity(id))
                .orElse(null);
        if (trackedEntity instanceof TNTPrimed trackedPrimed
                && !trackedPrimed.getUniqueId().equals(clickedId)
                && !trackedPrimed.isDead()) {
            trackedPrimed.remove();
        }
        primedService.removeSession(clickedId);
    }

    private UUID resolvePlacerId(TNTPrimed primed, Optional<PrimedDynamiteSession> session) {
        if (session.isPresent() && session.get().getPlacerId() != null) {
            return session.get().getPlacerId();
        }
        String raw = primed.getPersistentDataContainer().get(keys.primedPlacerId, PersistentDataType.STRING);
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String displayName(DynamiteDefinition definition) {
        if (definition == null) {
            return "";
        }
        return definition.item.displayName;
    }

}
