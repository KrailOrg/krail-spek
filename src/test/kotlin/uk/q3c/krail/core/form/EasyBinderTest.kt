package uk.q3c.krail.core.form

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.nhaarman.mockito_kotlin.whenever
import com.vaadin.ui.TextField
import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import uk.q3c.krail.core.validation.KrailInterpolator
import uk.q3c.krail.core.validation.KrailValidationModule
import javax.validation.constraints.Max
import javax.validation.constraints.Min


const val interpolatorMessage = "message from Krail interpolator"

/**
 * Created by David Sowerby on 11 Feb 2018
 */
class AutoBind : Spek({
    lateinit var easyBinder: EasyBinder

    context("we want to ensure that the KrailMessageInterpolator is used during validation") {
        beforeGroup {
            val injector = Guice.createInjector(KrailValidationModule(), LocalTestModule())
            easyBinder = injector.getInstance(EasyBinder::class.java)
        }

        on("creating an auto binder") {
            val binder = easyBinder.auto(KotlinWiggly::class.java)

            it("is instance of KrailAutoBinder") {
                binder.shouldBeInstanceOf(KrailAutoBinder::class.java)
            }
        }

        on("invoking the build") {
            val binder = easyBinder.auto(KotlinWiggly::class.java)
            val components = binder.buildAndBind()

            it("gives us the correct number of components") {
                components.size.shouldBe(2)
            }
        }

        on("setting a bean with an invalid field") {
            val binder = easyBinder.auto(KotlinWiggly::class.java)
            val components = binder.buildAndBind()
            binder.bean = KotlinWiggly("Who me", 12)

            it("should have a message from the KrailInterpolator, in the failed component ") {
                (components[1] as TextField).componentError.toString().shouldContain(interpolatorMessage)
            }
        }

    }

})

class KotlinWiggly(@Min(3)
                   var name: String, @get:Max(5) var age: Int)

class LocalTestModule : AbstractModule() {
    val interpolator: KrailInterpolator = mock()

    init {
        whenever(interpolator.interpolate(any(), any())).thenReturn(interpolatorMessage)
        whenever(interpolator.interpolate(any(), any(), any())).thenReturn(interpolatorMessage)
    }


    override fun configure() {
        bind(KrailInterpolator::class.java).toInstance(interpolator)
    }

}
