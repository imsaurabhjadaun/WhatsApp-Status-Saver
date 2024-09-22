package com.savestatus.wsstatussaver.fragments.message

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.WhatSaveViewModel
import com.savestatus.wsstatussaver.adapter.CountryAdapter
import com.savestatus.wsstatussaver.databinding.DialogRecyclerviewBinding
import com.savestatus.wsstatussaver.databinding.FragmentMessageANumberBinding
import com.savestatus.wsstatussaver.extensions.encodedUrl
import com.savestatus.wsstatussaver.extensions.getPreferredClient
import com.savestatus.wsstatussaver.extensions.showToast
import com.savestatus.wsstatussaver.extensions.startActivitySafe
import com.savestatus.wsstatussaver.fragments.base.BaseFragment
import com.savestatus.wsstatussaver.interfaces.ICountryCallback
import com.savestatus.wsstatussaver.model.Country
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class MessageFragment : BaseFragment(R.layout.fragment_message_a_number), ICountryCallback {

    private var _binding: MessageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WhatSaveViewModel by activityViewModel()
    private val phoneNumberUtil: PhoneNumberUtil by inject()

    private var adapter: CountryAdapter? = null
    private var countriesDialog: Dialog? = null
    private var numberFormatTextWatcher: PhoneNumberFormattingTextWatcher? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = MessageBinding(FragmentMessageANumberBinding.bind(view))
        postponeEnterTransition()
        enterTransition = MaterialFadeThrough().addTarget(view)
        reenterTransition = MaterialFadeThrough().addTarget(view)
        view.doOnPreDraw { startPostponedEnterTransition() }

        createCountriesDialog()
        statusesActivity.setSupportActionBar(binding.toolbar)
        binding.phoneInputLayout.setEndIconOnClickListener {
            countriesDialog?.show()
        }
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        viewModel.getCountriesObservable().observe(viewLifecycleOwner) {
            adapter?.countries = it
        }
        viewModel.getSelectedCountryObservable().observe(viewLifecycleOwner) { country ->
            numberFormatTextWatcher?.let { textWatcher ->
                binding.phoneNumber.removeTextChangedListener(textWatcher)
            }
            numberFormatTextWatcher = PhoneNumberFormattingTextWatcher(country.isoCode).also { textWatcher ->
                binding.phoneNumber.addTextChangedListener(textWatcher)
            }
            binding.phoneInputLayout.prefixText = country.getId()
            adapter?.selectedCode = country.isoCode
        }
        viewModel.loadCountries()
        viewModel.loadSelectedCountry()
    }

    private fun createCountriesDialog() {
        adapter = CountryAdapter(requireContext(), viewModel.getCountries(), this)
        val binding = DialogRecyclerviewBinding.inflate(layoutInflater)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        countriesDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_a_country_title)
            .setView(binding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        menu.removeItem(R.id.action_settings)
    }

    override fun countryClick(country: Country) {
        viewModel.setSelectedCountry(country)
        countriesDialog?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sendMessage() {
        val entered = binding.phoneNumber.text?.toString()
        val country = viewModel.getSelectedCountry() ?: return
        val formattedNumber = formatInput(entered, country)
        if (formattedNumber == null) {
            showToast(R.string.phone_number_invalid)
            return
        }
        val encodedMessage = binding.message.text?.toString()?.encodedUrl()
        val apiRequest = StringBuilder("https://api.whatsapp.com/send?phone=")
        apiRequest.append(formattedNumber)
        if (!encodedMessage.isNullOrBlank()) {
            apiRequest.append("&text=").append(encodedMessage)
        }
        val intent = Intent(Intent.ACTION_VIEW, apiRequest.toString().toUri())
        val whatsappClient = requireContext().getPreferredClient()
        if (whatsappClient != null) {
            intent.setPackage(whatsappClient.packageName)
        }
        startActivitySafe(intent) { _: Throwable, activityNotFound: Boolean ->
            if (activityNotFound) showToast(R.string.wa_is_not_installed_title)
        }
        findNavController().popBackStack()
    }

    private fun formatInput(input: String?, country: Country): String? {
        val number = try {
            phoneNumberUtil.parse(input, country.isoCode)
        } catch (e: NumberParseException) {
            null
        }
        if (number == null || !phoneNumberUtil.isValidNumberForRegion(number, country.isoCode)) {
            return null
        }
        return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
    }
}