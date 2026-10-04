package io.github.linxiks.pindex.data.local

/**
 * SQL shared by the Room DAOs and the JDBC-backed JVM tests, so the tests exercise the exact
 * statements Room runs. Parameters use `:name`; literals never contain a colon.
 */
object Queries {
    const val META = "SELECT * FROM meta"

    const val DEFAULT_POKEMON =
        "SELECT p.id AS pokemonId, p.species_id AS speciesId, s.generation_id AS generationId " +
            "FROM pokemon p JOIN pokemon_species s ON s.id = p.species_id " +
            "WHERE p.is_default = 1 ORDER BY p.species_id, p.id"

    const val DEFAULT_POKEMON_TYPES =
        "SELECT pt.pokemon_id AS pokemonId, pt.slot AS slot, pt.type_id AS typeId " +
            "FROM pokemon_type pt JOIN pokemon p ON p.id = pt.pokemon_id " +
            "WHERE p.is_default = 1 ORDER BY pt.pokemon_id, pt.slot"

    const val TYPES = "SELECT id, identifier FROM type ORDER BY id"

    const val GENERATION_IDS = "SELECT id FROM generation ORDER BY id"

    const val NAMES_OF_ENTITY =
        "SELECT entity_id AS entityId, lang, name, genus FROM localized_name WHERE entity = :entity"

    const val NAMES_OF_IDS =
        "SELECT entity_id AS entityId, lang, name, genus FROM localized_name " +
            "WHERE entity = :entity AND entity_id IN (:ids)"

    const val POKEMON_DETAIL =
        "SELECT p.id AS pokemonId, p.species_id AS speciesId, p.height AS height, p.weight AS weight, " +
            "p.base_experience AS baseExperience, p.is_default AS isDefault, " +
            "s.generation_id AS generationId, s.gender_rate AS genderRate, s.capture_rate AS captureRate, " +
            "s.base_happiness AS baseHappiness, s.hatch_counter AS hatchCounter, s.growth_rate_id AS growthRateId, " +
            "s.evolution_chain_id AS evolutionChainId " +
            "FROM pokemon p JOIN pokemon_species s ON s.id = p.species_id WHERE p.id = :pokemonId"

    const val POKEMON_TYPES =
        "SELECT pokemon_id AS pokemonId, slot, type_id AS typeId FROM pokemon_type " +
            "WHERE pokemon_id = :pokemonId ORDER BY slot"

    const val POKEMON_STATS =
        "SELECT stat_id AS statId, base_value AS baseValue FROM pokemon_stat " +
            "WHERE pokemon_id = :pokemonId AND stat_id BETWEEN 1 AND 6 ORDER BY stat_id"

    const val SPECIES_EGG_GROUP_IDS =
        "SELECT egg_group_id FROM species_egg_group WHERE species_id = :speciesId ORDER BY egg_group_id"

    const val SPECIES_FORMS =
        "SELECT p.id AS pokemonId, p.identifier AS identifier, " +
            "(SELECT f.id FROM pokemon_form f WHERE f.pokemon_id = p.id " +
            "ORDER BY f.is_default DESC, f.sort_order LIMIT 1) AS formId " +
            "FROM pokemon p WHERE p.species_id = :speciesId ORDER BY p.is_default DESC, p.id"

    const val GROWTH_RATE_IDENTIFIER = "SELECT identifier FROM growth_rate WHERE id = :id"

    const val TYPE_EFFICACY =
        "SELECT attack_type_id AS attackTypeId, defend_type_id AS defendTypeId, factor FROM type_efficacy"

    const val CHAIN_SPECIES =
        "SELECT s.id AS speciesId, s.evolves_from_species_id AS evolvesFromSpeciesId, p.id AS pokemonId " +
            "FROM pokemon_species s JOIN pokemon p ON p.species_id = s.id AND p.is_default = 1 " +
            "WHERE s.evolution_chain_id = :chainId ORDER BY s.id"

