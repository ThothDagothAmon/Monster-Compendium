/*
 * Copyright (C) 2024 Alexandre Gomes Pereira
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package br.alexandregpereira.hunter.domain.monster.lore

import br.alexandregpereira.hunter.domain.monster.lore.model.MonsterLore
import kotlinx.coroutines.flow.Flow

interface MonsterLoreLocalRepository {

    fun getMonsterLore(index: String): Flow<MonsterLore?>

    fun getLocalMonstersLore(indexes: List<String>): Flow<List<MonsterLore>>

    fun getMonstersLoreEdited(): Flow<List<MonsterLore>>

    fun save(monstersLore: List<MonsterLore>, isSync: Boolean): Flow<Unit>
}
