# D0-4 PokeAPI 多语言覆盖率

数据来源：PokeAPI/pokeapi@bc92d3b6029e。由 `tools/phase0/` 脚本生成，勿手改。

## 名称覆盖

分母为实体总数；名称 `strip()` 后非空才算覆盖。

| 实体 | 总数 | zh-hans | zh-hant | en | ja |
|---|---|---|---|---|---|
| species 名称 | 1025 | 1025/1025 | 1025/1025 | 1025/1025 | 1025/1025 |
| species genus | 1025 | 1010/1025 | 1010/1025 | 1025/1025 | 913/1025 |
| move 名称（id < 10000） | 919 | 919/919 | 919/919 | 919/919 | 919/919 |
| move 名称（全部） | 937 | 919/937 | 919/937 | 937/937 | 919/937 |
| ability 名称（is_main_series=1） | 314 | 311/314 | 311/314 | 314/314 | 311/314 |
| ability 名称（全部） | 374 | 311/374 | 311/374 | 374/374 | 311/374 |
| item 名称 | 2223 | 2172/2223 | 2172/2223 | 2221/2223 | 2174/2223 |
| type 名称 | 21 | 21/21 | 21/21 | 21/21 | 21/21 |
| nature 名称 | 25 | 25/25 | 25/25 | 25/25 | 25/25 |
| stat 名称 | 9 | 8/9 | 8/9 | 8/9 | 0/9 |
| version 名称 | 53 | 47/53 | 43/53 | 53/53 | 1/53 |
| generation 名称 | 9 | 9/9 | 9/9 | 9/9 | 9/9 |
| egg_group 名称 | 15 | 15/15 | 15/15 | 15/15 | 0/15 |
| pokemon_form 形态名（form_identifier 非空） | 618 | 247/618 | 247/618 | 559/618 | 504/618 |

## 图鉴说明（pokemon_species_flavor_text，按版本）

| version_id | version | zh-hans | zh-hant | en | ja |
|---|---|---|---|---|---|
| 1 | red | 0 | 0 | 151 | 0 |
| 2 | blue | 0 | 0 | 151 | 0 |
| 3 | yellow | 0 | 0 | 151 | 0 |
| 4 | gold | 0 | 0 | 251 | 0 |
| 5 | silver | 0 | 0 | 251 | 0 |
| 6 | crystal | 0 | 0 | 251 | 0 |
| 7 | ruby | 0 | 0 | 386 | 0 |
| 8 | sapphire | 0 | 0 | 386 | 0 |
| 9 | emerald | 0 | 0 | 386 | 0 |
| 10 | firered | 0 | 0 | 386 | 0 |
| 11 | leafgreen | 0 | 0 | 386 | 0 |
| 12 | diamond | 0 | 0 | 493 | 0 |
| 13 | pearl | 0 | 0 | 493 | 0 |
| 14 | platinum | 0 | 0 | 493 | 0 |
| 15 | heartgold | 0 | 0 | 493 | 0 |
| 16 | soulsilver | 0 | 0 | 493 | 0 |
| 17 | black | 0 | 0 | 649 | 0 |
| 18 | white | 0 | 0 | 649 | 0 |
| 19 | colosseum | 0 | 0 | 0 | 0 |
| 20 | xd | 0 | 0 | 0 | 0 |
| 21 | black-2 | 0 | 0 | 649 | 0 |
| 22 | white-2 | 0 | 0 | 649 | 0 |
| 23 | x | 0 | 0 | 721 | 721 |
| 24 | y | 0 | 0 | 721 | 721 |
| 25 | omega-ruby | 0 | 0 | 721 | 721 |
| 26 | alpha-sapphire | 0 | 0 | 721 | 721 |
| 27 | sun | 302 | 302 | 302 | 302 |
| 28 | moon | 302 | 302 | 302 | 302 |
| 29 | ultra-sun | 403 | 403 | 403 | 403 |
| 30 | ultra-moon | 403 | 403 | 403 | 403 |
| 31 | lets-go-pikachu | 153 | 153 | 153 | 153 |
| 32 | lets-go-eevee | 153 | 153 | 153 | 153 |
| 33 | sword | 609 | 609 | 609 | 609 |
| 34 | shield | 609 | 609 | 609 | 609 |
| 35 | the-isle-of-armor-sword | 0 | 0 | 0 | 0 |
| 36 | the-crown-tundra-sword | 0 | 0 | 0 | 0 |
| 37 | brilliant-diamond | 0 | 0 | 0 | 0 |
| 38 | shining-pearl | 0 | 0 | 0 | 0 |
| 39 | legends-arceus | 0 | 0 | 241 | 0 |
| 40 | scarlet | 0 | 0 | 120 | 0 |
| 41 | violet | 0 | 0 | 120 | 0 |
| 42 | the-teal-mask-scarlet | 0 | 0 | 0 | 0 |
| 43 | the-indigo-disk-scarlet | 0 | 0 | 0 | 0 |
| 44 | red-japan | 0 | 0 | 0 | 0 |
| 45 | green-japan | 0 | 0 | 0 | 0 |
| 46 | blue-japan | 0 | 0 | 0 | 0 |
| 47 | legends-za | 0 | 0 | 0 | 0 |
| 48 | mega-dimension | 0 | 0 | 0 | 0 |
| 49 | champions | 0 | 0 | 0 | 0 |
| 50 | the-isle-of-armor-shield | 0 | 0 | 0 | 0 |
| 51 | the-crown-tundra-shield | 0 | 0 | 0 | 0 |
| 52 | the-teal-mask-violet | 0 | 0 | 0 | 0 |
| 53 | the-indigo-disk-violet | 0 | 0 | 0 | 0 |

