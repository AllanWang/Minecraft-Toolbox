package ca.allanwang.minecraft.toolbox.db

import ca.allanwang.minecraft.toolbox.base.PluginScope
import ca.allanwang.minecraft.toolbox.sqldelight.MctDb
import com.squareup.sqldelight.sqlite.driver.JdbcDriver
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import dagger.Module
import dagger.Provides
import javax.sql.DataSource

@Module
object MctDbModule {

    @Provides
    @PluginScope
    fun jdbcDriver(dataSource: DataSource): JdbcDriver =
        dataSource.asJdbcDriver()

    @Provides
    @PluginScope
    fun db(jdbcDriver: JdbcDriver): MctDb = MctDb(jdbcDriver)
}