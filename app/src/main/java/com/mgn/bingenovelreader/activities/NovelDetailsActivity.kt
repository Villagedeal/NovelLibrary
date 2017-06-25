package com.mgn.bingenovelreader.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import com.mgn.bingenovelreader.R
import com.mgn.bingenovelreader.adapters.GenericAdapter
import com.mgn.bingenovelreader.database.getAllReadableWebPages
import com.mgn.bingenovelreader.database.getNovel
import com.mgn.bingenovelreader.dbHelper
import com.mgn.bingenovelreader.models.Novel
import com.mgn.bingenovelreader.models.WebPage
import com.mgn.bingenovelreader.utils.*
import kotlinx.android.synthetic.main.content_novel_details.*
import kotlinx.android.synthetic.main.listitem_string.view.*
import java.io.File
import java.io.FileInputStream


class NovelDetailsActivity : SlidingActivity(), GenericAdapter.Listener<WebPage> {

    var novel: Novel? = null
    var chapters: ArrayList<WebPage> = ArrayList()
    lateinit var adapter: GenericAdapter<WebPage>

    override fun init(savedInstanceState: Bundle?) {

        //Get Data From Intent & Database
        val novelId = intent.getLongExtra(Constants.NOVEL_ID, -1L)
        if (novelId != -1L) novel = dbHelper.getNovel(novelId)
        if (novel != null) chapters = ArrayList(dbHelper.getAllReadableWebPages(novelId))
        else
            finish()

        setContent(R.layout.content_novel_details)
        title = novel?.name

        setPrimaryColors(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorPrimaryDark))

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        try {
            val bitmap = BitmapFactory.decodeStream(FileInputStream(File(novel?.imageFilePath)), null, options)
            setImage(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setImageOverlay(0.6F)

        adapter = GenericAdapter(items = chapters, layoutResId = R.layout.listitem_string, listener = this)
        recyclerView.setDefaults(adapter)

        setFab(R.color.colorAccent, R.drawable.ic_delete_black_vector, { deleteNovel() }, android.R.color.white)
        if (chapters.isEmpty()) {
            setLoadingView(R.drawable.no_chapters, "There are no chapters!! (╥﹏╥)")
            recyclerView.visibility = View.INVISIBLE
            enableLoadingView(true)
        } else {
            //        setFab(R.color.colorAccent, R.drawable.ic_favorite_black_vector, null, android.R.color.white)
        }
    }

    override fun bind(item: WebPage, itemView: View) {
        itemView.listItemTitle.text = item.title
        itemView.listItemTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.toFloat())
    }

    override fun onItemClick(item: WebPage) {
        toast("${item.chapter} Clicked")
        startReaderActivity(item)
    }

    private fun startReaderActivity(webPage: WebPage) {
        val intent = Intent(this, ReaderPagerActivity::class.java)
        intent.putExtra(Constants.NOVEL_ID, webPage.novelId)
        intent.putExtra(Constants.WEB_PAGE_ID, webPage.id)
        startActivityForResult(intent, Constants.NOVEL_DETAILS_REQ_CODE)
    }

    private fun deleteNovel() {
        Util.deleteNovel(this, novel)
        val intent = Intent()
        val bundle = Bundle()
        bundle.putLong(Constants.NOVEL_ID, novel!!.id)
        intent.putExtras(bundle)
        setResult(Constants.NOVEL_DETAILS_RES_CODE, intent)
        finish()
    }

}
