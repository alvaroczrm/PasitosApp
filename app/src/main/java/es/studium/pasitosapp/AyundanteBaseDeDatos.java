package es.studium.pasitosapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AyundanteBaseDeDatos extends SQLiteOpenHelper {
        private static final String NOMBRE_BASE_DE_DATOS = "pasitos_app",
            NOMBRE_TABLA_PASITOS = "coordenadas_bat";
        private static final int VERSION_BASE_DE_DATOS =1;
        public AyundanteBaseDeDatos(Context context){
            super(context,NOMBRE_BASE_DE_DATOS,null,VERSION_BASE_DE_DATOS);
        }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "latitud DECIMAL, longitud DECIMAL, bateria INTEGER", NOMBRE_TABLA_PASITOS));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("DROP TABLE IF EXIST %s", NOMBRE_TABLA_PASITOS));
        onCreate(db);
    }
}
