package bm.b0b0b0.SoulBlast.message;



import bm.b0b0b0.SoulBlast.config.SerializerConfigs;

import net.elytrium.serializer.language.object.YamlSerializable;



public class MessagesConfig extends YamlSerializable {



    public MessagesConfig() {

        super(SerializerConfigs.YAML);

    }



    public String prefix = "&8[&cSoulBlast&8] &r";



    public String noPermission = "{prefix}&cНет прав.";



    public String playerOnly = "{prefix}&cТолько для игрока.";



    public String unknownDynamite = "{prefix}&cДинамит &f{id} &cне найден.";



    public String dynamiteGiven = "{prefix}&aВыдан &f{id} &7x{amount} &aигроку &f{player}.";



    public String dynamiteReceived = "{prefix}&aВы получили &f{display} &7x{amount}.";



    public String reloadDone = "{prefix}&aКонфигурация перезагружена.";



    public String igniteDenied = "{prefix}&cЭтот динамит нельзя поджечь таким способом.";



    public String commandUsage = "{prefix}&7/soulblast give <игрок> <id> [кол-во] | menu | reload | rollback <ник> <радиус> <время> &8| &7/soulgrimoire";

    public String coreprotectMissing = "{prefix}&cCoreProtect не найден или API выключен.";

    public String coreprotectRollbackStarted = "{prefix}&7Откат &f{user} &7в радиусе &f{radius} &7за &f{time}&7...";

    public String coreprotectRollbackDone = "{prefix}&aОткат завершён. Записей&8: &f{changes}";

    public String coreprotectRollbackEmpty = "{prefix}&cЗаписей 0. Взрывы пишутся как &f{user}&c. Стоя в центре кратера&8: &f/co lookup r:50 t:5m u:{user} a:block #count";

    public String coreprotectRollbackFailed = "{prefix}&cОткат не удался&8: &f{detail}";

    public String coreprotectRollbackInvalid = "{prefix}&cНеверный радиус или время. Пример&8: &f3600 &7или &f1h";

    public String coreprotectRollbackCenter = "{prefix}&cНужна позиция&8: &7встань в центр кратера или пусть игрок онлайн.";



    public String goalSelected = "{prefix}&5В копилку&8: &f{display}";

    public String goalCleared = "{prefix}&7Копилка очищена.";

    public String goalNotSelected = "{prefix}&7Сначала &fПКМ &7по заряду в списке.";

    public String autoIgniteOn = "{prefix}&aПоджиг&8: &fвзрыв сразу при установке";

    public String autoIgniteOff = "{prefix}&7Поджиг&8: &fтолько от огнива";



    public String purchaseSuccess = "{prefix}&aОбмен совершён&8: &f{display}";



    public String purchaseFailed = "{prefix}&cОбмен не удался.";



    public String purchaseFailedMoney = "{prefix}&cНедостаточно монет.";



    public String purchaseFailedExperience = "{prefix}&cНедостаточно опыта.";



    public String purchaseFailedTnt = "{prefix}&cВ копилке не хватает TNT&8: &c{current}&8/&c{required}";

    public String purchaseFailedInventory = "{prefix}&cОсвободите место в инвентаре.";

    public String cooldownPurchaseActive = "{prefix}&cПокупка &f{display} &cчерез &f{remaining}&c.";

    public String cooldownUseActive = "{prefix}&cЗаряд &f{display} &cможно использовать через &f{remaining}&c.";

    public String lastPyreFuseActive = "{prefix}&c&f{display} &cуже на фитиле — дождись взрыва.";

    public String purchaseRequirements = "{prefix}&7В списке не хватает&8: {missing}";

    public String catalogPayNotRequired = "{prefix}&7Этот заряд платится только TNT — &fПКМ &7выбор, &fЛКМ &7по копилке";

    public String copilkaReady = "{prefix}&aМожно забрать заряд. &7Нажми &fЛКМ &7по копилке";

    public String tntDeposited = "{prefix}&7Внесено &c{amount} &7TNT &8(&c{current}&8/&c{required}&7)";

    public String copilkaTntFull = "{prefix}&aСклад TNT полный &8(&c{current}&8/&c{required}&7). &7Забрать заряд — &fЛКМ &7по копилке";

