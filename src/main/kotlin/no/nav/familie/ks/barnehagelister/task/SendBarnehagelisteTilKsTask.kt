package no.nav.familie.ks.barnehagelister.task

import no.nav.familie.ks.barnehagelister.domene.mapTilBarnehageBarnKS
import no.nav.familie.ks.barnehagelister.kafka.BarnehagebarnKafkaProducer
import no.nav.familie.ks.barnehagelister.repository.BarnehagelisterRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendBarnehagelisteTilKsTask.TASK_STEP_TYPE,
    beskrivelse = "Mapper om barnehagelister og sender hvert barn på kø til Ks.",
    maxAntallFeil = 3,
    triggerTidVedFeilISekunder = 5,
)
class SendBarnehagelisteTilKsTask(
    private val barnehagelisterRepository: BarnehagelisterRepository,
    private val barnehagebarnKafkaProducer: BarnehagebarnKafkaProducer,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        logger.info("Sender barnehageliste til KS-sak")
        val barnehagelisteId = UUID.fromString(task.payload)

        val barnehageliste =
            barnehagelisterRepository.findByIdOrNull(barnehagelisteId)
                ?: error("Fant ikke barnehageliste med id $barnehagelisteId")

        check(barnehageliste.status == BarnehagelisteStatus.FERDIG) { "Barnehageliste med id $barnehagelisteId er ikke ferdig prossesert" }

        val barnehageBarnKs = barnehageliste.rawJson.mapTilBarnehageBarnKS()

        barnehageBarnKs?.forEach {
            barnehagebarnKafkaProducer.sendBarnehageBarn(it)
        }
    }

    companion object {
        const val TASK_STEP_TYPE = "sendBarnehagelisteTask"
        private val logger: Logger = LoggerFactory.getLogger(SendBarnehagelisteTilKsTask::class.java)

        fun opprettTask(barnehagelisteId: UUID): Task =
            Task(
                type = TASK_STEP_TYPE,
                payload = barnehagelisteId.toString(),
                properties =
                    Properties().apply {
                        this["barnehagelisteId"] = barnehagelisteId.toString()
                    },
            )
    }
}
