package no.nav.familie.ks.barnehagelister.repository

import no.nav.familie.ks.barnehagelister.domene.BarnehagelisteValideringsfeil
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional
interface BarnehagelisteValideringsfeilRepository :
    RepositoryInterface<BarnehagelisteValideringsfeil, UUID>,
    InsertUpdateRepository<BarnehagelisteValideringsfeil> {
    // language=PostgreSQL
}
