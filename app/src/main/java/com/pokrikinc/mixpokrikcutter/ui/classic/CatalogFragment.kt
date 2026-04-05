package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokrikinc.mixpokrikcutter.CatalogSource
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R

class CatalogFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recycler_state, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).setTitleAndBack(
            getString(R.string.title_catalog),
            false
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val emptyView = view.findViewById<TextView>(R.id.empty_view)
        val progressView = view.findViewById<View>(R.id.progress_view)

        val adapter = SimpleListAdapter { item ->
            (requireActivity() as MainActivity).openCatalogSection(item.id)
        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        progressView.visibility = View.GONE

        val sections = listOf(
            SimpleListItem(
                id = CatalogSource.BUILTIN,
                title = getString(R.string.title_catalog_builtin),
                subtitle = getString(R.string.catalog_builtin_subtitle),
                actionLabel = getString(R.string.action_open_section),
                imageUrl = "android.resource://${requireContext().packageName}/${R.drawable.ic_catalog_builtin}"
            ),
            SimpleListItem(
                id = CatalogSource.CUSTOM,
                title = getString(R.string.title_catalog_custom),
                subtitle = getString(R.string.catalog_custom_subtitle),
                actionLabel = getString(R.string.action_open_section),
                imageUrl = "android.resource://${requireContext().packageName}/${R.drawable.ic_catalog_custom}"
            )
        )

        adapter.submitList(sections)
        emptyView.visibility = if (sections.isEmpty()) View.VISIBLE else View.GONE
    }
}
