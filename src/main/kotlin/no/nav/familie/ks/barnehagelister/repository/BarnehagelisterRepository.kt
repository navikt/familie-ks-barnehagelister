package no.nav.familie.ks.barnehagelister.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BarnehagelisterRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun lagre(
        uuid: UUID,
        jsontext: String,
    ) {
        val sql =
            """
            INSERT INTO barnehagelister(id, raw_json, status)
            VALUES (:id, to_json(:jsontext::json), :status)
            """.trimIndent()
        val parameters =
            MapSqlParameterSource()
                .addValue("id", uuid.toString())
                .addValue("jsontext", jsontext)
                .addValue("status", "MOTTATT")
        jdbcTemplate.update(sql, parameters)
    }

    fun hentStatus(id: UUID): String? {
        val sql =
            """
            SELECT status FROM barnehagelister WHERE id = :id
            """.trimIndent()
        val parameters =
            MapSqlParameterSource()
                .addValue("id", id.toString())
        return jdbcTemplate.queryForObject(sql, parameters, String::class.java)
    }
}
