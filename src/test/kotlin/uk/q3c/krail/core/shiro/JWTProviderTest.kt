package uk.q3c.krail.core.shiro

import com.nhaarman.mockito_kotlin.whenever
import io.jsonwebtoken.MalformedJwtException
import org.amshove.kluent.mock
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.Subject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import uk.q3c.krail.core.user.DefaultUserObjectProvider


/**
 * Created by David Sowerby on 07 Mar 2018
 */
object DefaultJWTProviderTest : Spek({
    val headlessToken = "eyJzdWIiOiJkYXZpZCJ9.hUsNuHefSBZw_BvB8Ht7NzxtxFf0xuSx76jMqcxPx2TZP5iDKbPSBDhiDmGsPNcIlEOi5-Gi7MNSTGnvBClC6g"
    val tokenWithHeader = "eyJhbGciOiJIUzUxMiJ9.$headlessToken"


    given("a JWT Provider") {
        val jwtKeyProvider = DefaultJWTKeyProvider()
        val provider = DefaultJWTProvider(jwtKeyProvider, DefaultUserObjectProvider())
        val validSubject: Subject = mock()
        val principals: PrincipalCollection = mock()
        whenever(validSubject.principals).thenReturn(principals)
        whenever(principals.primaryPrincipal).thenReturn("david")

        on("requesting a token from a valid Subject") {
            val token = provider.encodeToJWT(validSubject)
            println(token)
            val tokenSegments = token.count { c -> c == ".".toCharArray()[0] }

            it("should create a headless token") {
                tokenSegments.shouldEqual(1)
                token.shouldEqual(headlessToken)
            }
        }

        on("verifying a correct headless token, header is correctly replaced") {
            provider.verify(headlessToken)

            it("does not throw exception") { }
        }

        on("verifying a token which already has a header, fails") {


            it("fails") {
                val verify = { provider.verify(tokenWithHeader) }
                verify.shouldThrow(MalformedJwtException::class)
            }
        }
    }

})