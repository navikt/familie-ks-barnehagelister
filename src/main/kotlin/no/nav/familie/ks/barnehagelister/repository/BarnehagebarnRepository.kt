package no.nav.familie.ks.barnehagelister.repository

import no.nav.familie.ks.barnehagelister.kafka.Barnehagebarn
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional
interface BarnehagebarnRepository :
    RepositoryInterface<Barnehagebarn, UUID>,
    InsertUpdateRepository<Barnehagebarn> {
    // language=PostgreSQL
}