    public String tntDepositNone = "{prefix}&cНет обычного TNT в инвентаре.";



    public String tntDepositComplete = "{prefix}&aСклад TNT уже полный.";

    public String tntDepositNotRequired = "{prefix}&7Для этого заряда TNT не требуется.";

    public String regionProtected = "{prefix}&cСлишком близко к региону &f{region}&c. Отступ &fx{margin} &cблоков.";

    public String regionProtectedBlast = "{prefix}&cВзрыв заденет регион &f{region}&c.";

    public String regionWorldguardMissing = "{prefix}&cНужен WorldGuard для защиты регионов.";

    public String fuseRecallSuccess = "{prefix}&aЗаряд &f{display} &aснят с таймера и возвращён в инвентарь.";

    public String fuseRecallNotOwner = "{prefix}&cЗабрать может только тот, кто поставил этот заряд.";

    public String fuseRecallNoOwner = "{prefix}&cЭтот заряд нельзя забрать.";

    public String fuseRecallWarhead = "{prefix}&cБоеголовку отдельно не забирают.";

    public String fuseRecallDisabled = "{prefix}&cОтмена таймера отключена на сервере.";

    public String fuseHologramName = "{display}";

    public String fuseHologramTimer = "&7⏱ {color}{fuse}";

    public String fuseHologramRecall = "&8▸ &7ЛКМ &8— &fзабрать заряд";

    public String fuseHologramMisfireWarning = "&e⚠ &7Возможна &cосечка";

    public String fuseHologramMisfireActive = "&c&lОСЕЧКА";

    public String fuseHologramMisfireTimer = "&7⏱ {color}{fuse}";

    public String fuseHologramMisfireIdle = "&7Фитиль погас — заряд на месте";

    public String fuseHologramMisfireHint = "&8▸ &7ПКМ &8— &fподжечь или риск взрыва";

    public String fuseHologramMisfireRecall = "&8▸ &7ЛКМ &8— &fзабрать заряд";

    public String fuseMisfireRelight = "{prefix}&aЗаряд &f{display} &aснова на таймере.";

    public String fuseMisfireDetonate = "{prefix}&c&lОсечка рванула! &f{display}";

    public String fuseMisfireFizzle = "{prefix}&7Щелчок... ничего. ПКМ ещё раз или ЛКМ — забрать.";

    public String fuseMisfireNotOwner = "{prefix}&cТолько поставивший может обезвредить осечку.";

    public String fuseMisfireExpired = "{prefix}&7Осечка &f{display} &7сгнила — время вышло, заряд уничтожен.";

    public String psHologramUsage = "{prefix}&7/psholo &fhide &7| &fshow &7| &ftoggle &8— затем &fЛКМ &8по своему блоку привата";

    public String psHologramArmedHide =
            "{prefix}&7Ударь &fЛКМ &7по своему блоку привата, чтобы &cскрыть &7голограмму &8(&7{seconds} &8сек)";

    public String psHologramArmedShow =
            "{prefix}&7Ударь &fЛКМ &7по своему блоку привата, чтобы &aпоказать &7голограмму &8(&7{seconds} &8сек)";

    public String psHologramArmedToggle =
            "{prefix}&7Ударь &fЛКМ &7по своему блоку привата, чтобы переключить голограмму &8(&7{seconds} &8сек)";

    public String psHologramHidden = "{prefix}&7Голограмма &cскрыта&7. Вернуть: &f/psholo show";

    public String psHologramShown = "{prefix}&7Голограмма &aпоказана";

    public String psHologramAlreadyHidden = "{prefix}&7Голограмма уже скрыта";

    public String psHologramAlreadyVisible = "{prefix}&7Голограмма уже видна";

    public String psHologramNotOwner = "{prefix}&cЭто не твой приват";

    public String psHologramNotTracked =
            "{prefix}&cУ этого блока нет данных SoulBlast — создай приват заново или дождись восстановления";

    public String psHologramNotProtectBlock = "{prefix}&cЭто не блок ProtectionStones";

    public String psHologramModuleOff = "{prefix}&cМодуль ProtectionStones+ не активен";

    public String psHologramDisabled = "{prefix}&cСкрытие голограмм отключено на сервере";

}

