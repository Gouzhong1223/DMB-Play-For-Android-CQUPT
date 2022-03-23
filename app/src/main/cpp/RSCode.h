#ifndef RS_CODE_H
#define RS_CODE_H

#include <cstdint>
#include <cmath>
#include <iostream>

class RSCode{
private:
    int M; /* 有限域的二进制个数 */
    int N;
    int K;
    int S; /* 码长 */
    int T1;
    int T2;
    int *alphaTo;
    int *indexOf;
    int *px;
    int *gx;
    int *syndrome;
    /** 查表得到生成多项式，用于生成有限域 */
    void generatePx();
    /** 产生生成多项式 */
    void generateGx();
    /** 生成有限域 */
    void generateGF();
    int findSyndrome(uint8_t *recd, int *syndrome);
    int findErrLocPoly(int *syndrome, int *errLocPoly);
    int findErrPos(int *errLocPoly, int errNum, int *errPos);
    void findErrValue(int *errLocPoly, int errNum, int *errPos, int *syndrome, uint8_t *recd);

public:
    RSCode(int _N,int _K,int _S);
    ~RSCode();
    void encode(uint8_t *msg,uint8_t *code);
    int decode(uint8_t *recd,uint8_t *msg);

};

#endif //RS_CODE_H