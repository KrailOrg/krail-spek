package uk.q3c.krail.core.shiro

import com.google.common.collect.ImmutableList
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.vaadin.server.VaadinSession
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import net.engio.mbassy.bus.common.PubSubSupport
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.realm.Realm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.mockito.InOrder
import org.mockito.Mockito
import uk.q3c.krail.core.eventbus.SessionBusProvider
import uk.q3c.krail.core.user.UserHasLoggedIn
import uk.q3c.krail.core.user.UserHasLoggedOut
import uk.q3c.krail.core.user.UserLoginFailed
import uk.q3c.krail.core.user.status.UserStatusChangeSource
import uk.q3c.krail.eventbus.BusMessage
import java.util.*
import java.util.concurrent.locks.Lock

/**
 * Created by David Sowerby on 07 Mar 2018
 */
object DefaultSubjectProviderTest : Spek({

    given("a SubjectProvider") {
        val realm = MockRealm()
        val securityManager = KrailSecurityManager(ImmutableList.of(realm) as Collection<Realm>?, Optional.empty())
        val eventBusProvider: SessionBusProvider = mock()
        val eventBus: PubSubSupport<BusMessage> = mock()
        whenever(eventBusProvider.get()).thenReturn(eventBus)
        val jwtProvider: JWTProvider<Jws<Claims>> = mock()
        val subjectProvider = DefaultSubjectProvider(securityManager, eventBusProvider, jwtProvider)
        val session: VaadinSession = mock()
        val lock: Lock = mock()
        val source: UserStatusChangeSource = mock()
        VaadinSession.setCurrent(session)
        whenever(session.lockInstance).thenReturn(lock)
        whenever(jwtProvider.encodeToJWT(any<Subject>())).thenReturn("wiggly")


        on("get when there is no subject in the session") {
            val subject = subjectProvider.get()

            it("returns an anonymous subject") {
                subject.principal.shouldBeNull()
            }

            it("does not attempt to retrieve token object") {
                verify(jwtProvider, never()).tokenAsObject(any())
            }

            it("does not store JWT in session, as user not authenticated") {
                verify(session, never()).setAttribute(any(), any())
            }

            it("returns a subject which is not authenticated") {
                subject.isAuthenticated.shouldBeFalse()
            }

        }

        on("get when there is a valid subject store in the session") {

            whenever(session.getAttribute(SUBJECT_ATTRIBUTE)).thenReturn("beastie")
            val tokenObject: Jws<Claims> = mock()
            whenever(jwtProvider.tokenAsObject("beastie")).thenReturn(tokenObject)
            val body: Claims = mock()
            whenever(tokenObject.body).thenReturn(body)
            whenever(body.subject).thenReturn("david")
            whenever(body.get("realmName")).thenReturn("default")
            val subject = subjectProvider.get()

            it("returns a Subject with correct principal") {
                subject.principal.shouldEqual("david")
            }

            it("returns a Subject as authenticated") {
                subject.isAuthenticated.shouldBeTrue()
            }


        }

        on("successful log in") {
            SecurityUtils.setSecurityManager(securityManager)
            val authenticationToken = UsernamePasswordToken("david", "password")
            val subject = subjectProvider.login(source, authenticationToken)
            it("sends a UserHasLoggedIn event") {
                verify(eventBus).publish(any<UserHasLoggedIn>())
            }

            it("stores a JWT in the session") {
                val orderVerifier: InOrder = Mockito.inOrder(lock, session, lock)
                orderVerifier.verify(lock).lock()
                orderVerifier.verify(session).setAttribute(SUBJECT_ATTRIBUTE, jwtProvider.encodeToJWT(subject))
                orderVerifier.verify(lock).unlock()
            }
        }

        on("unsuccessful login attempt") {
            reset(eventBus)
            val authenticationToken = UsernamePasswordToken("david", "rubbish")
            val subject = subjectProvider.login(source, authenticationToken)

            it("sends a UserLoginFailed event with identified cause") {

                argumentCaptor<BusMessage>().apply {
                    verify(eventBus).publish(capture())
                    (firstValue as UserLoginFailed).aggregateId.shouldBe("david")
                }
            }

            it("returns an anonymous subject") {
                subject.principal.shouldBe("david")
            }
        }

        on("logout") {
            reset(eventBus)
            subjectProvider.logout(source)

            it("sends a UserHasLoggedOut event") {
                verify(eventBus).publish(any<UserHasLoggedOut>())
            }
        }

    }
})

class MockRealm : AuthorizingRealm() {
    var account: SimpleAccount

    init {
        val principalCollection = SimplePrincipalCollection()
        principalCollection.add("david", "defaultRealm")

        account = SimpleAccount(principalCollection, "password")
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken?): AuthenticationInfo? {
        val up = token as UsernamePasswordToken
        if (up.password.contentEquals("password".toCharArray())) {
            return account
        } else {
            return null
        }
    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection?): AuthorizationInfo {
        return account
    }

}
