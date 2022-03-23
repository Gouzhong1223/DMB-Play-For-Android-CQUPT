#include "RSCode.h"

RSCode::RSCode(int _N, int _K, int _S) : N(_N), S(_S), K(_K) {
    alphaTo = new int[N + 1];
    indexOf = new int[N + 1];
    M = (int) log2(N + 1);
    T2 = N - K;
    T1 = T2 / 2;
    px = new int[T1 + 1];
    gx = new int[T2 + 1];
    syndrome = new int[T2 + 1];
    generatePx();
    generateGF();
    generateGx();

};

void RSCode::generatePx() {
    for (int i = 0; i <= M; i++) {
        px[i] = 0;
    }
    px[0] = px[2] = px[3] = px[4] = px[8] = 1;
}

void RSCode::generateGx() {
    int i, j;
    gx[0] = alphaTo[0];
    gx[1] = 1;
    for (i = 2; i <= T2; i++) {
        gx[i] = 1; /* 最高项 */
        for (j = i - 1; j > 0; j--) { /* 中间项 */
            if (gx[i] != 0) {
                gx[j] = gx[j - 1] ^ alphaTo[(indexOf[gx[j]] + i - 1) % N];
            } else {
                gx[j] = gx[j - 1];
            }
        }
        gx[0] = alphaTo[(indexOf[gx[0]] + i - 1) % N]; /* 常数项 */
    }
    /* 转化为log形式 */
    for (i = 0; i <= T2; i++) {
        gx[i] = indexOf[gx[i]];
    }
}

void RSCode::generateGF() {
    int i, mask = 1;
    alphaTo[M] = 0;
    for (i = 0; i < M; i++) {
        alphaTo[i] = mask;
        indexOf[alphaTo[i]] = i;
        if (px[i] != 0) {
            alphaTo[M] = alphaTo[M] ^ mask;
        }
        mask = mask << 1;
    }
    mask = mask >> 1;
    indexOf[alphaTo[M]] = M;
    for (i = M + 1; i < N; i++) {
        if (alphaTo[i - 1] >= mask) {
            alphaTo[i] = alphaTo[M] ^ ((alphaTo[i - 1] ^ mask) << 1);
        } else {
            alphaTo[i] = alphaTo[i - 1] << 1;
        }
        indexOf[alphaTo[i]] = i;
    }
    indexOf[0] = -1;
}

void RSCode::encode(uint8_t *msg, uint8_t *code) {
    int i, j;
    int feedback;
    int b[T2];
    for (i = 0; i < T2; i++) {
        b[i] = 0;
    }
    for (i = 0; i < S - T2; i++) {
        code[i] = msg[i];
        feedback = indexOf[msg[i] ^ b[T2 - 1]];
        if (feedback != -1) {
            for (j = T2 - 1; j > 0; j--) {
                if (gx[j] != -1) {
                    b[j] = b[j - 1] ^ alphaTo[(gx[j] + feedback) % N];
                } else {
                    b[j] = b[j - 1];
                }
            }
            b[0] = alphaTo[(gx[0] + feedback) % N];
        } else {
            for (j = T2 - 1; j > 0; j--) {
                b[j] = b[j - 1];
            }
            b[0] = 0;
        }
    }
    for (i = 0; i < T2; i++) {
        code[S - i - 1] = b[i];
    }
}

int RSCode::findSyndrome(uint8_t *recd, int *syndrome) {
    int i, j, error = 0;
    for (i = 1; i <= T2; i++) {
        syndrome[i] = 0;
        int product = 0;
        for (j = 0; j < S; j++) {
            if (recd[S - 1 - j] != 0) {
                syndrome[i] ^= alphaTo[(indexOf[recd[S - 1 - j]] + product) % N];
            }
            product += i - 1;
        }
        if (syndrome[i] != 0) {
            error = 1;
        }
        syndrome[i] = indexOf[syndrome[i]];

    }
    return error;
}

int RSCode::findErrLocPoly(int *syndrome, int *errLocPoly) {
    int i, j, q, u;
    int d[T2 + 2], l[T2 + 2], uLu[T2 + 2], elp[T2 + 2][T2];

    d[0] = 0;
    d[1] = syndrome[1];
    elp[0][0] = 0;/* log form */ elp[1][0] = 1; /* vector form */
    for (i = 1; i < T2; i++) {
        elp[0][i] = -1;
        elp[1][i] = 0;
    }
    l[0] = 0;
    l[1] = 0;
    uLu[0] = -1;
    uLu[1] = 0;

    for (u = 1; u <= T2 && l[u] <= T1; u++) {
        if (d[u] == -1) { /* $d_i = 0$ */
            l[u + 1] = l[u];
            for (i = 0; i <= l[u]; i++) {
                elp[u + 1][i] = elp[u][i];
                elp[u][i] = indexOf[elp[u][i]];
            }
        } else { /* $d_i != 0$ */
            /* 找到前面迭代的值 */
            q = u - 1;
            while (d[q] == -1 && q > 0) {
                q--;
            }
            if (q > 0) {
                for (j = q - 1; j > 0; j--) {
                    if (d[j] != -1 && uLu[q] < uLu[j]) {
                        q = j;
                    }
                }
            }

            /* 更新l_{i+1}的值 */
            if (l[u] > l[q] + u - q) {
                l[u + 1] = l[u];
            } else {
                l[u + 1] = l[q] + u - q;
            }

            /* 更新 */
            for (i = 0; i < T2; i++) {
                elp[u + 1][i] = 0;
            }
            for (i = 0; i <= l[q]; i++) {
                if (elp[q][i] != -1) {
                    elp[u + 1][i + u - q] = alphaTo[(d[u] + N - d[q] + elp[q][i]) % N];
                }
            }
            for (i = 0; i <= l[u]; i++) {
                elp[u + 1][i] ^= elp[u][i];
                elp[u][i] = indexOf[elp[u][i]];
            }
        }
        uLu[u + 1] = u - l[u + 1];

        /* 更新$d_i$ */
        if (u < T2) {
            if (syndrome[u + 1] != -1) {
                d[u + 1] = alphaTo[syndrome[u + 1]];
            } else {
                d[u + 1] = 0;
            }
            for (i = 1; i <= l[u + 1]; i++) {
                if (syndrome[u + 1 - i] != -1 && elp[u + 1][i] != 0) {
                    d[u + 1] ^= alphaTo[(syndrome[u + 1 - i] + indexOf[elp[u + 1][i]]) % N];
                }
            }
            d[u + 1] = indexOf[d[u + 1]];
        }
    }
    if (l[u] <= T1) {
        for (i = 0; i <= l[u]; i++) {
            errLocPoly[i] = indexOf[elp[u][i]];
        }
        errLocPoly[0] = 0;
        return l[u];
    }
    return -1;
}

