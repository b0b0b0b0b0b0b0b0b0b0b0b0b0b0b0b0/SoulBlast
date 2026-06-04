# SoulBlast

SoulBlast — это плагин для Minecraft, который добавляет **линейку кастомных динамитов**.

Это **не** кастрированный **RE4TNT**, и не любой другой **говноплагин** на **CustomTNT**, где три радиуса в конфиге. Это **линейка разновидных зарядов и кастомизаций динамита** — каждый кастомный TNT настраиваешь под свои нужды и **исключительно под свой сервер Minecraft**, а не «один пресет на всех». В коробке уже есть заряды с разной работой: один разрушает только **обсидиан**, другой — только **воду или лаву**, третий заточен под **разрушение построек и баз противника**. 

Есть и **«орешник»** — так её зовут неофициально; в игре это **Последний Разжигатель Пламени**. При взрыве **шесть боеголовок** вылетают и захватывают огромную территорию — самая мощная бомба в наборе. Обычным игрокам **не давать**, либо **нерфить**: способна оставить от сервера одни щепки. Взрыв **настраивается** — что ломать, что оставить, как глубоко копать, лутить или нет, жечь или нет. Тяжёлые заряды **размазываются по тикам**, а не вываливают кратер одним лагом в лицо.

## Какие есть готовые динамиты

Это **шаблончики под ваши нужды** — шесть зарядов из коробки. Админ крутит радиус, лут, огонь, цены в `dynamites/dynamites.yml`; названия в игре — как захочешь.

| Заряд | Зачем |
|-------|--------|
| **Свет Холодной Луны** | Дерево, кирпич, сундуки — по списку. Обсидиан стачивает медленно. Траву и землю не трогает |
| **Гром Великой Руны** | Штурм базы: постройки, **лут с сундуков**. Обсидиан сам не рвёт — для него отдельный заряд |
| **Плачущие души** | Только **обсидиан** и **плачущий обсидиан**. Ближе к заряду — быстрее. Дом из дерева не сносит |
| **Пламя Затменного Владыки** | Поджог + штурм как у руны. Обсидиан не ломает |
| **Вздох Пустоты** | Вода и лава в радиусе — вытягивает, воронка. Не каменный разнос |
| **Последний Разжигатель Пламени** («орешник») | Ломает почти всё кроме бедрока. Лава, адский пол, **6 боеголовок**. Ивентовая хуйня, не для масс |

## Гримуар

**Гримуар** — GUI-меню, где игроки **покупают и получают динамиты**. Открыть: `/soulgrimoire` (или `/soulblast menu`).

- **Удобная сортировка** — по силе, фитилю, имени; переключается в меню
- **Всё настраивается** — раскладка, иконки, названия, цены, что показывать в каталоге (`menu.yml` + `dynamites.yml`)
- **Нет дюпов** — предметы в GUI декоративные, реальный заряд только после обмена в инвентарь
- **Два способа оплаты** — **опыт или монеты** в каталоге (ЛКМ по заряду)
- **Копилка через обычный TNT** — на некоторые динамиты деньги не катят: игрок **закидывает горы ванильного TNT** в слот копилки (ПКМ — выбрал заряд, ЛКМ — вносишь из руки, набрал — забрал заряд). Сколько TNT на какой заряд — **всё настраивается** в `dynamites.yml`
- **Страницы** — много зарядов не ломают меню
- **Автоподжиг** — вкл/выкл в углу меню, на все твои установки
- **Hex-цвета** в названиях и лоре — `&#RRGGBB`, как в нормальных плагинах, не только `&a`

## Модуль для ProtectionStones

Это модуль для плагина **ProtectionStones**, который добавляет **гриф кастомными динамитами SoulBlast по приватам** — не вместо PS, а поверх него вместе с **WorldGuard**. Включён в `config.yml` по умолчанию; если PS на сервере нет — предупреждение в консоль, `protection-stones-integration` **не выключается сам**.

- **голограмму** над камнем привата — прочность, тип, владелец (`%owner_prefix%` / `%owner_suffix%` с **LuckPerms**, если есть);
- **прочность блока привата** — бьют только заряды из `only-dynamite-types` в `ps/types/<alias>.yml`, урон на каждый динамит свой;
- **рейд чужого региона** — ставить и поджигать динамит в чужом PS, если для типа привата разрешено;
- **сохранение** в `ps/regions/<мир>_<x>_<y>_<z>.yml` — после рестарта голограммы на месте (флаг `hologram-hidden` — игрок может скрыть свою через `/psholo`);
- **файлы типов** `ps/types/<alias>.yml` — alias из PS подтягиваются автоматически; в `hologram.display` — TextDisplay: `view-range`, `see-through`, `shadowed`, `scale`, `billboard`, `alignment` и др.

Без `settings.support-soulblast: true` в `ps/settings.yml` урон по привату **не идёт**. Нужны **ProtectionStones 2.10+** и **WorldGuard**. Конфиги: `plugins/SoulBlast/ps/`.

## Установка (админ)

### Требования

- **Paper** 1.21+
- **Java 21**
- **Vault** + экономика (**EssentialsX** или другой провайдер через Vault, опционально) — покупки за **монеты** в гримуаре; без Vault работают **опыт** и **TNT**
- **WorldGuard** + **ProtectionStones** 2.10+ (опционально, вместе) — модуль приватов: голограммы над камнем, прочность, гриф по PS; WG — спавн и защищённые зоны. Без PS интеграция в `config.yml` можно выключить, WG остаётся для регионов
- **LuckPerms** (опционально) — префикс/суффикс на голограмме привата

### Как поставить

1. Останови сервер.
2. Скачай зависимости (см. выше) → `plugins/`.
3. Скачай `SoulBlast.jar` → `plugins/`.
4. Запусти сервер.
5. Не перезагружай через **PlugMan** / **PlugManX** — только стоп, замена JAR, старт. Конфиги — `/soulblast reload`.

---

## Команды и права

| Команда | Описание |
|---------|----------|
| `/soulblast give <игрок> <id> [кол-во]` | Выдать динамит |
| `/soulblast menu` | Гримуар |
| `/soulblast reload` | Перезагрузка конфигов |
| `/soulgrimoire` | Меню (alias: `soulfire`, `soulblast-menu`) |
| `/psholo hide`, `show`, `toggle` | Скрыть, показать или переключить **свою** голограмму над PS-блоком: команда, затем **ЛКМ** по своему камню привата (alias: `pshologram`, `pshide`) |

**Decay — починка стен после рейда:** **песок** в инвентаре → **ПКМ/ЛКМ** по треснутому блоку (камень/обсидиан для починки не нужны — иначе проще сломать и поставить новый). Право: `soulblast.decay.repair`. Настройки: `decay/general.yml` → `manual-repair.sand-only: true`.

| Право | По умолчанию |
|-------|----------------|
| `soulblast.use` | op |
| `soulblast.give` | op |
| `soulblast.menu` | все |
| `soulblast.reload` | op |
| `soulblast.region.bypass` | op — WG-зоны из `region-protection` |
| `soulblast.cooldown.bypass` | op — кулдаун зарядов |
| `soulblast.decay.repair` | все — чинить треснутые стены (Decay) |
| `soulblast.ps.hologram` | все — `/psholo` для **своего** привата |
| `soulblast.ps.hologram.admin` | op — голограмма любого PS-блока |

Право меню гримуара: `menu.yml` → `command.permission`.  
Скрытие голограмм: `ps/settings.yml` → `hologram-hide` (`enabled`, `arm-timeout-seconds` — сколько секунд ждать удар после команды).


## Конфигурация

Всё лежит в `plugins/SoulBlast/`, в YAML параметры подписаны. Ниже — пустые блоки: **вставь свой файл** между \`\`\`yaml и \`\`\`.

### Основное

#### `config.yml`

> `plugins/SoulBlast/config.yml` — взрывы, лимиты, экономика, WG, PS, кулдауны, сущности

```yaml
general:
  #SAFE — баланс TPS; GRIEF — крупные ямы (см. grief-*)
  destruction-mode: GRIEF
  #true — не ограничивать grief-* (риск вылета клиента при нескольких взрывах)
  grief-unlimited-blocks: false
  #При destruction-mode: GRIEF — потолок блоков на один заряд (0 = 3200)
  grief-max-blocks-per-explosion: 3200
  #При GRIEF — блоков за тик на все взрывы (0 = 450)
  grief-max-blocks-per-explosion-tick: 450
  #При GRIEF — лучей при расчёте (0 = 8192)
  grief-max-sampling-rays: 0
  #При GRIEF — шагов лучей за тик (0 = 16000)
  grief-max-sampling-steps-per-tick: 16000
  #Потолок блоков заливки кратера (лава/магма); 0 = половина max-blocks-per-explosion
  max-crater-fill-blocks-per-explosion: 0
  #При GRIEF — заливка кратера (0 = 45000)
  grief-max-crater-fill-blocks-per-explosion: 45000
  #Сколько блоков одного взрыва ломать за тик сервера (1500–2500 — баланс TPS)
  max-blocks-per-explosion-tick: 1800
  #Потолок блоков на один взрыв (даже у царь-бомбы)
  max-blocks-per-explosion: 5500
  #Максимум лучей при расчёте сферы (меньше = меньше лаг при подготовке)
  max-sampling-rays: 576
  #Шагов лучей за тик при подготовке (только главный поток)
  max-sampling-steps-per-tick: 2400
  #Не трогать непрогруженные чанки (false = полный снос, но нагрузка)
  sample-only-loaded-chunks: false
  #Максимум одновременных отложенных взрывов в очереди
  max-queued-explosions: 16
  #Тиков между проверкой очереди взрывов (1 = каждый тик)
  explosion-queue-interval-ticks: 1
  #Автоподжиг при установке для новых игроков
  default-auto-ignite: true
fuse-recall:
  #ЛКМ по активированному заряду — остановить таймер и вернуть предмет поставившему
  enabled: true
fuse-misfire:
  #Система осечек (пер-заряд в dynamites.yml → fuse-misfire)
  enabled: true
economy:
  #Использовать Vault, если плагин установлен
  use-vault-if-present: true
  #Символ валюты в лore меню
  currency-symbol: "в›ѓ"
database:
  #Путь к SQLite относительно папки плагина
  file-name: users/player-data.db
region-protection:
  #Защита спавна и регионов WorldGuard (нужен WorldGuard)
  enabled: true
  #Без WorldGuard не давать ставить динамит, если защита включена
  require-world-guard: true
  #Отступ от границы региона = радиус взрыва × множитель (2.0 = x2 к мощности)
  margin-radius-multiplier: 2.0
  #Имена регионов WG (spawn, hub и т.д.), без учёта регистра
  region-names:
    - "spawn"
    - "__spawn__"
  #Регионы из списка не защищать (даже если совпало имя)
  exempt-region-names: []
  #Защищать регионы, где у игрока BUILD = deny (типичный spawn)
  protect-build-deny-regions: true
  #Защищать регионы, где флаг WG = deny (см. worldguard-flags)
  protect-flag-deny-regions: true
  #Флаги WG: deny в регионе = защита (soulblast-dynamite регистрируется плагином)
  worldguard-flags:
    - "soulblast-dynamite"
    - "tnt"
    - "other-explosion"
  #Проверять, что сфера взрыва не задевает защищённый регион
  check-explosion-footprint: true
  #Миры (пусто = все)
  worlds: []
  #Обход защиты
  bypass-permission: soulblast.region.bypass
protection-stones-integration:
  #Интеграция с плагином ProtectionStones (папка ps/ в данных SoulBlast)
  #При включении: голограммы над приватами, прочность блока привата от
  #кастомных динамитов, доп. разрешения внутри регионов PS.
  #Требуются WorldGuard и ProtectionStones 2.10+. Пока выключено — к API не подключаемся.
  #Типы из PS подхватываются автоматически; ps/types.yml только переопределяет настройки
  enabled: true
player-cooldown:
  #Персональный кулдаун на покупку и использование тяжёлых зарядов
  enabled: true
  #Обход кулдауна
  bypass-permission: soulblast.cooldown.bypass
  #Кулдаун при покупке / выдаче из меню
  apply-on-purchase: true
  #Кулдаун при установке и поджоге
  apply-on-use: true
  #Один таймер на всю группу tier (не давать скупать разные тяжёлые подряд)
  shared-tier-cooldown: true
  tiers:
    -       #Ключ группы (для shared-tier-cooldown)
id: extreme
      #Минимальный радиус взрыва динамита
      min-radius: 38.0
      #Считать tier, если quality = EXTREME
      match-extreme-quality: false
      #Кулдаун покупки в секундах (0 = не применять этот tier)
      purchase-seconds: 0
      #Кулдаун установки/поджога в секундах
      use-seconds: 0
  #Переопределение по id динамита
  dynamites:
    last_pyre:
      #Покупка в сек (-1 = как у tier)
      purchase-seconds: 45
      #Использование в сек (-1 = как у tier)
      use-seconds: 30
