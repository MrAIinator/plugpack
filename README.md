# PlugPack

Серверный лоадер плагинов для **NeoForge 1.21.1 / 21.1.249**.

Клиентам мод не нужен. В сборку на dedicated-сервер кладётся один jar, плагины — в папку `plugins/` рядом с `mods/`.

## Установка

1. Сервер NeoForge **1.21.1**, сборка **21.1.249** (любая 21.1.x тоже подходит).
2. `plugpack-1.1.0.jar` → `mods/` **только на сервере**.
3. Плагины (`plugin.yml` внутри jar) → `plugins/`.
4. Старт. В консоли будет `Enabled ...`. В игре: `/plugpack`.

Пример: `examples/helloplug`. Команда: `/helloplug`.

## Что умеет

- `plugin.yml`: name, main, version, depend, softdepend, loadbefore, commands, permissions
- Команды, tab-complete, права, op
- Конфиг YAML (`getConfig`, `saveDefaultConfig`)
- Шедулер: sync на тике сервера и async
- Ивенты: join, quit, chat, command, block break/place, interact, death, move (выключен в конфиге)
- Игрок: сообщение, телепорт, gamemode, здоровье, голод, опыт, инвентарь, кик
- Встроенный Vault (права есть, экономики нет)
- Для операторов: выделение кубоида и регионы. Это не jar WorldEdit и не jar WorldGuard.

## Команды оператора

Нужен уровень прав 2. Обычный игрок внутри региона не ломает, не ставит и не жмёт блоки.

- `//wand` — деревянный топор. ЛКМ позиция 1, ПКМ позиция 2
- `//pos1` `//pos2` `//set` `//replace` `//copy` `//paste` `//undo`
- `/region define <имя>` `/region remove <имя>` `/region list` (алиас `/rg`)

Объём за раз не больше 32768 блоков. Регионы пишутся в `config/plugpack/regions.txt`.

Официальные WorldEdit и WorldGuard на этот лоадер не встанут: им нужен Paper/NMS.

## Чего нет

Это не Mohist, не Arclight и не Paper. Не заведутся:

- NMS / CraftBukkit / `obc`
- Paper API, Adventure, ProtocolLib
- плагины, которым нужен чужой мир, scoreboard, пакеты, полноценный инвентарь-GUI
- экономика Vault

`/plugpack reload` перечитывает папку. Если плагин менял классы, надёжнее рестарт сервера.