    // Alias is triggerIdentifier, not trigger: TRIGGER is an SQLite keyword.
    const val CHAIN_EVOLUTIONS =
        "SELECT e.id AS id, e.evolved_species_id AS evolvedSpeciesId, e.is_default AS isDefault, " +
            "e.evolved_pokemon_form_id AS evolvedFormId, t.identifier AS triggerIdentifier, " +
            "e.raw_conditions AS rawConditions " +
            "FROM evolution e JOIN evolution_trigger t ON t.id = e.trigger_id " +
            "JOIN pokemon_species s ON s.id = e.evolved_species_id WHERE s.evolution_chain_id = :chainId"

    const val SEARCH_SPECIES_BY_ID =
        "SELECT DISTINCT entity_id AS entityId, display FROM search_index " +
            "WHERE entity = 'species' AND entity_id = :id"

    // Range scan on the primary key; LIKE would scan the whole table.
    const val SEARCH_SPECIES_PREFIX =
        "SELECT entity_id AS entityId, display, priority FROM search_index " +
            "WHERE term >= :lo AND term < :hi AND entity = 'species'"

    // instr instead of LIKE: user-typed % and _ stay literal.
    const val SEARCH_SPECIES_CONTAINS =
        "SELECT entity_id AS entityId, display, priority FROM search_index " +
            "WHERE entity = 'species' AND instr(term, :q) > 0"

    const val POKEMON_ABILITIES =
        "SELECT slot, ability_id AS abilityId, is_hidden AS isHidden FROM pokemon_ability " +
            "WHERE pokemon_id = :pokemonId ORDER BY slot"

    const val ABILITY_IDENTIFIER = "SELECT identifier FROM ability WHERE id = :abilityId"

    const val ABILITY_FLAVOR =
        "SELECT f.ability_id AS entityId, f.lang AS lang, f.text AS text, vg.sort_order AS sortOrder " +
            "FROM ability_flavor_text f JOIN version_group vg ON vg.id = f.version_group_id " +
            "WHERE f.ability_id IN (:ids) AND f.lang IN ('zh-Hans', 'zh-Hant', 'en')"

    const val ABILITY_SPECIES =
        "SELECT DISTINCT p.species_id FROM pokemon_ability pa JOIN pokemon p ON p.id = pa.pokemon_id " +
            "WHERE pa.ability_id = :abilityId ORDER BY p.species_id"

    const val MOVE_DETAIL =
        "SELECT id, type_id AS typeId, damage_class_id AS damageClassId, power, accuracy, pp FROM move WHERE id = :moveId"

    const val MOVE_FLAVOR =
        "SELECT f.move_id AS entityId, f.lang AS lang, f.text AS text, vg.sort_order AS sortOrder " +
            "FROM move_flavor_text f JOIN version_group vg ON vg.id = f.version_group_id " +
            "WHERE f.move_id = :moveId AND f.lang IN ('zh-Hans', 'zh-Hant', 'en')"

    // Uses index_pokemon_move_move_id_version_group_id.
    const val MOVE_SPECIES =
        "SELECT DISTINCT p.species_id FROM pokemon_move pm JOIN pokemon p ON p.id = pm.pokemon_id " +
            "WHERE pm.move_id = :moveId ORDER BY p.species_id"

    const val POKEMON_MOVES =
        "SELECT pm.version_group_id AS versionGroupId, pm.move_id AS moveId, mm.identifier AS methodIdentifier, " +
            "pm.level AS level, pm.sort_order AS sortOrder, m.type_id AS typeId, " +
            "m.damage_class_id AS damageClassId, m.power AS power " +
            "FROM pokemon_move pm JOIN move m ON m.id = pm.move_id JOIN move_method mm ON mm.id = pm.method_id " +
            "WHERE pm.pokemon_id = :pokemonId"

    const val VERSION_GROUP_VERSIONS =
        "SELECT vg.id AS versionGroupId, vg.identifier AS identifier, vg.sort_order AS sortOrder, v.id AS versionId " +
            "FROM version_group vg JOIN version v ON v.version_group_id = vg.id ORDER BY vg.sort_order, v.id"
}
