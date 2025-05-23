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

package br.alexandregpereira.hunter.detail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import br.alexandregpereira.hunter.domain.model.MonsterImageContentScale
import br.alexandregpereira.hunter.domain.model.MonsterType.CELESTIAL
import br.alexandregpereira.hunter.monster.detail.ColorState
import br.alexandregpereira.hunter.monster.detail.MonsterImageState
import br.alexandregpereira.hunter.monster.detail.MonsterState
import br.alexandregpereira.hunter.monster.detail.SpeedState
import br.alexandregpereira.hunter.monster.detail.StatsState
import br.alexandregpereira.hunter.ui.compose.AppBarIcon
import br.alexandregpereira.hunter.ui.compose.AppImageContentScale
import br.alexandregpereira.hunter.ui.compose.AppSurface
import br.alexandregpereira.hunter.ui.compose.BoxCloseButton
import br.alexandregpereira.hunter.ui.compose.ChallengeRatingCircle
import br.alexandregpereira.hunter.ui.compose.LocalScreenSize
import br.alexandregpereira.hunter.ui.compose.MonsterTypeIcon
import br.alexandregpereira.hunter.ui.compose.Window
import br.alexandregpereira.hunter.ui.compose.cardShape
import br.alexandregpereira.hunter.ui.compose.monsterAspectRatio
import br.alexandregpereira.hunter.ui.transition.AlphaTransition
import br.alexandregpereira.hunter.ui.transition.getPageOffset
import br.alexandregpereira.hunter.ui.transition.getTransitionData
import br.alexandregpereira.hunter.ui.transition.transitionHorizontalScrollable
import br.alexandregpereira.hunter.ui.util.toColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.absoluteValue

@Composable
internal fun MonsterDetailScreen(
    monsters: List<MonsterState>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pagerState: PagerState = rememberPagerState(
        pageCount = { monsters.size }
    ),
    scrollState: LazyListState = rememberLazyListState(),
    onOptionsClicked: () -> Unit = {},
    onSpellClicked: (String) -> Unit = {},
    onLoreClicked: (String) -> Unit = {},
    onClose: () -> Unit = {},
) = AppSurface(color = Color.Transparent) {
    HorizontalPagerTransitionController(pagerState)

    MonsterImageCompose(
        monsters,
        pagerState,
        contentPadding = contentPadding,
    )

    val getImagesScrollOffset = {
        scrollState.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key == "MonsterImageCompose"
        }?.offset ?: 200
    }

    /*
    To avoid seeing the image below LazyColumn when horizontal pager transition is happening,
    a background was added at the bottom of the Monster title and below LazyColumn.
    When the scrolls changes and the Monster title moves, the background offset is updated to
    follow the scroll.
    */
    ScrollableBackground(
        getScrollPositionOffset = {
            scrollState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == MONSTER_TITLE_ITEM_KEY }
                ?.let { itemInfo: LazyListItemInfo ->
                    itemInfo.offset.coerceAtLeast(0)
                } ?: 0
        },
        getImageScrollOffset = getImagesScrollOffset,
    )

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "MonsterImageCompose") {
            Box(
                modifier = Modifier
                    .monsterAspectRatio(maxHeight = getImageHeightInDp())
                    .transitionHorizontalScrollable(pagerState)
                    .animateItem()
            )
        }

        item(key = MONSTER_TITLE_ITEM_KEY) {
            MonsterTitleCompose(
                monsterTitleStates = monsters.map {
                    MonsterTitleState(
                        title = it.name,
                        subTitle = it.subtitle
                    )
                },
                pagerState = pagerState,
                onOptionsClicked = onOptionsClicked,
                modifier = Modifier.animateItem()
            )
        }

        monsterInfo(
            monsters = monsters,
            pagerState = pagerState,
            contentPadding = contentPadding,
            getItemsKeys = { scrollState.layoutInfo.visibleItemsInfo.map { it.key } },
            onSpellClicked = onSpellClicked,
            onLoreClick = onLoreClicked
        )
    }

    BoxCloseButton(onClick = onClose)

    MonsterTopBar(
        monsters,
        pagerState,
        firstVisibleItemIndex = { scrollState.firstVisibleItemIndex },
        contentPadding = PaddingValues(
            top = 16.dp + contentPadding.calculateTopPadding(),
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        onOptionsClicked = onOptionsClicked,
        scrollToTop = {
            scrollState.animateScrollToItem(0)
        }
    )
}

