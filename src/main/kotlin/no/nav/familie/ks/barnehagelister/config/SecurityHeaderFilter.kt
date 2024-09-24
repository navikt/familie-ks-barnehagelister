package no.nav.familie.ks.barnehagelister.config

import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.IOException

class SecurityHeaderFilter : HttpFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilter(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        httpServletResponse.setHeader("Cache-Control", "must-revalidate,no-cache,no-store")
        httpServletResponse.setHeader("X-Content-Type-Options", "nosniff")
        if (httpServletResponse.getHeader("Set-Cookie") != null) {
            httpServletResponse.setHeader("Set-Cookie", "${httpServletResponse.getHeader("Set-Cookie")};SameSite=strict")
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse)
    }

    private fun resolveUserId(httpServletRequest: HttpServletRequest): Cookie =
        httpServletRequest.cookies?.firstOrNull { it -> "RUIDC" == it.name }!!

    override fun init(filterConfig: FilterConfig) {
        // NOP
    }

    override fun destroy() {
        // NOP
    }

    companion object
}
