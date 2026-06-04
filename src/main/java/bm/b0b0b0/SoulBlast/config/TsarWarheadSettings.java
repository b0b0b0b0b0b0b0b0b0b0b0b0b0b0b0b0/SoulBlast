package bm.b0b0b0.SoulBlast.config;

import net.elytrium.serializer.annotations.Comment;
import net.elytrium.serializer.annotations.CommentValue;

import java.util.ArrayList;
import java.util.List;

public class TsarWarheadSettings {

    @Comment(@CommentValue("Царь-бомба: отделение боеголовок перед детонацией"))
    public boolean enabled = false;

    @Comment(@CommentValue("За сколько тиков до взрыва ядра отделить боеголовки"))
    public int launchTicksBeforeEnd = 35;

    @Comment(@CommentValue("Горизонтальная скорость разлёта"))
    public double launchSpeed = 1.15;

    @Comment(@CommentValue("Доп. скорость вверх"))
    public double upwardBoost = 0.42;

    @Comment(@CommentValue("Фитиль боеголовки после отделения"))
    public int warheadFuseTicks = 55;

    @Comment(@CommentValue("ID динамитов-боеголовок (порядок = направления по кругу)"))
    public List<String> warheadIds = new ArrayList<>();

}
