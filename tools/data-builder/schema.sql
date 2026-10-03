-- pokedex.db schema. Authoritative description: docs/development-plan.md section 3.
-- Column types are INTEGER or TEXT only. Index names follow Room: index_<table>_<cols>.

CREATE TABLE meta (
    key TEXT NOT NULL PRIMARY KEY,
    value TEXT NOT NULL
);

CREATE TABLE generation (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL
);

CREATE TABLE version_group (
    id INTEGER NOT NULL PRIMARY KEY,
    generation_id INTEGER NOT NULL REFERENCES generation(id),
    identifier TEXT NOT NULL,
    sort_order INTEGER NOT NULL
);

CREATE TABLE version (
    id INTEGER NOT NULL PRIMARY KEY,
    version_group_id INTEGER NOT NULL REFERENCES version_group(id),
    identifier TEXT NOT NULL
);

CREATE TABLE type (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL,
    generation_id INTEGER NOT NULL
);

CREATE TABLE type_efficacy (
    attack_type_id INTEGER NOT NULL REFERENCES type(id),
    defend_type_id INTEGER NOT NULL REFERENCES type(id),
    factor INTEGER NOT NULL,
    PRIMARY KEY (attack_type_id, defend_type_id)
);

CREATE TABLE growth_rate (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL
);

CREATE TABLE evolution_chain (
    id INTEGER NOT NULL PRIMARY KEY,
    baby_trigger_item_id INTEGER REFERENCES item(id)
);

CREATE TABLE pokemon_species (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL,
    generation_id INTEGER NOT NULL REFERENCES generation(id),
    evolution_chain_id INTEGER NOT NULL REFERENCES evolution_chain(id),
    evolves_from_species_id INTEGER REFERENCES pokemon_species(id),
    gender_rate INTEGER NOT NULL,
    capture_rate INTEGER NOT NULL,
    base_happiness INTEGER NOT NULL,
    hatch_counter INTEGER NOT NULL,
    growth_rate_id INTEGER NOT NULL REFERENCES growth_rate(id),
    is_baby INTEGER NOT NULL,
    is_legendary INTEGER NOT NULL,
    is_mythical INTEGER NOT NULL,
    sort_order INTEGER NOT NULL
);
CREATE INDEX index_pokemon_species_generation_id ON pokemon_species(generation_id);

CREATE TABLE pokemon (
    id INTEGER NOT NULL PRIMARY KEY,
    species_id INTEGER NOT NULL REFERENCES pokemon_species(id),
    identifier TEXT NOT NULL,
    height INTEGER NOT NULL,
    weight INTEGER NOT NULL,
    base_experience INTEGER,
    is_default INTEGER NOT NULL,
    sort_order INTEGER
);
CREATE INDEX index_pokemon_species_id ON pokemon(species_id);

CREATE TABLE pokemon_form (
    id INTEGER NOT NULL PRIMARY KEY,
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    form_identifier TEXT,
    is_default INTEGER NOT NULL,
    is_battle_only INTEGER NOT NULL,
    is_mega INTEGER NOT NULL,
    introduced_in_version_group_id INTEGER NOT NULL,
    sort_order INTEGER NOT NULL
);
CREATE INDEX index_pokemon_form_pokemon_id ON pokemon_form(pokemon_id);

CREATE TABLE pokemon_type (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    slot INTEGER NOT NULL,
    type_id INTEGER NOT NULL REFERENCES type(id),
    PRIMARY KEY (pokemon_id, slot)
);
CREATE INDEX index_pokemon_type_type_id ON pokemon_type(type_id);

CREATE TABLE stat (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL
);

CREATE TABLE pokemon_stat (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    stat_id INTEGER NOT NULL REFERENCES stat(id),
    base_value INTEGER NOT NULL,
    PRIMARY KEY (pokemon_id, stat_id)
);

CREATE TABLE ability (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL,
    generation_id INTEGER NOT NULL REFERENCES generation(id)
);

CREATE TABLE pokemon_ability (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    slot INTEGER NOT NULL,
    ability_id INTEGER NOT NULL REFERENCES ability(id),
    is_hidden INTEGER NOT NULL,
    PRIMARY KEY (pokemon_id, slot)
);
CREATE INDEX index_pokemon_ability_ability_id ON pokemon_ability(ability_id);

CREATE TABLE move_damage_class (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL
);

CREATE TABLE move (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL,
    type_id INTEGER NOT NULL REFERENCES type(id),
    damage_class_id INTEGER NOT NULL REFERENCES move_damage_class(id),
    power INTEGER,
    accuracy INTEGER,
    pp INTEGER NOT NULL,
    priority INTEGER NOT NULL,
    generation_id INTEGER NOT NULL REFERENCES generation(id)
);

