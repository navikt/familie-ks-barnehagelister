package no.nav.familie.ks.barnehagelister.repository

import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional
interface BarnehagelisteRepository :
    RepositoryInterface<Barnehageliste, UUID>,
    InsertUpdateRepository<Barnehageliste> {
    // language=PostgreSQL
}