#Переопределение взрывоустойчивости блоков
block-blast-resistance: {}
#Поведение взрыва для типов сущностей
entity-explosions: {}
```

#### `groups/material-groups.yml`

> `plugins/SoulBlast/groups/material-groups.yml` — группы блоков для `block-rules` в динамитах

```yaml
material-groups:
  stone_like:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 6.0
    #Список Material (Bukkit)
    materials:
      - "STONE"
      - "COBBLESTONE"
      - "DEEPSLATE"
      - "STONE_BRICKS"
  obsidian_hard:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 8.0
    #Список Material (Bukkit)
    materials:
      - "OBSIDIAN"
      - "CRYING_OBSIDIAN"
  base_raid:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 4.0
    #Список Material (Bukkit)
    materials:
      - "OAK_PLANKS"
      - "SPRUCE_PLANKS"
      - "BIRCH_PLANKS"
      - "JUNGLE_PLANKS"
      - "DARK_OAK_PLANKS"
  wood_like:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 3.0
    #Список Material (Bukkit)
    materials:
      - "OAK_PLANKS"
      - "SPRUCE_PLANKS"
      - "BIRCH_PLANKS"
  trap_raid:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 2.5
    #Список Material (Bukkit)
    materials:
      - "REDSTONE_WIRE"
      - "REDSTONE_TORCH"
      - "REPEATER"
      - "COMPARATOR"
      - "OBSERVER"
      - "PISTON"
      - "STICKY_PISTON"
      - "DISPENSER"
      - "DROPPER"
      - "HOPPER"
      - "TARGET"
      - "NOTE_BLOCK"
      - "LEVER"
      - "OAK_BUTTON"
      - "STONE_BUTTON"
      - "OAK_PRESSURE_PLATE"
      - "STONE_PRESSURE_PLATE"
      - "HEAVY_WEIGHTED_PRESSURE_PLATE"
      - "LIGHT_WEIGHTED_PRESSURE_PLATE"
      - "TRIPWIRE"
      - "TRIPWIRE_HOOK"
      - "REDSTONE_BLOCK"
      - "REDSTONE_LAMP"
      - "DETECTOR_RAIL"
      - "POWERED_RAIL"
      - "ACTIVATOR_RAIL"
      - "RAIL"
      - "DAYLIGHT_DETECTOR"
      - "SCULK_SENSOR"
      - "CALIBRATED_SCULK_SENSOR"
      - "RESPAWN_ANCHOR"
  fragile_trap:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 1.5
    #Список Material (Bukkit)
    materials:
      - "GLASS"
      - "TINTED_GLASS"
      - "GLASS_PANE"
      - "TORCH"
      - "SOUL_TORCH"
      - "LANTERN"
      - "SOUL_LANTERN"
      - "LADDER"
      - "SCAFFOLDING"
  raid_timber:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 2.5
    #Список Material (Bukkit)
    materials:
      - "OAK_LOG"
      - "STRIPPED_OAK_LOG"
      - "OAK_WOOD"
      - "STRIPPED_OAK_WOOD"
      - "OAK_PLANKS"
      - "OAK_STAIRS"
      - "OAK_SLAB"
      - "OAK_FENCE"
      - "OAK_FENCE_GATE"
      - "OAK_DOOR"
      - "OAK_TRAPDOOR"
      - "OAK_BUTTON"
      - "OAK_PRESSURE_PLATE"
      - "OAK_SIGN"
      - "OAK_HANGING_SIGN"
      - "OAK_SHELF"
      - "SPRUCE_LOG"
      - "STRIPPED_SPRUCE_LOG"
      - "SPRUCE_WOOD"
      - "STRIPPED_SPRUCE_WOOD"
      - "SPRUCE_PLANKS"
      - "SPRUCE_STAIRS"
      - "SPRUCE_SLAB"
      - "SPRUCE_FENCE"
      - "SPRUCE_FENCE_GATE"
      - "SPRUCE_DOOR"
      - "SPRUCE_TRAPDOOR"
      - "SPRUCE_BUTTON"
      - "SPRUCE_PRESSURE_PLATE"
      - "SPRUCE_SIGN"
      - "SPRUCE_HANGING_SIGN"
      - "SPRUCE_SHELF"
      - "BIRCH_LOG"
      - "STRIPPED_BIRCH_LOG"
      - "BIRCH_WOOD"
      - "STRIPPED_BIRCH_WOOD"
      - "BIRCH_PLANKS"
      - "BIRCH_STAIRS"
      - "BIRCH_SLAB"
      - "BIRCH_FENCE"
      - "BIRCH_FENCE_GATE"
      - "BIRCH_DOOR"
      - "BIRCH_TRAPDOOR"
      - "BIRCH_BUTTON"
      - "BIRCH_PRESSURE_PLATE"
      - "BIRCH_SIGN"
      - "BIRCH_HANGING_SIGN"
      - "BIRCH_SHELF"
      - "JUNGLE_LOG"
      - "STRIPPED_JUNGLE_LOG"
      - "JUNGLE_WOOD"
      - "STRIPPED_JUNGLE_WOOD"
      - "JUNGLE_PLANKS"
      - "JUNGLE_STAIRS"
      - "JUNGLE_SLAB"
      - "JUNGLE_FENCE"
      - "JUNGLE_FENCE_GATE"
      - "JUNGLE_DOOR"
      - "JUNGLE_TRAPDOOR"
      - "JUNGLE_BUTTON"
      - "JUNGLE_PRESSURE_PLATE"
      - "JUNGLE_SIGN"
      - "JUNGLE_HANGING_SIGN"
      - "JUNGLE_SHELF"
      - "ACACIA_LOG"
      - "STRIPPED_ACACIA_LOG"
      - "ACACIA_WOOD"
      - "STRIPPED_ACACIA_WOOD"
      - "ACACIA_PLANKS"
      - "ACACIA_STAIRS"
      - "ACACIA_SLAB"
      - "ACACIA_FENCE"
      - "ACACIA_FENCE_GATE"
      - "ACACIA_DOOR"
      - "ACACIA_TRAPDOOR"
      - "ACACIA_BUTTON"
      - "ACACIA_PRESSURE_PLATE"
      - "ACACIA_SIGN"
      - "ACACIA_HANGING_SIGN"
      - "ACACIA_SHELF"
      - "DARK_OAK_LOG"
      - "STRIPPED_DARK_OAK_LOG"
      - "DARK_OAK_WOOD"
      - "STRIPPED_DARK_OAK_WOOD"
      - "DARK_OAK_PLANKS"
      - "DARK_OAK_STAIRS"
      - "DARK_OAK_SLAB"
      - "DARK_OAK_FENCE"
      - "DARK_OAK_FENCE_GATE"
      - "DARK_OAK_DOOR"
      - "DARK_OAK_TRAPDOOR"
      - "DARK_OAK_BUTTON"
      - "DARK_OAK_PRESSURE_PLATE"
      - "DARK_OAK_SIGN"
      - "DARK_OAK_HANGING_SIGN"
      - "DARK_OAK_SHELF"
      - "MANGROVE_LOG"
      - "STRIPPED_MANGROVE_LOG"
      - "MANGROVE_WOOD"
      - "STRIPPED_MANGROVE_WOOD"
      - "MANGROVE_PLANKS"
      - "MANGROVE_STAIRS"
      - "MANGROVE_SLAB"
      - "MANGROVE_FENCE"
      - "MANGROVE_FENCE_GATE"
      - "MANGROVE_DOOR"
      - "MANGROVE_TRAPDOOR"
      - "MANGROVE_BUTTON"
      - "MANGROVE_PRESSURE_PLATE"
      - "MANGROVE_SIGN"
      - "MANGROVE_HANGING_SIGN"
      - "MANGROVE_SHELF"
      - "CHERRY_LOG"
      - "STRIPPED_CHERRY_LOG"
      - "CHERRY_WOOD"
      - "STRIPPED_CHERRY_WOOD"
      - "CHERRY_PLANKS"
      - "CHERRY_STAIRS"
      - "CHERRY_SLAB"
      - "CHERRY_FENCE"
      - "CHERRY_FENCE_GATE"
      - "CHERRY_DOOR"
      - "CHERRY_TRAPDOOR"
      - "CHERRY_BUTTON"
      - "CHERRY_PRESSURE_PLATE"
      - "CHERRY_SIGN"
      - "CHERRY_HANGING_SIGN"
      - "CHERRY_SHELF"
      - "PALE_OAK_LOG"
      - "STRIPPED_PALE_OAK_LOG"
      - "PALE_OAK_WOOD"
      - "STRIPPED_PALE_OAK_WOOD"
      - "PALE_OAK_PLANKS"
      - "PALE_OAK_STAIRS"
      - "PALE_OAK_SLAB"
      - "PALE_OAK_FENCE"
      - "PALE_OAK_FENCE_GATE"
      - "PALE_OAK_DOOR"
      - "PALE_OAK_TRAPDOOR"
      - "PALE_OAK_BUTTON"
      - "PALE_OAK_PRESSURE_PLATE"
      - "PALE_OAK_SIGN"
      - "PALE_OAK_HANGING_SIGN"
      - "PALE_OAK_SHELF"
      - "CRIMSON_STEM"
      - "STRIPPED_CRIMSON_STEM"
      - "CRIMSON_HYPHAE"
      - "STRIPPED_CRIMSON_HYPHAE"
      - "CRIMSON_PLANKS"
      - "CRIMSON_STAIRS"
      - "CRIMSON_SLAB"
      - "CRIMSON_FENCE"
      - "CRIMSON_FENCE_GATE"
      - "CRIMSON_DOOR"
      - "CRIMSON_TRAPDOOR"
      - "CRIMSON_BUTTON"
      - "CRIMSON_PRESSURE_PLATE"
      - "CRIMSON_SIGN"
      - "CRIMSON_HANGING_SIGN"
      - "CRIMSON_SHELF"
      - "WARPED_STEM"
      - "STRIPPED_WARPED_STEM"
      - "WARPED_HYPHAE"
      - "STRIPPED_WARPED_HYPHAE"
      - "WARPED_PLANKS"
      - "WARPED_STAIRS"
      - "WARPED_SLAB"
      - "WARPED_FENCE"
      - "WARPED_FENCE_GATE"
      - "WARPED_DOOR"
      - "WARPED_TRAPDOOR"
      - "WARPED_BUTTON"
      - "WARPED_PRESSURE_PLATE"
      - "WARPED_SIGN"
      - "WARPED_HANGING_SIGN"
      - "WARPED_SHELF"
      - "BAMBOO_BLOCK"
      - "STRIPPED_BAMBOO_BLOCK"
      - "BAMBOO_PLANKS"
      - "BAMBOO_MOSAIC"
      - "BAMBOO_STAIRS"
      - "BAMBOO_MOSAIC_STAIRS"
      - "BAMBOO_SLAB"
      - "BAMBOO_MOSAIC_SLAB"
      - "BAMBOO_FENCE"
      - "BAMBOO_FENCE_GATE"
      - "BAMBOO_DOOR"
      - "BAMBOO_TRAPDOOR"
      - "BAMBOO_BUTTON"
      - "BAMBOO_PRESSURE_PLATE"
      - "BAMBOO_SIGN"
      - "BAMBOO_HANGING_SIGN"
      - "BAMBOO_SHELF"
      - "BAMBOO"
      - "LADDER"
      - "SCAFFOLDING"
      - "COMPOSTER"
      - "BOOKSHELF"
      - "CHISELED_BOOKSHELF"
  raid_masonry:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 5.0
    #Список Material (Bukkit)
    materials:
      - "STONE"
      - "COBBLESTONE"
      - "MOSSY_COBBLESTONE"
      - "SMOOTH_STONE"
      - "STONE_BRICKS"
      - "MOSSY_STONE_BRICKS"
      - "CRACKED_STONE_BRICKS"
      - "CHISELED_STONE_BRICKS"
      - "ANDESITE"
      - "POLISHED_ANDESITE"
      - "DIORITE"
      - "POLISHED_DIORITE"
      - "GRANITE"
      - "POLISHED_GRANITE"
      - "CALCITE"
      - "TUFF"
      - "DRIPSTONE_BLOCK"
      - "COBBLESTONE_STAIRS"
      - "COBBLESTONE_SLAB"
      - "COBBLESTONE_WALL"
      - "MOSSY_COBBLESTONE_STAIRS"
      - "MOSSY_COBBLESTONE_SLAB"
      - "MOSSY_COBBLESTONE_WALL"
      - "STONE_BRICK_STAIRS"
      - "STONE_BRICK_SLAB"
      - "STONE_BRICK_WALL"
      - "MOSSY_STONE_BRICK_STAIRS"
      - "MOSSY_STONE_BRICK_SLAB"
      - "MOSSY_STONE_BRICK_WALL"
      - "ANDESITE_STAIRS"
      - "ANDESITE_SLAB"
      - "ANDESITE_WALL"
      - "POLISHED_ANDESITE_STAIRS"
      - "POLISHED_ANDESITE_SLAB"
      - "POLISHED_ANDESITE_WALL"
      - "DIORITE_STAIRS"
      - "DIORITE_SLAB"
      - "DIORITE_WALL"
      - "POLISHED_DIORITE_STAIRS"
      - "POLISHED_DIORITE_SLAB"
      - "POLISHED_DIORITE_WALL"
      - "GRANITE_STAIRS"
      - "GRANITE_SLAB"
      - "GRANITE_WALL"
      - "POLISHED_GRANITE_STAIRS"
      - "POLISHED_GRANITE_SLAB"
      - "POLISHED_GRANITE_WALL"
      - "SMOOTH_STONE_STAIRS"
      - "SMOOTH_STONE_SLAB"
      - "SMOOTH_STONE_WALL"
      - "TUFF_STAIRS"
      - "TUFF_SLAB"
      - "TUFF_WALL"
      - "TUFF_BRICK_STAIRS"
      - "TUFF_BRICK_SLAB"
      - "TUFF_BRICK_WALL"
      - "TUFF_BRICKS"
      - "CHISELED_TUFF"
      - "CHISELED_TUFF_BRICKS"
      - "POLISHED_TUFF"
      - "DEEPSLATE"
      - "COBBLED_DEEPSLATE"
      - "POLISHED_DEEPSLATE"
      - "DEEPSLATE_BRICKS"
      - "DEEPSLATE_TILES"
      - "CRACKED_DEEPSLATE_BRICKS"
      - "CRACKED_DEEPSLATE_TILES"
      - "CHISELED_DEEPSLATE"
      - "REINFORCED_DEEPSLATE"
      - "COBBLED_DEEPSLATE_STAIRS"
      - "COBBLED_DEEPSLATE_SLAB"
      - "COBBLED_DEEPSLATE_WALL"
      - "POLISHED_DEEPSLATE_STAIRS"
      - "POLISHED_DEEPSLATE_SLAB"
      - "POLISHED_DEEPSLATE_WALL"
      - "DEEPSLATE_BRICK_STAIRS"
      - "DEEPSLATE_BRICK_SLAB"
      - "DEEPSLATE_BRICK_WALL"
      - "DEEPSLATE_TILE_STAIRS"
      - "DEEPSLATE_TILE_SLAB"
      - "DEEPSLATE_TILE_WALL"
      - "BLACKSTONE"
      - "GILDED_BLACKSTONE"
      - "POLISHED_BLACKSTONE"
      - "CHISELED_POLISHED_BLACKSTONE"
      - "BLACKSTONE_SLAB"
      - "BLACKSTONE_STAIRS"
      - "BLACKSTONE_WALL"
      - "POLISHED_BLACKSTONE_SLAB"
      - "POLISHED_BLACKSTONE_STAIRS"
      - "POLISHED_BLACKSTONE_WALL"
      - "POLISHED_BLACKSTONE_BRICKS"
      - "CRACKED_POLISHED_BLACKSTONE_BRICKS"
      - "POLISHED_BLACKSTONE_BRICK_SLAB"
      - "POLISHED_BLACKSTONE_BRICK_STAIRS"
      - "POLISHED_BLACKSTONE_BRICK_WALL"
      - "BRICKS"
      - "MUD_BRICKS"
      - "PACKED_MUD"
      - "BRICK_STAIRS"
      - "BRICK_SLAB"
      - "BRICK_WALL"
      - "MUD_BRICK_STAIRS"
      - "MUD_BRICK_SLAB"
      - "MUD_BRICK_WALL"
      - "WHITE_CONCRETE"
      - "WHITE_CONCRETE_POWDER"
      - "ORANGE_CONCRETE"
      - "ORANGE_CONCRETE_POWDER"
      - "MAGENTA_CONCRETE"
      - "MAGENTA_CONCRETE_POWDER"
      - "LIGHT_BLUE_CONCRETE"
      - "LIGHT_BLUE_CONCRETE_POWDER"
      - "YELLOW_CONCRETE"
      - "YELLOW_CONCRETE_POWDER"
      - "LIME_CONCRETE"
      - "LIME_CONCRETE_POWDER"
      - "PINK_CONCRETE"
      - "PINK_CONCRETE_POWDER"
      - "GRAY_CONCRETE"
      - "GRAY_CONCRETE_POWDER"
      - "LIGHT_GRAY_CONCRETE"
      - "LIGHT_GRAY_CONCRETE_POWDER"
      - "CYAN_CONCRETE"
      - "CYAN_CONCRETE_POWDER"
      - "PURPLE_CONCRETE"
      - "PURPLE_CONCRETE_POWDER"
      - "BLUE_CONCRETE"
      - "BLUE_CONCRETE_POWDER"
      - "BROWN_CONCRETE"
      - "BROWN_CONCRETE_POWDER"
      - "GREEN_CONCRETE"
      - "GREEN_CONCRETE_POWDER"
      - "RED_CONCRETE"
      - "RED_CONCRETE_POWDER"
      - "BLACK_CONCRETE"
      - "BLACK_CONCRETE_POWDER"
      - "WHITE_TERRACOTTA"
      - "WHITE_GLAZED_TERRACOTTA"
      - "ORANGE_TERRACOTTA"
      - "ORANGE_GLAZED_TERRACOTTA"
      - "MAGENTA_TERRACOTTA"
      - "MAGENTA_GLAZED_TERRACOTTA"
      - "LIGHT_BLUE_TERRACOTTA"
      - "LIGHT_BLUE_GLAZED_TERRACOTTA"
      - "YELLOW_TERRACOTTA"
      - "YELLOW_GLAZED_TERRACOTTA"
      - "LIME_TERRACOTTA"
      - "LIME_GLAZED_TERRACOTTA"
      - "PINK_TERRACOTTA"
      - "PINK_GLAZED_TERRACOTTA"
      - "GRAY_TERRACOTTA"
      - "GRAY_GLAZED_TERRACOTTA"
      - "LIGHT_GRAY_TERRACOTTA"
      - "LIGHT_GRAY_GLAZED_TERRACOTTA"
      - "CYAN_TERRACOTTA"
      - "CYAN_GLAZED_TERRACOTTA"
      - "PURPLE_TERRACOTTA"
      - "PURPLE_GLAZED_TERRACOTTA"
      - "BLUE_TERRACOTTA"
      - "BLUE_GLAZED_TERRACOTTA"
      - "BROWN_TERRACOTTA"
      - "BROWN_GLAZED_TERRACOTTA"
      - "GREEN_TERRACOTTA"
      - "GREEN_GLAZED_TERRACOTTA"
      - "RED_TERRACOTTA"
      - "RED_GLAZED_TERRACOTTA"
      - "BLACK_TERRACOTTA"
      - "BLACK_GLAZED_TERRACOTTA"
      - "WHITE_STAINED_GLASS"
      - "WHITE_STAINED_GLASS_PANE"
      - "ORANGE_STAINED_GLASS"
      - "ORANGE_STAINED_GLASS_PANE"
      - "MAGENTA_STAINED_GLASS"
      - "MAGENTA_STAINED_GLASS_PANE"
      - "LIGHT_BLUE_STAINED_GLASS"
      - "LIGHT_BLUE_STAINED_GLASS_PANE"
      - "YELLOW_STAINED_GLASS"
      - "YELLOW_STAINED_GLASS_PANE"
      - "LIME_STAINED_GLASS"
      - "LIME_STAINED_GLASS_PANE"
      - "PINK_STAINED_GLASS"
      - "PINK_STAINED_GLASS_PANE"
      - "GRAY_STAINED_GLASS"
      - "GRAY_STAINED_GLASS_PANE"
      - "LIGHT_GRAY_STAINED_GLASS"
      - "LIGHT_GRAY_STAINED_GLASS_PANE"
      - "CYAN_STAINED_GLASS"
      - "CYAN_STAINED_GLASS_PANE"
      - "PURPLE_STAINED_GLASS"
      - "PURPLE_STAINED_GLASS_PANE"
      - "BLUE_STAINED_GLASS"
      - "BLUE_STAINED_GLASS_PANE"
      - "BROWN_STAINED_GLASS"
      - "BROWN_STAINED_GLASS_PANE"
      - "GREEN_STAINED_GLASS"
      - "GREEN_STAINED_GLASS_PANE"
      - "RED_STAINED_GLASS"
      - "RED_STAINED_GLASS_PANE"
      - "BLACK_STAINED_GLASS"
      - "BLACK_STAINED_GLASS_PANE"
      - "QUARTZ_BLOCK"
      - "SMOOTH_QUARTZ"
      - "QUARTZ_BRICKS"
      - "CHISELED_QUARTZ_BLOCK"
      - "QUARTZ_PILLAR"
      - "QUARTZ_STAIRS"
      - "QUARTZ_SLAB"
      - "QUARTZ_WALL"
      - "SMOOTH_QUARTZ_STAIRS"
      - "SMOOTH_QUARTZ_SLAB"
      - "SMOOTH_QUARTZ_WALL"
      - "COPPER_BLOCK"
      - "CUT_COPPER"
      - "CUT_COPPER_STAIRS"
      - "CUT_COPPER_SLAB"
      - "CHISELED_COPPER"
      - "EXPOSED_COPPER_BLOCK"
      - "CUT_EXPOSED_COPPER"
      - "CUT_EXPOSED_COPPER_STAIRS"
      - "CUT_EXPOSED_COPPER_SLAB"
      - "CHISELED_EXPOSED_COPPER"
      - "WEATHERED_COPPER_BLOCK"
      - "CUT_WEATHERED_COPPER"
      - "CUT_WEATHERED_COPPER_STAIRS"
      - "CUT_WEATHERED_COPPER_SLAB"
      - "CHISELED_WEATHERED_COPPER"
      - "OXIDIZED_COPPER_BLOCK"
      - "CUT_OXIDIZED_COPPER"
      - "CUT_OXIDIZED_COPPER_STAIRS"
      - "CUT_OXIDIZED_COPPER_SLAB"
      - "CHISELED_OXIDIZED_COPPER"
      - "WAXED_COPPER_BLOCK"
      - "WAXED_CUT_COPPER"
      - "WAXED_CUT_COPPER_STAIRS"
      - "WAXED_CUT_COPPER_SLAB"
      - "WAXED_EXPOSED_COPPER"
      - "WAXED_WEATHERED_COPPER"
      - "WAXED_OXIDIZED_COPPER"
      - "COPPER_GRATE"
      - "EXPOSED_COPPER_GRATE"
      - "WEATHERED_COPPER_GRATE"
      - "OXIDIZED_COPPER_GRATE"
      - "COPPER_BULB"
      - "EXPOSED_COPPER_BULB"
      - "WEATHERED_COPPER_BULB"
      - "OXIDIZED_COPPER_BULB"
      - "COPPER_DOOR"
      - "EXPOSED_COPPER_DOOR"
      - "WEATHERED_COPPER_DOOR"
      - "OXIDIZED_COPPER_DOOR"
      - "COPPER_TRAPDOOR"
      - "EXPOSED_COPPER_TRAPDOOR"
      - "WEATHERED_COPPER_TRAPDOOR"
      - "OXIDIZED_COPPER_TRAPDOOR"
      - "PRISMARINE"
      - "PRISMARINE_BRICKS"
      - "DARK_PRISMARINE"
      - "PRISMARINE_STAIRS"
      - "PRISMARINE_SLAB"
      - "PRISMARINE_WALL"
      - "PRISMARINE_BRICK_STAIRS"
      - "PRISMARINE_BRICK_SLAB"
      - "PRISMARINE_BRICK_WALL"
      - "DARK_PRISMARINE_STAIRS"
      - "DARK_PRISMARINE_SLAB"
      - "DARK_PRISMARINE_WALL"
      - "END_STONE"
      - "END_STONE_BRICKS"
      - "PURPUR_BLOCK"
      - "PURPUR_PILLAR"
      - "END_STONE_BRICK_STAIRS"
      - "END_STONE_BRICK_SLAB"
      - "END_STONE_BRICK_WALL"
      - "PURPUR_STAIRS"
      - "PURPUR_SLAB"
      - "PURPUR_WALL"
      - "GLASS"
      - "TINTED_GLASS"
      - "GLASS_PANE"
      - "IRON_BLOCK"
      - "IRON_BARS"
      - "CHAIN"
      - "RAW_IRON_BLOCK"
      - "RAW_GOLD_BLOCK"
      - "RAW_COPPER_BLOCK"
      - "GOLD_BLOCK"
      - "NETHER_BRICKS"
      - "RED_NETHER_BRICKS"
      - "CRACKED_NETHER_BRICKS"
      - "CHISELED_NETHER_BRICKS"
      - "NETHER_BRICK_FENCE"
      - "NETHER_BRICK_STAIRS"
      - "NETHER_BRICK_SLAB"
      - "NETHER_BRICK_WALL"
      - "GLOWSTONE"
      - "SEA_LANTERN"
      - "MAGMA_BLOCK"
      - "HAY_BLOCK"
      - "DRYING_RACK"
  natural_terrain:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 0.5
    #Список Material (Bukkit)
    materials:
      - "DIRT"
      - "GRASS_BLOCK"
      - "COARSE_DIRT"
      - "ROOTED_DIRT"
      - "PODZOL"
      - "MYCELIUM"
      - "SAND"
      - "RED_SAND"
      - "GRAVEL"
      - "CLAY"
      - "MUD"
      - "MUDDY_MANGROVE_ROOTS"
      - "MANGROVE_ROOTS"
      - "SNOW"
      - "SNOW_BLOCK"
      - "SOUL_SAND"
      - "SOUL_SOIL"
      - "FARMLAND"
      - "DIRT_PATH"
  storage_shulker:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 2.0
    #Список Material (Bukkit)
    materials:
      - "SHULKER_BOX"
      - "WHITE_SHULKER_BOX"
      - "ORANGE_SHULKER_BOX"
      - "MAGENTA_SHULKER_BOX"
      - "LIGHT_BLUE_SHULKER_BOX"
      - "YELLOW_SHULKER_BOX"
      - "LIME_SHULKER_BOX"
      - "PINK_SHULKER_BOX"
      - "GRAY_SHULKER_BOX"
      - "LIGHT_GRAY_SHULKER_BOX"
      - "CYAN_SHULKER_BOX"
      - "PURPLE_SHULKER_BOX"
      - "BLUE_SHULKER_BOX"
      - "BROWN_SHULKER_BOX"
      - "GREEN_SHULKER_BOX"
      - "RED_SHULKER_BOX"
      - "BLACK_SHULKER_BOX"
  liquid_sources:
    #Единая взрывоустойчивость для всех материалов группы
    blast-resistance: 0.1
    #Список Material (Bukkit)
    materials:
      - "WATER"
      - "LAVA"
      - "KELP"
      - "KELP_PLANT"
      - "SEAGRASS"
      - "TALL_SEAGRASS"
      - "BUBBLE_COLUMN"
