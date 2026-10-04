package io.github.linxiks.pindex.ui.preview

import io.github.linxiks.pindex.data.model.AbilityDetail
import io.github.linxiks.pindex.data.model.AbilitySlot
import io.github.linxiks.pindex.data.model.Generation
import io.github.linxiks.pindex.data.model.DamageGroup
import io.github.linxiks.pindex.data.model.EvolutionNode
import io.github.linxiks.pindex.data.model.FormLink
import io.github.linxiks.pindex.data.model.LearnedMove
import io.github.linxiks.pindex.data.model.Learnset
import io.github.linxiks.pindex.data.model.MoveDetail
import io.github.linxiks.pindex.data.model.PokemonDetail
import io.github.linxiks.pindex.data.model.PokemonListItem
import io.github.linxiks.pindex.data.model.StatValue
import io.github.linxiks.pindex.data.model.TypeInfo
import io.github.linxiks.pindex.data.model.VersionGroupOption
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.domain.MoveMethodGroup

/** Preview-only data: the five pokemon of design.md §60. */
object SampleData {
    private fun zh(text: String) = LocalizedText(text, "zh-Hans")

    private val grass = TypeInfo(12, "grass", zh("草"))
    private val poison = TypeInfo(4, "poison", zh("毒"))
    private val fire = TypeInfo(10, "fire", zh("火"))
    private val flying = TypeInfo(3, "flying", zh("飞行"))
    private val electric = TypeInfo(13, "electric", zh("电"))
    private val normal = TypeInfo(1, "normal", zh("一般"))
    private val dragon = TypeInfo(16, "dragon", zh("龙"))
    private val ground = TypeInfo(5, "ground", zh("地面"))
    private val steel = TypeInfo(9, "steel", zh("钢"))

    val pokemon: List<PokemonListItem> = listOf(
        PokemonListItem(1, 1, 1, zh("妙蛙种子"), listOf(grass, poison)),
        PokemonListItem(6, 6, 1, zh("喷火龙"), listOf(fire, flying)),
        PokemonListItem(25, 25, 1, zh("皮卡丘"), listOf(electric)),
        PokemonListItem(133, 133, 1, zh("伊布"), listOf(normal)),
        PokemonListItem(445, 445, 4, zh("烈咬陆鲨"), listOf(dragon, ground)),
    )

    private val generationNames = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九")

    val generations: List<Generation> = generationNames.mapIndexed { index, numeral ->
        Generation(index + 1, zh("第${numeral}世代"))
    }

    val pikachu = PokemonDetail(
        pokemonId = 25,
        speciesId = 25,
        isDefault = true,
        name = zh("皮卡丘"),
        enName = "Pikachu",
        jaName = "ピカチュウ",
        formName = null,
        genus = zh("鼠宝可梦"),
        types = listOf(electric),
        height = 4,
        weight = 60,
        genderRate = 4,
        captureRate = 190,
        baseHappiness = 70,
        hatchCounter = 10,
        baseExperience = 112,
        eggGroups = listOf(zh("陆上"), zh("妖精")),
        growthRateIdentifier = "medium",
        growthRateEnName = "medium",
        generation = zh("第一世代"),
        stats = listOf(
            StatValue(1, zh("HP"), 35),
            StatValue(2, zh("攻击"), 55),
            StatValue(3, zh("防御"), 40),
            StatValue(4, zh("特攻"), 50),
            StatValue(5, zh("特防"), 50),
            StatValue(6, zh("速度"), 90),
        ),
        damageTaken = listOf(
            DamageGroup(400, emptyList()),
            DamageGroup(200, listOf(ground)),
            DamageGroup(50, listOf(flying, steel, electric)),
            DamageGroup(25, emptyList()),
            DamageGroup(0, emptyList()),
        ),
        evolution = listOf(
            EvolutionNode(
                172, 172, zh("皮丘"), null,
                listOf(
                    EvolutionNode(
                        25, 25, zh("皮卡丘"), "升级，亲密度 ≥ 220",
                        listOf(EvolutionNode(26, 26, zh("雷丘"), "使用雷之石", emptyList())),
                    ),
                ),
            ),
        ),
        abilities = listOf(
            AbilitySlot(9, zh("静电"), zh("身上带有静电，有时会让接触到的对手麻痹。"), false),
            AbilitySlot(31, zh("避雷针"), zh("将电属性的招式吸引到自己身上，不会受到伤害，而是会提高特攻。"), true),
        ),
        learnset = Learnset(
            versionGroups = listOf(VersionGroupOption(25, "scarlet-violet", listOf(zh("朱"), zh("紫")))),
            defaultVersionGroupId = 25,
            moves = listOf(
                LearnedMove(609, 25, MoveMethodGroup.LevelUp, 1, zh("蹭蹭脸颊"), electric, zh("物理"), 20),
                LearnedMove(86, 25, MoveMethodGroup.LevelUp, 4, zh("电磁波"), electric, zh("变化"), null),
                LearnedMove(85, 25, MoveMethodGroup.LevelUp, 36, zh("十万伏特"), electric, zh("特殊"), 90),
            ),
        ),
        otherForms = listOf(
            FormLink(10080, LocalizedText("Pikachu Rock Star", "en"), "pikachu-rock-star"),
            FormLink(10094, LocalizedText("Original Cap", "en"), "pikachu-original-cap"),
        ),
    )

    val intimidate = AbilityDetail(
        abilityId = 22,
        name = zh("威吓"),
        enName = "Intimidate",
        effect = zh("出场时威吓对手，让其退缩，降低对手的攻击。"),
        holders = pokemon.take(3),
    )

    val thunderbolt = MoveDetail(
        moveId = 85,
        name = zh("十万伏特"),
        enName = "Thunderbolt",
        type = electric,
        damageClass = zh("特殊"),
        power = 90,
        accuracy = 100,
        pp = 15,
        description = zh("向对手发出强力电击进行攻击。有时会让对手陷入麻痹状态。"),
        learners = pokemon.filter { it.speciesId == 25 },
    )
}
