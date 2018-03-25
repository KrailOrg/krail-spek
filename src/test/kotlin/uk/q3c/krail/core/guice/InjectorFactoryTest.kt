package uk.q3c.krail.core.guice

import com.google.inject.AbstractModule
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBeInstanceOf
import org.apache.shiro.SecurityUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import uk.q3c.krail.core.shiro.KrailSecurityManager

/**
 * Created by David Sowerby on 19 Mar 2018
 */
object InjectorFactoryTest : Spek({


    given("an InjectorFactory") {
        val injectorFactory = InjectorFactory()

        on("creating the injector") {
            injectorFactory.createInjector(RuntimeEnvironment.SERVLET, TestBootstrapModule())

            it("creates and sets the SecurityManager") {
                SecurityUtils.getSecurityManager().shouldBeInstanceOf(KrailSecurityManager::class.java)
            }
        }

        on("complete these tests") {
            it("??") {
                TODO()
            }
        }
    }
})

class TestBootstrapModule : AbstractModule() {
    val mockBootstrapLoader: BootstrapLoader = mock()
    val collatorName = "uk.q3c.krail.core.guice.CoreBindingsCollator"


    override fun configure() {
        val bootstrapConfig = BootstrapConfig(EnvironmentConfig(collatorName), EnvironmentConfig(collatorName))
        whenever(mockBootstrapLoader.load()).thenReturn(bootstrapConfig)
        bind(BootstrapLoader::class.java).toInstance(mockBootstrapLoader)
        bind(InjectorLocator::class.java).to(ServletInjectorLocator::class.java)
    }

}

