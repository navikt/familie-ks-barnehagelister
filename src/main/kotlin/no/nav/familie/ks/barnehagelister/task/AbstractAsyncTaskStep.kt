package no.nav.familie.ks.barnehagelister.task

import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.domene.Task

/**
 * Abstract base class for AsyncTaskStep implementations.
 * Provides a default implementation of the doTask method with WithSpan annotation.
 */
abstract class AbstractAsyncTaskStep : AsyncTaskStep {
    /**
     * Implementation of the doTask method with WithSpan annotation for tracing.
     * This method should be overridden by subclasses to provide specific task execution logic.
     */
    @WithSpan
    abstract override fun doTask(task: Task)
}
