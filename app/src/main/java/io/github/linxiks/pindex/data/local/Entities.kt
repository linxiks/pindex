package io.github.linxiks.pindex.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Every entity mirrors tools/data-builder/schema.sql column by column: Room validates the
// pre-packaged database against these declarations on open. Tables not listed are not checked.

@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String,
)

@Entity(tableName = "generation")
data class GenerationEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "identifier") val identifier: String,
)

@Entity(tableName = "growth_rate")
data class GrowthRateEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "identifier") val identifier: String,
)

@Entity(tableName = "item")
data class ItemEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "identifier") val identifier: String,
    @ColumnInfo(name = "category_identifier") val categoryIdentifier: String,
)

@Entity(
    tableName = "evolution_chain",
    foreignKeys = [
        ForeignKey(entity = ItemEntity::class, parentColumns = ["id"], childColumns = ["baby_trigger_item_id"]),
    ],
)
data class EvolutionChainEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "baby_trigger_item_id") val babyTriggerItemId: Int?,
)

@Entity(
    tableName = "pokemon_species",
    foreignKeys = [
        ForeignKey(entity = GenerationEntity::class, parentColumns = ["id"], childColumns = ["generation_id"]),
        ForeignKey(entity = EvolutionChainEntity::class, parentColumns = ["id"], childColumns = ["evolution_chain_id"]),
        ForeignKey(entity = PokemonSpeciesEntity::class, parentColumns = ["id"], childColumns = ["evolves_from_species_id"]),
        ForeignKey(entity = GrowthRateEntity::class, parentColumns = ["id"], childColumns = ["growth_rate_id"]),
    ],
    indices = [Index("generation_id")],
)
data class PokemonSpeciesEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "identifier") val identifier: String,
    @ColumnInfo(name = "generation_id") val generationId: Int,
    @ColumnInfo(name = "evolution_chain_id") val evolutionChainId: Int,
    @ColumnInfo(name = "evolves_from_species_id") val evolvesFromSpeciesId: Int?,
    @ColumnInfo(name = "gender_rate") val genderRate: Int,
    @ColumnInfo(name = "capture_rate") val captureRate: Int,
    @ColumnInfo(name = "base_happiness") val baseHappiness: Int,
    @ColumnInfo(name = "hatch_counter") val hatchCounter: Int,
    @ColumnInfo(name = "growth_rate_id") val growthRateId: Int,
    @ColumnInfo(name = "is_baby") val isBaby: Boolean,
    @ColumnInfo(name = "is_legendary") val isLegendary: Boolean,
    @ColumnInfo(name = "is_mythical") val isMythical: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

@Entity(
    tableName = "pokemon",
    foreignKeys = [
        ForeignKey(entity = PokemonSpeciesEntity::class, parentColumns = ["id"], childColumns = ["species_id"]),
    ],
    indices = [Index("species_id")],
)
data class PokemonEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "species_id") val speciesId: Int,
    @ColumnInfo(name = "identifier") val identifier: String,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "weight") val weight: Int,
    @ColumnInfo(name = "base_experience") val baseExperience: Int?,
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int?,
)

@Entity(
    tableName = "pokemon_form",
    foreignKeys = [
        ForeignKey(entity = PokemonEntity::class, parentColumns = ["id"], childColumns = ["pokemon_id"]),
    ],
    indices = [Index("pokemon_id")],
)
data class PokemonFormEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "form_identifier") val formIdentifier: String?,
    @ColumnInfo(name = "is_default") val isDefault: Boolean,
    @ColumnInfo(name = "is_battle_only") val isBattleOnly: Boolean,
    @ColumnInfo(name = "is_mega") val isMega: Boolean,
    @ColumnInfo(name = "introduced_in_version_group_id") val introducedInVersionGroupId: Int,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

@Entity(tableName = "type")
data class TypeEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "identifier") val identifier: String,
    @ColumnInfo(name = "generation_id") val generationId: Int,
)

@Entity(
    tableName = "pokemon_type",
    primaryKeys = ["pokemon_id", "slot"],
    foreignKeys = [
        ForeignKey(entity = PokemonEntity::class, parentColumns = ["id"], childColumns = ["pokemon_id"]),
        ForeignKey(entity = TypeEntity::class, parentColumns = ["id"], childColumns = ["type_id"]),
    ],
    indices = [Index("type_id")],
)
data class PokemonTypeEntity(
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "slot") val slot: Int,
    @ColumnInfo(name = "type_id") val typeId: Int,
)

@Entity(tableName = "stat")
data class StatEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "identifier") val identifier: String,
)

@Entity(
    tableName = "pokemon_stat",
    primaryKeys = ["pokemon_id", "stat_id"],
    foreignKeys = [
        ForeignKey(entity = PokemonEntity::class, parentColumns = ["id"], childColumns = ["pokemon_id"]),
        ForeignKey(entity = StatEntity::class, parentColumns = ["id"], childColumns = ["stat_id"]),
    ],
)
data class PokemonStatEntity(
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo(name = "stat_id") val statId: Int,
    @ColumnInfo(name = "base_value") val baseValue: Int,
)

@Entity(tableName = "egg_group")
data class EggGroupEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "identifier") val identifier: String,
)

@Entity(
    tableName = "species_egg_group",
    primaryKeys = ["species_id", "egg_group_id"],
    foreignKeys = [
        ForeignKey(entity = PokemonSpeciesEntity::class, parentColumns = ["id"], childColumns = ["species_id"]),
        ForeignKey(entity = EggGroupEntity::class, parentColumns = ["id"], childColumns = ["egg_group_id"]),
    ],
)
data class SpeciesEggGroupEntity(
    @ColumnInfo(name = "species_id") val speciesId: Int,
    @ColumnInfo(name = "egg_group_id") val eggGroupId: Int,
)

@Entity(tableName = "localized_name", primaryKeys = ["entity", "entity_id", "lang"])
data class LocalizedNameEntity(
    @ColumnInfo(name = "entity") val entity: String,
    @ColumnInfo(name = "entity_id") val entityId: Int,
    @ColumnInfo(name = "lang") val lang: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "genus") val genus: String?,
    @ColumnInfo(name = "source") val source: String,
)

@Entity(tableName = "search_index", primaryKeys = ["term", "entity", "entity_id"])
data class SearchIndexEntity(
    @ColumnInfo(name = "term") val term: String,
    @ColumnInfo(name = "entity") val entity: String,
    @ColumnInfo(name = "entity_id") val entityId: Int,
    @ColumnInfo(name = "display") val display: String,
    @ColumnInfo(name = "priority") val priority: Int,
)
