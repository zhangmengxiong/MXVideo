package com.mx.video.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * 播放记录，使用sqlite存储
 */
internal class MXHistoryDB(val context: Context) {
    private val dbHelp by lazy { DBHelp(context.applicationContext).writableDatabase }

    fun addPlayTime(videoPath: String, playTime: Int) {
        if (videoPath.isEmpty() || playTime < 0) return
        // MXUtils.log("savePlayTime  $videoPath $playTime")
        try {
            val values = ContentValues()
            values.put(DBHelp.DB_KEY_VIDEO_PATH, videoPath.trim())
            values.put(DBHelp.DB_KEY_PLAY_TIME, playTime)
            dbHelp.replace(DBHelp.DB_NAME, null, values) >= 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPlayTime(videoPath: String): Int {
        if (videoPath.isEmpty()) return 0
        MXUtils.log("getPlayTime  $videoPath")
        var cursor: Cursor? = null
        try {
            cursor = dbHelp.query(
                DBHelp.DB_NAME, arrayOf(DBHelp.DB_KEY_PLAY_TIME), "${DBHelp.DB_KEY_VIDEO_PATH}=?",
                arrayOf(videoPath), null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(DBHelp.DB_KEY_PLAY_TIME))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                cursor?.close()
            } catch (e: Exception) {
            }
        }
        return 0
    }

    fun cleanAll() {
        dbHelp.delete(DBHelp.DB_NAME, null, null)
    }

    private class DBHelp(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {
        companion object {
            const val DB_NAME = "mx_video_v1_db"
            const val DB_KEY_VIDEO_PATH = "video_path"
            const val DB_KEY_PLAY_TIME = "play_time"
        }

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL("create table $DB_NAME($DB_KEY_VIDEO_PATH text NOT NULL UNIQUE , $DB_KEY_PLAY_TIME long )")
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        }
    }
}

