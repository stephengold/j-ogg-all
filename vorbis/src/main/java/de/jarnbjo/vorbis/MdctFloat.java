/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: MdctFloat.java,v 1.3 2003/04/10 19:49:04 jarnbjo Exp $
 * -----------------------------------------------------------
 *
 * $Author: jarnbjo $
 *
 * Description:
 *
 * Copyright 2002-2003 Tor-Einar Jarnbjo
 * -----------------------------------------------------------
 *
 * Change History
 * -----------------------------------------------------------
 * $Log: MdctFloat.java,v $
 * Revision 1.3  2003/04/10 19:49:04  jarnbjo
 * no message
 *
 * Revision 1.2  2003/03/16 01:11:12  jarnbjo
 * no message
 */
package de.jarnbjo.vorbis;

class MdctFloat {
    static private final float cPI3_8 = 0.38268343236508977175f;
    static private final float cPI2_8 = 0.70710678118654752441f;
    static private final float cPI1_8 = 0.92387953251128675613f;

    final private int n;
    final private int log2n;

    final private float[] trig;
    final private int[] bitrev;

    private float[] equalizer;

    final private float scale;

    private int itmp1, itmp2, itmp3, itmp4, itmp5, itmp6, itmp7, itmp8, itmp9;
    private float dtmp1, dtmp2, dtmp3, dtmp4, dtmp5, dtmp6, dtmp7, dtmp8, dtmp9;

    protected MdctFloat(int n) {
        bitrev = new int[n / 4];
        trig = new float[n + n / 4];

        int n2 = n >>> 1;
        log2n = (int) Math.rint(Math.log(n) / Math.log(2));
        this.n = n;

        int AE = 0;
        int AO = 1;
        int BE = AE + n / 2;
        int BO = BE + 1;
        int CE = BE + n / 2;
        int CO = CE + 1;
        // trig lookups...
        for (int i = 0; i < n / 4; i++) {
            trig[AE + i * 2] = (float) Math.cos((Math.PI / n) * (4 * i));
            trig[AO + i * 2] = (float) -Math.sin((Math.PI / n) * (4 * i));
            trig[BE + i * 2]
                    = (float) Math.cos((Math.PI / (2 * n)) * (2 * i + 1));
            trig[BO + i * 2]
                    = (float) Math.sin((Math.PI / (2 * n)) * (2 * i + 1));
        }
        for (int i = 0; i < n / 8; i++) {
            trig[CE + i * 2] = (float) Math.cos((Math.PI / n) * (4 * i + 2));
            trig[CO + i * 2] = (float) -Math.sin((Math.PI / n) * (4 * i + 2));
        }

        {
            int mask = (1 << (log2n - 1)) - 1;
            int msb = 1 << (log2n - 2);
            for (int i = 0; i < n / 8; i++) {
                int acc = 0;
                for (int j = 0; msb >>> j != 0; j++) {
                    if (((msb >>> j) & i) != 0) {
                        acc |= 1 << j;
                    }
                }
                bitrev[i * 2] = ((~acc) & mask);
                bitrev[i * 2 + 1] = acc;
            }
        }
        scale = 4.f / n;
    }

    private float[] tmpX = new float[1024];
    private float[] tmpW = new float[1024];

    protected void setEqualizer(float[] equalizer) {
        this.equalizer = equalizer;
    }

    protected float[] getEqualizer() {
        return equalizer;
    }

