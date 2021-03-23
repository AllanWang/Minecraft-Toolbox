package ca.allanwang.minecraft.toolbox.db

import ca.allanwang.minecraft.toolbox.MctConfig
import ca.allanwang.minecraft.toolbox.base.PluginScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject

class MctDb @Inject internal constructor(
    private val logger: Logger,
    private val config: MctConfig,
    private val database: Database,
    private val tables: Set<@JvmSuppressWildcards Table>
) {

    fun init() {
        logger.info { "Init ${tables.size} tables" }
        transaction {
            if (config.sqlLogging) addLogger(StdOutSqlLogger)
            SchemaUtils.create(*tables.toTypedArray())
        }
    }
}

suspend fun <T> mctTransaction(
    db: Database? = null,
    transactionIsolation: Int? = null,
    statement: suspend Transaction.() -> T
): T = newSuspendedTransaction(
    Dispatchers.IO,
    db,
    transactionIsolation,
    statement
)


class BeaconTable @Inject internal constructor(
    config: MctConfig
) : IntIdTable("${config.sqlPrefix}beacon") {
    val user: Column<UUID> = uuid("user")
    val x: Column<Int> = integer("x")
    val y: Column<Int> = integer("y")
    val z: Column<Int> = integer("z")
}

@Module
object MctDbModule {
    @Provides
    @IntoSet
    @PluginScope
    fun beaconTable(table: BeaconTable): Table = table
}