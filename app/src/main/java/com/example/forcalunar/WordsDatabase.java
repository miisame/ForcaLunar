package com.example.forcalunar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WordsDatabase extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "PalavrasForcaLunarApp.db";
    private static final int DATABASE_VERSION = 2;

    // Constantes da Tabela
    public static final String TABLE_NAME = "palavras";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PALAVRA = "palavra";
    public static final String COLUMN_CATEGORIA = "categoria";

    public WordsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Chamado quando o banco de dados é criado pela primeira vez
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PALAVRA + " TEXT, " +
                COLUMN_CATEGORIA + " TEXT)";
        db.execSQL(createTableQuery);

        // Alimenta o banco logo após a criação da tabela
        popularBancoInicial(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Método para inserir dados padrões (Matriz de Strings)
    private void popularBancoInicial(SQLiteDatabase db) {
        String[][] listaPalavras = {
                {"KHONSHU", "DEUS EGÍPICIO"},
                {"ESCARAVELHO", "INSETO"},
                {"MUSEU", "LUGAR"},
                {"TUMBA", "LUGAR"},
                {"PIRAMIDE", "LUGAR"},
                {"ECLIPSE", "LUA"},
                {"SANATORIO", "LUGAR"},
                {"BANDAGEM", "PRIMEIROS SOCORROS"},
                {"CHACAL", "CRIATURA"},
                {"MUMIA", "CRIATURA"},
        };

        for (String[] item : listaPalavras) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PALAVRA, item[0]);
            values.put(COLUMN_CATEGORIA, item[1]);
            db.insert(TABLE_NAME, null, values);
        }
    }

    public String[] obterPalavraAleatoria() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] resultado = null;

        // Query para buscar uma palavra aleatória baseada na categoria
        String query = "SELECT " + COLUMN_PALAVRA + ", " + COLUMN_CATEGORIA +
                " FROM " + TABLE_NAME + " ORDER BY RANDOM() LIMIT 1";

        // O segundo parâmetro substitui o "?" na query de forma segura
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            // Pegamos os índices das duas colunas
            int indicePalavra = cursor.getColumnIndexOrThrow(COLUMN_PALAVRA);
            int indiceCategoria = cursor.getColumnIndexOrThrow(COLUMN_CATEGORIA);

            // Extraímos os textos
            String palavra = cursor.getString(indicePalavra);
            String categoria = cursor.getString(indiceCategoria);

            // Guardamos ambos no array de retorno
            resultado = new String[]{palavra, categoria};
        }
        cursor.close();
        db.close();

        return resultado;
    }

    public boolean inserirNovaPalavra(String palavra, String categoria) {
        SQLiteDatabase db = this.getWritableDatabase(); // Abre o banco em modo de ESCRITA
        ContentValues values = new ContentValues();

        // .trim() remove espaços extras e .toUpperCase() padroniza para maiúsculas
        values.put(COLUMN_PALAVRA, palavra.trim().toUpperCase());
        values.put(COLUMN_CATEGORIA, categoria.trim().toUpperCase());

        // O método insert retorna o ID da linha inserida ou -1 se acontecer algum erro
        long resultado = db.insert(TABLE_NAME, null, values);
        db.close(); // Sempre feche a conexão

        // Retorna true se deu certo (id diferente de -1) ou false se deu erro
        return resultado != -1;
    }


}
