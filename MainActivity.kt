package com.dollcii.vipringer

import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var list: ListView
    private lateinit var status: TextView
    private lateinit var adapter: ArrayAdapter<String>
    private var entries = listOf<String>()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        status = TextView(this).apply { textSize = 16f; gravity = Gravity.CENTER }
        val roleBtn = Button(this).apply {
            text = "1) تفعيل التطبيق كفلتر مكالمات"
            setOnClickListener { requestRole() }
        }
        val sw = Switch(this).apply {
            text = "تشغيل الفلتر (غير المحددين = صامت)"
            isChecked = Store.isEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, v -> Store.setEnabled(this@MainActivity, v) }
        }
        val pickBtn = Button(this).apply {
            text = "2) إضافة من جهات الاتصال"
            setOnClickListener {
                startActivityForResult(
                    Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI), 2)
            }
        }
        val manualBtn = Button(this).apply {
            text = "إضافة رقم يدويًا"
            setOnClickListener { addManual() }
        }
        val hint = TextView(this).apply { text = "اضغط مطولًا على أي رقم لحذفه" }
        list = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        list.adapter = adapter
        list.setOnItemLongClickListener { _, _, pos, _ ->
            val e = entries[pos]
            AlertDialog.Builder(this)
                .setMessage("حذف ${e.substringBefore("|")}؟")
                .setPositiveButton("حذف") { _, _ -> Store.remove(this, e); refresh() }
                .setNegativeButton("إلغاء", null).show()
            true
        }
        listOf(status, roleBtn, sw, pickBtn, manualBtn, hint).forEach { root.addView(it) }
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun hasRole(): Boolean =
        getSystemService(RoleManager::class.java).isRoleHeld(RoleManager.ROLE_CALL_SCREENING)

    private fun requestRole() {
        val rm = getSystemService(RoleManager::class.java)
        if (!hasRole()) startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), 1)
    }

    private fun refresh() {
        status.text = if (hasRole()) "✅ الفلتر شغال" else "⚠️ لسه لازم تفعّل الصلاحية (الزر الأول)"
        entries = Store.list(this)
        adapter.clear()
        adapter.addAll(entries.map { "${it.substringBefore("|")}  —  ${it.substringAfter("|")}" })
    }

    private fun addManual() {
        val name = EditText(this).apply { hint = "الاسم (اختياري)" }
        val num = EditText(this).apply { hint = "الرقم"; inputType = InputType.TYPE_CLASS_PHONE }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 20, 40, 0); addView(name); addView(num) }
        AlertDialog.Builder(this).setView(box)
            .setPositiveButton("إضافة") { _, _ ->
                if (num.text.isNotBlank()) { Store.add(this, name.text.toString(), num.text.toString()); refresh() }
            }.setNegativeButton("إلغاء", null).show()
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == 2 && res == RESULT_OK && data?.data != null) {
            contentResolver.query(data.data!!, arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) Store.add(this, it.getString(1) ?: "", it.getString(0) ?: "")
            }
        }
        refresh()
    }
}
