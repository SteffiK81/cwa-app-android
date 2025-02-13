package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.CwaUserCardItemBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.LocalDate
import timber.log.Timber

class CwaUserCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<CwaUserCard.Item, CwaUserCardItemBinding>(
        layoutRes = R.layout.cwa_user_card_item,
        parent = parent
    ) {
    override val viewBinding: Lazy<CwaUserCardItemBinding> = lazy {
        CwaUserCardItemBinding.bind(itemView)
    }

    override val onBindData: CwaUserCardItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        val certificate = curItem.personCertificates.highestPriorityCertificate
        curItem.apply {
            userName.text = certificate?.fullName
            dateOfBirth.text = context.getString(
                R.string.person_details_cwa_user_birthdate,
                formatBirthDate(certificate?.dateOfBirthFormatted ?: "")
            )
            descriptionText.text = context.getString(
                R.string.person_details_cwa_user_description,
                certificate?.fullName
            )
            cwaUserSwitch.setOnCheckedChangeListener(null)
            cwaUserSwitch.isChecked = curItem.personCertificates.isCwaUser
            cwaUserSwitch.setOnCheckedChangeListener { _, isChecked -> onSwitch(isChecked) }
        }
    }

    private fun formatBirthDate(dateOfBirthFormatted: String): String =
        try {
            val delimiter = LocalDate().toShortDayFormat()
                .find { char -> !Character.isDigit(char) }
                .toString()
            when (delimiter) {
                "/" -> dateOfBirthFormatted.split("-").run {
                    listOfNotNull(getOrNull(1), getOrNull(2), getOrNull(0))
                }.joinToString(delimiter)
                else -> dateOfBirthFormatted.split("-").reversed().joinToString(delimiter)
            }
        } catch (e: Exception) {
            Timber.d(e, "Formatting to local format failed, falling back to $dateOfBirthFormatted")
            dateOfBirthFormatted
        }

    data class Item(
        val personCertificates: PersonCertificates,
        val onSwitch: (Boolean) -> Unit
    ) : CertificateItem, HasPayloadDiffer {

        override val stableId = Item::class.hashCode().toLong()
    }
}
