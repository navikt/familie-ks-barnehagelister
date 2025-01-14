package no.nav.familie.ks.barnehagelister.task

import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteService
import no.nav.familie.ks.barnehagelister.kafka.IBarnehagebarnKafkaProducer
import no.nav.familie.ks.barnehagelister.repository.BarnehagebarnRepository
import no.nav.familie.ks.barnehagelister.rest.dto.BarnehagelisteStatus
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendBarnehagebarnTilKsTask.TASK_STEP_TYPE,
    beskrivelse = "Legger barn på kø til KS",
    maxAntallFeil = 3,
    triggerTidVedFeilISekunder = 5,
)
class SendBarnehagebarnTilKsTask(
    private val barnehagebarnRepository: BarnehagebarnRepository,
    private val barnehagebarnKafkaProducer: IBarnehagebarnKafkaProducer,
    private val barnehagelisteService: BarnehagelisteService,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val barnehagebarnId = UUID.fromString(task.payload)
        logger.info("Sender barnehagebarn med id $barnehagebarnId til KS-sak")
        val barnehagebarn =
            barnehagebarnRepository.findByIdOrNull(barnehagebarnId)
                ?: error("Barnehagebarn med id $barnehagebarnId eksisterer ikke")

        val barnehageliste =
            barnehagelisteService.hentBarnehageliste(barnehagebarn.barnehagelisteId)
                ?: error("Fant ikke barnehageliste med id ${barnehagebarn.barnehagelisteId}")

        check(
            barnehageliste.status == BarnehagelisteStatus.FERDIG,
        ) { "Barnehageliste med id ${barnehageliste.id} er ikke ferdig prossesert" }

        barnehagebarnKafkaProducer.sendBarnehageBarn(barnehagebarn)
    }

    companion object {
        const val TASK_STEP_TYPE = "sendBarnehagebarnTask"
        private val logger: Logger = LoggerFactory.getLogger(SendBarnehagebarnTilKsTask::class.java)

        fun opprettTask(barnehagebarnId: UUID): Task =
            Task(
                type = TASK_STEP_TYPE,
                payload = barnehagebarnId.toString(),
            )
    }
}
