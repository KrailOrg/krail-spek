package uk.q3c.krail.core.guice

import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import uk.q3c.util.testutil.TestResource


/**
 * Created by David Sowerby on 18 Mar 2018
 */
object BootstrapConfigTest : Spek({

    given("a BootStrapConfig") {
        val resourceReference = KrailVertxBootstrapModule()
        lateinit var bootstrapConfig: BootstrapConfig

        on("loading a good file") {
            val sourceFile = TestResource.resource(resourceReference, "krail-bootstrap1.yml")
            val source = sourceFile?.readText()
            if (source != null) {
                bootstrapConfig = BootstrapYAMLReader().read(source)
            }

            it("correctly sets up config object") {
                with(bootstrapConfig) {
                    servletConfig.collatorClassName.shouldBeEqualTo("uk.q3c.krail.core.guice.CoreBindingsCollator")
                    vertxConfig.collatorClassName.shouldBeEqualTo("uk.q3c.krail.core.guice.CoreBindingsCollator")
                    servletConfig.additionalModules.shouldBeEmpty()
                    vertxConfig.additionalModules.shouldContain("uk.q3c.krail.core.vaadin.DataModule")
                }
            }
        }

        on("loading a bootstrap file with vertx missing") {
            val sourceFile = TestResource.resource(resourceReference, "krail-bootstrap2.yml")
            val source = sourceFile!!.readText()
            val expects = { BootstrapYAMLReader().read(source) }

            it("throws an exception") {
                expects.shouldThrow(EnvironmentConfigurationException::class)
            }
        }

        on("loading a bootstrap file with servlet missing") {
            val sourceFile = TestResource.resource(resourceReference, "krail-bootstrap3.yml")
            val source = sourceFile!!.readText()
            val expects = { BootstrapYAMLReader().read(source) }

            it("throws an exception") {
                expects.shouldThrow(EnvironmentConfigurationException::class)
            }
        }
    }
})