```

#### `dynamites/dynamites.yml`

> `plugins/SoulBlast/dynamites/dynamites.yml` — все заряды: радиус, взрыв, цена, эффекты

```yaml
void-crescent:
  #Уникальный ID (латиница, нижнее подчёркивание)
  id: "void_crescent"
  item:
    #Bukkit Material предмета
    material: "TNT"
    #Отображаемое имя (мини-сообщение &, без § в файле — используйте &
    display-name: "&b&lСвет Холодной Луны"
    #Строки описания (lore)
    lore:
      - "&bРоль: &7рассечение построек"
      - "&7Дерево, кирпич, стекло в радиусе"
      - "&7Обсидиан стачивается, но медленно"
      - "&7Землю и траву не трогает"
    #Custom Model Data (0 = выключено)
    custom-model-data: 0
    #Свечение предмета в инвентаре
    glow: true
  #Тики до взрыва (20 тиков = 1 сек)
  fuse-ticks: 80
  ignition:
    #Огниво / огненный заряд
    allow-flint-and-steel: true
    #Огонь / лава рядом
    allow-fire: true
    #Лук с Flame
    allow-flame-bow: true
    #Красный камень / кнопки / рычаги
    allow-redstone: true
    #Цепная реакция от другого взрыва
    allow-explosion: true
    #Поджигание другим динамитом
    allow-other-primed: true
  #Поджечь сразу при установке блока
  auto-ignite-on-place: true
  #Отключить гравитацию — динамит летит вверх
  disable-gravity: false
  #Вертикальная скорость при отключённой гравитации
  upward-velocity: 0.15
  crafting:
    #Включить рецепт в верстаке
    enabled: false
    #Ключ рецепта (NamespacedKey)
    recipe-key: "soulblast_example"
    #Форма: до 3 строк по 3 символа (материал или #)
    shape:
      - "TST"
      - "SGS"
      - "TST"
    #Символ -> Material (T=TNT, S=SAND, G=GUNPOWDER)
    ingredients:
      - "T=TNT"
      - "S=SAND"
      - "G=GUNPOWDER"
    #Количество в результате
    result-amount: 1
  glow:
    #Подсветка сущности динамита
    enabled: true
    #RGB цвет (0-255), формат r,g,b
    color-rgb: "120,160,255"
    #Анимация: NONE, PULSE, RAINBOW
    animation: "PULSE"
    #Интервал тиков между шагами анимации
    animation-interval-ticks: 4
    #Цветная обводка через scoreboard team (по color-rgb)
    use-team-color: true
    #Частицы пыли цвета души вокруг подожжённого динамита
    spawn-particles: true
  hologram:
    enabled: true
    #Строка 1: пусто = из messages fuse-hologram-name ({display})
    line-name: ""
    #Строка 2: пусто = из messages fuse-hologram-timer ({fuse})
    line-timer: ""
    #Смещение Y от центра сущности
    offset-y: 1.2
  explosion:
    #Радиус взрыва в блоках
    radius: 10.0
    #Урон сущностям (0 = только отбрасывание)
    entity-damage: 8.0
    #Наносить урон игрокам (false = без урона, отброс может остаться)
    damage-players: true
    #Сила взрыва / power для отбрасывания
    power: 6.0
    #LOW/MEDIUM/HIGH — лучи; WAVE — сфера как граната; EXTREME — царь, объём
    quality: "WAVE"
    #Лучей при расчёте (0 = авто по quality)
    sampling-ray-override: 0
    #Доля лимита general на блоки (1.0 = весь потолок)
    block-budget-multiplier: 1.0
    #Создавать огонь в мире
    create-fire: false
    #Разрушать блоки (false = только урон и эффекты)
    break-blocks: true
    #Использовать распределение по тикам из general
    spread-across-ticks: true
    #STANDARD — гриф-база; WHITELIST — только block-rules; OMNIVORE — царь, всё кроме KEEP
    block-policy: "WHITELIST"
    algorithm:
      #Множитель случайного разброса силы луча (1.0 = ванильно-подобно)
      ray-randomness: 1.15
      #Шаг луча в блоках (меньше = точнее, дороже)
      ray-step: 0.4
      #Множитель дропа (1.0 = лут с блоков, 0 = без дропа)
      drop-chance-multiplier: 1.0
      #Bukkit-физика (соседи, гравитация) только на оболочке объёма взрыва
      edge-physics-only: true
      #Дополнительный шанс поджечь блок (0-1)
      fire-chance: 0.0
      #Множитель к взрывоустойчивости (<1 = разрушительнее, гриф: 0.2–0.4)
      resistance-multiplier: 0.16
      #Минимальная сила луча для разрушения блока
      minimum-ray-power: 0.008
      #WAVE: не бить блоки за стенами (луч от центра заряда)
      wave-line-of-sight: false
      #WAVE: после сферы прогнать лучи — усиленный decay на их дорожках
      wave-ray-overlay: true
      #Число лучей оверлея (0 = авто от радиуса)
      wave-ray-overlay-rays: 144
      #Множитель decay на блоках, попавших и в волну, и в луч
      wave-ray-overlay-decay-multiplier: 1.9
      #Мгновенный разлом обсидиана (без decay), сила от расстояния до центра
      obsidian-instant-shatter: false
      #Порог близости (0–1) для мгновенного разлома обычного обсидиана
      obsidian-shatter-obsidian-proximity: 0.28
      #Порог близости (0–1) для плачущего обсидиана (выше = ближе к центру)
      obsidian-shatter-crying-proximity: 0.42
    effects:
      #Убирать воду в радиусе
      remove-water: false
      #Убирать лаву в радиусе
      remove-lava: false
      #0 = радиус взрыва
      liquid-radius: 0.0
      #Лимит снятия жидкости за детонацию; 0 = авто от liquidRadius
      liquid-drain-max-blocks: 0
      #Анимация засасывающей воронки при осушении
      drain-vortex: false
      #Длительность воронки в тиках
      drain-vortex-ticks: 36
      #Множитель частиц и звуков воронки
      drain-vortex-intensity: 1.0
      #Разрушать блоки TNT
      destroy-tnt-blocks: false
      #Поджигать другие primed TNT в радиусе
      detonate-other-primed: false
      crater-fill:
        #Залить дно кратера после разрушения блоков
        enabled: false
        radius: 5.0
        floor-material: "OBSIDIAN"
        #Пусто = без заливки; LAVA только при allow-lava-coat (царь-бомба)
        coat-material: ""
        floor-depth: 2
        #Шанс лавы на дне 0-1; для грифа оставь 0
        lava-chance: 0.0
        #Заливать лавой/жидким coat (только last_pyre)
        allow-lava-coat: false
        #Кольцо магмы вокруг кратера (маска)
        magma-shell: false
        #Ширина магматического кольца за пределами radius кратера
        magma-shell-width: 4.0
        #Высота магматической маски в блоках
        magma-shell-layers: 4
        shell-material: "MAGMA"
        spread-across-ticks: true
        #EXTREME: россыпь по полу вместо сплошных слоёв
        hell-floor-scatter: true
        #Шанс блока на клетку пола (0.35–0.45 — проходимо)
        hell-floor-density: 0.4
        #Доля адского камня/песка душ среди россыпи (магма — основа, проходимо)
        hell-floor-lava-ratio: 0.18
      fuse-lightning:
        #Молнии по земле перед взрывом (пока TNT ещё на месте)
        enabled: false
        #За сколько тиков до детонации начать град (20 = ~1 сек)
        ticks-before-end: 20
        #Число ударов
        bolt-count: 6
        #Интервал между ударами в тиках
        bolt-interval-ticks: 2
        #Разброс по горизонтали от центра TNT
        spread-radius: 10.0
        #Реальная молния (урон/поджог) или только эффект
        real-lightning: false
      warheads:
        #Царь-бомба: отделение боеголовок перед детонацией
        enabled: false
        #За сколько тиков до взрыва ядра отделить боеголовки
        launch-ticks-before-end: 35
        #Горизонтальная скорость разлёта
        launch-speed: 1.15
        #Доп. скорость вверх
        upward-boost: 0.42
        #Фитиль боеголовки после отделения
        warhead-fuse-ticks: 55
        #ID динамитов-боеголовок (порядок = направления по кругу)
        warhead-ids: []
      presentation:
        #Доп. частицы и звуки при детонации (поверх ванильной вспышки)
        enabled: true
        #Множитель количества частиц и громкости (1.0 = база)
        intensity: 1.45
    #Правила для конкретных блоков/групп
    block-rules:
      -         #Bukkit Material или ключ группы material-groups
