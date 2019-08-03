package io.github.nfdz.clubadmin.users.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import io.github.nfdz.clubadmin.common.User
import io.github.nfdz.clubadmin.users.NewUser
import io.github.nfdz.clubadmin.users.checkDateText
import io.github.nfdz.clubadmin.users.createUser
import io.github.nfdz.clubcommonlibrary.*
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_user_editor.*

class UserEditorActivity : AppCompatActivity() {

    companion object {
        private const val USER_TO_EDIT_KEY = "user"
        fun startActivity(context: Context, user: User? = null) {
            context.startActivity(Intent(context, UserEditorActivity::class.java).apply { putExtra(USER_TO_EDIT_KEY, user) })
        }
    }

    private var userToEdit: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_editor)
        userToEdit = intent.getParcelableExtra(USER_TO_EDIT_KEY)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = when(userToEdit) {
            null -> getString(R.string.user_editor_add_mode_title)
            else -> getString(R.string.user_editor_edit_mode_title)
        }

        userToEdit?.let {
            user_et_email.setText(it.email)
            user_et_email.isEnabled = false
            user_et_full_name.setText(it.fullName)
            user_et_alias.setText(it.alias)
            user_et_address.setText(it.address)
            user_et_phone_number.setText(it.phoneNumber)
            user_et_birthday_date.setText(it.birthday)
            user_et_points.setText(it.points.toString())
            user_switch_category.isChecked = it.highlightChatFlag
            user_tv_signup_date.text = (getString(R.string.user_editor_sign_up, it.signUpTimestamp.toDate().printDayMonthYear()))
        }

        if (userToEdit == null) {
            user_et_points.visibility = View.GONE
            user_et_alias.visibility = View.GONE
        }

        user_btn_save.setOnClickListener { handleSaveEvent() }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun handleSaveEvent() {
        // check email
        val email = user_et_email.text.toString()
        if (email.isEmpty()) {
            toast(R.string.user_editor_email_error)
            return
        }
        // check birthday
        val birthday = user_et_birthday_date.text.toString()
        if (!checkDateText(birthday)) {
            toast(R.string.user_editor_birthday_format_error)
            return
        }
        val points = try {
            user_et_points.text.toString().toInt()
        } catch (e: Exception) {
            0
        }

        user_form_container.fadeOut()
        user_pb_loading.fadeIn()
        val userToEditId = userToEdit?.id
        if (userToEditId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userToEditId)
                .update(
                    "fullName", user_et_full_name.text.toString(),
                    "alias", user_et_alias.text.toString(),
                    "address", user_et_address.text.toString(),
                    "phoneNumber", user_et_phone_number.text.toString(),
                    "birthday", birthday,
                    "points", points,
                    "highlightChatFlag", user_switch_category.isChecked
                )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        navigateToFinish()
                    } else {
                        user_form_container.fadeIn()
                        user_pb_loading.fadeOut()
                        toast(R.string.user_editor_save_error)
                    }
                }
        } else {
            val userToCreate = NewUser(email,
                user_et_full_name.text.toString(),
                user_et_address.text.toString(),
                user_et_phone_number.text.toString(),
                birthday,
                user_switch_category.isChecked,
                System.currentTimeMillis())
            createUser(this,
                userToCreate, {
                    navigateToFinish()
                }, { alreadyExists: Boolean, createError: Boolean, saveError: Boolean ->
                    user_form_container.fadeIn()
                    user_pb_loading.fadeOut()
                    when {
                        alreadyExists -> toast(R.string.user_editor_save_already_error)
                        createError -> toast(R.string.user_editor_save_create_error)
                        saveError -> toast(R.string.user_editor_save_error)
                    }
                })
        }

    }

    private fun navigateToFinish() {
        finish()
    }


}