CREATE TABLE move_method (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL
);

CREATE TABLE pokemon_move (
    pokemon_id INTEGER NOT NULL REFERENCES pokemon(id),
    version_group_id INTEGER NOT NULL REFERENCES version_group(id),
    move_id INTEGER NOT NULL REFERENCES move(id),
    method_id INTEGER NOT NULL REFERENCES move_method(id),
    level INTEGER NOT NULL,
    sort_order INTEGER,
    mastery INTEGER,
    PRIMARY KEY (pokemon_id, version_group_id, move_id, method_id, level)
) WITHOUT ROWID;
CREATE INDEX index_pokemon_move_move_id_version_group_id ON pokemon_move(move_id, version_group_id);

CREATE TABLE evolution_trigger (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL
);

CREATE TABLE region (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL
);

CREATE TABLE location (
    id INTEGER NOT NULL PRIMARY KEY,
    region_id INTEGER REFERENCES region(id),
    identifier TEXT NOT NULL
);

CREATE TABLE item (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL,
    category_identifier TEXT NOT NULL
);

CREATE TABLE evolution (
    id INTEGER NOT NULL PRIMARY KEY,
    evolved_species_id INTEGER NOT NULL REFERENCES pokemon_species(id),
    evolved_pokemon_form_id INTEGER REFERENCES pokemon_form(id),
    version_group_id INTEGER NOT NULL REFERENCES version_group(id),
    is_default INTEGER NOT NULL,
    trigger_id INTEGER NOT NULL REFERENCES evolution_trigger(id),
    min_level INTEGER,
    trigger_item_id INTEGER REFERENCES item(id),
    held_item_id INTEGER REFERENCES item(id),
    known_move_id INTEGER REFERENCES move(id),
    known_move_type_id INTEGER REFERENCES type(id),
    gender_id INTEGER,
    time_of_day TEXT,
    min_happiness INTEGER,
    min_affection INTEGER,
    min_beauty INTEGER,
    location_id INTEGER REFERENCES location(id),
    region_id INTEGER REFERENCES region(id),
    trade_species_id INTEGER REFERENCES pokemon_species(id),
    raw_conditions TEXT NOT NULL
);
CREATE INDEX index_evolution_evolved_species_id ON evolution(evolved_species_id);

CREATE TABLE nature (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL,
    increased_stat_id INTEGER NOT NULL REFERENCES stat(id),
    decreased_stat_id INTEGER NOT NULL REFERENCES stat(id)
);

CREATE TABLE egg_group (
    id INTEGER NOT NULL PRIMARY KEY,
    identifier TEXT NOT NULL
);

CREATE TABLE species_egg_group (
    species_id INTEGER NOT NULL REFERENCES pokemon_species(id),
    egg_group_id INTEGER NOT NULL REFERENCES egg_group(id),
    PRIMARY KEY (species_id, egg_group_id)
);

-- Polymorphic reference (entity, entity_id): checked by builder/checks.py, no FK.
CREATE TABLE localized_name (
    entity TEXT NOT NULL,
    entity_id INTEGER NOT NULL,
    lang TEXT NOT NULL,
    name TEXT NOT NULL,
    genus TEXT,
    source TEXT NOT NULL,
    PRIMARY KEY (entity, entity_id, lang)
) WITHOUT ROWID;

CREATE TABLE species_flavor_text (
    species_id INTEGER NOT NULL REFERENCES pokemon_species(id),
    version_id INTEGER NOT NULL REFERENCES version(id),
    lang TEXT NOT NULL,
    text TEXT NOT NULL,
    source TEXT NOT NULL,
    PRIMARY KEY (species_id, version_id, lang)
);

CREATE TABLE move_flavor_text (
    move_id INTEGER NOT NULL REFERENCES move(id),
    version_group_id INTEGER NOT NULL REFERENCES version_group(id),
    lang TEXT NOT NULL,
    text TEXT NOT NULL,
    source TEXT NOT NULL,
    PRIMARY KEY (move_id, version_group_id, lang)
);

CREATE TABLE ability_flavor_text (
    ability_id INTEGER NOT NULL REFERENCES ability(id),
    version_group_id INTEGER NOT NULL REFERENCES version_group(id),
    lang TEXT NOT NULL,
    text TEXT NOT NULL,
    source TEXT NOT NULL,
    PRIMARY KEY (ability_id, version_group_id, lang)
);

-- Polymorphic reference (entity, entity_id): checked by builder/checks.py, no FK.
CREATE TABLE search_index (
    term TEXT NOT NULL,
    entity TEXT NOT NULL,
    entity_id INTEGER NOT NULL,
    display TEXT NOT NULL,
    priority INTEGER NOT NULL,
    PRIMARY KEY (term, entity, entity_id)
) WITHOUT ROWID;
