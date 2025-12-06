package com.example.noteapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotesDatabseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        private const val DATABASE_NAME = "notesapp.db"
        private const val DATABASE_VERSION = 2
        //Bảng note
        private const val TABLE_NOTES = "allnotes"
        private const val COLUMN_NOTE_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_CATEGORY_ID = "categoryId"

        // TABLE CATEGORIES
        private const val TABLE_CATEGORY = "categories"
        private const val COLUMN_CAT_ID = "id"
        private const val COLUMN_CAT_NAME = "name"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val createCategoryTable = """
            CREATE TABLE $TABLE_CATEGORY (
                $COLUMN_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CAT_NAME TEXT
            )
        """.trimIndent()

        val createNotesTable = """
            CREATE TABLE $TABLE_NOTES (
                $COLUMN_NOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_CONTENT TEXT,
                $COLUMN_CATEGORY_ID INTEGER,
                FOREIGN KEY ($COLUMN_CATEGORY_ID) REFERENCES $TABLE_CATEGORY($COLUMN_CAT_ID)
            )
        """.trimIndent()

        db?.execSQL(createCategoryTable)
        db?.execSQL(createNotesTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_CATEGORY_ID INTEGER DEFAULT NULL")
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_CATEGORY (
                $COLUMN_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CAT_NAME TEXT)
            """.trimIndent())
        }
    }

    fun insertNote(note: Note){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_CATEGORY_ID, note.categoryId)
        }
        db.insert(TABLE_NOTES, null, values)
        db.close()
    }

    fun getAllNotes() : MutableList<Note>{
        val noteList = mutableListOf<Note>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NOTES"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            val note = Note(id, title, content)
            noteList.add(note)
        }
        cursor.close()
        db.close()
        return noteList
    }
    fun updateNote(note: Note){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
        }
        val whereClause = "$COLUMN_NOTE_ID = ?"
        val whereArgs = arrayOf(note.id.toString())
        db.update(TABLE_NOTES, values, whereClause, whereArgs)
        db.close()
    }

    fun getNoteByID(noteId: Int): Note {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NOTES WHERE $COLUMN_NOTE_ID = $noteId"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
        val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
        cursor.close()
        db.close()
        return Note(id, title, content)
    }
    fun deleteNote(noteId: Int){
        val db = writableDatabase
        val whereClause = "$COLUMN_NOTE_ID = ?"
        val whereArgs = arrayOf(noteId.toString())
        db.delete(TABLE_NOTES, whereClause, whereArgs)
        db.close()
    }
    fun insertCategory(category: Category) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CAT_NAME, category.name)
        }
        db.insert(TABLE_CATEGORY, null, values)
        db.close()
    }
    fun getAllCategories(): MutableList<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORY", null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAT_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAT_NAME))
            list.add(Category(id, name))
        }
        cursor.close()
        db.close()
        return list
    }
    fun getNotesByCategory(categoryId: Int): MutableList<Note> {
        val list = mutableListOf<Note>()
        val db = readableDatabase
        if (categoryId == -1) return getAllNotes()
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NOTES WHERE $COLUMN_CATEGORY_ID = ?",
            arrayOf(categoryId.toString())
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            val catId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
            list.add(Note(id, title, content, catId))
        }

        cursor.close()
        db.close()
        return list
    }
    fun deleteCategory(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_CATEGORY, "$COLUMN_CAT_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteNotesByCategory(categoryId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NOTES, "$COLUMN_CATEGORY_ID = ?", arrayOf(categoryId.toString()))
        db.close()
    }
}