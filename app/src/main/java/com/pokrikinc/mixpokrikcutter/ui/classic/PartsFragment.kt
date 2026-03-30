package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.AppDataStore
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import kotlinx.coroutines.launch

class PartsFragment : Fragment() {
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
        val deviceId = requireArguments().getString(ARG_DEVICE_ID).orEmpty()
        val device = AppDataStore.findDevice(categoryId, vendorId, deviceId)

        (requireActivity() as MainActivity).setTitleAndBack(
            device?.name ?: getString(R.string.title_parts),
            true
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val progressView = view.findViewById<View>(R.id.progress_view)

        val adapter = PartListAdapter { item ->
            printPart(item.id)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        progressView.visibility = View.GONE

        val items = device?.partlist?.map { part ->
            PartListItem(
                id = part.attfile,
                title = part.name,
                subtitle = part.picfile,
                canPrint = true,
                imageUrl = AppDataStore.resolveImagePath(part.picfile)
            )
        }.orEmpty()

        adapter.submitList(items)
        emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun printPart(attFile: String) {
        val appContext = context?.applicationContext ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val printResult = PlotterPrintHelper.printPartByAttFile(appContext, attFile)
            if (!isAdded) return@launch

            val messageRes = if (printResult.isSuccess) {
                R.string.print_sent
            } else {
                R.string.print_failed
            }
            Toast.makeText(appContext, messageRes, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_VENDOR_ID = "vendor_id"
        private const val ARG_DEVICE_ID = "device_id"

        fun newInstance(categoryId: String, vendorId: String, deviceId: String): PartsFragment {
            return PartsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId)
                    putString(ARG_VENDOR_ID, vendorId)
                    putString(ARG_DEVICE_ID, deviceId)
                }
            }
        }
    }
}
