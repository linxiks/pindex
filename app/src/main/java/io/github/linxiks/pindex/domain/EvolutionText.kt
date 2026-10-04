package io.github.linxiks.pindex.domain

/** raw_conditions key → localized_name entity whose name the text needs. */
val EVOLUTION_NAME_REFS: Map<String, String> = mapOf(
    "trigger_item_id" to "item", "held_item_id" to "item",
    "known_move_id" to "move", "used_move_id" to "move",
    "known_move_type_id" to "type", "party_type_id" to "type",
    "party_species_id" to "species", "trade_species_id" to "species",
    "location_id" to "location", "region_id" to "region",
    "required_pokemon_form_id" to "pokemon_form",
)

private val RAW_PAIR = Regex(""""([^"\\]*)":"((?:[^"\\]|\\.)*)"""")

/** Parses the builder's flat JSON object of string values; only \" and \\ are unescaped. */
fun parseRawConditions(json: String): Map<String, String> {
    val trimmed = json.trim()
    if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return emptyMap()
    return RAW_PAIR.findAll(trimmed).associate { m ->
        m.groupValues[1] to m.groupValues[2].replace("\\\"", "\"").replace("\\\\", "\\")
    }
}

/** Chinese text for one evolution row; [name] returns the display name or null when none exists. */
fun describeEvolution(trigger: String, c: Map<String, String>, name: (entity: String, id: Int) -> String?): String {
    fun ref(key: String, entity: String) = c[key]?.toIntOrNull()?.let { name(entity, it) ?: "#$it" }
    fun flag(key: String) = c[key].let { it != null && it != "0" }

    val level = c["minimum_level"]
    val heldItem = ref("held_item_id", "item")
    var heldItemUsed = false
    val base = when (trigger) {
        "level-up" -> if (level != null) "Lv.$level" else "升级"
        "trade" -> when {
            heldItem != null -> "携带${heldItem}通信交换".also { heldItemUsed = true }
            c.containsKey("trade_species_id") -> "与${ref("trade_species_id", "species")}通信交换"
            else -> "通信交换"
        }
        "use-item" -> "使用${ref("trigger_item_id", "item")}"
        "shed" -> "队伍有空位且包中有精灵球"
        "spin" -> "原地旋转"
        "tower-of-darkness" -> "挑战恶之塔"
        "tower-of-waters" -> "挑战水之塔"
        "three-critical-hits" -> "一场战斗中击中要害 3 次"
        "take-damage" -> "受到 ≥ ${c["minimum_damage_taken"]} 点伤害"
        "in-battle-level-up" -> if (level != null) "战斗中升到 Lv.$level" else "战斗中升级"
        "agile-style-move" -> "以迅疾使用${ref("used_move_id", "move")} ${c["minimum_move_count"]} 次"
        "strong-style-move" -> "以刚猛使用${ref("used_move_id", "move")} ${c["minimum_move_count"]} 次"
        "use-move" -> "使用${ref("used_move_id", "move")} ${c["minimum_move_count"]} 次"
        "recoil-damage" -> "累计受到 ≥ ${c["minimum_damage_taken"]} 点反作用力伤害"
        "three-defeated-bisharp" -> "击败 3 只携带首领之证的劈斩司令"
        "gimmighoul-coins" -> "收集 999 枚索财灵的硬币"
        "meltan-candies" -> "在 Pokémon GO 中使用 400 颗美录坦糖果"
        else -> "特殊条件"
    }

    val parts = mutableListOf(base)
    when (c["gender_id"]) {
        "1" -> parts += "限♀"
        "2" -> parts += "限♂"
    }
    if (heldItem != null && !heldItemUsed) parts += "携带$heldItem"
    ref("known_move_id", "move")?.let { parts += "学会$it" }
    ref("known_move_type_id", "type")?.let { parts += "学会${it}属性招式" }
    c["minimum_happiness"]?.let { parts += "亲密度 ≥ $it" }
    c["minimum_affection"]?.let { parts += "友好度 ≥ $it" }
    c["minimum_beauty"]?.let { parts += "美丽度 ≥ $it" }
    c["time_of_day"]?.let {
        parts += when (it) {
            "day" -> "白天"
            "night" -> "夜晚"
            "dusk" -> "黄昏"
            "full-moon" -> "满月"
            else -> it
        }
    }
    ref("location_id", "location")?.let { parts += "在$it" }
    ref("region_id", "region")?.let { parts += "在${it}地区" }
    when (c["relative_physical_stats"]) {
        "1" -> parts += "攻击 > 防御"
        "-1" -> parts += "攻击 < 防御"
        "0" -> parts += "攻击 = 防御"
    }
    ref("party_species_id", "species")?.let { parts += "队伍中有$it" }
    ref("party_type_id", "type")?.let { parts += "队伍中有${it}属性宝可梦" }
    if (flag("needs_overworld_rain")) parts += "野外下雨"
    if (flag("turn_upside_down")) parts += "将游戏机倒置"
    if (flag("needs_multiplayer")) parts += "在联盟圈中"
    c["minimum_steps"]?.let { parts += "同行 $it 步" }
    // Default forms usually have no name; the bare form id would mean nothing to the reader.
    c["required_pokemon_form_id"]?.toIntOrNull()?.let { id -> name("pokemon_form", id)?.let { parts += "限$it" } }
    c["percentage_chance"]?.let { parts += "概率 $it%" }
    if (c.containsKey("nature_bitmask")) parts += "取决于性格"
    return parts.joinToString("，")
}