@Composable
private fun HorizontalPagerTransitionController(pagerState: PagerState) = HorizontalPager(
    state = pagerState,
) {
    Box(Modifier.fillMaxWidth())
}

@Composable
private fun ScrollableBackground(
    getScrollPositionOffset: () -> Int,
    getImageScrollOffset: () -> Int,
) = Layout(
    content = {
        val offset = getImageScrollOffset().absoluteValue
        val fraction = offset.coerceAtMost(200) / 200f
        val backgroundColor = lerp(
            start = MaterialTheme.colors.background,
            stop = MaterialTheme.colors.surface,
            fraction = fraction
        )
        val initialAlpha = lerp(
            start = 0f,
            stop = 1f,
            fraction = fraction
        )
        val alpha1 = lerp(
            start = .7f,
            stop = 1f,
            fraction = fraction
        )
        val alpha2 = lerp(
            start = .8f,
            stop = 1f,
            fraction = fraction
        )
        val colorStops = arrayOf(
            .0f to backgroundColor.copy(alpha = initialAlpha),
            .1f to backgroundColor.copy(alpha = alpha1),
            .2f to backgroundColor.copy(alpha = alpha2),
            .3f to backgroundColor,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    shape = remember { RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) },
                    brush = Brush.verticalGradient(
                        colorStops = colorStops
                    )
                )
        )
    }
) { measurables, constraints ->
    val placeable = measurables.first().measure(constraints)
    layout(constraints.maxWidth, constraints.maxHeight) {
        val offset = getScrollPositionOffset()
        placeable.placeRelative(x = 0, y = offset)
    }
}

@Composable
private fun MonsterImageCompose(
    monsters: List<MonsterState>,
    pagerState: PagerState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(
        Modifier
            .monsterImageBackground(
                monsters = monsters,
                shape = cardShape,
                getPageOffset = { pagerState.getPageOffset() }
            )
    ) {
        MonsterImages(
            images = remember(monsters) {
                monsters.map {
                    ImageState(
                        url = it.imageUrl,
                        contentDescription = it.name,
                        contentScale = when (it.imageContentScale) {
                            MonsterImageContentScale.Fit -> AppImageContentScale.Fit
                            MonsterImageContentScale.Crop -> AppImageContentScale.Crop
                        },
                    )
                }
            },
            pagerState = pagerState,
            shape = cardShape,
        )

        ChallengeRatingCompose(
            monsters,
            pagerState,
            contentTopPadding = contentPadding.calculateTopPadding()
        )

        MonsterTypeIcon(
            monsters = monsters,
            pagerState = pagerState,
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
                .align(Alignment.TopEnd)
        )
    }
}

private fun Modifier.monsterImageBackground(
    monsters: List<MonsterState>,
    shape: Shape = RectangleShape,
    getPageOffset: () -> Float,
) = composed {
    val transitionData = getTransitionData(monsters, getPageOffset)

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val startColor = transitionData.data.getBackgroundColor(isSystemInDarkTheme)
    val endColor = transitionData.nextData.getBackgroundColor(isSystemInDarkTheme)

    val backgroundColor = lerp(
        start = startColor.toColor(),
        stop = endColor.toColor(),
        fraction = transitionData.fraction
    )

    this.background(backgroundColor, shape)
}

@Composable
private fun MonsterTopBar(
    monsters: List<MonsterState>,
    pagerState: PagerState,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    firstVisibleItemIndex: () -> Int,
    scrollToTop: suspend CoroutineScope.() -> Unit,
    onOptionsClicked: () -> Unit,
) {
    val visible by remember {
        derivedStateOf {
            firstVisibleItemIndex() > 0
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
    ) {
        Column {
            Row(
                Modifier
                    .background(color = MaterialTheme.colors.surface),
            ) {

                val composableScope = rememberCoroutineScope()
                AppBarIcon(
                    Icons.Filled.KeyboardArrowUp,
                    contentDescription = strings.goToTop,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(
                            start = contentPadding.calculateStartPadding(LayoutDirection.Rtl),
                            top = contentPadding.calculateTopPadding(),
                            bottom = contentPadding.calculateBottomPadding(),
                        ),
                    onClicked = {
                        composableScope.launch(block = scrollToTop)
                    }
                )

                MonsterTitleCompose(
                    monsterTitleStates = monsters.map { MonsterTitleState(title = it.name) },
                    pagerState = pagerState,
                    titleFontSize = MonsterTitleFontSize.SMALL,
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                        start = 24.dp,
                        end = 16.dp
                    ),
                    onOptionsClicked = onOptionsClicked,
                )
            }
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.background)
            )
        }
    }
}

