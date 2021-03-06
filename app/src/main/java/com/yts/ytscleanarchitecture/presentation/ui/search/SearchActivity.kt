package com.yts.ytscleanarchitecture.presentation.ui.search

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.SparseArray
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.yts.ytscleanarchitecture.BR
import com.yts.ytscleanarchitecture.R
import com.yts.ytscleanarchitecture.databinding.ActivitySearchBinding
import com.yts.ytscleanarchitecture.extension.makeToast
import com.yts.ytscleanarchitecture.extension.showLoading
import com.yts.ytscleanarchitecture.extension.startCircularRevealAnimation
import com.yts.ytscleanarchitecture.extension.visible
import com.yts.ytscleanarchitecture.presentation.base.BackDoubleClickFinishActivity
import com.yts.ytscleanarchitecture.utils.Const
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_search.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class SearchActivity : BackDoubleClickFinishActivity<ActivitySearchBinding>(),
    View.OnClickListener {
    private val model: SearchViewModel by viewModel()

    private var currentFragmentTag: String? = null
    private var filterTextVisibilityDisposable: Disposable? = null

    override fun onLayoutId(): Int {
        return R.layout.activity_search
    }

    override fun setupViewModel(): SparseArray<ViewModel> {
        val setupViewModel = SparseArray<ViewModel>()
        setupViewModel.put(BR.model, model)
        return setupViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        ViewCompat.setTransitionName(
            text_title,
            Const.TRANS_VIEW_NAME_TITLE
        )
        initView()

    }

    private fun initView() {
        btn_text_delete.setOnClickListener(this)
        model.setViewType(SearchViewType.NONE)
    }


    private fun changeFragment(fragment: Fragment) {
        val tag = fragment.javaClass.simpleName
        if (currentFragmentTag == null || currentFragmentTag != tag) {
            currentFragmentTag = tag
            val fragmentManager = supportFragmentManager
            val fragmentTransaction =
                fragmentManager.beginTransaction()
            fragmentTransaction
                .replace(R.id.content, fragment, tag)
                .commitAllowingStateLoss()
        }
    }


    override fun observer() {
        model.isLoading.observe(this, Observer {
            loading.showLoading(it)
        })

        model.toastMessageId.observe(this, Observer {
            makeToast(it)
        })

        model.viewType.observe(this, Observer { type ->
            if (type == SearchViewType.NONE) {
                var spannableStringBuilder =
                    SpannableStringBuilder(getString(R.string.kakao_commerce))
                spannableStringBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    5,
                    13,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                text_title.text = spannableStringBuilder

                changeFragment(SearchFragment.newInstance())
            } else if (type == SearchViewType.RESULT) {

                var spannableStringBuilder =
                    SpannableStringBuilder(getString(R.string.search))
                spannableStringBuilder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    6,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                text_title.text = spannableStringBuilder


                changeFragment(SearchResultFragment.newInstance())
            }
        })

        model.query.observe(this, Observer { query ->
            btn_text_delete.visible(query != null && query.isNotEmpty())
        })
        model.documentList.observe(this, Observer {
            model.setDocumentFilterList(it)
            model.setFilterHashSet(it)
        })

        model.filter.observe(this, Observer { filter ->
            filterTextVisibilityDisposable?.dispose()

            if (filter != null && filter.isNotEmpty()) {
                text_filter.text = filter
                text_filter.startCircularRevealAnimation()
                if (filter == Const.FILTER_ALL) {
                    filterTextVisibilityDisposable = Observable.timer(1000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer {
                            text_filter.visibility = View.GONE
                        })
                    model.addDisposable(filterTextVisibilityDisposable!!)
                }
            } else {
                text_filter.visibility = View.GONE
            }
        })

        edit_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                model.changeQueryText(s.toString())

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_text_delete -> {
                edit_search.setText("")
            }
        }
    }
}