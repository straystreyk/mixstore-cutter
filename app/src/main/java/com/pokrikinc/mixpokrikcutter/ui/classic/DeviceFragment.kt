package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.AppDataStore
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R

class DeviceFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recycler_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        val adapter = SimpleListAdapter { item ->
            (requireActivity() as MainActivity).openParts(categoryId, vendorId, item.id)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        progressView.visibility = View.GONE

        val items = vendor?.devices?.map { device ->
            SimpleListItem(
                id = device.id,
                title = device.name,
                subtitle = "${device.partlist.size} деталей",
                actionLabel = getString(R.string.action_open_parts)
            )
        }.orEmpty()

        adapter.submitList(items)
        emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_VENDOR_ID = "vendor_id"

        fun newInstance(categoryId: String, vendorId: String): DeviceFragment {
            return DeviceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId)
                    putString(ARG_VENDOR_ID, vendorId)
                }
            }
        }
    }
}
