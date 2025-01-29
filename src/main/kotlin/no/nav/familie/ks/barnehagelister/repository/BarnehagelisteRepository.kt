package no.nav.familie.ks.barnehagelister.repository

import no.nav.familie.ks.barnehagelister.domene.Barnehageliste
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional
interface BarnehagelisteRepository :
    RepositoryInterface<Barnehageliste, UUID>,
    InsertUpdateRepository<Barnehageliste> {
    // language=PostgreSQL
    @Query(value = "SELECT b FROM Barnehageliste WHERE b.id = :id")
    fun finnById(id: UUID): Barnehageliste
}
