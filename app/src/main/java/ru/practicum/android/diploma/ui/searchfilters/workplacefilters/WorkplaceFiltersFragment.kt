package ru.practicum.android.diploma.ui.searchfilters.workplacefilters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.WorkplaceFragmentBinding
import ru.practicum.android.diploma.domain.models.filters.FilterParameters
import ru.practicum.android.diploma.domain.models.filters.SelectionType
import ru.practicum.android.diploma.presentation.workplacescreen.WorkplaceFiltersViewModel
import ru.practicum.android.diploma.util.renderFilterField
import ru.practicum.android.diploma.util.setupInputField

class WorkplaceFiltersFragment : Fragment() {

    private var _binding: WorkplaceFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<WorkplaceFiltersViewModel>()

    private var gray: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(SELECTION_RESULT_KEY) { _, bundle ->
            val type = SelectionType.from(bundle.getString(SELECTION_TYPE_KEY))
            val countryName = bundle.getString(COUNTRY_NAME_KEY)

            when (type) {
                SelectionType.COUNTRY -> {
                    val countryId = bundle.getString(COUNTRY_ID_KEY)
                    viewModel.setTempCountrySelection(countryId, countryName)
                }

                SelectionType.REGION -> {
                    val regionName = bundle.getString(REGION_NAME_KEY)
                    val regionId = bundle.getString(REGION_ID_KEY)
                    viewModel.setTempRegionSelection(regionName, regionId, countryName)
                }

                null -> Unit
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = WorkplaceFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gray = ContextCompat.getColor(requireContext(), R.color.gray)

        binding.arrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupInputField(
            binding.inputLayoutCountry,
            binding.editTextCountry,
            navigateAction = { openCountry() },
            clearAction = { viewModel.clearCountry() }
        )
        setupInputField(
            binding.inputLayoutRegion,
            binding.editTextRegion,
            navigateAction = {
                viewModel.getSelectedParams.observe(viewLifecycleOwner) { params ->
                    viewModel.getTempCountry.observe(viewLifecycleOwner) { tempCountry ->
                        openRegion(params?.countryId, tempCountry?.id)
                    }
                }
            },
            clearAction = { viewModel.clearRegion() }
        )

        binding.btnChoose.setOnClickListener {
            viewModel.saveSelection()
            findNavController().popBackStack()
        }

        viewModel.getTempCountry.observe(viewLifecycleOwner) { country ->
            viewModel.getTempRegion.observe(viewLifecycleOwner) { region ->
                viewModel.getSelectedParams.observe(viewLifecycleOwner) { params ->
                    updateCountryView(country?.name, region?.name, params)
                }
            }
        }

        viewModel.loadParameters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateCountryView(
        tempCountry: String?,
        tempRegion: String?,
        savedParams: FilterParameters
    ) {

        val countryName = tempCountry ?: savedParams?.countryName
        val regionName = tempRegion ?: savedParams?.regionName

        renderSelectedRegion(regionName)
        renderSelectedCountry(countryName)
        buttonChooseVisibility(countryName, regionName)
    }

    private fun renderSelectedCountry(name: String?) {
        binding.inputLayoutCountry.renderFilterField(
            requireContext(),
            name,
            R.string.country,
            gray
        )
    }

    private fun renderSelectedRegion(name: String?) {
        binding.inputLayoutRegion.renderFilterField(
            context = requireContext(),
            text = name,
            hintResId = R.string.region,
            grayColor = gray
        )
    }

    private fun buttonChooseVisibility(countryName: String?, regionName: String?) {
        binding.btnChoose.isVisible = !countryName.isNullOrBlank() || !regionName.isNullOrBlank()
    }

    private fun openCountry() {
        val action = WorkplaceFiltersFragmentDirections.actionWorkplaceFiltersFragmentToCountryFiltersFragment()
        findNavController().navigate(action)
        viewModel.clearRegion()
    }

    private fun openRegion(savedId: String?, tempId: String?) {
        val countryIdToUse = tempId ?: savedId ?: ""
        val action = WorkplaceFiltersFragmentDirections
            .actionWorkplaceFiltersFragmentToRegionsFilterFragment(countryIdToUse)
        findNavController().navigate(action)
    }

    companion object {
        const val SELECTION_TYPE_KEY = "selection_type"
        const val COUNTRY_NAME_KEY = "country_name"
        const val COUNTRY_ID_KEY = "country_id"
        const val REGION_NAME_KEY = "region_name"
        const val REGION_ID_KEY = "region_id"
        const val SELECTION_RESULT_KEY = "result_key"
    }
}
