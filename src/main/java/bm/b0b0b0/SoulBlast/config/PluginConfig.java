package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

public class PluginConfig extends YamlSerializable {

    public PluginConfig() {
        super(SerializerConfigs.YAML);
    }

    public GeneralSettings general = new GeneralSettings();

    public FuseRecallSettings fuseRecall = new FuseRecallSettings();

    public FuseMisfireGlobalSettings fuseMisfire = new FuseMisfireGlobalSettings();

    public EconomySettings economy = new EconomySettings();

    public DatabaseSettings database = new DatabaseSettings();

    public RegionProtectionSettings regionProtection = new RegionProtectionSettings();

    public ProtectionStonesIntegrationSettings protectionStonesIntegration =
            new ProtectionStonesIntegrationSettings();

    public PlayerCooldownSettings playerCooldown = new PlayerCooldownSettings();

    @Comment(@CommentValue("Переопределение взрывоустойчивости блоков"))
    public Map<String, BlockResistanceEntry> blockBlastResistance = new LinkedHashMap<>();

    @Comment(@CommentValue("Поведение взрыва для типов сущностей"))
    public Map<String, EntityExplosionEntry> entityExplosions = new LinkedHashMap<>();

}
