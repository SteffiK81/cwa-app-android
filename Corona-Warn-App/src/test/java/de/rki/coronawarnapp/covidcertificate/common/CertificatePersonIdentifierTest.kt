package de.rki.coronawarnapp.covidcertificate.common

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.DOB_MISMATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NAME_MISMATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CertificatePersonIdentifierTest : BaseTest() {
    private val testPersonMaxData = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1966-11-11",
        firstNameStandardized = "ANDREAS",
        lastNameStandardized = "ASTRA<EINS"
    )

    private val testPersonMaxDataFuzzy = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1966-11-11",
        firstNameStandardized = "ANDREAS  ",
        lastNameStandardized = " ASTRA<<EINS "
    )

    private val testPersonMin = CertificatePersonIdentifier(
        dateOfBirthFormatted = "1900-01-01",
        lastNameStandardized = "#",
        firstNameStandardized = null
    )

    @Test
    fun `internal identifier code`() {
        testPersonMaxData.groupingKey shouldBe "1966-11-11#ASTRA<EINS#ANDREAS"
        testPersonMin.groupingKey shouldBe "1900-01-01###null"
    }

    @Test
    fun `external identifier code sha256`() {
        testPersonMaxData.codeSHA256 shouldBe "9d8927b04b57074dd8edc77fd4d9b9232112a2dc99d54892405ea6b0e5fce22e"
        testPersonMin.codeSHA256 shouldBe "2219f43f265f9c0b1e36feeba39d52c401663cfbe7f4a6566be55a86b9384fc4"
    }

    @Test
    fun `person equality`() {
        val person1 = testPersonMaxData
        val person2 = CertificatePersonIdentifier(
            dateOfBirthFormatted = "1966-11-11",
            firstNameStandardized = "ANDREAS",
            lastNameStandardized = "ASTRA<EINS"
        )

        person1 shouldBe person2
        person1.groupingKey shouldBe person2.groupingKey
        person1.codeSHA256 shouldBe person2.codeSHA256
    }

    @Test
    fun `required matching`() {
        shouldNotThrowAny {
            testPersonMaxData.requireMatch(testPersonMaxData)
        }

        shouldThrow<InvalidVaccinationCertificateException> {
            testPersonMaxData.requireMatch(testPersonMaxData.copy(firstNameStandardized = "nope"))
        }.errorCode shouldBe NAME_MISMATCH

        shouldThrow<InvalidVaccinationCertificateException> {
            testPersonMaxData.requireMatch(testPersonMaxData.copy(lastNameStandardized = "nope"))
        }.errorCode shouldBe NAME_MISMATCH

        shouldThrow<InvalidVaccinationCertificateException> {
            testPersonMaxData.requireMatch(testPersonMaxData.copy(dateOfBirthFormatted = "1900-12-31"))
        }.errorCode shouldBe DOB_MISMATCH
    }

    @Test
    fun `all fields of person identifier are missing`() {
        CertificatePersonIdentifier(
            dateOfBirthFormatted = "",
            firstNameStandardized = "",
            lastNameStandardized = ""
        ).groupingKey shouldBe "##"

        CertificatePersonIdentifier(
            dateOfBirthFormatted = "",
            lastNameStandardized = "",
            firstNameStandardized = null
        ).groupingKey shouldBe "##null"
    }

    @Test
    fun `groupingkey should match`() {
        testPersonMaxData.groupingKey shouldBe testPersonMaxDataFuzzy.groupingKey
    }
}
