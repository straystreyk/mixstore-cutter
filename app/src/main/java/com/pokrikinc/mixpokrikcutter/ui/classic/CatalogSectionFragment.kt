package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.AppDataStore
import com.pokrikinc.mixpokrikcutter.CatalogSource
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import kotlinx.coroutines.launch

class CatalogSectionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recycler_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val source = requireArguments().getString(ARG_SOURCE).orEmpty()
        val activity = requireActivity() as MainActivity
        activity.setTitleAndBack(
            if (source == CatalogSource.CUSTOM) {
                getString(R.string.title_catalog_custom)
            } else {
                getString(R.string.title_catalog_builtin)
            },
            true
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val progressView = view.findViewById<View>(R.id.progress_view)

        val adapter = SimpleListAdapter { item ->
            activity.openVendors(source, item.id)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            progressView.visibility = View.VISIBLE
            val items = if (source == CatalogSource.CUSTOM) {
                val result = AppDataStore.ensureCustomCategoriesLoaded()
                if (!result.isSuccess) {
                    emptyView.text = result.message ?: getString(R.string.custom_catalog_error)
                    emptyView.visibility = View.VISIBLE
                    progressView.visibility = View.GONE
                    return@launch
                }
                AppDataStore.getCustomCategories().map { category ->
                    SimpleListItem(
                        id = category.id,
                        title = category.name,
                        subtitle = getString(R.string.catalog_custom_category_subtitle),
                        actionLabel = getString(R.string.action_open_parts),
                        imageUrl = category.imageUrl
                    )
                }
            } else {
                AppDataStore.getCatalog().map { category ->
                    SimpleListItem(
                        id = category.id,
                        title = category.name,
                        subtitle = "${category.vendors.size} vendors",
                        actionLabel = getString(R.string.action_open_section),
                        imageUrl = AppDataStore.resolveImagePath(category.img)
                    )
                }
            }

            adapter.submitList(items)
            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            progressView.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_SOURCE = "source"

        fun newInstance(source: String): CatalogSectionFragment {
            return CatalogSectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SOURCE, source)
                }
            }
        }
    }
}

