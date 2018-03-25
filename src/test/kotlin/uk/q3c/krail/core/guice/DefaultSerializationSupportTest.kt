package uk.q3c.krail.core.guice

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.name.Named
import com.google.inject.name.Names
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.amshove.kluent.shouldThrow
import org.apache.commons.lang3.SerializationUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.mockito.Mockito
import uk.q3c.krail.core.view.ViewBase
import uk.q3c.krail.core.view.component.ViewChangeBusMessage
import uk.q3c.krail.i18n.Translate
import java.io.Serializable

/**
 * Created by David Sowerby on 17 Mar 2018
 */
object DefaultSerializationSupportTest : Spek({

    given("An injector has been set up") {

        val locator = ServletInjectorLocator()
        locator.put(Guice.createInjector(TestModule(), Dummy1Module()))


        given("an instance that needs no guice injections") {
            val testViewClass = ClassWithNoGuiceInjections::class.java

            on("deserialization") {
                val testView = locator.get().getInstance(testViewClass)
                val output = SerializationUtils.serialize(testView)

                it(" passes its check") {
                    val result = SerializationUtils.deserialize<ClassWithNoGuiceInjections>(output)
                    result.translate.shouldNotBeNull()
                    isMock(result.translate).shouldBeTrue()
                    result.serializationSupport.shouldNotBeNull()
                    result.serializationSupport.shouldBeInstanceOf(DefaultSerializationSupport::class.java)

                }
            }
        }

        given("an instance which has two fields that need injection") {
            val testViewClass = ClassWithTwoGuiceInjections::class.java
            on("deserialization") {
                val testView = locator.get().getInstance(testViewClass)
                val output = SerializationUtils.serialize(testView)

                it("populates both fields") {
                    val result = SerializationUtils.deserialize<ClassWithTwoGuiceInjections>(output)
                    result.translate.shouldNotBeNull()
                    isMock(result.translate).shouldBeTrue()
                    result.serializationSupport.shouldNotBeNull()
                    result.serializationSupport.shouldBeInstanceOf(DefaultSerializationSupport::class.java)
                    result.dummy1.shouldBeInstanceOf(Dummy1::class)
                    result.dummy2.shouldBeInstanceOf(Dummy2::class)
                }

            }
        }


        given("an instance which has two fields of the same type, that need injection, fields annotated correctly") {
            val testViewClass = ClassWithTwoAnnotatedGuiceInjections::class.java
            on("deserialization") {
                val testView = locator.get().getInstance(testViewClass)
                val output = SerializationUtils.serialize(testView)

                it("populates both fields") {
                    val result = SerializationUtils.deserialize<ClassWithTwoAnnotatedGuiceInjections>(output)
                    result.translate.shouldNotBeNull()
                    isMock(result.translate).shouldBeTrue()
                    result.serializationSupport.shouldNotBeNull()
                    result.serializationSupport.shouldBeInstanceOf(DefaultSerializationSupport::class.java)
                    result.dummy1.age.shouldEqual(23)
                    result.dummy2.age.shouldEqual(99)
                }


            }
        }

        given("an instance which has two fields of the same type, that need injection, one field missing annotation") {
            val testViewClass = ClassWithMissingGuiceFieldAnnotation::class.java
            on("deserialization") {
                val testView = locator.get().getInstance(testViewClass)
                val output = SerializationUtils.serialize(testView)
                it(" fails its check") {
                    val result = { SerializationUtils.deserialize<ClassWithMissingGuiceFieldAnnotation>(output) }
                    result.shouldThrow(SerializationSupportException::class)
                }
            }
        }

        given("an instance which has two fields of the same type, that need injection, one field missing annotation.  Missing field is excluded") {
            on("deserialization") {

                it("passes its check") {
                    TODO()
                }
            }
        }


        given("an instance which has two fields of the same type, that need injection, fields annotated correctly, but one filled by user code before injection and one after injection") {
            on("deserialization") {

                it(" passes its check") {
                    //                    support.checkForNullTransients()
                }

                it("fields are filled by user code action, not injection") {
                    TODO()
                }
            }
        }

    }
})

fun isMock(obj: Any): Boolean {
    return obj.javaClass.name.contains("\$\$EnhancerByMockito")
}

val translate: Translate = Mockito.mock(Translate::class.java)

class TestModule : AbstractModule() {

    override fun configure() {
        bind(Translate::class.java).toInstance(translate)
        bind(SerializationSupport::class.java).to(DefaultSerializationSupport::class.java)
        bind(InjectorLocator::class.java).to(ServletInjectorLocator::class.java)
    }

}

class Dummy1Module : AbstractModule() {
    val dummyA = Dummy1()
    val dummyB = Dummy1()

    override fun configure() {
        dummyA.age = 23
        dummyB.age = 99
        bind(Dummy1::class.java).annotatedWith(Names.named("1")).toInstance(dummyA)
        bind(Dummy1::class.java).annotatedWith(Names.named("2")).toInstance(dummyB)
    }

}

class ClassWithNoGuiceInjections @Inject constructor(translate: Translate, serializationSupport: SerializationSupport) : Serializable, ViewBase(translate, serializationSupport) {

    override fun doBuild(busMessage: ViewChangeBusMessage?) {

    }

}


class ClassWithTwoGuiceInjections @Inject constructor(translate: Translate, serializationSupport: SerializationSupport, @Transient val dummy1: Dummy1, @Transient val dummy2: Dummy2) : Serializable, ViewBase(translate, serializationSupport) {

    override fun doBuild(busMessage: ViewChangeBusMessage?) {

    }
}

class ClassWithTwoAnnotatedGuiceInjections @Inject constructor(
        translate: Translate,
        serializationSupport: SerializationSupport,
        @field:Named("1") @param:Named("1") @Transient val dummy1: Dummy1,
        @field:Named("2") @param:Named("2") @Transient val dummy2: Dummy1) : Serializable, ViewBase(translate, serializationSupport) {

    override fun doBuild(busMessage: ViewChangeBusMessage?) {

    }
}


class ClassWithMissingGuiceFieldAnnotation @Inject constructor(
        translate: Translate,
        serializationSupport: SerializationSupport,
        @field:Named("1") @param:Named("1") @Transient val dummy1: Dummy1,
        @Named("2") @Transient val dummy2: Dummy1) : Serializable, ViewBase(translate, serializationSupport) {

    override fun doBuild(busMessage: ViewChangeBusMessage?) {

    }
}


class Dummy1 @Inject constructor() {
    var age: Int = 0
}

class Dummy2