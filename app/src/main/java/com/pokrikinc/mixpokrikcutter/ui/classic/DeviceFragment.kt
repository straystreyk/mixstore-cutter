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

class DeviceFragment : Fragment() {
    private val allItems = mutableListOf<SimpleListItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recycler_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val source = requireArguments().getString(ARG_SOURCE).orEmpty()
        val categoryId = requireArguments().getString(ARG_CATEGORY_ID).orEmpty()
        val vendorId = requireArguments().getString(ARG_VENDOR_ID).orEmpty()
        val vendor = AppDataStore.findVendor(categoryId, vendorId)

        (requireActivity() as MainActivity).setTitleAndBack(
            vendor?.name ?: getString(R.string.title_devices),
            true
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val progressView = view.findViewById<View>(R.id.progress_view)
        val searchView = view.findViewById<EditText>(R.id.search_view)

        val adapter = SimpleListAdapter { item ->
            (requireActivity() as MainActivity).openParts(source, categoryId, vendorId, item.id)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        progressView.visibility = View.GONE
        searchView.visibility = View.VISIBLE

        allItems.clear()
        allItems.addAll(vendor?.devices?.map { device ->
            SimpleListItem(
                id = device.id,
                title = device.name,
                subtitle = "ID ${device.id}",
                actionLabel = getString(R.string.action_open_parts)
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
        private const val ARG_SOURCE = "source"
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_VENDOR_ID = "vendor_id"

        fun newInstance(source: String, categoryId: String, vendorId: String): DeviceFragment {
            return DeviceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SOURCE, source)
                    putString(ARG_CATEGORY_ID, categoryId)
                    putString(ARG_VENDOR_ID, vendorId)
                }
            }
        }
    }
}
