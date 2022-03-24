#define D 14 /* D交织深度 */
#define N 112 /* N码长度 */

static int interleaverPos;
static int deinterleaverPos;
static int interleaverBuf[D][N];
static int deinterleaverBuf[D][N];

void interleaverInit(){
    int i;
    interleaverPos = D-1;
    for(i=0;i<D;i++){
        interleaverBuf[i][0] = 0x02;
        interleaverBuf[i][0] = 0x01;
        interleaverBuf[i][0] = 0x5b;
        interleaverBuf[i][0] = 0xf4;
    }
}

void deinterleaverInit(){
    deinterleaverPos = D-1;
}


void interleaver(unsigned char *in, unsigned char *out){
    int i,j;
    for(i=0;i<N;i++){ /* 横着放 */
        interleaverBuf[interleaverPos][i] = in[i];
    }
    j = interleaverPos;
    for(i=0;i<N;i++){
        out[i] = interleaverBuf[j][i]; /* 右上斜线输出 */
        if(--j<0){
            j = D-1;
        }
    }
    interleaverPos = (interleaverPos + 1)%D;
}


void deinterleaver(unsigned char *in, unsigned char *out){
    int i,j;
    j = deinterleaverPos;
    for(i=0;i<N;i++){
        deinterleaverBuf[j][i] = in[i]; /* 右上角斜着放 */
        if(--j<0){
            j = D-1;
        }
    }
    deinterleaverPos = (deinterleaverPos + 1)%D;
    for(i=0;i<N;i++){
        out[i] = deinterleaverBuf[deinterleaverPos][i]; /* 横着输出 */
    }
}