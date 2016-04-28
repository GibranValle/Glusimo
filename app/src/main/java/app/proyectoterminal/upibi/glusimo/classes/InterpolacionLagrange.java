package app.proyectoterminal.upibi.glusimo.classes;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Gibran on 24/04/2016.
 */
public class InterpolacionLagrange {

    public static final String TAG = "Interpolacion";

    public void InterpolacionLagrange() {

    }

    double calcularPunto(double[] coeficientes, double punto) {
        int largo = coeficientes.length - 1;
        int i;
        double resultado = 0;
        int potencia = 0;

        for (i = largo; i >= 0; i--) {
            if (i == largo) {
                resultado = resultado + coeficientes[i];
                Log.d(TAG, "xo: " + resultado);
            } else {
                potencia = largo - i;
                resultado = resultado + coeficientes[i] * Math.pow(punto, potencia);
                Log.d(TAG, "" + coeficientes[i] + "*x^" + potencia + " = " + punto * Math.pow(coeficientes[i], potencia) + " |x = " + punto);
            }
        }
        Log.d(TAG, "resultado final: " + resultado);
        return resultado;
    }

    double[] polyLagrange(int[] x, int[] y) {
        Convolucion conv = new Convolucion();
        int j, k, l;
        int largo = x.length;
        int n = largo - 1;
        long a[] = new long[n];
        long L[][] = new long[largo][largo];
        int xi, xj, xis;
        long v[];
        long v1[];
        long den = 0;
        double division = 0;
        double denv[] = new double[largo];
        int r = 0;
        int i1 = 0, i2 = 0;
        long poly[] = new long[largo];

        //Log.d(TAG, "realizando el polinomio de lagrange");

        //Log.d(TAG, "" + largo + "n:" + n);
        // Log.d(TAG, "vector glucosa: " + Arrays.toString(y));
        for (j = 0; j <= n; j++) {
            l = 0;
            for (k = 0; k <= n; k++) {
                if (j != k) {
                    //Log.d(TAG, "k: " + k + " j: " + j);
                    if (l == 0) {
                        i1 = k;
                        //Log.d(TAG, "k: " + k + " j: " + j + " i1: " + i1 + " i2: " + i2);
                    } else if (l == 1) {
                        i2 = k;
                        //Log.d(TAG, "k: " + k + " j: " + j + " i1: " + i1 + " i2: " + i2);
                    } else if (l == 2) {
                        xi = x[i1];
                        xis = x[i2];
                        xj = x[j];
                        den = (xj - xi) * (xj - xis);
                        //Log.d(TAG, "denominador inicial: "+ den);
                        v = new long[]{1, -xi};
                        v1 = new long[]{1, -xis};
                        //Log.d(TAG, "v: " + Arrays.toString(v) + " v1 " + Arrays.toString(v1));
                        a = conv.convolution(v, v1);
                        r = 1;
                        //Log.d(TAG, "xi: " + xi+ " xis: "+xis);
                        //Log.d(TAG, "vector: " + Arrays.toString(a));
                    }

                    if (r > 0) {
                        xi = x[k];
                        xj = x[j];
                        v1 = new long[]{1, -xi};
                        //Log.d(TAG, "v1 " + Arrays.toString(v1));
                        a = conv.convolution(a, v1);
                        //Log.d(TAG, "vecotr final:["+j+"]: " + Arrays.toString(a));
                        //Log.d(TAG,"j: "+j+" k: "+k);
                        den = den * (xj - xi);
                        //Log.d(TAG, "denominador aumentado: "+ den);
                    }
                    l = l + 1;

                } // fin if i=!j
            }// fin for k
            if (l == n) {
                //Log.d(TAG, "denominador final: "+ den);
                //Log.d(TAG, "vetor final: "+ Arrays.toString(a));
                denv[j] = den;
                //Log.d(TAG, "denominador["+j+"]: " + Arrays.toString(denv));
                L[j] = a;
                //Log.d(TAG, "vector["+j+"]: " + Arrays.deepToString(L));
            }
        }// fin for j


        double cociente;
        long numerador;
        long denominador;
        double L2[][] = new double[largo][largo];
        // for para multiplicar
        for (k = n; k >= 0; k--) {
            for (j = 0; j <= n; j++) {
                numerador = L[j][k];
                if (numerador != 0) {
                    denominador = (long) denv[j];
                    // Log.d(TAG,"k: "+k +" j: "+j);
                    //Log.d(TAG,"valor: "+numerador);
                    cociente = (double) numerador / denominador;
                    //Log.d(TAG,"numerador: "+numerador +" dif a 0");
                    //Log.d(TAG,"denominador: "+denominador);
                    //Log.d(TAG,"cociente: "+cociente);
                    L2[j][k] = cociente * y[j];
                }
            } // fir for j
            //Log.d(TAG, "nuevo polinomio: "+Arrays.deepToString(L2));
        }// fin for k


        double L3[] = new double[largo];
        double acumulado;
        double num = 0;
        for (k = n; k >= 0; k--) {
            acumulado = 0;
            //Log.d(TAG, "reiniciando acumulado: "+acumulado);
            for (j = 0; j <= n; j++) {
                num = L2[j][k];
                acumulado = num + acumulado;
                //Log.d(TAG, "acumulado parcial: "+acumulado);
            } // fir for j
            // Log.d(TAG, "acumulado final: "+acumulado);
            L3[k] = acumulado;
        }// fin for k
        Log.d(TAG, "coeficientes de polinomio: " + Arrays.toString(L3));
        return L3;
    }// fin metodo
}
