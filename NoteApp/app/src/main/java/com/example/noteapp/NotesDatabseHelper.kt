package com.example.noteapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotesDatabseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        private const val DATABASE_NAME = "notesapp.db"
        private const val DATABASE_VERSION = 3
        //Bảng note
        private const val TABLE_NOTES = "allnotes"
        private const val COLUMN_NOTE_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_IS_PINNED = "isPinned"
        private const val COLUMN_CREATED_AT = "createdAt"
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
                $COLUMN_IS_PINNED INTEGER DEFAULT 0,
                $COLUMN_CATEGORY_ID INTEGER,
                $COLUMN_CREATED_AT TEXT,
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
        if (oldVersion < 3){
            db?.execSQL("ALTER TABLE $TABLE_NOTES ADD COLUMN $COLUMN_IS_PINNED INTEGER DEFAULT 0")
        }
    }

    //Hàm thêm note
    fun insertNote(note: Note){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_CATEGORY_ID, note.categoryId)
            put(COLUMN_CREATED_AT, System.currentTimeMillis().toString())
        }
        db.insert(TABLE_NOTES, null, values)
        db.close()
    }

    //Hàm lấy ra tất cả note
    fun getAllNotes() : MutableList<Note>{
        val noteList = mutableListOf<Note>()
        val db = readableDatabase

        val query = "SELECT * FROM $TABLE_NOTES ORDER BY isPinned DESC, createdAt DESC"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            val catId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
            val isPinned = cursor.getInt(cursor.getColumnIndexOrThrow("isPinned"))
            val createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
            noteList.add(Note(id, title, content, catId, isPinned, createdAt))
        }
        cursor.close()
        db.close()
        return noteList
    }
    //Hàm cập nật note
    fun updateNote(note: Note){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_CREATED_AT, System.currentTimeMillis().toString())
        }
        val whereClause = "$COLUMN_NOTE_ID = ?"
        val whereArgs = arrayOf(note.id.toString())
        db.update(TABLE_NOTES, values, whereClause, whereArgs)
        db.close()
    }
    //Hàm lấy note theo id

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
    //Hàm xoá note
    fun deleteNote(noteId: Int){
        val db = writableDatabase
        val whereClause = "$COLUMN_NOTE_ID = ?"
        val whereArgs = arrayOf(noteId.toString())
        db.delete(TABLE_NOTES, whereClause, whereArgs)
        db.close()
    }
    //Hàm thêm danh mục
    fun insertCategory(category: Category) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CAT_NAME, category.name)
        }
        db.insert(TABLE_CATEGORY, null, values)
        db.close()
    }
    //Hàm lấy danh mục
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
    //Hàm lấy note theo danh mục
    fun getNotesByCategory(categoryId: Int): MutableList<Note> {
        val list = mutableListOf<Note>()
        val db = readableDatabase
        if (categoryId == -1) return getAllNotes()
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NOTES WHERE $COLUMN_CATEGORY_ID = ? ORDER BY isPinned DESC, createdAt DESC",
            arrayOf(categoryId.toString())
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
            val catId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
            val isPinned = cursor.getInt(cursor.getColumnIndexOrThrow("isPinned"))
            val createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
            list.add(Note(id, title, content, catId, isPinned, createdAt))
        }

        cursor.close()
        db.close()
        return list
    }
    //Hàm xoá danh mục
    fun deleteCategory(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_CATEGORY, "$COLUMN_CAT_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    //Hàm xoá note theo danh mục
    fun deleteNotesByCategory(categoryId: Int) {
        val db = writableDatabase
        db.delete(TABLE_NOTES, "$COLUMN_CATEGORY_ID = ?", arrayOf(categoryId.toString()))
        db.close()
    }
    //Hàm ghim note
    fun togglePin(noteId: Int, pinned: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("isPinned", pinned)
        }
        db.update(TABLE_NOTES, values, "$COLUMN_NOTE_ID = ?", arrayOf(noteId.toString()))
        db.close()
    }
}