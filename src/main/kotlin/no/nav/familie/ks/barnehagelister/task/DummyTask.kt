package no.nav.familie.ks.barnehagelister.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = DummyTask.TASK_STEP_TYPE,
    beskrivelse = "Patcher ident for identer som er merget",
    maxAntallFeil = 1,
    settTilManuellOppfølgning = true,
)
class DummyTask : AsyncTaskStep {
    override fun doTask(task: Task) {
        println(task.payload)
    }

    companion object {
        const val TASK_STEP_TYPE = "dummy"

        fun opprettTask(payload: String) = Task(TASK_STEP_TYPE, payload)
    }
}
