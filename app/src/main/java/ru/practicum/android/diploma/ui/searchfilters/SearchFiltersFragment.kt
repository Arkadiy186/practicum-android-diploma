package ru.practicum.android.diploma.ui.searchfilters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.SearchFiltersFragmentBinding
import ru.practicum.android.diploma.domain.models.filters.FilterParameters
import ru.practicum.android.diploma.presentation.SearchFiltersViewModel
import ru.practicum.android.diploma.util.getThemeColor
import ru.practicum.android.diploma.util.hideKeyboardOnDone
import ru.practicum.android.diploma.util.hideKeyboardOnIconClose
import ru.practicum.android.diploma.util.renderFilterField
import ru.practicum.android.diploma.util.setupInputField

class SearchFiltersFragment : Fragment() {

    private var _binding: SearchFiltersFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<SearchFiltersViewModel>()

    private var themeColor: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SearchFiltersFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = Bundle().apply {
            putBoolean(SEARCH_WITH_FILTERS_KEY, true)
        }
        themeColor = requireContext().getThemeColor(com.google.android.material.R.attr.colorOnContainer)

        setupInputField(
            binding.inputLayoutWorkplace,
            binding.editTextWorkplace,
            navigateAction = {
                findNavController().navigate(R.id.action_searchFiltersFragment_to_workplaceFiltersFragment) },
            clearAction = { viewModel.clearWorkplace() }
        )

        setupInputField(
            binding.inputLayoutIndustry,
            binding.editTextIndustry,
            navigateAction = {
                findNavController().navigate(R.id.action_searchFiltersFragment_to_industryFilterFragment) },
            clearAction = { viewModel.clearIndustry() }
        )

        binding.icon.setOnClickListener {
            binding.editText.setText("")
            binding.editText.clearFocus()
            binding.topHint.setTextColor(themeColor)
            viewModel.clearSalary()
            binding.editText.hideKeyboardOnIconClose(requireContext())
        }

        binding.editText.doOnTextChanged { text, start, before, count ->
            val query = text?.toString()?.trim().orEmpty()

            viewModel.saveSalary(query)
            binding.icon.isVisible = query.isNotEmpty()
            binding.btnApply.isVisible = query.isNotEmpty()
            binding.btnCancel.isVisible = query.isNotEmpty()
        }

        binding.editText.setOnFocusChangeListener { v, hasFocus ->
            val color = if (hasFocus) {
                ContextCompat.getColor(requireContext(), R.color.blue)
            } else {
                val currentText = binding.editText.text.toString()
                if (currentText.isEmpty()) {
                    themeColor
                } else {
                    ContextCompat.getColor(requireContext(), R.color.black)
                }
            }
            binding.topHint.setTextColor(color)
        }

        binding.editText.hideKeyboardOnDone(requireContext())

        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCancel.setOnClickListener {
            binding.editText.setText("")
            viewModel.resetFilters()
            setFragmentResult(SEARCH_WITH_FILTERS_KEY, bundle)
        }

        binding.btnApply.setOnClickListener {
            setFragmentResult(SEARCH_WITH_FILTERS_KEY, bundle)
            findNavController().popBackStack()
        }

        viewModel.getFiltersParametersScreen.observe(viewLifecycleOwner) {
            renderWorkplace(it)
        }

        viewModel.loadParameters()

        binding.materialCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.saveCheckBoxState(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderWorkplace(state: FilterParameters) {
        val country = state.countryName
        val region = state.regionName
        val industry = state.industryName
        val gray = ContextCompat.getColor(requireContext(), R.color.gray)

        val currentText = binding.editText.text.toString()
        val newSalary = state.salary.orEmpty()

        if (currentText != newSalary) {
            binding.editText.setText(newSalary)
        }

        binding.materialCheckbox.isChecked = state.checkboxWithoutSalary ?: false

        viewModel.getFiltersParametersScreen.observe(viewLifecycleOwner) { filters ->
            updateActionButtonVisibility(filters ?: FilterParameters())
        }

        val workplaceText = listOfNotNull(country, region)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        binding.inputLayoutWorkplace.renderFilterField(
            context = requireContext(),
            text = workplaceText,
            hintResId = R.string.workplace,
            grayColor = gray
        )

        binding.inputLayoutIndustry.renderFilterField(
            context = requireContext(),
            text = industry,
            hintResId = R.string.industry,
            grayColor = gray
        )

        val hasSalary = !state.salary.isNullOrBlank()
        val topHintColor = if (hasSalary) ContextCompat.getColor(requireContext(), R.color.black) else themeColor
        binding.topHint.setTextColor(topHintColor)
    }

    private fun updateActionButtonVisibility(filters: FilterParameters) {

        val isWorkplaceEmpty = filters.countryName.isNullOrBlank() && filters.regionName.isNullOrBlank()
        val isIndustryEmpty = filters.industryName.isNullOrBlank()
        val hasSalary = filters.salary.isNullOrBlank()
        val hasCheckbox = filters.checkboxWithoutSalary ?: false

        val hasAnyFilters = !isWorkplaceEmpty || !isIndustryEmpty || !hasSalary || hasCheckbox

        binding.btnApply.isVisible = hasAnyFilters
        binding.btnCancel.isVisible = hasAnyFilters
    }

    private fun showActionButtons() {
        binding.btnApply.isVisible = true
        binding.btnCancel.isVisible = true
    }

    companion object {
        const val SEARCH_WITH_FILTERS_KEY = "search_with_filters_key"
    }
}
