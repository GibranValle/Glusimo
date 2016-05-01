package app.proyectoterminal.upibi.glusimo.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseManager extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "LISTADEUSUARIOS.db";
    private static final int VERSION_BD = 1;

    public static final String NOMBRE_TABLA = "ITEMYPRECIO";
    public static final String ID = "_id";
    public static final String C_ELEMENTO = "ELEMENTO";
    public static final String C_PRECIO = "PRECIO";
    public static final String C_EXTRA = "EXTRA";
    public static final String C_PRECIO_EXTRA = "PRECIOEXTRA";
    public static final String C_INVENTARIO = "INVENTARIO";

    static final String TAG = "MANAGER";

    public DataBaseManager(Context context)
    {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    /* CREA LA TABLA 1, ARGUMENTOS:(_id INTEGER PRIMAREY KEY, age INTEGER, name TEXT) */
    private static final String CREAR_TABLA =
            "CREATE TABLE "+ NOMBRE_TABLA + " ("+
                    ID + " INTEGER PRIMARY KEY,"+
                    C_ELEMENTO + " TEXT,"+
                    C_PRECIO + " INTEGER,"+
                    C_EXTRA +" TEXT,"+
                    C_PRECIO_EXTRA + " INTEGER,"+
                    C_INVENTARIO + " INTEGER)" ;

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA);
        Log.d(TAG,"CREANDO TABLA");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ NOMBRE_TABLA);
        onCreate(db);
        Log.d(TAG,"ACTUALIZANDO TABLA");
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
        Log.d(TAG,"DESACTUALIZAR TABLA");
    }

    public long insertarDatos(String elemento, String precio, String extra, String precioExtra, String inventario)
    {
        Log.d(TAG,"INSERTANDO DATOS...");
        //cargar la base de datos;
        SQLiteDatabase db = getWritableDatabase();
        //crear el objeto contenido, para escribir los datos
        ContentValues contenido = new ContentValues();
        //ESCRIBIR EN LA COLUMNA, EL VALOR
        contenido.put(C_ELEMENTO,elemento);
        contenido.put(C_PRECIO,precio);
        contenido.put(C_EXTRA,extra);
        contenido.put(C_PRECIO_EXTRA,precioExtra);
        contenido.put(C_INVENTARIO,inventario);

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

    public int actualizarDatos(String elementoAnterior, String elemento, String precio, String extra, String precioExtra, String inventario) //ACTUALIZAR A PARTIR DEL NOMBRE
    {
        Log.d(TAG,"ACTUALIZANDO DATOS");
        // CARGAR BASE DE DATOS PARA INICIAR LA EDICION
        SQLiteDatabase db = getWritableDatabase();

        // CREAR UN OBJETO DE CONTENIDO PARA GUARDAR LOS DATOS
        ContentValues contenido = new ContentValues();
        contenido.put(C_ELEMENTO,elemento);
        contenido.put(C_PRECIO,precio);
        contenido.put(C_EXTRA,extra);
        contenido.put(C_PRECIO_EXTRA,precioExtra);
        contenido.put(C_INVENTARIO,inventario);

        // EDITAR CONTENIDO DEL QUERRY AQUI
        String nombre = NOMBRE_TABLA;
        String seleccion = C_ELEMENTO +" =?";
        String argselec[] = {elementoAnterior};

        //querry listo para no editar
        int actualizado = db.update(nombre,contenido, seleccion, argselec); //regresa el numero de row actualizados
        return actualizado;
    }

    public int eliminarDatos(String elemento) // ELIMINAR A PARTIR DEL NOMBRE
    {
        Log.d(TAG,"ELIMINANDO DATO...");
        // CARGAR BASE DE DATOS PARA INICIAR LA EDIC
        SQLiteDatabase db = getWritableDatabase();

       // CREAR UN OBJETO DE CONTENIDO PARA GUARDAR LOS DATOS
        ContentValues contenido = new ContentValues();
        contenido.put(C_ELEMENTO,elemento); //escribir en la columna elemento, el elemento recibido

        //editar contenido del querry aqui
        String nombre = NOMBRE_TABLA;
        String seleccion = C_ELEMENTO +" =?";
        String argselec[] = {elemento};
        //querry listo para no editar
        int borrado = db.delete(nombre, seleccion, argselec);
        return borrado;
    }

    public String comprobar(String nombre) //Comprobar que no exista otro elemento con el mismo nombre
    {
        Log.d(TAG, "COMPROBANDO...");
        //cargar la base de datos
        SQLiteDatabase db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = ID;
        String usuario = C_ELEMENTO;

        String [] columnas = {id};
        String seleccion = usuario + "=?";
        String[] args_selec ={nombre};
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
        String elemento = C_ELEMENTO;
        String precio = C_PRECIO;
        String extra = C_EXTRA;
        String precioExtra = C_PRECIO_EXTRA;
        String inventario = C_INVENTARIO;

        String [] columnas = {elemento, precio, extra, precioExtra, inventario}; //datos a recuperar
        String seleccion = id + "=?"; //a apartir del
        String[] args_selec ={String.valueOf(_id)}; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;

        //crear un cursor
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexElemento = cursor.getColumnIndex(elemento);
        int indexPrecio = cursor.getColumnIndex(precio);
        int indexExtra = cursor.getColumnIndex(extra);
        int indexPrecioExtra = cursor.getColumnIndex(precioExtra);
        int indexInventario = cursor.getColumnIndex(inventario);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext())
        {
            String cElemento = cursor.getString(indexElemento);
            String cPrecio = cursor.getString(indexPrecio);
            String cExtra = cursor.getString(indexExtra);
            String cPrecioExtra = cursor.getString(indexPrecioExtra);
            String cInventario = cursor.getString(indexInventario);
            buffer.append(cElemento + "|" +cPrecio + "||" +cExtra + "|||" +cPrecioExtra + "||||" + cInventario + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

}