package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

public class DynamiteDefinition {

    @Comment(@CommentValue("Уникальный ID (латиница, нижнее подчёркивание)"))
    public String id = "example";

    public DynamiteItemSettings item = new DynamiteItemSettings();

    @Comment(@CommentValue("Тики до взрыва (20 тиков = 1 сек)"))
    public int fuseTicks = 80;

    public IgnitionSettings ignition = new IgnitionSettings();

    @Comment(@CommentValue("Поджечь сразу при установке блока"))
    public boolean autoIgniteOnPlace = true;

    @Comment(@CommentValue("Отключить гравитацию — динамит летит вверх"))
    public boolean disableGravity = false;

    @Comment(@CommentValue("Вертикальная скорость при отключённой гравитации"))
    public double upwardVelocity = 0.15;

    public CraftingSettings crafting = new CraftingSettings();

    public GlowSettings glow = new GlowSettings();

    public HologramSettings hologram = new HologramSettings();

    public ExplosionSettings explosion = new ExplosionSettings();

    public PurchaseSettings purchase = new PurchaseSettings();

    public FuseMisfireSettings fuseMisfire = new FuseMisfireSettings();

}
