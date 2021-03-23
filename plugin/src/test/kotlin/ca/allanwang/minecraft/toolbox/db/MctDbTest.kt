package ca.allanwang.minecraft.toolbox.db

import ca.allanwang.minecraft.toolbox.MctConfig
import ca.allanwang.minecraft.toolbox.base.PluginScope
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dagger.Component
import dagger.Module
import dagger.Provides
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.logging.Logger

class MctDbTest {

    private lateinit var component: MctDbTestComponent

    @BeforeEach
    fun before() {
        component = DaggerMctDbTestComponent.create()
        component.mctDb().init()
    }

    @AfterEach
    fun after() {
        transaction {
            SchemaUtils.drop(*component.tables().toTypedArray())
        }
    }

    @Test
    fun addBeacon() {
        transaction {
            component.beaconTable().insert {
                it[id] = 1
                it[user] = UUID.randomUUID()
                it[x] = 3
                it[y] = 4
                it[z] = 5
            }
        }
        val count = transaction {
            component.beaconTable().selectAll().count()
        }
        assertThat(count, equalTo(1))
    }

}

@Module
object MctTestCoreModule {
    @Provides
    @PluginScope
    fun logger(): Logger = Logger.getGlobal()
}

@Module(includes = [MctTestConfigModule::class])
object MctTestDbModule {
    @Provides
    @PluginScope
    fun testDb(mctConfig: MctConfig): Database = Database.connect(
        url = mctConfig.sqlUrl,
        driver = mctConfig.sqlDriver,
        user = mctConfig.sqlUsername,
        password = mctConfig.sqlPassword
    )
}

@Module
object MctTestConfigModule {
    @Provides
    fun testConfig(): MctConfig = object : MctConfig {
        override val sqlLogging: Boolean = true
        override val sqlPrefix: String = "mct_"
        override val sqlUrl: String = "jdbc:h2:mem:mysql;MODE=MySQL;DB_CLOSE_DELAY=-1"
        override val sqlDriver: String = "org.h2.Driver"
        override val sqlUsername: String = "test"
        override val sqlPassword: String = "test"
    }
}

@Component(modules = [MctDbModule::class, MctTestCoreModule::class, MctTestDbModule::class, MctTestConfigModule::class])
@PluginScope
interface MctDbTestComponent {

    @PluginScope
    fun mctDb(): MctDb

    @PluginScope
    fun db(): Database

    @PluginScope
    fun beaconTable(): BeaconTable

    @PluginScope
    fun tables(): Set<@JvmSuppressWildcards Table>

}