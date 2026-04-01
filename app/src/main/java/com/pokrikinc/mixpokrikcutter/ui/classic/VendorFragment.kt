package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.AppDataStore
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R

class VendorFragment : Fragment() {
    private val allItems = mutableListOf<SimpleListItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recycler_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val categoryId = requireArguments().getString(ARG_CATEGORY_ID).orEmpty()
        val category = AppDataStore.findCategory(categoryId)

        (requireActivity() as MainActivity).setTitleAndBack(
            category?.name ?: getString(R.string.title_vendors),
            true
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val progressView = view.findViewById<View>(R.id.progress_view)
        val searchView = view.findViewById<EditText>(R.id.search_view)

        val adapter = SimpleListAdapter { item ->
            (requireActivity() as MainActivity).openDevices(categoryId, item.id)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        progressView.visibility = View.GONE
        searchView.visibility = View.VISIBLE

        allItems.clear()
        allItems.addAll(category?.vendors?.map { vendor ->
            SimpleListItem(
                id = vendor.id,
                title = vendor.name,
                subtitle = "ID ${vendor.id}",
                actionLabel = getString(R.string.action_open_devices),
                imageUrl = AppDataStore.resolveImagePath(vendor.img)
            )
        }.orEmpty())

        searchView.addTextChangedListener {
            updateList(adapter, emptyView, it?.toString().orEmpty())
        }

        updateList(adapter, emptyView, searchView.text?.toString().orEmpty())
    }

    private fun updateList(adapter: SimpleListAdapter, emptyView: TextView, query: String) {
        val filtered = allItems.filter { item ->
            query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.id.contains(query, ignoreCase = true)
        }

        adapter.submitList(filtered)
        emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"

        fun newInstance(categoryId: String): VendorFragment {
            return VendorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId)
                }
            }
        }
    }
}
