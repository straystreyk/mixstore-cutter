package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.AppDataStore
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.data.repository.CustomCatalogRepository
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomCategoryPartsFragment : Fragment() {
    private val loadedItems = mutableListOf<PartListItem>()
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var currentQuery = ""
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recycler_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val categoryId = requireArguments().getString(ARG_CATEGORY_ID).orEmpty()
        val category = AppDataStore.findCustomCategory(categoryId)
        val categoryName = category?.name.orEmpty()

        (requireActivity() as MainActivity).setTitleAndBack(
            category?.name ?: getString(R.string.title_parts),
            true
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val progressView = view.findViewById<View>(R.id.progress_view)
        val searchView = view.findViewById<EditText>(R.id.search_view)
        val layoutManager = LinearLayoutManager(requireContext())

        lateinit var adapter: PartListAdapter
        adapter = PartListAdapter(
            onPrintClick = {
                printCustomPart(it.id)
            },
            onItemClick = { item ->
                toggleFavorite(item.id)
                adapter.submitList(loadedItems.toList())
            }
        )

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        progressView.visibility = View.VISIBLE
        searchView.visibility = View.VISIBLE

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 || isLoading || isLastPage) return
                val totalItems = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= totalItems - LOAD_MORE_THRESHOLD) {
                    loadNextPage(
                        categoryName = categoryName,
                        emptyView = emptyView,
                        progressView = progressView,
                        adapter = adapter,
                        reset = false
                    )
                }
            }
        })

        searchView.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(350)
                val nextQuery = editable?.toString().orEmpty().trim()
                if (nextQuery == currentQuery) return@launch
                currentQuery = nextQuery
                loadNextPage(
                    categoryName = categoryName,
                    emptyView = emptyView,
                    progressView = progressView,
                    adapter = adapter,
                    reset = true
                )
            }
        }

        loadNextPage(
            categoryName = categoryName,
            emptyView = emptyView,
            progressView = progressView,
            adapter = adapter,
            reset = true
        )
    }

    private fun loadNextPage(
        categoryName: String,
        emptyView: TextView,
        progressView: View,
        adapter: PartListAdapter,
        reset: Boolean
    ) {
        if (categoryName.isBlank()) {
            emptyView.text = getString(R.string.custom_catalog_error)
            emptyView.visibility = View.VISIBLE
            progressView.visibility = View.GONE
            return
        }
        if (isLoading) return
        if (!reset && isLastPage) return

        viewLifecycleOwner.lifecycleScope.launch {
            isLoading = true
            if (reset) {
                currentPage = 0
                isLastPage = false
                loadedItems.clear()
                adapter.submitList(emptyList())
                progressView.visibility = View.VISIBLE
            }

            val repository = CustomCatalogRepository(RetrofitProvider.getCustomCatalogApi())
            val baseUrl = PreferenceManager.getBaseUrl().trimEnd('/')
            runCatching {
                repository.loadPartsPage(
                    baseUrl = baseUrl,
                    categoryName = categoryName,
                    page = currentPage,
                    filter = currentQuery
                )
            }.onSuccess { page ->
                val favorites = PreferenceManager.getFavoriteTemplates()
                val nextItems = page.items.map { part ->
                    val favoriteId = "custom:${part.id}"
                    PartListItem(
                        id = favoriteId,
                        title = part.title,
                        subtitle = part.subtitle,
                        canPrint = true,
                        isFavorite = favorites.contains(favoriteId),
                        imageUrl = part.imageUrl
                    )
                }
                loadedItems.addAll(nextItems)
                adapter.submitList(loadedItems.toList())
                emptyView.visibility = if (loadedItems.isEmpty()) View.VISIBLE else View.GONE
                progressView.visibility = View.GONE
                isLastPage = page.isLastPage
                currentPage = page.page + 1
            }.onFailure {
                if (loadedItems.isEmpty()) {
                    emptyView.text = it.message ?: getString(R.string.custom_catalog_error)
                    emptyView.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show()
                }
                progressView.visibility = View.GONE
            }
            isLoading = false
        }
    }

    private fun printCustomPart(partToken: String) {
        val partId = partToken.removePrefix("custom:").toIntOrNull()
        if (partId == null) {
            Toast.makeText(requireContext(), R.string.print_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val appContext = context?.applicationContext ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val result = runCatching {
                PlotterPrintHelper.printCustomPartById(partId)
            }.getOrElse {
                com.pokrikinc.mixpokrikcutter.plotter.PrintResult(
                    false,
                    it.message ?: "Print file is unavailable"
                )
            }

            if (!isAdded) return@launch
            Toast.makeText(
                appContext,
                if (result.isSuccess) R.string.print_sent else R.string.print_failed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleFavorite(partId: String) {
        val favorites = PreferenceManager.getFavoriteTemplates().toMutableSet()
        if (favorites.contains(partId)) {
            favorites.remove(partId)
        } else {
            favorites.add(partId)
        }
        PreferenceManager.setFavoriteTemplates(favorites)

        for (index in loadedItems.indices) {
            val item = loadedItems[index]
            if (item.id == partId) {
                loadedItems[index] = item.copy(isFavorite = favorites.contains(partId))
                break
            }
        }
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val LOAD_MORE_THRESHOLD = 8

        fun newInstance(categoryId: String): CustomCategoryPartsFragment {
            return CustomCategoryPartsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId)
                }
            }
        }
    }
}
