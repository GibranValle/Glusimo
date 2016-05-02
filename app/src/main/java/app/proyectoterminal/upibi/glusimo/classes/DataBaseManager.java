package app.proyectoterminal.upibi.glusimo.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseManager extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "registroGlucemia.db";
    private static final int VERSION_BD = 2;
    public static final String NOMBRE_TABLA = "registroPorFecha";
    public static final String ID = "_id";
    public static final String C_FECHA = "FECHA";
    public static final String C_AÑO = "AÑO";
    public static final String C_MES = "MES";
    public static final String C_DIA = "DÍA";
    public static final String C_NDIA = "NOMBRE_DÍA";
    public static final String C_FECHA_CORTA = "FECHA_CORTA";
    public static final String C_TIEMPO = "TIEMPO";
    public static final String C_CONCENTRACIÓN = "CONCENTRACIÓN";

    static final String TAG = "Interfaz";

    public DataBaseManager(Context context)
    {

        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    /* CREA LA TABLA 1, ARGUMENTOS:(_id INTEGER PRIMAREY KEY, age INTEGER, name TEXT) */
    private static final String CREAR_TABLA =
            "CREATE TABLE "+ NOMBRE_TABLA + " ("+
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    C_FECHA + " TEXT,"+
                    C_AÑO + " TEXT,"+
                    C_MES + " TEXT,"+
                    C_DIA +" TEXT,"+
                    C_NDIA +" TEXT,"+
                    C_FECHA_CORTA + " TEXT,"+
                    C_TIEMPO + " TEXT,"+
                    C_CONCENTRACIÓN + " INTEGER)" ;

    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            db.execSQL(CREAR_TABLA);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        Log.d(TAG,"CREANDO TABLA");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

        try
        {
            db.execSQL("DROP TABLE IF EXISTS "+ NOMBRE_TABLA);
            Log.d(TAG,"DROPING TABLA");
            onCreate(db);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }        Log.d(TAG,"ACTUALIZANDO TABLA");
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
        Log.d(TAG,"DESACTUALIZAR TABLA");
    }

    public long insertarDatos(String fecha ,String año, String mes, String dia, String nombre_dia,
                              String tiempo, String concentración)
    {
        Log.d(TAG,"INSERTANDO DATOS...");
        String fecha_corta = nombre_dia+" "+dia+" "+mes+" "+año;
        Log.d(TAG,"fecha_corta"+fecha_corta);
        //cargar la base de datos;
        SQLiteDatabase db = getWritableDatabase();
        //crear el objeto contenido, para escribir los datos
        ContentValues contenido = new ContentValues();
        //ESCRIBIR EN LA COLUMNA, EL VALOR
        contenido.put(C_FECHA,fecha);
        contenido.put(C_AÑO,año);
        contenido.put(C_MES,mes);
        contenido.put(C_DIA,dia);
        contenido.put(C_NDIA,nombre_dia);
        contenido.put(C_FECHA_CORTA,fecha_corta);
        contenido.put(C_TIEMPO,tiempo);
        contenido.put(C_CONCENTRACIÓN,concentración);

        long id = db.insert(NOMBRE_TABLA,null,contenido); //insertar el contenido, regresando el id.
        return id;
        // FALLA SI REGRESA UN ID NEGATIVO
    }

    public Cursor posicionCero() {
        // Obtenemos la base de datos
        SQLiteDatabase db = getReadableDatabase();
        // Creamos un cursor para iterar al query
        Cursor c;
        // Realizamos el query
        c = db.rawQuery("SELECT * FROM "+ NOMBRE_TABLA, null);

        // Regresamos el cursor en su primera posicion
        if( c!= null)
            c.moveToFirst();
        return c;
    }

    public int actualizarDatos(String fechaAnterior, String fecha ,String año, String mes,
                               String dia, String nombre_dia, String tiempo, String concentración)
    //ACTUALIZAR A PARTIR DEL NOMBRE
    {
        Log.d(TAG,"ACTUALIZANDO DATOS");
        String fecha_corta = nombre_dia+" "+dia+" "+mes+" "+año;
        Log.d(TAG,"fecha_corta: "+fecha_corta);

        // CARGAR BASE DE DATOS PARA INICIAR LA EDICION
        SQLiteDatabase db = getWritableDatabase();

        // CREAR UN OBJETO DE CONTENIDO PARA GUARDAR LOS DATOS
        ContentValues contenido = new ContentValues();
        contenido.put(C_FECHA,fecha);
        contenido.put(C_AÑO,año);
        contenido.put(C_MES,mes);
        contenido.put(C_DIA,dia);
        contenido.put(C_NDIA,nombre_dia);
        contenido.put(C_FECHA_CORTA,fecha_corta);
        contenido.put(C_TIEMPO,tiempo);
        contenido.put(C_CONCENTRACIÓN,concentración);

        // EDITAR CONTENIDO DEL QUERRY AQUI
        String nombre = NOMBRE_TABLA;
        String seleccion = C_FECHA +" =?";
        String argselec[] = {fechaAnterior};

        //querry listo para no editar
        int actualizado = db.update(nombre,contenido,
                seleccion, argselec);
        //regresa el numero de row actualizados
        return actualizado;
    }

    public int eliminarDatos(String fecha) // ELIMINAR A PARTIR DEL NOMBRE
    {
        Log.d(TAG,"ELIMINANDO DATO...");
        // CARGAR BASE DE DATOS PARA INICIAR LA EDIC
        SQLiteDatabase db = getWritableDatabase();

       // CREAR UN OBJETO DE CONTENIDO PARA GUARDAR LOS DATOS
        ContentValues contenido = new ContentValues();
        contenido.put(C_FECHA,fecha);
        //escribir en la columna elemento, el elemento recibido

        //editar contenido del querry aqui
        String nombre = NOMBRE_TABLA;
        String seleccion = C_FECHA +" =?";
        String argselec[] = {fecha};
        //querry listo para no editar
        int borrado = db.delete(nombre, seleccion, argselec);
        return borrado;
    }

    public String comprobar(String fecha) //Comprobar que no exista otro elemento con el mismo nombre
    {
        Log.d(TAG, "COMPROBANDO...");
        //cargar la base de datos
        SQLiteDatabase db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = ID;
        String FECHA = C_FECHA;

        String [] columnas = {id};
        String seleccion = FECHA + "=?";
        String[] args_selec ={fecha};
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;
        //crear un cursor
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexid = cursor.getColumnIndex(id);
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext())
        {
            int cid = cursor.getInt(indexid);
            buffer.append(cid+"\n");
        }
        if (buffer.toString().equals(""))
        {
            Log.d(TAG,"no existe en la base de datos");
        }
        else
        {
            Log.d(TAG,"existe en la base de datos, reintentar");
        }
        return buffer.toString();
    }

    public String recuperarTodos()
    {
        Log.d(TAG, "RECUPERANDO TODOS LOS DATOS");
        //cargar la base de datos
        SQLiteDatabase db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = ID;
        String fecha = C_FECHA;
        String año = C_AÑO;
        String mes = C_MES;
        String dia = C_DIA;
        String fecha_corta = C_FECHA_CORTA;
        String nombre_dia =C_NDIA;
        String tiempo = C_TIEMPO;
        String concentración = C_CONCENTRACIÓN;
        //datos a recuperar
        String [] columnas = {fecha, año, mes, dia, nombre_dia,fecha_corta, tiempo, concentración};

        // selection
        String seleccion = null; //a apartir del
        String[] args_selec = null; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexFecha = cursor.getColumnIndex(fecha);
        int indexAño = cursor.getColumnIndex(año);
        int indexMes = cursor.getColumnIndex(mes);
        int indexDía = cursor.getColumnIndex(dia);
        int indexNombreDia = cursor.getColumnIndex(nombre_dia);
        int indexFechaCorta = cursor.getColumnIndex(fecha_corta);
        int indexTiempo = cursor.getColumnIndex(tiempo);
        int IndexConcentración = cursor.getColumnIndex(concentración);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext())
        {
            String cFecha = cursor.getString(indexFecha);
            String cAño = cursor.getString(indexAño);
            String cMes = cursor.getString(indexMes);
            String cDía = cursor.getString(indexDía);
            String cNombreDia = cursor.getString(indexNombreDia);
            String cFechaCorta = cursor.getString(indexFechaCorta);
            String cTiempo = cursor.getString(indexTiempo);
            String cConcentración = cursor.getString(IndexConcentración);
            buffer.append(cFecha+"|"+cAño + "*" +cMes + "+" +cDía + "="
                    +cNombreDia+"¿"+cFechaCorta+"&"+cTiempo + "!" + cConcentración + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

    public String recuperarPorId(Long _id) //Comprobar que no exista otro usuario con el mismo nombre
    {
        Log.d(TAG, "RECUPERANDO NOMBRE POR ID... "+_id);
        //cargar la base de datos
        SQLiteDatabase db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = ID;
        String fecha = C_FECHA;
        String año = C_AÑO;
        String mes = C_MES;
        String dia = C_DIA;
        String nombre_dia =C_NDIA;
        String fecha_corta = C_FECHA_CORTA;
        String tiempo = C_TIEMPO;
        String concentración = C_CONCENTRACIÓN;
        //datos a recuperar
        String [] columnas = {fecha, año, mes, dia, nombre_dia,fecha_corta, tiempo, concentración};
        String seleccion = id + "=?"; //a apartir del
        String[] args_selec ={String.valueOf(_id)}; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;

        //crear un cursor
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexFecha = cursor.getColumnIndex(fecha);
        int indexAño = cursor.getColumnIndex(año);
        int indexMes = cursor.getColumnIndex(mes);
        int indexDía = cursor.getColumnIndex(dia);
        int indexNombreDia = cursor.getColumnIndex(nombre_dia);
        int indexFechaCorta = cursor.getColumnIndex(fecha_corta);
        int indexTiempo = cursor.getColumnIndex(tiempo);
        int IndexConcentración = cursor.getColumnIndex(concentración);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext())
        {
            String cFecha = cursor.getString(indexFecha);
            String cAño = cursor.getString(indexAño);
            String cMes = cursor.getString(indexMes);
            String cDía = cursor.getString(indexDía);
            String cNombreDia = cursor.getString(indexNombreDia);
            String cFechaCorta = cursor.getString(indexFechaCorta);
            String cTiempo = cursor.getString(indexTiempo);
            String cConcentración = cursor.getString(IndexConcentración);
            buffer.append(cFecha+"|"+cAño + "*" +cMes + "+" +cDía + "="
                    +cNombreDia+"¿"+cFechaCorta+"&"+cTiempo + "!" + cConcentración + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }
}