target: "BEDROCK"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "BREAK"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
    #Действия после взрыва
    post-actions: []
  purchase:
    #Один канал: MONEY | EXPERIENCE | VANILLA_TNT | FREE
    type: "MONEY"
    #Сумма: монеты, опыт или штуки TNT
    amount: 350.0
    #Кулдаун покупки в секундах (0 = из config.yml, -1 = выкл)
    purchase-cooldown-seconds: 0
    #Кулдаун установки/взрыва в секундах (0 = из config.yml, -1 = выкл)
    use-cooldown-seconds: 0
    money: 0.0
    experience: 0
    vanilla-tnt: 0
  fuse-misfire:
    #Осечка: таймер кончился, взрыва нет — нужно подойти и ПКМ
    enabled: true
    #Шанс осечки при старте таймера (0–1)
    end-chance: 0.1
    #За сколько тиков до конца показать предупреждение, если уже осечка
    warning-ticks: 40
    #ПКМ на осечке: шанс снова запустить таймер (0–1)
    activate-relight-chance: 0.52
    #ПКМ на осечке: шанс мгновенного взрыва (0–1)
    activate-detonate-chance: 0.33
    #Звук осечки (entity.creeper.primed или ENTITY_CREEPER_PRIMED)
    dud-sound: "entity.creeper.primed"
    dud-sound-volume: 1.0
    dud-sound-pitch: 1.25
    #Лёгкие частицы пока заряд в осечке
    dud-ambient-particles: true
soul-supernova:
  #Уникальный ID (латиница, нижнее подчёркивание)
  id: "soul_supernova"
  item:
    #Bukkit Material предмета
    material: "TNT"
    #Отображаемое имя (мини-сообщение &, без § в файле — используйте &
    display-name: "&d&lГром Великой Руны"
    #Строки описания (lore)
    lore:
      - "&dРоль: &7штурм базы"
      - "&7Ломает постройки, сундуки — лут"
      - "&7Обсидиан только &5Плачущие души"
    #Custom Model Data (0 = выключено)
    custom-model-data: 0
    #Свечение предмета в инвентаре
    glow: true
  #Тики до взрыва (20 тиков = 1 сек)
  fuse-ticks: 80
  ignition:
    #Огниво / огненный заряд
    allow-flint-and-steel: true
    #Огонь / лава рядом
    allow-fire: true
    #Лук с Flame
    allow-flame-bow: true
    #Красный камень / кнопки / рычаги
    allow-redstone: true
    #Цепная реакция от другого взрыва
    allow-explosion: true
    #Поджигание другим динамитом
    allow-other-primed: true
  #Поджечь сразу при установке блока
  auto-ignite-on-place: true
  #Отключить гравитацию — динамит летит вверх
  disable-gravity: false
  #Вертикальная скорость при отключённой гравитации
  upward-velocity: 0.15
  crafting:
    #Включить рецепт в верстаке
    enabled: false
    #Ключ рецепта (NamespacedKey)
    recipe-key: "soulblast_example"
    #Форма: до 3 строк по 3 символа (материал или #)
    shape:
      - "TST"
      - "SGS"
      - "TST"
    #Символ -> Material (T=TNT, S=SAND, G=GUNPOWDER)
    ingredients:
      - "T=TNT"
      - "S=SAND"
      - "G=GUNPOWDER"
    #Количество в результате
    result-amount: 1
  glow:
    #Подсветка сущности динамита
    enabled: true
    #RGB цвет (0-255), формат r,g,b
    color-rgb: "200,80,220"
    #Анимация: NONE, PULSE, RAINBOW
    animation: "PULSE"
    #Интервал тиков между шагами анимации
    animation-interval-ticks: 4
    #Цветная обводка через scoreboard team (по color-rgb)
    use-team-color: true
    #Частицы пыли цвета души вокруг подожжённого динамита
    spawn-particles: true
  hologram:
    enabled: true
    #Строка 1: пусто = из messages fuse-hologram-name ({display})
    line-name: ""
    #Строка 2: пусто = из messages fuse-hologram-timer ({fuse})
    line-timer: ""
    #Смещение Y от центра сущности
    offset-y: 1.2
  explosion:
    #Радиус взрыва в блоках
    radius: 10.0
    #Урон сущностям (0 = только отбрасывание)
    entity-damage: 14.0
    #Наносить урон игрокам (false = без урона, отброс может остаться)
    damage-players: true
    #Сила взрыва / power для отбрасывания
    power: 6.5
    #LOW/MEDIUM/HIGH — лучи; WAVE — сфера как граната; EXTREME — царь, объём
    quality: "HIGH"
    #Лучей при расчёте (0 = авто по quality)
    sampling-ray-override: 0
    #Доля лимита general на блоки (1.0 = весь потолок)
    block-budget-multiplier: 1.0
    #Создавать огонь в мире
    create-fire: false
    #Разрушать блоки (false = только урон и эффекты)
    break-blocks: true
    #Использовать распределение по тикам из general
    spread-across-ticks: true
    #STANDARD — гриф-база; WHITELIST — только block-rules; OMNIVORE — царь, всё кроме KEEP
    block-policy: "STANDARD"
    algorithm:
      #Множитель случайного разброса силы луча (1.0 = ванильно-подобно)
      ray-randomness: 1.15
      #Шаг луча в блоках (меньше = точнее, дороже)
      ray-step: 0.4
      #Множитель дропа (1.0 = лут с блоков, 0 = без дропа)
      drop-chance-multiplier: 1.0
      #Bukkit-физика (соседи, гравитация) только на оболочке объёма взрыва
      edge-physics-only: true
      #Дополнительный шанс поджечь блок (0-1)
      fire-chance: 0.0
      #Множитель к взрывоустойчивости (<1 = разрушительнее, гриф: 0.2–0.4)
      resistance-multiplier: 0.24
      #Минимальная сила луча для разрушения блока
      minimum-ray-power: 0.015
      #WAVE: не бить блоки за стенами (луч от центра заряда)
      wave-line-of-sight: true
      #WAVE: после сферы прогнать лучи — усиленный decay на их дорожках
      wave-ray-overlay: false
      #Число лучей оверлея (0 = авто от радиуса)
      wave-ray-overlay-rays: 0
      #Множитель decay на блоках, попавших и в волну, и в луч
      wave-ray-overlay-decay-multiplier: 1.85
      #Мгновенный разлом обсидиана (без decay), сила от расстояния до центра
      obsidian-instant-shatter: false
      #Порог близости (0–1) для мгновенного разлома обычного обсидиана
      obsidian-shatter-obsidian-proximity: 0.28
      #Порог близости (0–1) для плачущего обсидиана (выше = ближе к центру)
      obsidian-shatter-crying-proximity: 0.42
    effects:
      #Убирать воду в радиусе
      remove-water: true
      #Убирать лаву в радиусе
      remove-lava: true
      #0 = радиус взрыва
      liquid-radius: 10.0
      #Лимит снятия жидкости за детонацию; 0 = авто от liquidRadius
      liquid-drain-max-blocks: 0
      #Анимация засасывающей воронки при осушении
      drain-vortex: false
      #Длительность воронки в тиках
      drain-vortex-ticks: 36
      #Множитель частиц и звуков воронки
      drain-vortex-intensity: 1.0
      #Разрушать блоки TNT
      destroy-tnt-blocks: false
      #Поджигать другие primed TNT в радиусе
      detonate-other-primed: false
      crater-fill:
        #Залить дно кратера после разрушения блоков
        enabled: false
        radius: 5.0
        floor-material: "OBSIDIAN"
        #Пусто = без заливки; LAVA только при allow-lava-coat (царь-бомба)
        coat-material: ""
        floor-depth: 2
        #Шанс лавы на дне 0-1; для грифа оставь 0
        lava-chance: 0.0
        #Заливать лавой/жидким coat (только last_pyre)
        allow-lava-coat: false
        #Кольцо магмы вокруг кратера (маска)
        magma-shell: false
        #Ширина магматического кольца за пределами radius кратера
        magma-shell-width: 4.0
        #Высота магматической маски в блоках
        magma-shell-layers: 4
        shell-material: "MAGMA"
        spread-across-ticks: true
        #EXTREME: россыпь по полу вместо сплошных слоёв
        hell-floor-scatter: true
        #Шанс блока на клетку пола (0.35–0.45 — проходимо)
        hell-floor-density: 0.4
        #Доля адского камня/песка душ среди россыпи (магма — основа, проходимо)
        hell-floor-lava-ratio: 0.18
      fuse-lightning:
        #Молнии по земле перед взрывом (пока TNT ещё на месте)
        enabled: false
        #За сколько тиков до детонации начать град (20 = ~1 сек)
        ticks-before-end: 20
        #Число ударов
        bolt-count: 6
        #Интервал между ударами в тиках
        bolt-interval-ticks: 2
        #Разброс по горизонтали от центра TNT
        spread-radius: 10.0
        #Реальная молния (урон/поджог) или только эффект
        real-lightning: false
      warheads:
        #Царь-бомба: отделение боеголовок перед детонацией
        enabled: false
        #За сколько тиков до взрыва ядра отделить боеголовки
        launch-ticks-before-end: 35
        #Горизонтальная скорость разлёта
        launch-speed: 1.15
        #Доп. скорость вверх
        upward-boost: 0.42
        #Фитиль боеголовки после отделения
        warhead-fuse-ticks: 55
        #ID динамитов-боеголовок (порядок = направления по кругу)
        warhead-ids: []
      presentation:
        #Доп. частицы и звуки при детонации (поверх ванильной вспышки)
        enabled: true
        #Множитель количества частиц и громкости (1.0 = база)
        intensity: 1.0
    #Правила для конкретных блоков/групп
    block-rules:
      -         #Bukkit Material или ключ группы material-groups
target: "obsidian_hard"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "KEEP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
      -         #Bukkit Material или ключ группы material-groups
target: "REINFORCED_DEEPSLATE"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "KEEP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
      -         #Bukkit Material или ключ группы material-groups
target: "BEDROCK"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "KEEP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
      -         #Bukkit Material или ключ группы material-groups
target: "CHEST"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "DROP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
      -         #Bukkit Material или ключ группы material-groups
target: "TRAPPED_CHEST"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "DROP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
      -         #Bukkit Material или ключ группы material-groups
target: "BARREL"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "DROP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
      -         #Bukkit Material или ключ группы material-groups
target: "ENDER_CHEST"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "DROP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
    #Действия после взрыва
    post-actions:
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "SOUND"
        #Параметры: particle, sound, команда и т.д.
        param: "entity_generic_explode"
        #Доп. аргументы
        args: []
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 0
  purchase:
    #Один канал: MONEY | EXPERIENCE | VANILLA_TNT | FREE
    type: "EXPERIENCE"
    #Сумма: монеты, опыт или штуки TNT
    amount: 1500.0
    #Кулдаун покупки в секундах (0 = из config.yml, -1 = выкл)
    purchase-cooldown-seconds: 0
    #Кулдаун установки/взрыва в секундах (0 = из config.yml, -1 = выкл)
    use-cooldown-seconds: 0
    money: 0.0
    experience: 0
    vanilla-tnt: 0
  fuse-misfire:
    #Осечка: таймер кончился, взрыва нет — нужно подойти и ПКМ
    enabled: false
    #Шанс осечки при старте таймера (0–1)
    end-chance: 0.0
    #За сколько тиков до конца показать предупреждение, если уже осечка
    warning-ticks: 40
    #ПКМ на осечке: шанс снова запустить таймер (0–1)
    activate-relight-chance: 0.5
    #ПКМ на осечке: шанс мгновенного взрыва (0–1)
    activate-detonate-chance: 0.35
    #Звук осечки (entity.creeper.primed или ENTITY_CREEPER_PRIMED)
    dud-sound: "entity.creeper.primed"
    dud-sound-volume: 1.0
    dud-sound-pitch: 1.25
    #Лёгкие частицы пока заряд в осечке
    dud-ambient-particles: true
