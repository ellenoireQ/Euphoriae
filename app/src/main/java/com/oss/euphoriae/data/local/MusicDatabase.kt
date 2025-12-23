package com.oss.euphoriae.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oss.euphoriae.data.model.Playlist
import com.oss.euphoriae.data.model.PlaylistSong
import com.oss.euphoriae.data.model.Song

@Database(
    entities = [Song::class, Playlist::class, PlaylistSong::class],
    version = 2,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    
    abstract fun musicDao(): MusicDao
    
    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE songs ADD COLUMN mimeType TEXT")
            }
        }
        
        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "euphoriae_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
