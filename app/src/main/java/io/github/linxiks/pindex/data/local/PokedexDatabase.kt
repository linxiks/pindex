package io.github.linxiks.pindex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// version tracks tools/data-builder/build.py SCHEMA_VERSION.
@Database(
    entities = [
        MetaEntity::class,
        GenerationEntity::class,
        GrowthRateEntity::class,
        ItemEntity::class,
        EvolutionChainEntity::class,
        PokemonSpeciesEntity::class,
        PokemonEntity::class,
        PokemonFormEntity::class,
        TypeEntity::class,
        PokemonTypeEntity::class,
        StatEntity::class,
        PokemonStatEntity::class,
        EggGroupEntity::class,
        SpeciesEggGroupEntity::class,
        LocalizedNameEntity::class,
        SearchIndexEntity::class,
        VersionGroupEntity::class,
        TypeEfficacyEntity::class,
        MoveDamageClassEntity::class,
        MoveEntity::class,
        EvolutionTriggerEntity::class,
        RegionEntity::class,
        LocationEntity::class,
        EvolutionEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun metaDao(): MetaDao
    abstract fun pokemonDao(): PokemonDao
    abstract fun searchDao(): SearchDao
}