crying-souls:
  #Уникальный ID (латиница, нижнее подчёркивание)
  id: "crying_souls"
  item:
    #Bukkit Material предмета
    material: "TNT"
    #Отображаемое имя (мини-сообщение &, без § в файле — используйте &
    display-name: "&5&lПлачущие души"
    #Строки описания (lore)
    lore:
      - "&5Роль: &7штурм обсидиана"
      - "&7Только обсидиан и плачущий"
      - "&7Ближе к заряду — мгновенный разлом"
      - "&7Постройки из дерева/кирпича не трогает"
    #Custom Model Data (0 = выключено)
    custom-model-data: 0
    #Свечение предмета в инвентаре
    glow: true
  #Тики до взрыва (20 тиков = 1 сек)
  fuse-ticks: 90
  ignition:
    #Огниво / огненный заряд
    allow-flint-and-steel: true
    #Огонь / лава рядом
    allow-fire: true
    #Лук с Flame
    allow-flame-bow: true
    #Красный камень / кнопки / рычаги
    allow-redstone: true
    #Цепная реакция от другого взрыва
    allow-explosion: true
    #Поджигание другим динамитом
    allow-other-primed: true
  #Поджечь сразу при установке блока
  auto-ignite-on-place: true
  #Отключить гравитацию — динамит летит вверх
  disable-gravity: false
  #Вертикальная скорость при отключённой гравитации
  upward-velocity: 0.15
  crafting:
    #Включить рецепт в верстаке
    enabled: false
    #Ключ рецепта (NamespacedKey)
    recipe-key: "soulblast_example"
    #Форма: до 3 строк по 3 символа (материал или #)
    shape:
      - "TST"
      - "SGS"
      - "TST"
    #Символ -> Material (T=TNT, S=SAND, G=GUNPOWDER)
    ingredients:
      - "T=TNT"
      - "S=SAND"
      - "G=GUNPOWDER"
    #Количество в результате
    result-amount: 1
  glow:
    #Подсветка сущности динамита
    enabled: true
    #RGB цвет (0-255), формат r,g,b
    color-rgb: "90,20,120"
    #Анимация: NONE, PULSE, RAINBOW
    animation: "PULSE"
    #Интервал тиков между шагами анимации
    animation-interval-ticks: 4
    #Цветная обводка через scoreboard team (по color-rgb)
    use-team-color: true
    #Частицы пыли цвета души вокруг подожжённого динамита
    spawn-particles: true
  hologram:
    enabled: true
    #Строка 1: пусто = из messages fuse-hologram-name ({display})
    line-name: ""
    #Строка 2: пусто = из messages fuse-hologram-timer ({fuse})
    line-timer: ""
    #Смещение Y от центра сущности
    offset-y: 1.2
  explosion:
    #Радиус взрыва в блоках
    radius: 10.0
    #Урон сущностям (0 = только отбрасывание)
    entity-damage: 4.0
    #Наносить урон игрокам (false = без урона, отброс может остаться)
    damage-players: false
    #Сила взрыва / power для отбрасывания
    power: 5.0
    #LOW/MEDIUM/HIGH — лучи; WAVE — сфера как граната; EXTREME — царь, объём
    quality: "WAVE"
    #Лучей при расчёте (0 = авто по quality)
    sampling-ray-override: 0
    #Доля лимита general на блоки (1.0 = весь потолок)
    block-budget-multiplier: 1.0
    #Создавать огонь в мире
    create-fire: false
    #Разрушать блоки (false = только урон и эффекты)
    break-blocks: true
    #Использовать распределение по тикам из general
    spread-across-ticks: false
    #STANDARD — гриф-база; WHITELIST — только block-rules; OMNIVORE — царь, всё кроме KEEP
    block-policy: "WHITELIST"
    algorithm:
      #Множитель случайного разброса силы луча (1.0 = ванильно-подобно)
      ray-randomness: 1.15
      #Шаг луча в блоках (меньше = точнее, дороже)
      ray-step: 0.4
      #Множитель дропа (1.0 = лут с блоков, 0 = без дропа)
      drop-chance-multiplier: 1.0
      #Bukkit-физика (соседи, гравитация) только на оболочке объёма взрыва
      edge-physics-only: true
      #Дополнительный шанс поджечь блок (0-1)
      fire-chance: 0.0
      #Множитель к взрывоустойчивости (<1 = разрушительнее, гриф: 0.2–0.4)
      resistance-multiplier: 0.04
      #Минимальная сила луча для разрушения блока
      minimum-ray-power: 0.001
      #WAVE: не бить блоки за стенами (луч от центра заряда)
      wave-line-of-sight: true
      #WAVE: после сферы прогнать лучи — усиленный decay на их дорожках
      wave-ray-overlay: false
      #Число лучей оверлея (0 = авто от радиуса)
      wave-ray-overlay-rays: 0
      #Множитель decay на блоках, попавших и в волну, и в луч
      wave-ray-overlay-decay-multiplier: 1.85
      #Мгновенный разлом обсидиана (без decay), сила от расстояния до центра
      obsidian-instant-shatter: true
      #Порог близости (0–1) для мгновенного разлома обычного обсидиана
      obsidian-shatter-obsidian-proximity: 0.26
      #Порог близости (0–1) для плачущего обсидиана (выше = ближе к центру)
      obsidian-shatter-crying-proximity: 0.4
    effects:
      #Убирать воду в радиусе
      remove-water: false
      #Убирать лаву в радиусе
      remove-lava: false
      #0 = радиус взрыва
      liquid-radius: 0.0
      #Лимит снятия жидкости за детонацию; 0 = авто от liquidRadius
      liquid-drain-max-blocks: 0
      #Анимация засасывающей воронки при осушении
      drain-vortex: false
      #Длительность воронки в тиках
      drain-vortex-ticks: 36
      #Множитель частиц и звуков воронки
      drain-vortex-intensity: 1.0
      #Разрушать блоки TNT
      destroy-tnt-blocks: false
      #Поджигать другие primed TNT в радиусе
      detonate-other-primed: false
      crater-fill:
        #Залить дно кратера после разрушения блоков
        enabled: false
        radius: 5.0
        floor-material: "OBSIDIAN"
        #Пусто = без заливки; LAVA только при allow-lava-coat (царь-бомба)
        coat-material: ""
        floor-depth: 2
        #Шанс лавы на дне 0-1; для грифа оставь 0
        lava-chance: 0.0
        #Заливать лавой/жидким coat (только last_pyre)
        allow-lava-coat: false
        #Кольцо магмы вокруг кратера (маска)
        magma-shell: false
        #Ширина магматического кольца за пределами radius кратера
        magma-shell-width: 4.0
        #Высота магматической маски в блоках
        magma-shell-layers: 4
        shell-material: "MAGMA"
        spread-across-ticks: true
        #EXTREME: россыпь по полу вместо сплошных слоёв
        hell-floor-scatter: true
        #Шанс блока на клетку пола (0.35–0.45 — проходимо)
        hell-floor-density: 0.4
        #Доля адского камня/песка душ среди россыпи (магма — основа, проходимо)
        hell-floor-lava-ratio: 0.18
      fuse-lightning:
        #Молнии по земле перед взрывом (пока TNT ещё на месте)
        enabled: false
        #За сколько тиков до детонации начать град (20 = ~1 сек)
        ticks-before-end: 20
        #Число ударов
        bolt-count: 6
        #Интервал между ударами в тиках
        bolt-interval-ticks: 2
        #Разброс по горизонтали от центра TNT
        spread-radius: 10.0
        #Реальная молния (урон/поджог) или только эффект
        real-lightning: false
      warheads:
        #Царь-бомба: отделение боеголовок перед детонацией
        enabled: false
        #За сколько тиков до взрыва ядра отделить боеголовки
        launch-ticks-before-end: 35
        #Горизонтальная скорость разлёта
        launch-speed: 1.15
        #Доп. скорость вверх
        upward-boost: 0.42
        #Фитиль боеголовки после отделения
        warhead-fuse-ticks: 55
        #ID динамитов-боеголовок (порядок = направления по кругу)
        warhead-ids: []
      presentation:
        #Доп. частицы и звуки при детонации (поверх ванильной вспышки)
        enabled: true
        #Множитель количества частиц и громкости (1.0 = база)
        intensity: 1.55
    #Правила для конкретных блоков/групп
    block-rules:
      -         #Bukkit Material или ключ группы material-groups
target: "obsidian_hard"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "BREAK"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
    #Действия после взрыва
    post-actions:
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "SOUND"
        #Параметры: particle, sound, команда и т.д.
        param: "block_amethyst_block_break"
        #Доп. аргументы
        args: []
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 0
  purchase:
    #Один канал: MONEY | EXPERIENCE | VANILLA_TNT | FREE
    type: "MONEY"
    #Сумма: монеты, опыт или штуки TNT
    amount: 450.0
    #Кулдаун покупки в секундах (0 = из config.yml, -1 = выкл)
    purchase-cooldown-seconds: 0
    #Кулдаун установки/взрыва в секундах (0 = из config.yml, -1 = выкл)
    use-cooldown-seconds: 0
    money: 0.0
    experience: 0
    vanilla-tnt: 0
  fuse-misfire:
    #Осечка: таймер кончился, взрыва нет — нужно подойти и ПКМ
    enabled: false
    #Шанс осечки при старте таймера (0–1)
    end-chance: 0.0
    #За сколько тиков до конца показать предупреждение, если уже осечка
    warning-ticks: 40
    #ПКМ на осечке: шанс снова запустить таймер (0–1)
    activate-relight-chance: 0.5
    #ПКМ на осечке: шанс мгновенного взрыва (0–1)
    activate-detonate-chance: 0.35
    #Звук осечки (entity.creeper.primed или ENTITY_CREEPER_PRIMED)
    dud-sound: "entity.creeper.primed"
    dud-sound-volume: 1.0
    dud-sound-pitch: 1.25
    #Лёгкие частицы пока заряд в осечке
    dud-ambient-particles: true
eclipse-heart:
  #Уникальный ID (латиница, нижнее подчёркивание)
  id: "eclipse_heart"
  item:
    #Bukkit Material предмета
    material: "TNT"
    #Отображаемое имя (мини-сообщение &, без § в файле — используйте &
    display-name: "&4&lПламя Затменного Владыки"
    #Строки описания (lore)
    lore:
      - "&cРоль: &7поджог и сжигание"
      - "&7Огонь в радиусе, блоки как у штурма"
      - "&7Обсидиан не рвёт"
    #Custom Model Data (0 = выключено)
    custom-model-data: 0
    #Свечение предмета в инвентаре
    glow: true
  #Тики до взрыва (20 тиков = 1 сек)
  fuse-ticks: 80
  ignition:
    #Огниво / огненный заряд
    allow-flint-and-steel: true
    #Огонь / лава рядом
    allow-fire: true
    #Лук с Flame
    allow-flame-bow: true
    #Красный камень / кнопки / рычаги
    allow-redstone: true
    #Цепная реакция от другого взрыва
    allow-explosion: true
    #Поджигание другим динамитом
    allow-other-primed: true
  #Поджечь сразу при установке блока
  auto-ignite-on-place: true
  #Отключить гравитацию — динамит летит вверх
  disable-gravity: false
  #Вертикальная скорость при отключённой гравитации
  upward-velocity: 0.15
  crafting:
    #Включить рецепт в верстаке
    enabled: false
    #Ключ рецепта (NamespacedKey)
    recipe-key: "soulblast_example"
    #Форма: до 3 строк по 3 символа (материал или #)
    shape:
      - "TST"
      - "SGS"
      - "TST"
    #Символ -> Material (T=TNT, S=SAND, G=GUNPOWDER)
    ingredients:
      - "T=TNT"
      - "S=SAND"
      - "G=GUNPOWDER"
    #Количество в результате
    result-amount: 1
  glow:
    #Подсветка сущности динамита
    enabled: true
    #RGB цвет (0-255), формат r,g,b
    color-rgb: "160,25,35"
    #Анимация: NONE, PULSE, RAINBOW
    animation: "PULSE"
    #Интервал тиков между шагами анимации
    animation-interval-ticks: 4
    #Цветная обводка через scoreboard team (по color-rgb)
    use-team-color: true
    #Частицы пыли цвета души вокруг подожжённого динамита
    spawn-particles: true
  hologram:
    enabled: true
    #Строка 1: пусто = из messages fuse-hologram-name ({display})
    line-name: ""
    #Строка 2: пусто = из messages fuse-hologram-timer ({fuse})
    line-timer: ""
    #Смещение Y от центра сущности
    offset-y: 1.2
  explosion:
    #Радиус взрыва в блоках
    radius: 10.0
    #Урон сущностям (0 = только отбрасывание)
    entity-damage: 12.0
    #Наносить урон игрокам (false = без урона, отброс может остаться)
    damage-players: true
    #Сила взрыва / power для отбрасывания
    power: 6.0
    #LOW/MEDIUM/HIGH — лучи; WAVE — сфера как граната; EXTREME — царь, объём
    quality: "HIGH"
    #Лучей при расчёте (0 = авто по quality)
    sampling-ray-override: 0
    #Доля лимита general на блоки (1.0 = весь потолок)
    block-budget-multiplier: 1.0
    #Создавать огонь в мире
    create-fire: true
    #Разрушать блоки (false = только урон и эффекты)
    break-blocks: true
    #Использовать распределение по тикам из general
    spread-across-ticks: true
    #STANDARD — гриф-база; WHITELIST — только block-rules; OMNIVORE — царь, всё кроме KEEP
    block-policy: "STANDARD"
    algorithm:
      #Множитель случайного разброса силы луча (1.0 = ванильно-подобно)
      ray-randomness: 1.15
      #Шаг луча в блоках (меньше = точнее, дороже)
      ray-step: 0.4
      #Множитель дропа (1.0 = лут с блоков, 0 = без дропа)
      drop-chance-multiplier: 1.0
      #Bukkit-физика (соседи, гравитация) только на оболочке объёма взрыва
      edge-physics-only: true
      #Дополнительный шанс поджечь блок (0-1)
      fire-chance: 0.45
      #Множитель к взрывоустойчивости (<1 = разрушительнее, гриф: 0.2–0.4)
      resistance-multiplier: 0.24
      #Минимальная сила луча для разрушения блока
      minimum-ray-power: 0.015
      #WAVE: не бить блоки за стенами (луч от центра заряда)
      wave-line-of-sight: true
      #WAVE: после сферы прогнать лучи — усиленный decay на их дорожках
      wave-ray-overlay: false
      #Число лучей оверлея (0 = авто от радиуса)
      wave-ray-overlay-rays: 0
      #Множитель decay на блоках, попавших и в волну, и в луч
      wave-ray-overlay-decay-multiplier: 1.85
      #Мгновенный разлом обсидиана (без decay), сила от расстояния до центра
      obsidian-instant-shatter: false
      #Порог близости (0–1) для мгновенного разлома обычного обсидиана
      obsidian-shatter-obsidian-proximity: 0.28
      #Порог близости (0–1) для плачущего обсидиана (выше = ближе к центру)
      obsidian-shatter-crying-proximity: 0.42
    effects:
      #Убирать воду в радиусе
      remove-water: true
      #Убирать лаву в радиусе
      remove-lava: false
      #0 = радиус взрыва
      liquid-radius: 0.0
      #Лимит снятия жидкости за детонацию; 0 = авто от liquidRadius
      liquid-drain-max-blocks: 0
      #Анимация засасывающей воронки при осушении
      drain-vortex: false
      #Длительность воронки в тиках
      drain-vortex-ticks: 36
      #Множитель частиц и звуков воронки
      drain-vortex-intensity: 1.0
      #Разрушать блоки TNT
      destroy-tnt-blocks: false
      #Поджигать другие primed TNT в радиусе
      detonate-other-primed: false
      crater-fill:
        #Залить дно кратера после разрушения блоков
        enabled: false
        radius: 5.0
        floor-material: "OBSIDIAN"
        #Пусто = без заливки; LAVA только при allow-lava-coat (царь-бомба)
        coat-material: ""
        floor-depth: 2
        #Шанс лавы на дне 0-1; для грифа оставь 0
        lava-chance: 0.0
        #Заливать лавой/жидким coat (только last_pyre)
        allow-lava-coat: false
        #Кольцо магмы вокруг кратера (маска)
        magma-shell: false
        #Ширина магматического кольца за пределами radius кратера
        magma-shell-width: 4.0
        #Высота магматической маски в блоках
        magma-shell-layers: 4
        shell-material: "MAGMA"
        spread-across-ticks: true
        #EXTREME: россыпь по полу вместо сплошных слоёв
        hell-floor-scatter: true
        #Шанс блока на клетку пола (0.35–0.45 — проходимо)
        hell-floor-density: 0.4
        #Доля адского камня/песка душ среди россыпи (магма — основа, проходимо)
        hell-floor-lava-ratio: 0.18
      fuse-lightning:
        #Молнии по земле перед взрывом (пока TNT ещё на месте)
        enabled: false
        #За сколько тиков до детонации начать град (20 = ~1 сек)
        ticks-before-end: 20
        #Число ударов
        bolt-count: 6
        #Интервал между ударами в тиках
        bolt-interval-ticks: 2
        #Разброс по горизонтали от центра TNT
        spread-radius: 10.0
        #Реальная молния (урон/поджог) или только эффект
        real-lightning: false
      warheads:
        #Царь-бомба: отделение боеголовок перед детонацией
        enabled: false
        #За сколько тиков до взрыва ядра отделить боеголовки
        launch-ticks-before-end: 35
        #Горизонтальная скорость разлёта
        launch-speed: 1.15
        #Доп. скорость вверх
        upward-boost: 0.42
        #Фитиль боеголовки после отделения
        warhead-fuse-ticks: 55
        #ID динамитов-боеголовок (порядок = направления по кругу)
        warhead-ids: []
      presentation:
        #Доп. частицы и звуки при детонации (поверх ванильной вспышки)
        enabled: true
        #Множитель количества частиц и громкости (1.0 = база)
        intensity: 1.0
    #Правила для конкретных блоков/групп
    block-rules:
      -         #Bukkit Material или ключ группы material-groups
