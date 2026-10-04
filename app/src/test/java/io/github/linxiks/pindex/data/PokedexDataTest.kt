package io.github.linxiks.pindex.data

import io.github.linxiks.pindex.data.model.EvolutionNode
import io.github.linxiks.pindex.data.repository.MetaRepository
import io.github.linxiks.pindex.data.repository.PokemonRepository
import io.github.linxiks.pindex.domain.LocalizedText
import io.github.linxiks.pindex.domain.MoveMethodGroup
import io.github.linxiks.pindex.domain.filterByGeneration
import io.github.linxiks.pindex.testutil.JdbcMetaDao
import io.github.linxiks.pindex.testutil.JdbcPokemonDao
import io.github.linxiks.pindex.testutil.PokedexJdbc
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Runs the real repositories over the Queries constants against data/generated/pokedex.db. */
class PokedexDataTest {
    private val pokemon = PokemonRepository(JdbcPokemonDao())

    @Test
    fun metaAndUserVersionMatchSchemaVersion() = runTest {
        val meta = JdbcMetaDao().all().associate { it.key to it.value }
        assertEquals("3", meta["schema_version"])
        assertEquals("1", meta["data_version"])
        assertEquals(3, PokedexJdbc.userVersion())
        val version = MetaRepository(JdbcMetaDao()).dataVersion()
        assertEquals("1", version.dataVersion)
        assertTrue(version.buildDate.isNotBlank())
    }

    @Test
    fun listHasEveryDefaultPokemonInDexOrder() = runTest {
        val list = pokemon.list()
        assertEquals((1..1025).toList(), list.map { it.speciesId })
        assertTrue(list.all { it.types.isNotEmpty() })
        assertTrue(list.none { it.name.lang == "und" })
    }

    @Test
    fun generationOneFilterKeeps151() = runTest {
        val gen1 = filterByGeneration(pokemon.list(), 1) { it.generationId }
        assertEquals(151, gen1.size)
        assertEquals(151, gen1.last().speciesId)
    }

    @Test
    fun generationsAreNineWithChineseNames() = runTest {
        val generations = pokemon.generations()
        assertEquals((1..9).toList(), generations.map { it.id })
        assertEquals(LocalizedText("第一世代", "zh-Hans"), generations.first().name)
    }

    @Test
    fun pikachuDetail() = runTest {
        val d = checkNotNull(pokemon.detail(25))
        assertEquals(LocalizedText("皮卡丘", "zh-Hans"), d.name)
        assertEquals("Pikachu", d.enName)
        assertEquals("ピカチュウ", d.jaName)
        assertNull(d.formName)
        assertEquals(listOf("electric"), d.types.map { it.identifier })
        assertEquals(listOf(35, 55, 40, 50, 50, 90), d.stats.map { it.value })
        assertEquals(320, d.stats.sumOf { it.value })
        assertTrue(d.stats.all { it.label.lang == "zh-Hans" })
        assertEquals(LocalizedText("鼠宝可梦", "zh-Hans"), d.genus)
        assertEquals(listOf("陆上", "妖精"), d.eggGroups.map { it.text })
        assertEquals("medium", d.growthRateIdentifier)
        assertEquals("第一世代", d.generation?.text)
        assertEquals(4, d.height)
        assertEquals(60, d.weight)
        assertEquals(4, d.genderRate)
        assertEquals(190, d.captureRate)
        assertEquals(112, d.baseExperience)
        assertTrue(d.otherForms.isNotEmpty())
        assertFalse(d.otherForms.any { it.pokemonId == 25 })
    }

    @Test
    fun nonDefaultFormLinksBackToDefault() = runTest {
        val pikachu = checkNotNull(pokemon.detail(25))
        val other = checkNotNull(pokemon.detail(pikachu.otherForms.first().pokemonId))
        assertFalse(other.isDefault)
        assertEquals(25, other.speciesId)
        assertTrue(other.formName != null)
        assertTrue(other.otherForms.any { it.pokemonId == 25 })
    }

