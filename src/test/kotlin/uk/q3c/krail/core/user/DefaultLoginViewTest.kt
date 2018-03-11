package uk.q3c.krail.core.user

import com.google.common.collect.ImmutableSet
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.vaadin.event.ListenerMethod
import net.engio.mbassy.bus.common.PubSubSupport
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldThrow
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.subject.Subject
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import uk.q3c.krail.core.eventbus.SessionBusProvider
import uk.q3c.krail.core.i18n.LabelKey
import uk.q3c.krail.core.shiro.SubjectProvider
import uk.q3c.krail.core.view.component.ViewChangeBusMessage
import uk.q3c.krail.eventbus.BusMessage
import uk.q3c.krail.i18n.Translate
import uk.q3c.krail.testutil.view.ViewFieldChecker

/**
 * Created by David Sowerby on 10 Mar 2018
 */
object DefaultLoginViewTest : Spek({


    given("a DefaultLoginView") {
        val event: ViewChangeBusMessage = mock()
        lateinit var view: DefaultLoginView
        lateinit var subject: Subject
        lateinit var sessionBusProvider: SessionBusProvider
        lateinit var eventBus: PubSubSupport<BusMessage>
        lateinit var subjectProvider: SubjectProvider
        lateinit var translate: Translate



        beforeEachTest {
            subject = mock()
            subjectProvider = mock()
            whenever(subjectProvider.get()).thenReturn(subject)
            sessionBusProvider = mock()
            eventBus = mock()
            whenever(sessionBusProvider.get()).thenReturn(eventBus)
            translate = mock()
            whenever(translate.from(eq(LabelKey.Active_Source))).thenReturn("Active Source")
            whenever(translate.from(eq(LoginDescriptionKey.Account_Locked))).thenReturn("Your account is locked")
            view = DefaultLoginView(subjectProvider, translate)
        }

        on("checking getters and setters") {

            view.buildView(event)
            view.username("b").password("a").setStatusMessage(LabelKey.Active_Source)

            it("contains the correct input values") {
                view.username.value.shouldEqual("b")
                view.password.value.shouldEqual("a")
            }

            it("has constructed the submit button") {
                view.submit.shouldNotBeNull()
            }

            it("displays the correct status message") {
                view.statusMessage.value.shouldEqual("Active Source")
            }
        }

        on("attempting to submit with empty username") {
            view.buildView(event)
            view.password("a")
            val verify = { view.submit.click() }
            it("throws an exception") {
                verify.shouldThrow(ListenerMethod.MethodException::class)
            }
        }

        on("attempting to submit with empty password") {
            view.buildView(event)
            view.username("b")
            val verify = { view.submit.click() }
            it("throws an exception") {
                verify.shouldThrow(ListenerMethod.MethodException::class)
            }
        }

        on("receiving a LoginFailed event") {
            view.buildView(event)
            view.handleLoginFailed(UserLoginFailed(aggregateId = "david", label = LoginLabelKey.Account_Locked, description = LoginDescriptionKey.Account_Locked))

            it("shows the correct message") {
                view.statusMessage.value.shouldEqual("Your account is locked")
            }
        }

        on("checking that I18N annotations have been set") {
            val fieldsWithoutCaptions = ImmutableSet.of("label", "statusMessage")
            val viewFieldChecker = ViewFieldChecker(view, fieldsWithoutCaptions, ImmutableSet.of())
            view.buildView(event)

            it("should be successful") {
                viewFieldChecker.check().shouldBeTrue()
            }
        }

        on("submitting credentials") {
            view.buildView(event)
            view.username("a").password("password")
            view.submit.click()

            it("calls SubjectProvider to login") {
                verify(subjectProvider).login(eq(view), any<UsernamePasswordToken>())
            }
        }

    }


})