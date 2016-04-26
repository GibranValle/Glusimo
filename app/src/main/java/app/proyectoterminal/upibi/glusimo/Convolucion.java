package app.proyectoterminal.upibi.glusimo;

/**
 * Created by Gibran on 20/04/2016.
 */
public class Convolucion {


    public void Convolucion() {

    }

    public long[] convolution(long x[], long y[]) {
        int largoX;
        int largoY;
        int i, j, k, l;
        long v = 0;
        int offSet = 0;
        int min, max = 0;
        int largoFinal = 0;
        final String TAG = "Convolucion";
        long a[];
        long b[];

        //Log.d(TAG,"x: "+ Arrays.toString(x) + " y: "+ Arrays.toString(y));

        largoX = x.length;
        largoY = y.length;


        if (largoX > largoY) {
            min = largoY;
            max = largoX;
            a = y;
            b = x;

        } else {
            min = largoX;
            max = largoY;
            a = x;
            b = y;
        }
        //Log.d(TAG,"largo: "+ max + " largo: "+min + " a: "+Arrays.toString(a) +" b: "+Arrays.toString(b));

        if (min > 1) {
            largoFinal = max + min - 1;
        } else {
            largoFinal = 1;
        }
        long vectorIntermedio[][] = new long[largoFinal][largoFinal];

        //Log.d(TAG,"largoFinal: " +largoFinal);
        offSet = 0;

        for (i = 0; i <= min - 1; i++) {
            //Log.d(TAG,"i: "+i);
            for (j = 0; j <= max - 1; j++) {
                //Log.d(TAG,"j: "+j +" offset: "+offSet);
                //Log.d(TAG,"b: "+b[max-1-j] + " a: "+a[min-i-1]);

                vectorIntermedio[i][j + offSet] = b[max - 1 - j] * a[min - i - 1];
                //Log.d(TAG,""+vectorIntermedio[i][j+ offSet]);

            }
            offSet = offSet + 1;
        }

        long vectorFinal[] = new long[largoFinal];
        i = 0;

        for (k = 0; k <= largoFinal - 1; k++) {
            v = 0;
            for (l = 0; l <= max - 1; l++) {
                v = v + vectorIntermedio[l][k];
                //Log.d(TAG,"v: "+v);
            }
            vectorFinal[largoFinal - 1 - i] = v;
            i = i + 1;
        }
        //Log.d(TAG,"vector: "+Arrays.toString(vectorFinal));
        return vectorFinal;
    }

    public int[] convolution(int x[], int y[]) {
        int largoX;
        int largoY;
        int i, j, k, l;
        int v = 0;
        int offSet = 0;
        int min, max = 0;
        int largoFinal = 0;
        final String TAG = "Convolucion";
        int a[];
        int b[];

        //Log.d(TAG,"x: "+ Arrays.toString(x) + " y: "+ Arrays.toString(y));

        largoX = x.length;
        largoY = y.length;


        if (largoX > largoY) {
            min = largoY;
            max = largoX;
            a = y;
            b = x;

        } else {
            min = largoX;
            max = largoY;
            a = x;
            b = y;
        }
        //Log.d(TAG,"largo: "+ max + " largo: "+min + " a: "+Arrays.toString(a) +" b: "+Arrays.toString(b));

        if (min > 1) {
            largoFinal = max + min - 1;
        } else {
            largoFinal = 1;
        }

        int vectorIntermedio[][] = new int[largoFinal][largoFinal];

        //Log.d(TAG,"largoFinal: " +largoFinal);
        offSet = 0;

        for (i = 0; i <= min - 1; i++) {
            //Log.d(TAG,"i: "+i);
            for (j = 0; j <= max - 1; j++) {
                //Log.d(TAG,"j: "+j +" offset: "+offSet);
                //Log.d(TAG,"b: "+b[max-1-j] + " a: "+a[min-i-1]);

                vectorIntermedio[i][j + offSet] = b[max - 1 - j] * a[min - i - 1];
                //Log.d(TAG,""+vectorIntermedio[i][j+ offSet]);

            }
            offSet = offSet + 1;
        }

        int vectorFinal[] = new int[largoFinal];
        i = 0;

        for (k = 0; k <= largoFinal - 1; k++) {
            v = 0;
            for (l = 0; l <= max - 1; l++) {
                v = v + vectorIntermedio[l][k];
                //Log.d(TAG,"v: "+v);
            }
            vectorFinal[largoFinal - 1 - i] = v;
            i = i + 1;
        }
        //Log.d(TAG,"vector: "+Arrays.toString(vectorFinal));
        return vectorFinal;
    }
}