    @Test
    fun genusFallsBackToEnglish() = runTest {
        val d = checkNotNull(pokemon.detail(1011))
        assertEquals(LocalizedText("Candy Apple Pokémon", "en"), d.genus)
    }

    @Test
    fun unknownPokemonIsNull() = runTest {
        assertNull(pokemon.detail(99999))
    }

    @Test
    fun garchompDamageTaken() = runTest {
        val groups = checkNotNull(pokemon.detail(445)).damageTaken
            .associate { g -> g.percent to g.types.map { it.identifier } }
        assertEquals(
            mapOf(
                400 to listOf("ice"),
                200 to listOf("dragon", "fairy"),
                50 to listOf("poison", "rock", "fire"),
                25 to emptyList(),
                0 to listOf("electric"),
            ),
            groups,
        )
        assertEquals(listOf(400, 200, 50, 25, 0), checkNotNull(pokemon.detail(445)).damageTaken.map { it.percent })
    }

    private fun EvolutionNode.flatten(): List<EvolutionNode> = listOf(this) + children.flatMap { it.flatten() }

    private suspend fun conditionOf(speciesId: Int): String? =
        checkNotNull(pokemon.detail(speciesId)).evolution.flatMap { it.flatten() }
            .single { it.speciesId == speciesId }.condition

    @Test
    fun garchompLinearChain() = runTest {
        val roots = checkNotNull(pokemon.detail(445)).evolution
        val gible = roots.single()
        assertEquals(443, gible.speciesId)
        val gabite = gible.children.single()
        assertEquals(444 to "Lv.24", gabite.speciesId to gabite.condition)
        val garchomp = gabite.children.single()
        assertEquals(445 to "Lv.48", garchomp.speciesId to garchomp.condition)
        assertTrue(garchomp.children.isEmpty())
    }

    @Test
    fun eeveeBranches() = runTest {
        val eevee = checkNotNull(pokemon.detail(133)).evolution.single()
        assertEquals(listOf(134, 135, 136, 196, 197, 470, 471, 700), eevee.children.map { it.speciesId })
        val byId = eevee.children.associate { it.speciesId to it.condition }
        assertEquals("使用水之石", byId[134])
        assertEquals("使用叶之石", byId[470])
        assertEquals("升级，亲密度 ≥ 160，白天", byId[196])
        assertEquals("升级，学会妖精属性招式，亲密度 ≥ 160", byId[700])
    }

    @Test
    fun pikachuChainUsesFriendshipAndStone() = runTest {
        val pichu = checkNotNull(pokemon.detail(25)).evolution.single()
        assertEquals(172, pichu.speciesId)
        val pika = pichu.children.single()
        assertEquals(25 to "升级，亲密度 ≥ 220", pika.speciesId to pika.condition)
        val raichu = pika.children.single()
        assertEquals(26 to "使用雷之石", raichu.speciesId to raichu.condition)
    }

    @Test
    fun tradeAndSpecialConditions() = runTest {
        assertEquals("携带王者之证通信交换", conditionOf(186))
        assertEquals("通信交换", conditionOf(65))
        assertEquals("Lv.30，将游戏机倒置", conditionOf(687))
    }

    @Test
    fun nonEvolvingAndTwoRootChains() = runTest {
        val ditto = checkNotNull(pokemon.detail(132)).evolution
        assertEquals(listOf(132), ditto.map { it.speciesId })
        assertTrue(ditto.single().children.isEmpty())
        assertEquals(listOf(489, 490), checkNotNull(pokemon.detail(490)).evolution.map { it.speciesId })
    }

    @Test
    fun everyEvolutionHasReadableCondition() = runTest {
        val chainIds = PokedexJdbc.query("SELECT id FROM evolution_chain") { it.getInt(1) }
        val evolved = chainIds.flatMap { id ->
            pokemon.evolution(id).flatMap { root -> root.flatten().filter { it !== root } }
        }
        assertEquals(484, evolved.size)
        val bad = evolved.filter { val c = it.condition; c == null || '#' in c || c == "特殊条件" }
        assertEquals(emptyList<EvolutionNode>(), bad)
    }

