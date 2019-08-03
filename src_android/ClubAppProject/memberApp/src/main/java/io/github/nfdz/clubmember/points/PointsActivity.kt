package io.github.nfdz.clubmember.points

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.bumptech.glide.Glide
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_points.*

class PointsActivity : AppCompatActivity() {

    companion object {
        private const val USER_POINTS_KEY = "user_points"
        @JvmStatic
        fun startActivity(context: Context, userPoints: Int) {
            context.startActivity(Intent(context, PointsActivity::class.java).apply { putExtra(USER_POINTS_KEY, userPoints) })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun setupView() {
        setContentView(R.layout.activity_points)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        points_tv_points.text = intent.getIntExtra(USER_POINTS_KEY, 0).toString()
        Glide.with(this)
            .asGif()
            .load(R.drawable.trophy)
            .into(points_iv_coming_soon)
    }

}
