package io.github.xwz.base.api;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

@Migration(version = 6, databaseName = ContentDatabase.NAME)
public class Migration6 extends AlterTableMigration<EpisodeBaseModel> {

    public Migration6() {
        super(EpisodeBaseModel.class);
    }

    public void onPreMigrate() {
        super.onPreMigrate();
        addColumn(Long.class, "pubDate");
    }
}
