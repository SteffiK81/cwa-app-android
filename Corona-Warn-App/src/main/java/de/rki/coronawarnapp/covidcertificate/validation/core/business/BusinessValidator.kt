package de.rki.coronawarnapp.covidcertificate.validation.core.business

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.CertLogicEngineWrapper
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.filterRelevantRules
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.typeString
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import kotlinx.coroutines.flow.first
import org.joda.time.LocalDateTime
import timber.log.Timber
import javax.inject.Inject

@Reusable
class BusinessValidator @Inject constructor(
    private val certLogicEngineWrapper: CertLogicEngineWrapper,
    private val ruleRepository: DccValidationRepository,
) {
    suspend fun validate(
        arrivalCountry: DccCountry,
        validationDateTime: LocalDateTime,
        certificate: DccData<out DccV1.MetaData>,
    ): BusinessValidation {

        Timber.i("Start CertLogic validation for arrival in ${arrivalCountry.countryCode} on $validationDateTime.")
        Timber.i("${certificate.typeString} certificate of ${certificate.certificate.nameData.fullName}.")
        Timber.i("Certificate was issued by ${certificate.header.issuer} at ${certificate.header.issuedAt}.")

        // accepted by arrival country
        Timber.i("Validating acceptance rules of ${arrivalCountry.countryCode} on $validationDateTime.")
        val acceptanceResults = certLogicEngineWrapper.process(
            rules = ruleRepository.acceptanceRules.first().filterRelevantRules(
                validationDateTime = validationDateTime,
                certificateType = certificate.typeString,
                country = arrivalCountry,
            ),
            validationDateTime = validationDateTime,
            certificate = certificate,
            countryCode = arrivalCountry.countryCode,
        )

        // valid as defined by the issuing country
        Timber.i("Validating invalidation rules of ${arrivalCountry.countryCode} on $validationDateTime.")
        val issuerCountry = DccCountry(certificate.header.issuer)
        val invalidationResults = certLogicEngineWrapper.process(
            rules = ruleRepository.invalidationRules.first().filterRelevantRules(
                validationDateTime = validationDateTime,
                certificateType = certificate.typeString,
                country = issuerCountry,
            ),
            validationDateTime = validationDateTime,
            certificate = certificate,
            countryCode = issuerCountry.countryCode,
        )

        return BusinessValidation(
            acceptanceRules = acceptanceResults,
            invalidationRules = invalidationResults
        )
    }
}
