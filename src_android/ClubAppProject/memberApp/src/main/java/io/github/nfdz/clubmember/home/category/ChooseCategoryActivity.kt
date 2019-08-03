package io.github.nfdz.clubmember.home.category

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import io.github.nfdz.clubcommonlibrary.EventCategory
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_choose_category.*

class ChooseCategoryActivity : AppCompatActivity() {

    companion object {

        private const val CATEGORY_RESULT_EXTRA = "category"

        @JvmStatic
        fun startActivityForResult(fragment: Fragment, requestCode: Int) {
            fragment.startActivityForResult(Intent(fragment.context, ChooseCategoryActivity::class.java), requestCode)
        }

        @JvmStatic
        fun getCategoryFromResult(data: Intent?): EventCategory? {
            return data?.getStringExtra(CATEGORY_RESULT_EXTRA)?.let { EventCategory.valueOf(it) }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
    }

    private fun setupView() {
        setContentView(R.layout.activity_choose_category)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        choose_category_iv_art.setOnClickListener { handleCategoryClick(EventCategory.ART) }
        choose_category_iv_video.setOnClickListener { handleCategoryClick(EventCategory.VIDEO) }
        choose_category_iv_wellness.setOnClickListener { handleCategoryClick(EventCategory.WELLNESS) }
        choose_category_iv_food.setOnClickListener { handleCategoryClick(EventCategory.FOOD) }
        choose_category_iv_children.setOnClickListener { handleCategoryClick(EventCategory.CHILDREN) }
        choose_category_iv_music.setOnClickListener { handleCategoryClick(EventCategory.MUSIC) }
        choose_category_iv_season.setOnClickListener { handleCategoryClick(EventCategory.SEASON) }
    }

    private fun handleCategoryClick(category: EventCategory) {
        setResult(Activity.RESULT_OK, Intent().apply { putExtra(CATEGORY_RESULT_EXTRA, category.name) })
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

}