- 至少有一条 zh-Hans 说明的物种：722/1025
- 至少有一条 zh-Hant 说明的物种：722/1025
- 至少有一条 zh-Hans 或 zh-Hant 说明的物种：722/1025
- 有任意语言说明的物种：1025/1025；完全无说明：0
- "最新有文本的版本"含 zh-Hans 的物种：550/1025
- 没有任何 zh-Hans 说明的版本（45 个）：red, blue, yellow, gold, silver, crystal, ruby, sapphire, emerald, firered, leafgreen, diamond, pearl, platinum, heartgold, soulsilver, black, white, colosseum, xd, black-2, white-2, x, y, omega-ruby, alpha-sapphire, the-isle-of-armor-sword, the-crown-tundra-sword, brilliant-diamond, shining-pearl, legends-arceus, scarlet, violet, the-teal-mask-scarlet, the-indigo-disk-scarlet, red-japan, green-japan, blue-japan, legends-za, mega-dimension, champions, the-isle-of-armor-shield, the-crown-tundra-shield, the-teal-mask-violet, the-indigo-disk-violet

## 招式说明（move_flavor_text），按版本组

| version_group_id | version_group | zh-hans | zh-hant | en | ja |
|---|---|---|---|---|---|
| 28 | red-green-japan | 0 | 0 | 0 | 0 |
| 29 | blue-japan | 0 | 0 | 0 | 0 |
| 1 | red-blue | 0 | 0 | 0 | 0 |
| 2 | yellow | 0 | 0 | 0 | 0 |
| 3 | gold-silver | 0 | 0 | 251 | 0 |
| 4 | crystal | 0 | 0 | 251 | 0 |
| 5 | ruby-sapphire | 0 | 0 | 354 | 0 |
| 6 | emerald | 0 | 0 | 354 | 0 |
| 12 | colosseum | 0 | 0 | 0 | 0 |
| 13 | xd | 0 | 0 | 0 | 0 |
| 7 | firered-leafgreen | 0 | 0 | 354 | 0 |
| 8 | diamond-pearl | 0 | 0 | 467 | 0 |
| 9 | platinum | 0 | 0 | 467 | 0 |
| 10 | heartgold-soulsilver | 0 | 0 | 467 | 0 |
| 11 | black-white | 0 | 0 | 559 | 0 |
| 14 | black-2-white-2 | 0 | 0 | 559 | 0 |
| 15 | x-y | 0 | 0 | 617 | 617 |
| 16 | omega-ruby-alpha-sapphire | 0 | 0 | 621 | 621 |
| 17 | sun-moon | 719 | 719 | 719 | 719 |
| 18 | ultra-sun-ultra-moon | 728 | 728 | 728 | 728 |
| 19 | lets-go-pikachu-lets-go-eevee | 742 | 742 | 742 | 742 |
| 20 | sword-shield | 826 | 826 | 826 | 826 |
| 21 | the-isle-of-armor | 0 | 0 | 0 | 0 |
| 22 | the-crown-tundra | 0 | 0 | 0 | 0 |
| 23 | brilliant-diamond-shining-pearl | 0 | 0 | 0 | 0 |
| 24 | legends-arceus | 0 | 0 | 614 | 0 |
| 25 | scarlet-violet | 0 | 0 | 681 | 0 |
| 26 | the-teal-mask | 0 | 0 | 3 | 0 |
| 27 | the-indigo-disk | 0 | 0 | 1 | 0 |
| 30 | legends-za | 0 | 0 | 0 | 0 |
| 31 | mega-dimension | 0 | 0 | 0 | 0 |
| 32 | champions | 0 | 0 | 0 | 0 |

至少有一条说明的实体数（任一版本组）：zh-hans 826/937，zh-hant 826/937，en 914/937，ja 826/937

## 特性说明（ability_flavor_text），按版本组

