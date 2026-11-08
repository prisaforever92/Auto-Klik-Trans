package com.autoklik.android.data

import androidx.room.*

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val loopCount: Int = 0,
    val antiDetectionEnabled: Boolean = true,
    val antiDetectionJitter: Int = 15,
    val subLoopEnabled: Boolean = false,
    val subLoopStartOrder: Int? = null,
    val subLoopEndOrder: Int? = null,
    val subLoopAfterCycles: Int = 1,
    val subLoopRepeatCount: Int = 0,
    val pointsJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class GesturePointDto(
    val id: String,
    val order: Int,
    val type: String, // "tap", "swipe_up", "swipe_down", "paste"
    val x: Float,
    val y: Float,
    val endX: Float? = null,
    val endY: Float? = null,
    val delayMs: Long = 100L,
    val durationMs: Long = 50L,
    val intervalMs: Long = 200L,
    val repeatCount: Int = 1,
    val pasteText: String? = null,
    val autoTapBeforePaste: Boolean = true,
    val label: String? = null,
    val jumpEnabled: Boolean = false,
    val jumpToPointOrder: Int? = null,
    val jumpAfterCycle: Int = 1
)

@Dao
interface ScriptDao {
    @Query("SELECT * FROM scripts ORDER BY updatedAt DESC")
    suspend fun getAllScripts(): List<ScriptEntity>

    @Query("SELECT * FROM scripts WHERE id = :id")
    suspend fun getScriptById(id: String): ScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity)

    @Delete
    suspend fun deleteScript(script: ScriptEntity)
}

@Database(entities = [ScriptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao
}