target: "obsidian_hard"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "KEEP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
      -         #Bukkit Material или ключ группы material-groups
target: "BEDROCK"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "KEEP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
    #Действия после взрыва
    post-actions: []
  purchase:
    #Один канал: MONEY | EXPERIENCE | VANILLA_TNT | FREE
    type: "EXPERIENCE"
    #Сумма: монеты, опыт или штуки TNT
    amount: 950.0
    #Кулдаун покупки в секундах (0 = из config.yml, -1 = выкл)
    purchase-cooldown-seconds: 0
    #Кулдаун установки/взрыва в секундах (0 = из config.yml, -1 = выкл)
    use-cooldown-seconds: 0
    money: 0.0
    experience: 0
    vanilla-tnt: 0
  fuse-misfire:
    #Осечка: таймер кончился, взрыва нет — нужно подойти и ПКМ
    enabled: false
    #Шанс осечки при старте таймера (0–1)
    end-chance: 0.0
    #За сколько тиков до конца показать предупреждение, если уже осечка
    warning-ticks: 40
    #ПКМ на осечке: шанс снова запустить таймер (0–1)
    activate-relight-chance: 0.5
    #ПКМ на осечке: шанс мгновенного взрыва (0–1)
    activate-detonate-chance: 0.35
    #Звук осечки (entity.creeper.primed или ENTITY_CREEPER_PRIMED)
    dud-sound: "entity.creeper.primed"
    dud-sound-volume: 1.0
    dud-sound-pitch: 1.25
    #Лёгкие частицы пока заряд в осечке
    dud-ambient-particles: true
abyss-collapse:
  #Уникальный ID (латиница, нижнее подчёркивание)
  id: "abyss_collapse"
  item:
    #Bukkit Material предмета
    material: "TNT"
    #Отображаемое имя (мини-сообщение &, без § в файле — используйте &
    display-name: "&1&lВздох Пустоты"
    #Строки описания (lore)
    lore:
      - "&9Роль: &7осушение"
      - "&7Засасывающая воронка пустоты"
      - "&7Сразу вытягивает воду и лаву"
      - "&7Сфера осушения &f18 &7блоков"
    #Custom Model Data (0 = выключено)
    custom-model-data: 0
    #Свечение предмета в инвентаре
    glow: true
  #Тики до взрыва (20 тиков = 1 сек)
  fuse-ticks: 80
  ignition:
    #Огниво / огненный заряд
    allow-flint-and-steel: true
    #Огонь / лава рядом
    allow-fire: true
    #Лук с Flame
    allow-flame-bow: true
    #Красный камень / кнопки / рычаги
    allow-redstone: true
    #Цепная реакция от другого взрыва
    allow-explosion: true
    #Поджигание другим динамитом
    allow-other-primed: true
  #Поджечь сразу при установке блока
  auto-ignite-on-place: true
  #Отключить гравитацию — динамит летит вверх
  disable-gravity: false
  #Вертикальная скорость при отключённой гравитации
  upward-velocity: 0.15
  crafting:
    #Включить рецепт в верстаке
    enabled: false
    #Ключ рецепта (NamespacedKey)
    recipe-key: "soulblast_example"
    #Форма: до 3 строк по 3 символа (материал или #)
    shape:
      - "TST"
      - "SGS"
      - "TST"
    #Символ -> Material (T=TNT, S=SAND, G=GUNPOWDER)
    ingredients:
      - "T=TNT"
      - "S=SAND"
      - "G=GUNPOWDER"
    #Количество в результате
    result-amount: 1
  glow:
    #Подсветка сущности динамита
    enabled: true
    #RGB цвет (0-255), формат r,g,b
    color-rgb: "25,35,110"
    #Анимация: NONE, PULSE, RAINBOW
    animation: "PULSE"
    #Интервал тиков между шагами анимации
    animation-interval-ticks: 4
    #Цветная обводка через scoreboard team (по color-rgb)
    use-team-color: true
    #Частицы пыли цвета души вокруг подожжённого динамита
    spawn-particles: true
  hologram:
    enabled: true
    #Строка 1: пусто = из messages fuse-hologram-name ({display})
    line-name: ""
    #Строка 2: пусто = из messages fuse-hologram-timer ({fuse})
    line-timer: ""
    #Смещение Y от центра сущности
    offset-y: 1.2
  explosion:
    #Радиус взрыва в блоках
    radius: 18.0
    #Урон сущностям (0 = только отбрасывание)
    entity-damage: 6.0
    #Наносить урон игрокам (false = без урона, отброс может остаться)
    damage-players: false
    #Сила взрыва / power для отбрасывания
    power: 5.0
    #LOW/MEDIUM/HIGH — лучи; WAVE — сфера как граната; EXTREME — царь, объём
    quality: "WAVE"
    #Лучей при расчёте (0 = авто по quality)
    sampling-ray-override: 0
    #Доля лимита general на блоки (1.0 = весь потолок)
    block-budget-multiplier: 1.0
    #Создавать огонь в мире
    create-fire: false
    #Разрушать блоки (false = только урон и эффекты)
    break-blocks: true
    #Использовать распределение по тикам из general
    spread-across-ticks: false
    #STANDARD — гриф-база; WHITELIST — только block-rules; OMNIVORE — царь, всё кроме KEEP
    block-policy: "WHITELIST"
    algorithm:
      #Множитель случайного разброса силы луча (1.0 = ванильно-подобно)
      ray-randomness: 1.15
      #Шаг луча в блоках (меньше = точнее, дороже)
      ray-step: 0.4
      #Множитель дропа (1.0 = лут с блоков, 0 = без дропа)
      drop-chance-multiplier: 1.0
      #Bukkit-физика (соседи, гравитация) только на оболочке объёма взрыва
      edge-physics-only: true
      #Дополнительный шанс поджечь блок (0-1)
      fire-chance: 0.0
      #Множитель к взрывоустойчивости (<1 = разрушительнее, гриф: 0.2–0.4)
      resistance-multiplier: 0.019999999552965164
      #Минимальная сила луча для разрушения блока
      minimum-ray-power: 0.001
      #WAVE: не бить блоки за стенами (луч от центра заряда)
      wave-line-of-sight: false
      #WAVE: после сферы прогнать лучи — усиленный decay на их дорожках
      wave-ray-overlay: false
      #Число лучей оверлея (0 = авто от радиуса)
      wave-ray-overlay-rays: 0
      #Множитель decay на блоках, попавших и в волну, и в луч
      wave-ray-overlay-decay-multiplier: 1.85
      #Мгновенный разлом обсидиана (без decay), сила от расстояния до центра
      obsidian-instant-shatter: false
      #Порог близости (0–1) для мгновенного разлома обычного обсидиана
      obsidian-shatter-obsidian-proximity: 0.28
      #Порог близости (0–1) для плачущего обсидиана (выше = ближе к центру)
      obsidian-shatter-crying-proximity: 0.42
    effects:
      #Убирать воду в радиусе
      remove-water: true
      #Убирать лаву в радиусе
      remove-lava: true
      #0 = радиус взрыва
      liquid-radius: 18.0
      #Лимит снятия жидкости за детонацию; 0 = авто от liquidRadius
      liquid-drain-max-blocks: 0
      #Анимация засасывающей воронки при осушении
      drain-vortex: true
      #Длительность воронки в тиках
      drain-vortex-ticks: 36
      #Множитель частиц и звуков воронки
      drain-vortex-intensity: 1.4
      #Разрушать блоки TNT
      destroy-tnt-blocks: false
      #Поджигать другие primed TNT в радиусе
      detonate-other-primed: false
      crater-fill:
        #Залить дно кратера после разрушения блоков
        enabled: false
        radius: 5.0
        floor-material: "OBSIDIAN"
        #Пусто = без заливки; LAVA только при allow-lava-coat (царь-бомба)
        coat-material: ""
        floor-depth: 2
        #Шанс лавы на дне 0-1; для грифа оставь 0
        lava-chance: 0.0
        #Заливать лавой/жидким coat (только last_pyre)
        allow-lava-coat: false
        #Кольцо магмы вокруг кратера (маска)
        magma-shell: false
        #Ширина магматического кольца за пределами radius кратера
        magma-shell-width: 4.0
        #Высота магматической маски в блоках
        magma-shell-layers: 4
        shell-material: "MAGMA"
        spread-across-ticks: true
        #EXTREME: россыпь по полу вместо сплошных слоёв
        hell-floor-scatter: true
        #Шанс блока на клетку пола (0.35–0.45 — проходимо)
        hell-floor-density: 0.4
        #Доля адского камня/песка душ среди россыпи (магма — основа, проходимо)
        hell-floor-lava-ratio: 0.18
      fuse-lightning:
        #Молнии по земле перед взрывом (пока TNT ещё на месте)
        enabled: false
        #За сколько тиков до детонации начать град (20 = ~1 сек)
        ticks-before-end: 20
        #Число ударов
        bolt-count: 6
        #Интервал между ударами в тиках
        bolt-interval-ticks: 2
        #Разброс по горизонтали от центра TNT
        spread-radius: 10.0
        #Реальная молния (урон/поджог) или только эффект
        real-lightning: false
      warheads:
        #Царь-бомба: отделение боеголовок перед детонацией
        enabled: false
        #За сколько тиков до взрыва ядра отделить боеголовки
        launch-ticks-before-end: 35
        #Горизонтальная скорость разлёта
        launch-speed: 1.15
        #Доп. скорость вверх
        upward-boost: 0.42
        #Фитиль боеголовки после отделения
        warhead-fuse-ticks: 55
        #ID динамитов-боеголовок (порядок = направления по кругу)
        warhead-ids: []
      presentation:
        #Доп. частицы и звуки при детонации (поверх ванильной вспышки)
        enabled: true
        #Множитель количества частиц и громкости (1.0 = база)
        intensity: 1.85
    #Правила для конкретных блоков/групп
    block-rules:
      -         #Bukkit Material или ключ группы material-groups
target: "liquid_sources"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "BREAK"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
    #Действия после взрыва
    post-actions: []
  purchase:
    #Один канал: MONEY | EXPERIENCE | VANILLA_TNT | FREE
    type: "MONEY"
    #Сумма: монеты, опыт или штуки TNT
    amount: 650.0
    #Кулдаун покупки в секундах (0 = из config.yml, -1 = выкл)
    purchase-cooldown-seconds: 0
    #Кулдаун установки/взрыва в секундах (0 = из config.yml, -1 = выкл)
    use-cooldown-seconds: 0
    money: 0.0
    experience: 0
    vanilla-tnt: 0
  fuse-misfire:
    #Осечка: таймер кончился, взрыва нет — нужно подойти и ПКМ
    enabled: false
    #Шанс осечки при старте таймера (0–1)
    end-chance: 0.0
    #За сколько тиков до конца показать предупреждение, если уже осечка
    warning-ticks: 40
    #ПКМ на осечке: шанс снова запустить таймер (0–1)
    activate-relight-chance: 0.5
    #ПКМ на осечке: шанс мгновенного взрыва (0–1)
    activate-detonate-chance: 0.35
    #Звук осечки (entity.creeper.primed или ENTITY_CREEPER_PRIMED)
    dud-sound: "entity.creeper.primed"
    dud-sound-volume: 1.0
    dud-sound-pitch: 1.25
    #Лёгкие частицы пока заряд в осечке
    dud-ambient-particles: true
