package de.rki.coronawarnapp.presencetracing.risk

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.Days
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTracingRiskRepository @Inject constructor(
    private val presenceTracingRiskCalculator: PresenceTracingRiskCalculator,
    private val databaseFactory: TraceLocationDatabase.Factory,
    private val timeStamper: TimeStamper
) {

    private val database by lazy {
        databaseFactory.create()
    }

    private val traceTimeIntervalMatchDao by lazy {
        database.traceTimeIntervalMatchDao()
    }

    private val normalizedTime = traceTimeIntervalMatchDao.allEntries().map {
        it.map {
            it.toModel()
        }
    }.map {
        presenceTracingRiskCalculator.calculateNormalizedTime(it)
    }

    val traceLocationCheckInRiskStates: Flow<List<TraceLocationCheckInRisk>> =
        normalizedTime.map {
            presenceTracingRiskCalculator.calculateCheckInRiskPerDay(it)
        }

    val presenceTracingDayRisk: Flow<List<PresenceTracingDayRisk>> =
        normalizedTime.map {
            presenceTracingRiskCalculator.calculateAggregatedRiskPerDay(it)
        }

    suspend fun replaceAllMatches(list: List<CheckInWarningOverlap>) {
        deleteAllMatches()
        addAll(list)
    }

    private suspend fun addAll(list: List<CheckInWarningOverlap>) {
        traceTimeIntervalMatchDao.insert(list.map { it.toEntity() })
    }

    suspend fun deleteStaleMatches() {
        val endTime = timeStamper.nowUTC.minus(Days.days(15).toStandardDuration())
        traceTimeIntervalMatchDao.deleteOlderThan(endTime.millis)
    }

    suspend fun deleteAllMatches() {
        traceTimeIntervalMatchDao.deleteAll()
    }
}

@Dao
interface TraceTimeIntervalMatchDao {

    @Query("SELECT * FROM TraceTimeIntervalMatchEntity")
    fun allEntries(): Flow<List<TraceTimeIntervalMatchEntity>>

    @Query("DELETE FROM TraceTimeIntervalMatchEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM TraceTimeIntervalMatchEntity WHERE endTimeMillis < :endTimeMillis")
    suspend fun deleteOlderThan(endTimeMillis: Long)

    @Insert
    suspend fun insert(entities: List<TraceTimeIntervalMatchEntity>)
}

@Entity
data class TraceTimeIntervalMatchEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long? = null,
    @ForeignKey(
        entity = TraceLocationCheckInEntity::class,
        parentColumns = ["id"],
        childColumns = ["checkInId"],
        onDelete = ForeignKey.CASCADE
    )
    @ColumnInfo(name = "checkInId") val checkInId: Long,
    @ColumnInfo(name = "traceWarningPackageId") val traceWarningPackageId: String,
    @ColumnInfo(name = "transmissionRiskLevel") val transmissionRiskLevel: Int,
    @ColumnInfo(name = "startTimeMillis") val startTimeMillis: Long,
    @ColumnInfo(name = "endTimeMillis") val endTimeMillis: Long
)

private fun CheckInWarningOverlap.toEntity() = TraceTimeIntervalMatchEntity(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTimeMillis = startTime.millis,
    endTimeMillis = endTime.millis
)

private fun TraceTimeIntervalMatchEntity.toModel() = CheckInWarningOverlap(
    checkInId = checkInId,
    traceWarningPackageId = traceWarningPackageId,
    transmissionRiskLevel = transmissionRiskLevel,
    startTime = Instant.ofEpochMilli(startTimeMillis),
    endTime = Instant.ofEpochMilli(endTimeMillis)
)