@Composable
private fun ChallengeRatingCompose(
    monsters: List<MonsterState>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    contentTopPadding: Dp = 0.dp
) {
    AlphaTransition(dataList = monsters, pagerState, modifier = modifier) { data: MonsterState ->
        ChallengeRatingCircle(
            challengeRating = data.challengeRating,
            xp = data.xp,
            size = 100.dp,
            fontSize = 22.sp,
            xpFontSize = 16.sp,
            contentTopPadding = contentTopPadding
        )
    }
}

@Composable
private fun MonsterTypeIcon(
    monsters: List<MonsterState>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    AlphaTransition(dataList = monsters, pagerState, modifier = modifier) { data: MonsterState ->
        MonsterTypeIcon(
            icon = data.type.toIcon(),
            iconSize = 24.dp,
            size = 80.dp,
        )
    }
}

@Composable
private fun getImageHeightInDp(): Dp {
    val screenSizeInfo = LocalScreenSize.current
    return (screenSizeInfo.heightInDp.value * 0.84).dp
}

private const val MONSTER_TITLE_ITEM_KEY = "MonsterTitleCompose"

@Preview
@Composable
private fun MonsterDetailPreview() = Window {
    MonsterDetailScreen(
        monsters = (0..10).map {
            MonsterState(
                index = "",
                name = "Monster of the monsters",
                imageState = MonsterImageState(
                    url = "",
                    type = CELESTIAL,
                    challengeRating = "1",
                    xp = "100 XP",
                    backgroundColor = ColorState(
                        light = "#ffe2e2",
                        dark = "#ffe2e2"
                    ),
                ),
                subtitle = "This is the subtitle",
                stats = StatsState(
                    armorClass = 0,
                    hitPoints = 0,
                    hitDice = ""
                ),
                speed = SpeedState(hover = false, values = listOf()),
                abilityScores = listOf(),
                savingThrows = listOf(),
                skills = listOf(),
                damageVulnerabilities = listOf(),
                damageResistances = listOf(),
                damageImmunities = listOf(),
                conditionImmunities = listOf(),
                senses = listOf(),
                languages = "Test",
                specialAbilities = listOf(),
                actions = listOf(),
                reactions = listOf()
            )
        },
    )
}

@Preview
@Composable
private fun MonsterTopBarPreview() = Window {
    MonsterTopBar(
        monsters = listOf(
            MonsterState(
                index = "",
                name = "Monster of the monsters",
                imageState = MonsterImageState(
                    url = "",
                    type = CELESTIAL,
                    challengeRating = "1",
                    xp = "100 XP",
                    backgroundColor = ColorState(
                        light = "#ffe2e2",
                        dark = "#ffe2e2"
                    ),
                ),
                subtitle = "This is the subtitle",
                stats = StatsState(
                    armorClass = 0,
                    hitPoints = 0,
                    hitDice = ""
                ),
                speed = SpeedState(hover = false, values = listOf()),
                abilityScores = listOf(),
                savingThrows = listOf(),
                skills = listOf(),
                damageVulnerabilities = listOf(),
                damageResistances = listOf(),
                damageImmunities = listOf(),
                conditionImmunities = listOf(),
                senses = listOf(),
                languages = "Test",
                specialAbilities = listOf(),
                actions = listOf(),
                reactions = listOf()
            )
        ),
        pagerState = rememberPagerState(
            pageCount = { 1 }
        ),
        firstVisibleItemIndex = { 0 },
        onOptionsClicked = {

        },
        scrollToTop = {}
    )
}