last-pyre:
  #Уникальный ID (латиница, нижнее подчёркивание)
  id: "last_pyre"
  item:
    #Bukkit Material предмета
    material: "TNT"
    #Отображаемое имя (мини-сообщение &, без § в файле — используйте &
    display-name: "&6&lПоследний Разжигатель Пламени"
    #Строки описания (lore)
    lore:
      - "&6Роль: &cсудный приговор"
      - "&7Ломает &fвсё&7 (кроме бедрока)"
      - "&7Ядро + 7 боеголовок, ад и лава"
    #Custom Model Data (0 = выключено)
    custom-model-data: 0
    #Свечение предмета в инвентаре
    glow: true
  #Тики до взрыва (20 тиков = 1 сек)
  fuse-ticks: 220
  ignition:
    #Огниво / огненный заряд
    allow-flint-and-steel: true
    #Огонь / лава рядом
    allow-fire: true
    #Лук с Flame
    allow-flame-bow: true
    #Красный камень / кнопки / рычаги
    allow-redstone: true
    #Цепная реакция от другого взрыва
    allow-explosion: true
    #Поджигание другим динамитом
    allow-other-primed: true
  #Поджечь сразу при установке блока
  auto-ignite-on-place: true
  #Отключить гравитацию — динамит летит вверх
  disable-gravity: false
  #Вертикальная скорость при отключённой гравитации
  upward-velocity: 0.15
  crafting:
    #Включить рецепт в верстаке
    enabled: false
    #Ключ рецепта (NamespacedKey)
    recipe-key: "soulblast_example"
    #Форма: до 3 строк по 3 символа (материал или #)
    shape:
      - "TST"
      - "SGS"
      - "TST"
    #Символ -> Material (T=TNT, S=SAND, G=GUNPOWDER)
    ingredients:
      - "T=TNT"
      - "S=SAND"
      - "G=GUNPOWDER"
    #Количество в результате
    result-amount: 1
  glow:
    #Подсветка сущности динамита
    enabled: true
    #RGB цвет (0-255), формат r,g,b
    color-rgb: "240,160,40"
    #Анимация: NONE, PULSE, RAINBOW
    animation: "PULSE"
    #Интервал тиков между шагами анимации
    animation-interval-ticks: 4
    #Цветная обводка через scoreboard team (по color-rgb)
    use-team-color: true
    #Частицы пыли цвета души вокруг подожжённого динамита
    spawn-particles: true
  hologram:
    enabled: true
    #Строка 1: пусто = из messages fuse-hologram-name ({display})
    line-name: ""
    #Строка 2: пусто = из messages fuse-hologram-timer ({fuse})
    line-timer: ""
    #Смещение Y от центра сущности
    offset-y: 1.5
  explosion:
    #Радиус взрыва в блоках
    radius: 96.0
    #Урон сущностям (0 = только отбрасывание)
    entity-damage: 80.0
    #Наносить урон игрокам (false = без урона, отброс может остаться)
    damage-players: true
    #Сила взрыва / power для отбрасывания
    power: 52.0
    #LOW/MEDIUM/HIGH — лучи; WAVE — сфера как граната; EXTREME — царь, объём
    quality: "EXTREME"
    #Лучей при расчёте (0 = авто по quality)
    sampling-ray-override: 0
    #Доля лимита general на блоки (1.0 = весь потолок)
    block-budget-multiplier: 1.35
    #Создавать огонь в мире
    create-fire: true
    #Разрушать блоки (false = только урон и эффекты)
    break-blocks: true
    #Использовать распределение по тикам из general
    spread-across-ticks: true
    #STANDARD — гриф-база; WHITELIST — только block-rules; OMNIVORE — царь, всё кроме KEEP
    block-policy: "OMNIVORE"
    algorithm:
      #Множитель случайного разброса силы луча (1.0 = ванильно-подобно)
      ray-randomness: 1.5
      #Шаг луча в блоках (меньше = точнее, дороже)
      ray-step: 0.25
      #Множитель дропа (1.0 = лут с блоков, 0 = без дропа)
      drop-chance-multiplier: 1.0
      #Bukkit-физика (соседи, гравитация) только на оболочке объёма взрыва
      edge-physics-only: true
      #Дополнительный шанс поджечь блок (0-1)
      fire-chance: 1.0
      #Множитель к взрывоустойчивости (<1 = разрушительнее, гриф: 0.2–0.4)
      resistance-multiplier: 0.02
      #Минимальная сила луча для разрушения блока
      minimum-ray-power: 0.001
      #WAVE: не бить блоки за стенами (луч от центра заряда)
      wave-line-of-sight: true
      #WAVE: после сферы прогнать лучи — усиленный decay на их дорожках
      wave-ray-overlay: false
      #Число лучей оверлея (0 = авто от радиуса)
      wave-ray-overlay-rays: 0
      #Множитель decay на блоках, попавших и в волну, и в луч
      wave-ray-overlay-decay-multiplier: 1.85
      #Мгновенный разлом обсидиана (без decay), сила от расстояния до центра
      obsidian-instant-shatter: false
      #Порог близости (0–1) для мгновенного разлома обычного обсидиана
      obsidian-shatter-obsidian-proximity: 0.28
      #Порог близости (0–1) для плачущего обсидиана (выше = ближе к центру)
      obsidian-shatter-crying-proximity: 0.42
    effects:
      #Убирать воду в радиусе
      remove-water: true
      #Убирать лаву в радиусе
      remove-lava: true
      #0 = радиус взрыва
      liquid-radius: 96.0
      #Лимит снятия жидкости за детонацию; 0 = авто от liquidRadius
      liquid-drain-max-blocks: 0
      #Анимация засасывающей воронки при осушении
      drain-vortex: false
      #Длительность воронки в тиках
      drain-vortex-ticks: 36
      #Множитель частиц и звуков воронки
      drain-vortex-intensity: 1.0
      #Разрушать блоки TNT
      destroy-tnt-blocks: true
      #Поджигать другие primed TNT в радиусе
      detonate-other-primed: true
      crater-fill:
        #Залить дно кратера после разрушения блоков
        enabled: true
        radius: 52.0
        floor-material: "MAGMA_BLOCK"
        #Пусто = без заливки; LAVA только при allow-lava-coat (царь-бомба)
        coat-material: "LAVA"
        floor-depth: 14
        #Шанс лавы на дне 0-1; для грифа оставь 0
        lava-chance: 1.0
        #Заливать лавой/жидким coat (только last_pyre)
        allow-lava-coat: true
        #Кольцо магмы вокруг кратера (маска)
        magma-shell: true
        #Ширина магматического кольца за пределами radius кратера
        magma-shell-width: 16.0
        #Высота магматической маски в блоках
        magma-shell-layers: 14
        shell-material: "MAGMA_BLOCK"
        spread-across-ticks: true
        #EXTREME: россыпь по полу вместо сплошных слоёв
        hell-floor-scatter: true
        #Шанс блока на клетку пола (0.35–0.45 — проходимо)
        hell-floor-density: 0.4
        #Доля адского камня/песка душ среди россыпи (магма — основа, проходимо)
        hell-floor-lava-ratio: 0.18
      fuse-lightning:
        #Молнии по земле перед взрывом (пока TNT ещё на месте)
        enabled: true
        #За сколько тиков до детонации начать град (20 = ~1 сек)
        ticks-before-end: 20
        #Число ударов
        bolt-count: 10
        #Интервал между ударами в тиках
        bolt-interval-ticks: 2
        #Разброс по горизонтали от центра TNT
        spread-radius: 21.12
        #Реальная молния (урон/поджог) или только эффект
        real-lightning: false
      warheads:
        #Царь-бомба: отделение боеголовок перед детонацией
        enabled: true
        #За сколько тиков до взрыва ядра отделить боеголовки
        launch-ticks-before-end: 35
        #Горизонтальная скорость разлёта
        launch-speed: 1.15
        #Доп. скорость вверх
        upward-boost: 0.42
        #Фитиль боеголовки после отделения
        warhead-fuse-ticks: 55
        #ID динамитов-боеголовок (порядок = направления по кругу)
        warhead-ids:
          - "void_crescent"
          - "soul_supernova"
          - "crying_souls"
          - "eclipse_heart"
          - "abyss_collapse"
          - "soul_supernova"
      presentation:
        #Доп. частицы и звуки при детонации (поверх ванильной вспышки)
        enabled: true
        #Множитель количества частиц и громкости (1.0 = база)
        intensity: 1.0
    #Правила для конкретных блоков/групп
    block-rules:
      -         #Bukkit Material или ключ группы material-groups
target: "BEDROCK"
        #BREAK, KEEP, DROP, TRANSFORM
        mode: "KEEP"
        #Material после TRANSFORM (если применимо)
        transform-into: "AIR"
        #Шанс применить правило 0-1
        chance: 1.0
        #Переопределение дропа (пусто = ваниль)
        drop-material: ""
    #Действия после взрыва
    post-actions:
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "SOUND"
        #Параметры: particle, sound, команда и т.д.
        param: "entity_generic_explode"
        #Доп. аргументы
        args: []
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 0
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "PARTICLE"
        #Параметры: particle, sound, команда и т.д.
        param: "LAVA"
        #Доп. аргументы
        args:
          - "320"
          - "36"
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 4
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "PARTICLE"
        #Параметры: particle, sound, команда и т.д.
        param: "FLAME"
        #Доп. аргументы
        args:
          - "160"
          - "22"
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 6
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "PARTICLE"
        #Параметры: particle, sound, команда и т.д.
        param: "EXPLOSION_EMITTER"
        #Доп. аргументы
        args:
          - "16"
          - "8"
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 1
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "SOUND"
        #Параметры: particle, sound, команда и т.д.
        param: "entity_wither_break_block"
        #Доп. аргументы
        args: []
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 8
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "SOUND"
        #Параметры: particle, sound, команда и т.д.
        param: "entity_ender_dragon_growl"
        #Доп. аргументы
        args: []
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 12
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "PARTICLE"
        #Параметры: particle, sound, команда и т.д.
        param: "LAVA"
        #Доп. аргументы
        args:
          - "900"
          - "72"
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 2
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "PARTICLE"
        #Параметры: particle, sound, команда и т.д.
        param: "EXPLOSION_EMITTER"
        #Доп. аргументы
        args:
          - "48"
          - "24"
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 0
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "PARTICLE"
        #Параметры: particle, sound, команда и т.д.
        param: "EXPLOSION_EMITTER"
        #Доп. аргументы
        args:
          - "32"
          - "16"
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 15
      -         #PARTICLE, SOUND, COMMAND (LIGHTNING — в effects.fuse-lightning)
type: "SOUND"
        #Параметры: particle, sound, команда и т.д.
        param: "entity_generic_explode"
        #Доп. аргументы
        args: []
        #Задержка в тиках после завершения разрушения блоков
        delay-ticks: 20
  purchase:
    #Один канал: MONEY | EXPERIENCE | VANILLA_TNT | FREE
    type: "VANILLA_TNT"
    #Сумма: монеты, опыт или штуки TNT
    amount: 16.0
    #Кулдаун покупки в секундах (0 = из config.yml, -1 = выкл)
    purchase-cooldown-seconds: 30
    #Кулдаун установки/взрыва в секундах (0 = из config.yml, -1 = выкл)
    use-cooldown-seconds: 30
    money: 0.0
    experience: 0
    vanilla-tnt: 0
  fuse-misfire:
    #Осечка: таймер кончился, взрыва нет — нужно подойти и ПКМ
    enabled: false
    #Шанс осечки при старте таймера (0–1)
    end-chance: 0.0
    #За сколько тиков до конца показать предупреждение, если уже осечка
    warning-ticks: 40
    #ПКМ на осечке: шанс снова запустить таймер (0–1)
    activate-relight-chance: 0.5
    #ПКМ на осечке: шанс мгновенного взрыва (0–1)
    activate-detonate-chance: 0.35
    #Звук осечки (entity.creeper.primed или ENTITY_CREEPER_PRIMED)
    dud-sound: "entity.creeper.primed"
    dud-sound-volume: 1.0
    dud-sound-pitch: 1.25
    #Лёгкие частицы пока заряд в осечке
    dud-ambient-particles: true
```

#### `menu/menu.yml`

> `plugins/SoulBlast/menu/menu.yml` — гримуар: раскладка, иконки, сортировка

```yaml
options:
  #Заголовок меню (&#RRGGBB hex и &-коды)
  title: '        &#C084FC✦ &#721ddbГримуар Между Мирами &#C084FC✦'
  #Схема слотов: 1–6 строк по 9 символов, каждый символ — ключ из icons
  layout:
    - "P   G   S"
    - " @@@@@@@ "
    - " <     > "
  #HIGHEST_POWER, LOWEST_POWER, SHORTEST_FUSE, LONGEST_FUSE, NAME_AZ, NAME_ZA
  default-sorting-type: HIGHEST_POWER
  #Убрать стрелку «назад/вперёд», если страницы нет
  remove-direction-icon-if-none-exists: true
  #ID динамитов, скрытых из списка меню
  exclude: []
command:
  #Включить отдельную команду открытия меню (смена alias — перезапуск)
  enabled: true
  #Право на открытие; пусто — только soulblast.menu из plugin.yml
  permission: soulblast.menu
  #Должны совпадать с alias в plugin.yml у команды soulgrimoire
  alias:
    - "soulgrimoire"
    - "soulfire"
