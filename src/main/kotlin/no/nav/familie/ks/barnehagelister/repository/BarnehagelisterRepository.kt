package no.nav.familie.ks.barnehagelister.repository

import no.nav.familie.ks.barnehagelister.domene.Barnehagelister
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional
interface BarnehagelisterRepository :
    RepositoryInterface<Barnehagelister, UUID>,
    InsertUpdateRepository<Barnehagelister>
{
    // language=PostgreSQL

}
