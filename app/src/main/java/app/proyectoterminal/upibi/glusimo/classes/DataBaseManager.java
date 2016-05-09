package app.proyectoterminal.upibi.glusimo.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class DataBaseManager extends SQLiteOpenHelper {

    private static final String NOMBRE_BD = "registroGlucemia.db";
    private static final int VERSION_BD = 2;
    public static final String NOMBRE_TABLA = "registroPorFecha";
    public static final String C_ID = "_id";
    public static final String C_FECHA = "FECHA";
    public static final String C_AÑO = "AÑO";
    public static final String C_MES = "MES";
    public static final String C_DIA = "DÍA";
    public static final String C_NDIA = "NOMBRE_DÍA";
    public static final String C_FECHA_SEGUNDOS = "FECHA_SEGUNDOS";
    public static final String C_FECHA_CORTA = "FECHA_CORTA";
    public static final String C_TIEMPO = "TIEMPO";
    public static final String C_CONCENTRACIÓN = "CONCENTRACIÓN";
    public static final String C_SALUD = "SALUD";
    SQLiteDatabase db;

    static final String TAG = "DataBaseManager";

    public DataBaseManager(Context context)
    {

        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    /* CREA LA TABLA 1, ARGUMENTOS:(_id INTEGER PRIMAREY KEY, age INTEGER, name TEXT) */
    private static final String CREAR_TABLA =
            "CREATE TABLE "+ NOMBRE_TABLA + " ("+
                    C_ID + " INTEGER PRIMARY KEY,"+
                    C_FECHA + " TEXT,"+
                    C_AÑO + " TEXT,"+
                    C_MES + " TEXT,"+
                    C_DIA +" TEXT,"+
                    C_NDIA +" TEXT,"+
                    C_FECHA_SEGUNDOS + " INTEGER,"+
                    C_FECHA_CORTA + " TEXT,"+
                    C_TIEMPO + " TEXT,"+
                    C_SALUD + " TEXT,"+
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
                              String tiempo, String estado, String concentración)
    {
        Log.d(TAG,"INSERTANDO DATOS...");
        String fecha_corta = nombre_dia+" "+dia+" "+mes+" "+año;
        Log.d(TAG,"fecha_corta"+fecha_corta);
        Log.i(TAG, "fecha: " + fecha + " largo: " + fecha.length());
        SimpleDateFormat df = new SimpleDateFormat("EEEE dd/MMMM/yyyy-HH:mm:ss");
        long milis;
        long seconds = 0;
        try
        {
            Date d = df.parse(fecha);
            milis = d.getTime();
            seconds = milis/1000;
            Log.i(TAG,"TIEMPO EN MILIS: "+milis+" tiempo en secs: "+seconds);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        //cargar la base de datos;
        db = getWritableDatabase();
        //crear el objeto contenido, para escribir los datos
        ContentValues contenido = new ContentValues();
        //ESCRIBIR EN LA COLUMNA, EL VALOR
        contenido.put(C_FECHA,fecha);
        contenido.put(C_AÑO,año);
        contenido.put(C_MES,mes);
        contenido.put(C_DIA,dia);
        contenido.put(C_NDIA,nombre_dia);
        contenido.put(C_FECHA_SEGUNDOS,seconds);
        contenido.put(C_FECHA_CORTA,fecha_corta);
        contenido.put(C_TIEMPO,tiempo);
        contenido.put(C_SALUD,estado);
        contenido.put(C_CONCENTRACIÓN,concentración);

        long id = db.insert(NOMBRE_TABLA,null,contenido); //insertar el contenido, regresando el id.
        return id;
        // FALLA SI REGRESA UN ID NEGATIVO
    }

    /*
    public Cursor posicionCero() {
        // Obtenemos la base de datos
        db = getReadableDatabase();
        // Creamos un cursor para iterar al query
        Cursor c;
        // Realizamos el query
        c = db.rawQuery("SELECT * FROM "+ NOMBRE_TABLA, null);

        // Regresamos el cursor en su primera posicion
        if( c!= null)
            c.moveToFirst();
        return c;
    }*/

    public Cursor posicionDesdeHastaFiltro(String inicio, String fin)
    {
        // Obtenemos la base de datos
        db = getReadableDatabase();
        // Creamos un cursor para iterar al query
        Cursor c;
        int anterior = 0;
        // Realizamos el query
        String q="", q1="", q2="", q3="", q4="", query ="";
        q = ("SELECT * FROM "+NOMBRE_TABLA);
        // logica
        if(!inicio.equals(""))
        {
            Log.w(TAG,"existe filtro de inicio: "+inicio);
            // si hay algo dentro de dia
            q1 = (" where "+C_ID+" >= "+inicio);
            anterior = 1;
        }

        if(!fin.equals(""))
        {
            // si hay algo dentro de mes
            if(anterior == 1)
            {
                Log.w(TAG,"tambn existe filtro de fin: "+fin);
                q2 = (" AND "+C_ID+" <= "+fin);
            }
            else
            {
                q2 = (" where "+C_ID+" <= "+fin);
                Log.w(TAG,"existe filtro de fin: "+fin);
            }
        }
        query = query.concat(q+q1+q2);
        c = db.rawQuery(query,null);

        if( c!= null)
            c.moveToFirst();
        return c;
    }

    public Cursor posicionCeroFiltro(String dia, String mes, String año, String estado) {
        // Obtenemos la base de datos
        db = getReadableDatabase();
        // Creamos un cursor para iterar al query
        Cursor c;
        int anterior = 0;
        // Realizamos el query
        String q="", q1="", q2="", q3="", q4="", query ="";
        q = ("SELECT * FROM "+NOMBRE_TABLA);
        // logica
        if(!dia.equals(""))
        {
            Log.w(TAG,"existe filtro de dia: "+dia);
            // si hay algo dentro de dia
            q1 = (" where "+C_DIA+" like '%"+dia+"%'");
            anterior = 1;
        }

        if(!mes.equals(""))
        {
            // si hay algo dentro de mes
            if(anterior == 1)
            {
                Log.w(TAG,"tambn existe filtro de mes: "+mes);
                q2 = (" AND "+C_MES+" like '%"+mes+"%'");
            }
            else
            {
                q2 = (" where "+C_MES+" like '%"+mes+"%'");
                Log.w(TAG,"existe filtro de mes: "+mes);
                anterior = 1;
            }
        }

        if(!año.equals(""))
        {
            // si hay algo dentro de año
            if(anterior == 1)
            {
                Log.w(TAG,"tambn existe filtro de año: "+año);
                q3 = (" AND "+C_AÑO+" like '%"+año+"%'");
            }
            else
            {
                Log.w(TAG,"existe filtro de año: "+año);
                q3 = (" where "+C_AÑO+" like '%"+año+"%'");
                anterior = 1;
            }
        }

        if(!estado.equals(""))
        {
            // si hay algo dentro de estado
            if(anterior == 1)
            {
                Log.w(TAG,"tambn existe filtro de estado: "+estado);
                q4 = (" AND "+C_SALUD+" like '%"+estado+"%'");
            }
            else
            {
                Log.w(TAG,"existe filtro de estado: "+estado);
                q4 = (" where "+C_SALUD+" like '%"+estado+"%'");
            }
        }

        //query = query.concat(q+q1+q2+q3+q4);
        query = query.concat(q+q1+q2+q3+q4);
        c = db.rawQuery(query,null);
        // Regresamos el cursor en su primera posicion
        if( c!= null)
            c.moveToFirst();
        return c;
    }

    public int actualizarDatos(String fechaAnterior, String fecha ,String año, String mes,
                               String dia, String nombre_dia,
                               String tiempo, String estado, String concentración)
    {
        //ACTUALIZAR A PARTIR DE LA FECHA

        Log.d(TAG,"ACTUALIZANDO DATOS");
        String fecha_corta = nombre_dia+" "+dia+" "+mes+" "+año;
        Log.d(TAG,"fecha_corta: "+fecha_corta);

        // CARGAR BASE DE DATOS PARA INICIAR LA EDICION
        db = getWritableDatabase();

        // CREAR UN OBJETO DE CONTENIDO PARA GUARDAR LOS DATOS
        ContentValues contenido = new ContentValues();
        contenido.put(C_FECHA,fecha);
        contenido.put(C_AÑO,año);
        contenido.put(C_MES,mes);
        contenido.put(C_DIA,dia);
        contenido.put(C_NDIA,nombre_dia);
        contenido.put(C_FECHA_CORTA,fecha_corta);
        contenido.put(C_TIEMPO,tiempo);
        contenido.put(C_SALUD,estado);
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

    public int eliminarDatos(String fecha) // ELIMINAR A PARTIR DE LA FECHA
    {
        Log.d(TAG,"ELIMINANDO DATO...");
        // CARGAR BASE DE DATOS PARA INICIAR LA EDIC
        db = getWritableDatabase();

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

    public int eliminarTodo() // ELIMINAR A PARTIR DE LA FECHA
    {
        Log.d(TAG,"ELIMINANDO DATO...");
        // CARGAR BASE DE DATOS PARA INICIAR LA EDIC
        db = getWritableDatabase();
        //editar contenido del querry aqui
        String nombre = NOMBRE_TABLA;
        String seleccion =null;
        String argselec[] = null;
        //querry listo para no editar
        int borrado = db.delete(nombre, seleccion, argselec);
        return borrado;
    }

    public Cursor recuperarPorDia(String Dia) throws SQLException
    {
        Log.w(TAG, Dia);
        Cursor cursor = null;
        db = getWritableDatabase();
        if (Dia == null  ||  Dia.length () == 0)
        {
            cursor = db.query(NOMBRE_TABLA, new String[] {C_ID,
                            C_MES,C_DIA,C_AÑO, C_TIEMPO, C_SALUD, C_CONCENTRACIÓN},
                    null, null, null, null, null);

        }
        else {
            cursor = db.query(true, NOMBRE_TABLA, new String[] {C_MES,C_DIA,C_AÑO,
                            C_TIEMPO, C_SALUD, C_CONCENTRACIÓN},
                    C_DIA + " like '%" + Dia + "%'", null,
                    null, null, null, null);
        }
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public String comprobar(String fecha) //Comprobar que no exista otro elemento con el mismo nombre
    {
        Log.d(TAG, "COMPROBANDO...");
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = C_ID;
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

    public String recuperarTodosAños()
    {
        Log.d(TAG, "RECUPERANDO TODOS LOS AÑOS");
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String año = C_AÑO;
        //datos a recuperar: año namas
        String [] columnas = {año};
        // selection
        String seleccion = null; //a apartir del
        String[] args_selec = null; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;
        // cursor para hacer el querry
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexAño = cursor.getColumnIndex(año);
        StringBuffer buffer = new StringBuffer();
        // armar el string para regresar los datos
        while (cursor.moveToNext())
        {
            String cAño = cursor.getString(indexAño);
            buffer.append(cAño + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

    public String recuperarTodosMeses()
    {
        Log.d(TAG, "RECUPERANDO TODOS LOS MESES");
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String mes = C_MES;
        //datos a recuperar: año namas
        String [] columnas = {mes};
        // selection
        String seleccion = null; //a apartir del
        String[] args_selec = null; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;
        // cursor para hacer el querry
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexAño = cursor.getColumnIndex(mes);
        StringBuffer buffer = new StringBuffer();
        // armar el string para regresar los datos
        while (cursor.moveToNext())
        {
            String cAño = cursor.getString(indexAño);
            buffer.append(cAño + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

    public String recuperarTodosDias()
    {
        Log.d(TAG, "RECUPERANDO TODOS LOS DIAS");
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String dia = C_DIA;
        //datos a recuperar: año namas
        String [] columnas = {dia};
        // selection
        String seleccion = null; //a apartir del
        String[] args_selec = null; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;
        // cursor para hacer el querry
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexAño = cursor.getColumnIndex(dia);
        StringBuffer buffer = new StringBuffer();
        // armar el string para regresar los datos
        while (cursor.moveToNext())
        {
            String cAño = cursor.getString(indexAño);
            buffer.append(cAño + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

    public String recuperarTodosEstados()
    {
        Log.d(TAG, "RECUPERANDO TODOS LOS ESTADOS");
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String salud = C_SALUD;
        //datos a recuperar: año namas
        String [] columnas = {salud};
        // selection
        String seleccion = null; //a apartir del
        String[] args_selec = null; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;
        // cursor para hacer el querry
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexSalud = cursor.getColumnIndex(salud);
        StringBuffer buffer = new StringBuffer();
        // armar el string para regresar los datos
        while (cursor.moveToNext())
        {
            String cSalud = cursor.getString(indexSalud);
            buffer.append(cSalud + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

    public String recuperarTodasFechas()
    {
        Log.d(TAG, "RECUPERANDO TODOS LAS FECHAS");
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String fecha = C_FECHA;
        //datos a recuperar
        String [] columnas = {fecha};

        // selection
        String seleccion = null; //a apartir del
        String[] args_selec = null; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        // posicion de cursores
        int indexFecha = cursor.getColumnIndex(fecha);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext())
        {
            String cFecha = cursor.getString(indexFecha);
            buffer.append(cFecha+"\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

    public String recuperarTodos()
    {
        Log.d(TAG, "RECUPERANDO TODOS LOS DATOS");
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = C_ID;
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

    public String recuperarDesdeHasta(long inicio, long fin) //Comprobar que no exista otro usuario con el mismo nombre
    {
        // CON LOS ID PARA HACER OPERACION LOGICA
        Log.d(TAG, "RECUPERANDO DESDE: "+inicio + " HASTA: "+fin);
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = C_ID;
        String fecha = C_FECHA;
        String concentración = C_CONCENTRACIÓN;
        //datos a recuperar
        String [] columnas ={fecha,concentración};

        Log.i(TAG,"REGRESA TODAS POR DEFAULT SI NO SE PIDE LO CONTRARIO");
        String seleccion; //regresa todas
        String[] args_selec; //regresa todas

        if(inicio > 0 && fin > 0)
        {
            // viene con filtros
            seleccion = id+">=? AND "+id+"<=?"; //a apartir del
            args_selec = new String[]{String.valueOf(inicio), String.valueOf(fin)}; //id entrante
            Log.i(TAG,"FILTROS DE INICIO Y FINAL");
        }

        else if(inicio > 0)
        {
            seleccion = id+">=?"; //a apartir del
            args_selec =new String[]{String.valueOf(inicio)}; //id entrante
            Log.i(TAG,"FILTRO DE INICIO");
        }

        else if(fin>0)
        {
            seleccion = id+"<=?"; //a apartir del
            args_selec =new String[]{String.valueOf(fin)}; //id entrante
            Log.i(TAG,"FILTRO DE FINAL");
        }
        else
        {
            seleccion = null;
            args_selec = null;
        }

        Log.d(TAG,"parametros "+seleccion+" "+ Arrays.toString(args_selec));

        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;

        //crear un cursor
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indexFecha = cursor.getColumnIndex(fecha);
        int IndexConcentración = cursor.getColumnIndex(concentración);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext())
        {
            String cFecha = cursor.getString(indexFecha);
            String cConcentración = cursor.getString(IndexConcentración);
            buffer.append(cFecha+"|" + cConcentración + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

    public String recuperarPorId(Long _id) //Comprobar que no exista otro usuario con el mismo nombre
    {
        Log.d(TAG, "RECUPERANDO NOMBRE POR ID... "+_id);
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = C_ID;
        String fecha = C_FECHA;
        String año = C_AÑO;
        String mes = C_MES;
        String dia = C_DIA;
        String nombre_dia =C_NDIA;
        String fecha_corta = C_FECHA_CORTA;
        String tiempo = C_TIEMPO;
        String salud = C_SALUD;
        String concentración = C_CONCENTRACIÓN;
        //datos a recuperar
        String [] columnas = {fecha, año, mes, dia, nombre_dia,fecha_corta, tiempo, salud,
                concentración};
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
        int indexSalud = cursor.getColumnIndex(salud);
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
            String cSalud = cursor.getString(indexSalud);
            String cConcentración = cursor.getString(IndexConcentración);
            buffer.append(cFecha+"|"+cAño + "*" +cMes + "+" +cDía + "="
                    +cNombreDia+"¿"+cFechaCorta+"&"+cTiempo + "<" + cSalud + ">" +
                    "!" + cConcentración + "\n");
        }
        Log.d(TAG,buffer.toString());
        return buffer.toString();
    }

    public long recuperarId(String Fecha) //Comprobar que no exista otro usuario con el mismo nombre
    {
        Log.d(TAG, "RECUPERANDO NOMBRE POR FECHA... "+Fecha);
        //cargar la base de datos
        db = getWritableDatabase();
        //crear strings para el metodo
        String NombreTabla = NOMBRE_TABLA;
        String id = C_ID;
        String fecha = C_FECHA;
        //datos a recuperar
        String [] columnas = {id};
        String seleccion = fecha + "=?"; //a apartir del
        String[] args_selec ={Fecha}; //id entrante
        //filtros
        String agrupar = null;
        String tener = null;
        String ordenar = null;

        //crear un cursor
        Cursor cursor = db.query(NombreTabla,columnas,seleccion,args_selec,agrupar,tener,ordenar);
        int indesId = cursor.getColumnIndex(id);

        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext())
        {
            String cId = cursor.getString(indesId);
            buffer.append(cId);
        }
        Log.d(TAG,buffer.toString());
        return Long.parseLong(buffer.toString());
    }
}