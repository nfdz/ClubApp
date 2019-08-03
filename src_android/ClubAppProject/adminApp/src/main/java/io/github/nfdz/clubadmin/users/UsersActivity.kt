package io.github.nfdz.clubadmin.users

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.github.nfdz.clubadmin.common.User
import io.github.nfdz.clubadmin.users.editor.UserEditorActivity
import io.github.nfdz.clubcommonlibrary.printDayMonthYear
import io.github.nfdz.clubcommonlibrary.toDate
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_users.*
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class UsersActivity : AppCompatActivity(), UsersAdapter.Listener {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, UsersActivity::class.java))
        }
    }

    private val adapter = UsersAdapter(listener = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        users_rv.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        users_refresh.setOnRefreshListener { refreshContent() }
        users_fab_add.setOnClickListener { navigateToAddUser() }
        users_rv.adapter = adapter
        users_fab_add_from_csv.setOnClickListener { askCsvText() }
        users_fab_search.setOnClickListener {
            if (adapter.filter.isEmpty()) {
                askSearchText()
            } else {
                adapter.filter = ""
                users_fab_search.setImageResource(R.drawable.ic_search_light)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshContent()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun refreshContent() {
        users_refresh.isRefreshing = true
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("fullName", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(this) { task ->
                try {
                    if (task.isSuccessful) {
                        adapter.data = task.result?.documents?.map { doc ->
                            User(doc.id,
                                doc.data!!["email"] as String,
                                doc.data!!["alias"] as String,
                                doc.data!!["fullName"] as String,
                                doc.data!!["address"] as String,
                                doc.data!!["phoneNumber"] as String,
                                doc.data!!["birthday"] as String,
                                (doc.data!!["signUpTimestamp"] as Number).toLong(),
                                (doc.data!!["points"] as Number).toInt(),
                                doc.data!!["highlightChatFlag"] as Boolean,
                                doc.data!!["isDisabled"] as Boolean)
                        } ?: emptyList()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    toast(R.string.users_refresh_error)
                }
                users_refresh.isRefreshing = false
            }

    }

    override fun onToggleUserEnableStateClick(user: User) {
        if (user.isDisabled) {
            askEnableUserConfirmation(user)
        } else {
            askDisableUserConfirmation(user)
        }
    }

    override fun onEditUserClick(user: User) {
        UserEditorActivity.startActivity(this, user)
    }

    private fun askDisableUserConfirmation(user: User) {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.users_disable_title)
            setMessage(getString(R.string.users_disable_message, user.fullName, user.email))
        }.setPositiveButton(android.R.string.yes) { dialog, _ ->
            setUserEnabled(user, true)
            dialog.dismiss()
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.show()
    }

    private fun askEnableUserConfirmation(user: User) {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.users_enable_title)
            setMessage(getString(R.string.users_enable_message, user.fullName, user.email))
        }.setPositiveButton(android.R.string.yes) { dialog, _ ->
            setUserEnabled(user, false)
            dialog.dismiss()
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.show()
    }

    private fun setUserEnabled(user: User, isDisabled: Boolean) {
        toast(R.string.users_editing_state)
        FirebaseFirestore.getInstance().collection("users")
            .document(user.id)
            .update("isDisabled", isDisabled)
            .addOnCompleteListener(this) {
                refreshContent()
            }
    }

    private fun navigateToAddUser() {
        UserEditorActivity.startActivity(this)
    }

    private fun askSearchText() {
        val paddingHorizontal = resources.getDimensionPixelSize(R.dimen.dialog_horizontal_padding)
        val paddingTop = resources.getDimensionPixelSize(R.dimen.dialog_top_padding)
        val input = AppCompatEditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
            maxLines = 1
            hint = getString(R.string.users_search_hint)
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }
        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            setPadding(paddingHorizontal, paddingTop, paddingHorizontal, 0)
            addView(input)
        }
        AlertDialog.Builder(this).apply {
            setView(container)
        }.setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int ->
            dialog.cancel()
        }.setPositiveButton(R.string.users_search_button) { dialog: DialogInterface, _: Int ->
            val filter = input.text.toString().trim()
            if (!filter.isEmpty()) {
                adapter.filter = filter
                users_fab_search.setImageResource(R.drawable.ic_clear_light)
            }
            dialog.dismiss()
        }.show()
    }

    private fun askCsvText() {
        val paddingHorizontal = resources.getDimensionPixelSize(R.dimen.dialog_horizontal_padding)
        val paddingTop = resources.getDimensionPixelSize(R.dimen.dialog_top_padding)
        val input = AppCompatEditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(false)
            isVerticalScrollBarEnabled = true
            isHorizontalScrollBarEnabled = true
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }
        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            setPadding(paddingHorizontal, paddingTop, paddingHorizontal, 0)
            addView(input)
        }
        AlertDialog.Builder(this).apply {
            setView(container)
        }.setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int ->
            dialog.cancel()
        }.setPositiveButton(R.string.user_editor_button_save) { dialog: DialogInterface, _: Int ->
            addUsersFromCsv(input.text.toString())
            dialog.dismiss()
        }.show()
    }

    private fun addUsersFromCsv(csvText: String) {
        val usersToCreate: List<NewUser> = csvText.split('\n').map { line ->
            val columns = line.split(',')
            if (columns.size >= 8 && columns[0].isEmpty()) {
                val firstName = columns[1].trim()
                val lastName = columns[2].trim()
                val email = columns[3].trim()
                val phoneNumber = columns[4].trim()
                val address = columns[5].trim()
                val birthdayText = columns[6].trim()
                val signUpDateText = columns[7]
                val signUpDate = getDateFromText(signUpDateText, true)
                val birthdayDate = getDateFromText(birthdayText, true)
                if (!firstName.isEmpty() &&
                    !lastName.isEmpty() &&
                    !email.isEmpty() &&
                    !phoneNumber.isEmpty() &&
                    !address.isEmpty() &&
                    signUpDate != null &&
                    birthdayDate != null) {
                    NewUser(email,
                        "$firstName $lastName",
                        address,
                        phoneNumber,
                        birthdayDate.toDate().printDayMonthYear(),
                        false,
                        signUpDate)
                } else {
                    null
                }
            } else {
                null
            }
        }.filterNotNull().toSet().toList()
        if (usersToCreate.isEmpty()) {
            toast(R.string.users_add_from_csv_empty)
        } else {
            users_refresh.isRefreshing = true
            val successCount = AtomicInteger(0)
            val count = AtomicInteger(0)
            addUser(usersToCreate, count, successCount)
        }
    }

    private fun addUser(usersToCreate: List<NewUser>, count: AtomicInteger, successCount: AtomicInteger) {
        val userIndex = count.get()
        val countText = "${userIndex+1}/${usersToCreate.size}"
        Timber.d("Creating user from CSV: $countText")
        toast(countText, Toast.LENGTH_SHORT)
        val userToCreate = usersToCreate[userIndex]
        val continueCallback = {
            if (count.incrementAndGet() >= usersToCreate.size) {
                toast(getString(R.string.users_add_from_csv_report, successCount.get().toString(), count.get().toString()))
                refreshContent()
            } else {
                addUser(usersToCreate, count, successCount)
            }
        }
        createUser(this,
            userToCreate, {
                successCount.incrementAndGet()
                continueCallback()
            }, { alreadyExists: Boolean, createError: Boolean, saveError: Boolean ->
                if (createError || saveError) {
                    Timber.e("There was an error creating user from csv: $userToCreate")
                } else if (alreadyExists) {
                    Timber.d("User already exists: $userToCreate")
                }
                continueCallback()
            })
    }

}
