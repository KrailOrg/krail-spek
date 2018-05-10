package uk.q3c.krail.core.navigate.sitemap

import com.google.inject.Injector
import com.google.inject.Module
import io.mockk.mockk
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import uk.q3c.krail.config.ApplicationConfiguration
import uk.q3c.krail.core.config.KrailApplicationConfigurationModule
import uk.q3c.krail.core.eventbus.VaadinEventBusModule
import uk.q3c.krail.core.guice.ServletEnvironmentModule
import uk.q3c.krail.core.guice.uiscope.UIScopeModule
import uk.q3c.krail.core.guice.vsscope.VaadinSessionScopeModule
import uk.q3c.krail.core.i18n.LabelKey
import uk.q3c.krail.core.navigate.NavigationModule
import uk.q3c.krail.core.push.PushModule
import uk.q3c.krail.core.shiro.DefaultShiroModule
import uk.q3c.krail.core.shiro.PageAccessControl
import uk.q3c.krail.core.shiro.ShiroVaadinModule
import uk.q3c.krail.core.ui.DefaultUIModule
import uk.q3c.krail.core.user.UserModule
import uk.q3c.krail.core.vaadin.MockVaadinSession
import uk.q3c.krail.core.vaadin.createMockVaadinSession
import uk.q3c.krail.core.view.PublicHomeView
import uk.q3c.krail.core.view.ViewModule
import uk.q3c.krail.core.view.component.DefaultComponentModule
import uk.q3c.krail.eventbus.mbassador.EventBusModule
import uk.q3c.krail.option.mock.TestOptionModule
import uk.q3c.krail.persist.inmemory.InMemoryModule
import uk.q3c.krail.util.DefaultResourceUtils
import uk.q3c.krail.util.ResourceUtils
import uk.q3c.krail.util.UtilsModule
import uk.q3c.util.UtilModule
import uk.q3c.util.guice.SerializationSupportModule
import java.io.File

/**
 * Created by David Sowerby on 10 May 2018
 */
object DefaultSitemapServiceTest : Spek({

    given("A Guice built sitemap service") {
        val resourceUtils: ResourceUtils = DefaultResourceUtils()
        lateinit var mockVaadinSession: MockVaadinSession
        lateinit var injector: Injector
        lateinit var config: ApplicationConfiguration

        beforeEachTest {
            mockVaadinSession = createMockVaadinSession()
            config = mockk()
            val inifile = File(resourceUtils.userTempDirectory(), "WEB-INF/krail.ini")
        }
        afterEachTest {
            mockVaadinSession.clear()
        }
    }

})

private fun guiceModules(): List<Module> {
    return listOf(TestDirectSitemapModule::class.java, UIScopeModule::class.java, ViewModule::class.java, VaadinEventBusModule::class.java,
            ShiroVaadinModule::class.java, TestKrailI18NModule::class.java, SitemapModule::class.java, UserModule::class.java, KrailApplicationConfigurationModule::class.java, DefaultShiroModule::class.java,
            DefaultComponentModule::class.java, InMemoryModule::class.java, StandardPagesModule::class.java, VaadinSessionScopeModule::class.java,
            NavigationModule::class.java, ServletEnvironmentModule::class.java, SerializationSupportModule::class.java, PushModule::class.java, EventBusModule::class.java, UtilsModule::class.java, UtilModule::class.java, DefaultUIModule::class.java, TestOptionModule::class.java)
}

private class TestDirectSitemapModule : DirectSitemapModule() {

    override fun define() {
        addEntry("direct", null, LabelKey.Home_Page, PageAccessControl.PUBLIC)
        addEntry("direct/a", PublicHomeView::class.java, LabelKey.Home_Page, PageAccessControl.PUBLIC)
        addRedirect("direct", "direct/a")
    }

}