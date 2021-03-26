package ca.allanwang.minecraft.toolbox.db

import ca.allanwang.minecraft.toolbox.MctConfig
import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.sqldelight.MctDb
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dagger.Component
import dagger.Module
import dagger.Provides
import org.h2.jdbcx.JdbcDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.logging.Logger
import javax.sql.DataSource

class MctDbTest {

    private lateinit var component: MctDbTestComponent
    private val mctDb: MctDb get() = component.mctDb()

    @BeforeEach
    fun before() {
        component = DaggerMctDbTestComponent.create()
    }

    @AfterEach
    fun after() {
        component.dataSource().connection.close()
    }

    @Test
    fun addBeacon() {
        mctDb.mctDbQueries.insert(UUID.randomUUID().toString(), 1, 2, 3)
        val selected = mctDb.mctDbQueries.selectAll()
        assertThat(selected.executeAsList().size, equalTo(1))
    }

}

@Module
object MctTestCoreModule {
    @Provides
    @PluginScope
    fun logger(): Logger = Logger.getGlobal()
}

@Module(includes = [MctDbModule::class])
object MctTestDbModule {
    @Provides
    @PluginScope
    fun dataSource(): DataSource = JdbcDataSource().apply {
        setURL("jdbc:h2:mem:mysql;MODE=MySQL;DB_CLOSE_DELAY=-1")
        user = "test"
        password = "test"
    }
}

@Module
object MctTestConfigModule {
    @Provides
    fun testConfig(): MctConfig = object : MctConfig {
        override val sqlLogging: Boolean = true
        override val sqlPrefix: String = "mct_"
        override val sqlUrl: String =
            "jdbc:h2:mem:mysql;MODE=MySQL;DB_CLOSE_DELAY=-1"
        override val sqlDriver: String = "org.h2.Driver"
        override val sqlUsername: String = "test"
        override val sqlPassword: String = "test"
    }
}

@Component(modules = [MctTestCoreModule::class, MctTestDbModule::class, MctTestConfigModule::class])
@PluginScope
interface MctDbTestComponent {

    @PluginScope
    fun dataSource(): DataSource

    @PluginScope
    fun mctDb(): MctDb

}