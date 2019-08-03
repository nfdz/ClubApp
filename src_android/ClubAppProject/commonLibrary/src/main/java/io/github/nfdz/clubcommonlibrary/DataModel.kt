package io.github.nfdz.clubcommonlibrary

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class EventCategory(@StringRes val textRes: Int, @ColorRes val colorRes: Int, @DrawableRes val imageRes: Int) {
    ART(R.string.category_art, R.color.categoryArtColor, R.drawable.arts),
    VIDEO(R.string.category_video, R.color.categoryVideoColor, R.drawable.video),
    WELLNESS(R.string.category_wellness, R.color.categoryWellnessColor, R.drawable.wellness),
    FOOD(R.string.category_food, R.color.categoryFoodColor, R.drawable.food),
    CHILDREN(R.string.category_children, R.color.categoryChildrenColor, R.drawable.children),
    MUSIC(R.string.category_music, R.color.categoryMusicColor, R.drawable.music),
    SEASON(R.string.category_season, R.color.categorySeasonColor, R.drawable.season)
}