int RSCode::findErrPos(int *errLocPoly, int errNum, int *errPos) {
    int i, j, q, count = 0;
    for (i = 1; i <= N; i++) {
        q = 1;
        for (j = 1; j <= errNum; j++) {
            if (errLocPoly[j] != -1) {
                errLocPoly[j] = (errLocPoly[j] + j) % N;
                q ^= alphaTo[errLocPoly[j]];
            }
        }
        if (q == 0) {
            errPos[count] = N - i;
            if (N - i >= S) {
                count = T2;
                break;
            }
            count++;
        }
    }
    return count;
}

void RSCode::findErrValue(int *errLocPoly, int errNum, int *errPos, int *syndrome, uint8_t *recd) {
    int i, j, degphi, q;
    int omega[T2 + 1], phi[T2 + 1], phiprime[T2 + 1], err[N], root[T2 + 1];

    for (i = 0; i <= T2; i++) {
        root[i] = 0;
        omega[i] = 0;
        phi[i] = 0;
        phiprime[i] = -1;
    }
    for (i = 0; i < errNum; i++) {
        root[i] = N - errPos[i];
    }

    syndrome[0] = 0;

    // for (j = 0; j <= errNum; j++)
    //     printf("%d\n",errLocPoly[j]);

    /* 计算z_0 */
    for (i = 0; i <= T2; i++) {
        for (j = 0; j <= errNum; j++) {
            if (i + j <= T2) {
                if (syndrome[i] != -1 && errLocPoly[j] != -1) {
                    omega[i + j] ^= alphaTo[(syndrome[i] + errLocPoly[j]) % N];
                }
            }
        }
    }
    for (i = 0; i <= T2; i++) {
        omega[i] = indexOf[omega[i]];
    }

    for (i = 0; i <= errNum; i++) {
        phi[i] = 0;
    }
    for (j = 0; j <= errNum; j++) {
        if (errLocPoly[j] != -1) {
            phi[j] ^= alphaTo[(errLocPoly[j]) % N];
        }
    }
    for (i = 0; i <= errNum; i++) {
        phi[i] = indexOf[phi[i]];
    }

    for (i = 0; i <= errNum; i++) {
        if (i % 2 != 0) {
            phiprime[i - 1] = phi[i];
        }
    }

    for (i = 0; i < errNum; i++) {
        // printf("%d\n",errPos[i]);
        err[errPos[i]] = 0;
        for (j = 0; j <= T2; j++) {
            if (omega[j] != -1 && root[i] != -1) {
                err[errPos[i]] ^= alphaTo[(omega[j] + j * root[i]) % N];
            }
        }


        if (err[errPos[i]] != 0 && errPos[i] != -1) {
            err[errPos[i]] = alphaTo[(indexOf[err[errPos[i]]] + errPos[i] * (2 + N)) % N];
        }

        if (err[errPos[i]] != 0) {
            err[errPos[i]] = indexOf[err[errPos[i]]];
            q = 0;
            for (j = 0; j <= errNum; j++) {
                if (phiprime[j] != -1 && root[i] != -1) {
                    q ^= alphaTo[(phiprime[j] + j * root[i]) % N];
                }
            }
            err[errPos[i]] = alphaTo[(err[errPos[i]] - indexOf[q] + N) % N];
            //printf("%d %d\n",CODE_LEN - 1 - errPos[i],err[errPos[i]]);
            recd[S - 1 - errPos[i]] ^= err[errPos[i]];
        }


    }
}

int RSCode::decode(uint8_t *recd, uint8_t *msg) {

    int syndrome[T2 + 1];
    int ret = findSyndrome(recd, syndrome);
    int i;
    if (ret != 0) {
        int errLocPoly[T2 + 1];
        int errPos[T2 + 1];
        int polyNum = findErrLocPoly(syndrome, errLocPoly);
        int errNum = findErrPos(errLocPoly, polyNum, errPos);
        if (errNum > T1) {
            for (i = 0; i < S - T2; i++) {
                msg[i] = recd[i];
            }
            return -1;
        }
        findErrValue(errLocPoly, errNum, errPos, syndrome, recd);
    }
    for (i = 0; i < S - T2; i++) {
        msg[i] = recd[i];
    }
    return 0;

}

RSCode::~RSCode() {
    delete[] alphaTo;
    delete[] indexOf;
    delete[] px;
    delete[] gx;
    delete[] syndrome;
}