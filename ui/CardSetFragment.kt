package com.mindcards.app.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindcards.app.MainActivity
import com.mindcards.app.R
import com.mindcards.app.adapters.CardSetAdapter
import com.mindcards.app.data.AppRepository
import com.mindcards.app.data.CardSet
import com.mindcards.app.data.CardSetParser
import com.mindcards.app.databinding.FragmentCardSetsBinding
import com.mindcards.app.util.CardReloadSignal
import com.mindcards.app.util.GameReloadSignal

class CardSetsFragment : Fragment() {

    private var _binding: FragmentCardSetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CardSetAdapter

    private val openDoc = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { importFile(it) } }

    private val reloadListener: () -> Unit = { refreshList() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardSetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CardSetAdapter(
            onSelect = { cs -> onSelectSet(cs) },
            onReset = { cs -> confirmReset(cs) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.addFab.setOnClickListener { openDoc.launch(arrayOf("*/*")) }
        CardReloadSignal.subscribe(reloadListener)
        refreshList()
    }

    override fun onDestroyView() {
        CardReloadSignal.unsubscribe(reloadListener)
        _binding = null
        super.onDestroyView()
    }

    private fun refreshList() {
        adapter.submit(AppRepository.cardSets.toList())
        binding.emptyState.visibility =
            if (AppRepository.cardSets.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun importFile(uri: Uri) {
        try {
            val text = requireContext().contentResolver.openInputStream(uri)
                ?.bufferedReader()?.use { it.readText() } ?: return
            val id = "user_" + System.currentTimeMillis()
            val parsed = CardSetParser.parse(text, "Imported Set", id)
            if (parsed == null) {
                Toast.makeText(requireContext(), R.string.import_failed, Toast.LENGTH_SHORT).show()
                return
            }
            AppRepository.addCardSet(parsed)
            Toast.makeText(requireContext(), R.string.import_success, Toast.LENGTH_SHORT).show()
            refreshList()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.import_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSelectSet(cs: CardSet) {
        val p = AppRepository.getProgress(cs.id)
        val progressComplete = p.answered >= cs.questions.size
        if (progressComplete) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.start_over_title)
                .setMessage(R.string.start_over_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    AppRepository.resetProgress(cs.id)
                    AppRepository.setCurrent(cs.id)
                    GameReloadSignal.fire()
                    (activity as? MainActivity)?.goToGameTab()
                }
                .setNegativeButton(R.string.no) { d, _ -> d.dismiss() }
                .show()
        } else {
            AppRepository.setCurrent(cs.id)
            GameReloadSignal.fire()
            (activity as? MainActivity)?.goToGameTab()
        }
    }

    private fun confirmReset(cs: CardSet) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.reset_progress)
            .setMessage(getString(R.string.reset_progress_confirm, cs.name))
            .setPositiveButton(R.string.yes) { _, _ ->
                AppRepository.resetProgress(cs.id)
                refreshList()
                if (AppRepository.currentCardSetId == cs.id) GameReloadSignal.fire()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