#Символ схемы → иконка слота
icons:
  "":
    #DYNAMITE_ENTRY, PREVIOUS_PAGE, NEXT_PAGE, SORT_CYCLE, INFO_PANEL, FILLER
    type: FILLER
    display:
      material: PURPLE_STAINED_GLASS_PANE
      name: ' '
      lore: []
      custom-model-data: 0
      texture: ""
  G:
    #DYNAMITE_ENTRY, PREVIOUS_PAGE, NEXT_PAGE, SORT_CYCLE, INFO_PANEL, FILLER
    type: GOAL_SLOT
    display:
      material: END_CRYSTAL
      name: '&#C084FC✦ &#721ddbКопилка обмена'
      lore:
        - ""
        - "&#AAAAAA&#F3E8FFПКМ &#AAAAAA— выбрать заряд для склада"
        - "&#AAAAAA&#F3E8FFЛКМ &#AAAAAAв списке — опыт / монеты"
        - ""
      custom-model-data: 0
      texture: ""
  P:
    #DYNAMITE_ENTRY, PREVIOUS_PAGE, NEXT_PAGE, SORT_CYCLE, INFO_PANEL, FILLER
    type: PLAYER_SETTINGS
    display:
      material: BLAZE_POWDER
      name: '&#C084FC✦ &#E9D5FFИскра поджига'
      lore:
        - ""
        - "&#AAAAAAСейчас&#757575: %auto_ignite_label%"
        - ""
        - "&#757575Нажмите, чтобы сменить."
        - "&#AAAAAAВлияет на &#F3E8FFвсе &#AAAAAAваши заряды"
        - "&#AAAAAAпри установке."
        - ""
      custom-model-data: 0
      texture: ""
  @:
    #DYNAMITE_ENTRY, PREVIOUS_PAGE, NEXT_PAGE, SORT_CYCLE, INFO_PANEL, FILLER
    type: DYNAMITE_ENTRY
    display:
      material: TNT
      name: '%soul_name%'
      lore:
        - ""
        - " %soul_desc_1% "
        - " %soul_desc_2% "
        - "%soul_desc_gap%"
        - "&#721ddbСила души&#757575: &#E9D5FF%blast_power%"
        - "&#721ddbИскра до вспышки&#757575: &#86EFAC%fuse_seconds%с"
        - ""
      custom-model-data: 0
      texture: ""
  <:
    #DYNAMITE_ENTRY, PREVIOUS_PAGE, NEXT_PAGE, SORT_CYCLE, INFO_PANEL, FILLER
    type: PREVIOUS_PAGE
    display:
      material: ARROW
      name: '&#721ddb◀ &#C084FCПыль прошлого'
      lore:
        - ""
        - "&#AAAAAAСтраница &#F3E8FF%current_page%&#757575/&#F3E8FF%max_pages%"
        - ""
        - "&#757575Нажмите, чтобы листать гримуар."
        - ""
      custom-model-data: 0
      texture: ""
  >:
    #DYNAMITE_ENTRY, PREVIOUS_PAGE, NEXT_PAGE, SORT_CYCLE, INFO_PANEL, FILLER
    type: NEXT_PAGE
    display:
      material: ARROW
      name: '&#721ddbБуря впереди &#C084FC▶'
      lore:
        - ""
        - "&#AAAAAAСтраница &#F3E8FF%current_page%&#757575/&#F3E8FF%max_pages%"
        - ""
        - "&#757575Нажмите, чтобы листать гримуар."
        - ""
      custom-model-data: 0
      texture: ""
  S:
    #DYNAMITE_ENTRY, PREVIOUS_PAGE, NEXT_PAGE, SORT_CYCLE, INFO_PANEL, FILLER
    type: SORT_CYCLE
    display:
      material: HOPPER
      name: '&#C084FC✦ &#721ddbСортировка душ'
      lore:
        - ""
        - "&#AAAAAAПорядок&#757575: &#E9D5FF%sort_label%"
        - ""
        - "&#757575Нажмите, чтобы сменить круг силы."
        - ""
      custom-model-data: 0
      texture: ""
```

#### `lang/messages.yml`

> `plugins/SoulBlast/lang/messages.yml` — чат, ошибки, подписи

```yaml
prefix: "&8[&cSoulBlast&8] &r"
no-permission: "{prefix}&cНет прав."
player-only: "{prefix}&cТолько для игрока."
unknown-dynamite: "{prefix}&cДинамит &f{id} &cне найден."
dynamite-given: "{prefix}&aВыдан &f{id} &7x{amount} &aигроку &f{player}."
dynamite-received: "{prefix}&aВы получили &f{display} &7x{amount}."
reload-done: "{prefix}&aКонфигурация перезагружена."
ignite-denied: "{prefix}&cЭтот динамит нельзя поджечь таким способом."
command-usage: "{prefix}&7/soulblast give <игрок> <id> [кол-во] | menu | reload &8| &7/soulgrimoire"
goal-selected: "{prefix}&5В копилку&8: &f{display}"
goal-cleared: "{prefix}&7Копилка очищена."
goal-not-selected: "{prefix}&7Сначала &fПКМ &7по заряду в списке."
auto-ignite-on: "{prefix}&aПоджиг&8: &fвзрыв сразу при установке"
auto-ignite-off: "{prefix}&7Поджиг&8: &fтолько от огнива"
purchase-success: "{prefix}&aОбмен совершён&8: &f{display}"
purchase-failed: "{prefix}&cОбмен не удался."
purchase-failed-money: "{prefix}&cНедостаточно монет."
purchase-failed-experience: "{prefix}&cНедостаточно опыта."
purchase-failed-tnt: "{prefix}&cВ копилке не хватает TNT&8: &c{current}&8/&c{required}"
purchase-failed-inventory: "{prefix}&cОсвободите место в инвентаре."
cooldown-purchase-active: "{prefix}&cПокупка &f{display} &cчерез &f{remaining}&c."
cooldown-use-active: "{prefix}&cЗаряд &f{display} &cможно использовать через &f{remaining}&c."
purchase-requirements: "{prefix}&7В списке не хватает&8: {missing}"
catalog-pay-not-required: "{prefix}&7Этот заряд платится только TNT — &#F3E8FFПКМ &#AAAAAAвыбор, &#F3E8FFЛКМ &#AAAAAAпо копилке"
copilka-ready: "{prefix}&aМожно забрать заряд. &7Нажми &fЛКМ &7по копилке"
tnt-deposited: "{prefix}&7Внесено &c{amount} &7TNT &8(&c{current}&8/&c{required}&7)"
copilka-tnt-full: "{prefix}&aСклад TNT полный &8(&c{current}&8/&c{required}&7). &7Забрать заряд — &fЛКМ &7по копилке"
tnt-deposit-none: "{prefix}&cНет обычного TNT в инвентаре."
tnt-deposit-complete: "{prefix}&aСклад TNT уже полный."
tnt-deposit-not-required: "{prefix}&7Для этого заряда TNT не требуется."
region-protected: "{prefix}&cСлишком близко к защищённому региону &f{region}&c. Нужен отступ &fx{margin} &cблоков."
region-protected-blast: "{prefix}&cВзрыв заденет регион &f{region}&c."
region-worldguard-missing: "{prefix}&cЗащита регионов включена, но WorldGuard не найден."
fuse-recall-success: "{prefix}&aЗаряд &f{display} &aснят с таймера и возвращён в инвентарь."
fuse-recall-not-owner: "{prefix}&cЗабрать может только тот, кто поставил этот заряд."
fuse-recall-no-owner: "{prefix}&cЭтот заряд нельзя забрать."
fuse-recall-warhead: "{prefix}&cБоеголовку отдельно не забирают."
fuse-recall-disabled: "{prefix}&cОтмена таймера отключена на сервере."
fuse-hologram-name: "{display}"
fuse-hologram-timer: "&7⏱ &f{fuse} &7сек"
fuse-hologram-recall: "&8▸ &7ЛКМ &8— &fзабрать заряд"
fuse-hologram-misfire-warning: "&e⚠ &7Возможна &cосечка"
fuse-hologram-misfire-active: "&c&lОСЕЧКА"
fuse-hologram-misfire-idle: "&7Фитиль погас — заряд на месте"
fuse-hologram-misfire-hint: "&8▸ &7ПКМ &8— &fподжечь или риск взрыва"
fuse-hologram-misfire-recall: "&8▸ &7ЛКМ &8— &fзабрать в инвентарь"
fuse-misfire-relight: "{prefix}&aЗаряд &f{display} &aснова на таймере."
fuse-misfire-detonate: "{prefix}&c&lОсечка рванула! &f{display}"
fuse-misfire-fizzle: "{prefix}&7Щелчок... ничего. ПКМ ещё раз или ЛКМ — забрать."
fuse-misfire-not-owner: "{prefix}&cТолько поставивший может обезвредить осечку."
```

### Decay (постепенное разрушение)

#### `decay/general.yml`

> `plugins/SoulBlast/decay/general.yml`

```yaml
general:
  #Постепенное разрушение вместо мгновенного ломания для блоков из decay/blocks.yml
  enabled: true
  #Максимум блоков в состоянии decay одновременно
  max-active-blocks: 4096
  #Сколько блоков обновлять трещинами за тик (пакеты игрокам)
  damage-packets-per-tick: 120
  #Радиус зрителей для sendBlockDamage
  viewer-radius: 48
  #Интервал тика decay при активных блоках
  tick-interval: 1
  #Как часто пересчитывать реген (тики)
  regeneration-interval-ticks: 20
  #Период повторной отправки трещин клиенту (тики)
  crack-refresh-ticks: 20
  #Стабильный sourceId для overlay трещин
  crack-source-id: 917364
  #Максимум урона decay за один удар взрыва по блоку (0.05–0.25)
  max-damage-per-hit: 0.14
  #Урон decay слабее к краю радиуса взрыва
  explosion-distance-falloff-enabled: true
  #Множитель урона у границы радиуса (0 = нет урона, 1 = как в центре)
  explosion-min-damage-multiplier-at-edge: 0.15
  #LINEAR или QUADRATIC — насколько быстро падает урон с расстоянием
  explosion-distance-falloff-curve: QUADRATIC
```

#### `decay/blocks.yml`

> `plugins/SoulBlast/decay/blocks.yml` — какие блоки крошатся по тикам

```yaml
types:
  BRICKS:
    resistance: 0.2
    regeneration:
      every: "1 min"
      materials:
        BRICK: "1"
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: true
    variables:
      display_name: "&aКирпичи"
      regeneration_name: "&aБыстрая"
      resistance_name: "&aСлабая"
  OBSIDIAN:
    resistance: 0.5
    regeneration:
      every: "3 min"
      materials: {}
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: false
    variables:
      display_name: "&eОбсидиан"
      regeneration_name: "&eСредняя"
      resistance_name: "&6Выше среднего"
  CRYING_OBSIDIAN:
    resistance: 0.9
    regeneration:
      every: "5 min"
      materials: {}
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: false
    variables:
      display_name: "&cПлачущий обсидиан"
      regeneration_name: "&cДолгая"
      resistance_name: "&cСильная"
  STONE_BRICKS:
    resistance: 0.35
    regeneration:
      every: "8 min"
      materials:
        STONE: "1"
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: true
    variables:
      display_name: "&aКаменные кирпичи"
      regeneration_name: "&aМедленная"
      resistance_name: "&eСредняя"
  NETHER_BRICKS:
    resistance: 0.2
    regeneration:
      every: "1 min"
      materials:
        NETHERRACK: "1"
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: true
    variables:
      display_name: "&aНезерские кирпичи"
      regeneration_name: "&aБыстрая"
      resistance_name: "&aСлабая"
  END_STONE_BRICKS:
    resistance: 0.2
    regeneration:
      every: "1 min"
      materials:
        END_STONE: "1"
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: true
    variables:
      display_name: "&aЭндерняковые кирпичи"
      regeneration_name: "&aБыстрая"
      resistance_name: "&aСлабая"
  PURPUR_BLOCK:
    resistance: 0.3
    regeneration:
      every: "1 min"
      materials:
        POPPED_CHORUS_FRUIT: "1"
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: true
    variables:
      display_name: "&aПурпур"
      regeneration_name: "&aБыстрая"
      resistance_name: "&eСредняя"
  RED_NETHER_BRICKS:
    resistance: 0.3
    regeneration:
      every: "1 min"
      materials:
        NETHER_WART: "1"
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: true
    variables:
      display_name: "&aКрасные незерские кирпичи"
      regeneration_name: "&aБыстрая"
      resistance_name: "&eСредняя"
  ENDER_CHEST:
    resistance: 0.5
    regeneration:
      every: "3 min"
      materials: {}
    options:
      can-player-break: true
      can-player-interact: true
      can-piston-move: true
    variables:
      display_name: "&eЭндер-сундук"
      regeneration_name: "&eСредняя"
      resistance_name: "&6Выше среднего"
```

#### `decay/damage-sources.yml`

> `plugins/SoulBlast/decay/damage-sources.yml` — урон decay от динамитов

```yaml
types:
  DEFAULT: "1"
  TNT: "1-2"
  last_pyre: "2-3"
  void_crescent: "1.5-2.2"
  soul_supernova: "1-2"
  crying_souls: "3.2-4.5"
  eclipse_heart: "1-2"
  abyss_collapse: "2-3"
```

#### `decay/messages.yml`

> `plugins/SoulBlast/decay/messages.yml`

```yaml
menu-title: "&8Типы прочных блоков"
menu-info-name: "&eИнформация"
menu-sort-name: "&bСортировка"
```

### ProtectionStones+

#### `ps/settings.yml`

> `plugins/SoulBlast/ps/settings.yml` — `support-soulblast`, debug, флаги в регионе

```yaml
settings:
  #Подробные логи в консоль (создание привата, алиасы, голограмма)
  debug: true
  #Лог прочности привата: постановка динамита, ДО/ПОСЛЕ взрыва
  durability-trace: true
  silent-startup: false
  #Запрет пересечения нового привата с существующими
  block-merge-with-other-regions: true
  #Прочность привата от кастомных динамитов SoulBlast
  support-soulblast: true
  allow-in-region:
    mine-cart:
      #Удочка для перемещения вагонетки
      hook-up: true
      #Открыть вагонетку с воронкой
      open: true
    #Песок, гравий, сухой бетон и т.д. в чужом регионе
    falling-block: true
    break-block-with-wither: true
    use-fire-arrow-to-ignite-tnt: true
    use-piston: true
    use-spawn-eggs: true
```

#### `ps/types/<alias>.yml`

> `plugins/SoulBlast/ps/types/64.yml` — на каждый тип PS: голограмма, прочность, какие динамиты бьют приват

```yaml
#Голограмма над блоком привата (TextDisplay)
hologram:
  #Включить голограмму
  enabled: true
  #Высота над блоком
  offset-y: 2.0
  #Строки (%owner_name%, %owner%, %durability%, %durability_maximum%,
  #%radius_x%, %radius_y%, %radius_z%, %owner_prefix%, %owner_suffix%)
  lines:
    - "&bБлок привата &7(&f64&7)"
    - "&fВладелец: &b%owner_name%"
    - "&fПрочность: &b%durability%&7/&b%durability_maximum%"
    - "&fРадиус&8: &b%radius_x%&7x&b%radius_y%&7x&b%radius_z%"
    - "&8EMERALD_ORE"
#Молния при установке и снятии
lightning-strike:
  create: true
  remove: true
#Звуки при установке и снятии
sound:
  create: "BLOCK_BEACON_ACTIVATE"
  remove: "BLOCK_BEACON_DEACTIVATE"
#Прочность от динамитов SoulBlast
durability:
  #Считать прочность привата (как шкаф в Rust)
  enabled: true
  #Сколько урона нужно суммарно, чтобы снести блок привата
  maximum: 3
  #Урон, если взрыв SoulBlast в радиусе от блока привата (не только прямой удар)
  proximity-damage: true
  #Радиус от центра взрыва до блока привата (-1 = радиус динамита)
  proximity-radius: -1.0
#Разрушение блока привата
break-protection-block:
  with-explosion:
    #Снос блока привата от кастомных динамитов SoulBlast
    enabled: true
    #id динамита -> урон за один взрыв (1 = один взрыв снимает 1 прочности)
    only-dynamite-types:
      crying_souls: 1
      void_crescent: 1
      soul_supernova: 1
  with-wither:
    enabled: true
#Свечение предмета привата в инвентаре (/ps get)
item-glow:
  #Свечение предмета привата в инвентаре (/ps get, выдача)
  enabled: true
#Ставить и взрывать SoulBlast-динамит в чужом регионе этого типа (не spawn из config.yml)
allow-soulblast-dynamite-in-foreign-claims: true
```

#### `ps/regions/`

> `plugins/SoulBlast/ps/regions/<мир>_<x>_<y>_<z>.yml` — **данные сервера** (прочность, владелец). Руками обычно не правят; плагин сам пишет при создании привата.

### Прочее

> `plugins/SoulBlast/users/player-data.db` — SQLite: копилка TNT, автоподжиг, профили (не YAML)