| version_group_id | version_group | zh-hans | zh-hant | en | ja |
|---|---|---|---|---|---|
| 28 | red-green-japan | 0 | 0 | 0 | 0 |
| 29 | blue-japan | 0 | 0 | 0 | 0 |
| 1 | red-blue | 0 | 0 | 0 | 0 |
| 2 | yellow | 0 | 0 | 0 | 0 |
| 3 | gold-silver | 0 | 0 | 0 | 0 |
| 4 | crystal | 0 | 0 | 0 | 0 |
| 5 | ruby-sapphire | 0 | 0 | 76 | 0 |
| 6 | emerald | 0 | 0 | 76 | 0 |
| 12 | colosseum | 0 | 0 | 0 | 0 |
| 13 | xd | 0 | 0 | 0 | 0 |
| 7 | firered-leafgreen | 0 | 0 | 76 | 0 |
| 8 | diamond-pearl | 0 | 0 | 123 | 0 |
| 9 | platinum | 0 | 0 | 123 | 0 |
| 10 | heartgold-soulsilver | 0 | 0 | 123 | 0 |
| 11 | black-white | 0 | 0 | 164 | 0 |
| 14 | black-2-white-2 | 0 | 0 | 164 | 0 |
| 15 | x-y | 0 | 0 | 188 | 188 |
| 16 | omega-ruby-alpha-sapphire | 0 | 0 | 191 | 191 |
| 17 | sun-moon | 232 | 232 | 232 | 232 |
| 18 | ultra-sun-ultra-moon | 233 | 233 | 233 | 233 |
| 19 | lets-go-pikachu-lets-go-eevee | 233 | 233 | 233 | 233 |
| 20 | sword-shield | 267 | 267 | 267 | 267 |
| 21 | the-isle-of-armor | 0 | 0 | 0 | 0 |
| 22 | the-crown-tundra | 0 | 0 | 0 | 0 |
| 23 | brilliant-diamond-shining-pearl | 0 | 0 | 0 | 0 |
| 24 | legends-arceus | 0 | 0 | 0 | 0 |
| 25 | scarlet-violet | 0 | 0 | 237 | 31 |
| 26 | the-teal-mask | 0 | 0 | 13 | 5 |
| 27 | the-indigo-disk | 0 | 0 | 16 | 4 |
| 30 | legends-za | 0 | 0 | 0 | 0 |
| 31 | mega-dimension | 0 | 0 | 0 | 0 |
| 32 | champions | 0 | 0 | 7 | 4 |

至少有一条说明的实体数（任一版本组）：zh-hans 267/374，zh-hant 267/374，en 314/374，ja 311/374

## 缺 zh-Hans 名称的实体

### move（18）

10001 shadow-rush, 10002 shadow-blast, 10003 shadow-blitz, 10004 shadow-bolt, 10005 shadow-break, 10006 shadow-chill, 10007 shadow-end, 10008 shadow-fire, 10009 shadow-rave, 10010 shadow-storm, 10011 shadow-wave, 10012 shadow-down, 10013 shadow-half, 10014 shadow-hold, 10015 shadow-mist, 10016 shadow-panic, 10017 shadow-shed, 10018 shadow-sky

### ability（63）

312 eelevate, 313 fire-mane, 314 aura-guard, 10001 mountaineer, 10002 wave-rider, 10003 skater, 10004 thrust, 10005 perception, 10006 parry, 10007 instinct, 10008 dodge, 10009 jagged-edge, 10010 frostbite, 10011 tenacity, 10012 pride, 10013 deep-sleep, 10014 power-nap, 10015 spirit, 10016 warm-blanket, 10017 gulp, 10018 herbivore, 10019 sandpit, 10020 hot-blooded, 10021 medic, 10022 life-force, 10023 lunchbox, 10024 nurse, 10025 melee, 10026 sponge, 10027 bodyguard, 10028 hero, 10029 last-bastion, 10030 stealth, 10031 vanguard, 10032 nomad, 10033 sequence, 10034 grass-cloak, 10035 celebrate, 10036 lullaby, 10037 calming, 10038 daze, 10039 frighten, 10040 interference, 10041 mood-maker, 10042 confidence, 10043 fortune, 10044 bonanza, 10045 explode, 10046 omnipotent, 10047 share, 10048 black-hole, 10049 shadow-dash, 10050 sprint, 10051 disgust, 10052 high-rise, 10053 climber, 10054 flame-boost, 10055 aqua-boost, 10056 run-up, 10057 conqueror, 10058 shackle, 10059 decoy, 10060 shield

### item（51）

114 grass-mail, 115 flame-mail, 116 bubble-mail, 117 bloom-mail, 118 tunnel-mail, 119 steel-mail, 120 heart-mail, 121 snow-mail, 122 space-mail, 123 air-mail, 124 mosaic-mail, 125 brick-mail, 404 hm08, 427 bicycle, 515 orange-mail, 516 harbor-mail, 517 glitter-mail, 518 mech-mail, 519 wood-mail, 520 wave-mail, 521 bead-mail, 522 shadow-mail, 523 tropic-mail, 524 dream-mail, 525 fab-mail, 526 retro-mail, 530 devon-goods, 532 pokeblock-case, 538 rm-1-key, 539 rm-2-key, 540 rm-4-key, 541 rm-6-key, 543 oaks-parcel, 545 bike-voucher, 549 fame-checker, 551 berry-pouch, 552 teachy-tv, 553 tri-pass, 554 rainbow-pass, 556 mysticticket, 557 auroraticket, 558 powder-jar, 559 ruby, 560 sapphire, 561 magma-emblem, 562 old-sea-map, 663 god-stone, 2100 roto-stick, 2118 fresh-start-mochi, 2278 hopo-berry, 2279 roseli-berry