    protected synchronized void imdct(
            final float[] frq, final float[] window, final int[] pcm) {
        //, float[] out){
        float[] in = frq; //, out=buf;
        if (tmpX.length < n / 2) {
            tmpX = new float[n / 2];
        }
        if (tmpW.length < n / 2) {
            tmpW = new float[n / 2];
        }
        final float[] x = tmpX;
        final float[] w = tmpW;
        int n2 = n >> 1;
        int n4 = n >> 2;
        int n8 = n >> 3;

        if (equalizer != null) {
            for (int i = 0; i < n; i++) {
                frq[i] *= equalizer[i];
            }
        }

        // rotate + step 1
        {
            int inO = -1;
            int xO = 0;
            int A = n2;

            int i;
            for (i = 0; i < n8; i++) {
                dtmp1 = in[inO += 2];
                dtmp2 = in[inO += 2];
                dtmp3 = trig[--A];
                dtmp4 = trig[--A];
                x[xO++] = -dtmp2 * dtmp3 - dtmp1 * dtmp4;
                x[xO++] = dtmp1 * dtmp3 - dtmp2 * dtmp4;
            }

            inO = n2; //-4;

            for (i = 0; i < n8; i++) {
                dtmp1 = in[inO -= 2];
                dtmp2 = in[inO -= 2];
                dtmp3 = trig[--A];
                dtmp4 = trig[--A];
                x[xO++] = dtmp2 * dtmp3 + dtmp1 * dtmp4;
                x[xO++] = dtmp2 * dtmp4 - dtmp1 * dtmp3;
            }
        }

        float[] xxx = kernel(x, w, n, n2, n4, n8);
        int xx = 0;

        // step 8
        {
            int B = n2;
            int o1 = n4, o2 = o1 - 1;
            int o3 = n4 + n2, o4 = o3 - 1;

            for (int i = 0; i < n4; i++) {
                dtmp1 = xxx[xx++];
                dtmp2 = xxx[xx++];
                dtmp3 = trig[B++];
                dtmp4 = trig[B++];

                float temp1 = (dtmp1 * dtmp4 - dtmp2 * dtmp3);
                float temp2 = -(dtmp1 * dtmp3 + dtmp2 * dtmp4);

                pcm[o1] = (int) (-temp1 * window[o1]);
                pcm[o2] = (int) (temp1 * window[o2]);
                pcm[o3] = (int) (temp2 * window[o3]);
                pcm[o4] = (int) (temp2 * window[o4]);

                o1++;
                o2--;
                o3++;
                o4--;
            }
        }
    }

    private float[] kernel(
            float[] x, float[] w, int n, int n2, int n4, int n8) {
        // step 2
        int xA = n4;
        int xB = 0;
        int w2 = n4;
        int A = n2;

        for (int i = 0; i < n4;) {
            float x0 = x[xA] - x[xB];
            float x1;
            w[w2 + i] = x[xA++] + x[xB++];

            x1 = x[xA] - x[xB];
            A -= 4;

            w[i++] = x0 * trig[A] + x1 * trig[A + 1];
            w[i] = x1 * trig[A] - x0 * trig[A + 1];

            w[w2 + i] = x[xA++] + x[xB++];
            i++;
        }

        // step 3
        {
            for (int i = 0; i < log2n - 3; i++) {
                int k0 = n >>> (i + 2);
                int k1 = 1 << (i + 3);
                int wbase = n2 - 2;

                A = 0;
                float[] temp;

                for (int r = 0; r < (k0 >>> 2); r++) {
                    int w1 = wbase;
                    w2 = w1 - (k0 >> 1);
                    float AEv = trig[A], wA;
                    float AOv = trig[A + 1], wB;
                    wbase -= 2;

                    k0++;
                    for (int s = 0; s < (2 << i); s++) {
                        dtmp1 = w[w1];
                        dtmp2 = w[w2];
                        wB = dtmp1 - dtmp2;
                        x[w1] = dtmp1 + dtmp2;
                        dtmp1 = w[++w1];
                        dtmp2 = w[++w2];
                        wA = dtmp1 - dtmp2;
                        x[w1] = dtmp1 + dtmp2;
                        x[w2] = wA * AEv - wB * AOv;
                        x[w2 - 1] = wB * AEv + wA * AOv;

                        w1 -= k0;
                        w2 -= k0;
                    }
                    k0--;
                    A += k1;
                }

                temp = w;
                w = x;
                x = temp;
            }
        }

        // step 4, 5, 6, 7
        {
            int C = n;
            int bit = 0;
            int x1 = 0;
            int x2 = n2 - 1;

            for (int i = 0; i < n8; i++) {
                int t1 = bitrev[bit++];
                int t2 = bitrev[bit++];

                float wA = w[t1] - w[t2 + 1];
                float wB = w[t1 - 1] + w[t2];
                float wC = w[t1] + w[t2 + 1];
                float wD = w[t1 - 1] - w[t2];

                float wACE = wA * trig[C];
                float wBCE = wB * trig[C++];
                float wACO = wA * trig[C];
                float wBCO = wB * trig[C++];

                x[x1++] = (wC + wACO + wBCE) * 16383.0f;
                x[x2--] = (-wD + wBCO - wACE) * 16383.0f;
                x[x1++] = (wD + wBCO - wACE) * 16383.0f;
                x[x2--] = (wC - wACO - wBCE) * 16383.0f;
            }
        }
        return x;
    }
}