    @Test
    fun pikachuAbilities() = runTest {
        val abilities = checkNotNull(pokemon.detail(25)).abilities
        assertEquals(
            listOf(Triple(9, "静电", false), Triple(31, "避雷针", true)),
            abilities.map { Triple(it.abilityId, it.name.text, it.isHidden) },
        )
        assertEquals(LocalizedText("身上带有静电，有时会让接触到的对手麻痹。", "zh-Hans"), abilities.first().effect)
    }

    @Test
    fun garchompHiddenAbility() = runTest {
        val abilities = checkNotNull(pokemon.detail(445)).abilities
        assertEquals(listOf(8 to false, 24 to true), abilities.map { it.abilityId to it.isHidden })
    }

    @Test
    fun intimidateDetail() = runTest {
        val a = checkNotNull(pokemon.ability(22))
        assertEquals(LocalizedText("威吓", "zh-Hans"), a.name)
        assertEquals("Intimidate", a.enName)
        assertEquals("出场时威吓对手，让其退缩，降低对手的攻击。", a.effect?.text)
        val species = a.holders.map { it.speciesId }
        assertEquals(37, species.size)
        assertEquals(species.sorted(), species)
        assertTrue(130 in species && 310 in species)
    }

    @Test
    fun abilityEdgeCases() = runTest {
        val eelevate = checkNotNull(pokemon.ability(312))
        assertEquals("en", eelevate.name.lang)
        assertNull(eelevate.enName)
        assertEquals("en", eelevate.effect?.lang)
        assertTrue(checkNotNull(pokemon.ability(303)).holders.isEmpty())
        assertNull(pokemon.ability(99999))
    }

    @Test
    fun pikachuLearnsetScarletViolet() = runTest {
        val learnset = checkNotNull(pokemon.detail(25)).learnset
        assertEquals(32, learnset.versionGroups.first().id)
        assertEquals(25, learnset.defaultVersionGroupId)
        assertEquals(listOf("朱", "紫"), learnset.versionGroups.single { it.id == 25 }.versions.map { it.text })
        val sv = learnset.moves.filter { it.versionGroupId == 25 }
        val levelUp = sv.filter { it.group == MoveMethodGroup.LevelUp }
        assertEquals(
            listOf(609, 417, 204, 186, 589, 98, 39, 84, 45, 86, 104, 486, 364, 209, 97, 231, 435, 85, 113, 87),
            levelUp.map { it.moveId },
        )
        assertEquals(List(9) { 1 } + listOf(4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44), levelUp.map { it.level })
        assertEquals(47, sv.count { it.group == MoveMethodGroup.Machine })
    }

    @Test
    fun gmaxHasNoLearnset() = runTest {
        val learnset = checkNotNull(pokemon.detail(10195)).learnset
        assertTrue(learnset.moves.isEmpty())
        assertNull(learnset.defaultVersionGroupId)
    }

    @Test
    fun thunderboltDetail() = runTest {
        val m = checkNotNull(pokemon.move(85))
        assertEquals(LocalizedText("十万伏特", "zh-Hans"), m.name)
        assertEquals("Thunderbolt", m.enName)
        assertEquals("electric", m.type.identifier)
        assertEquals("特殊", m.damageClass.text)
        assertEquals(listOf(90, 100, 15), listOf(m.power, m.accuracy, m.pp))
        assertEquals(LocalizedText("向对手发出强力电击进行攻击。有时会让对手陷入麻痹状态。", "zh-Hans"), m.description)
        assertEquals(260, m.learners.size)
        assertTrue(m.learners.any { it.speciesId == 25 })
        assertTrue(checkNotNull(pokemon.move(165)).learners.isEmpty())
        assertNull(pokemon.move(99999))
    }
}
