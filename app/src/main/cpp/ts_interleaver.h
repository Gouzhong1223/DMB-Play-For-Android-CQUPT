#ifndef FILE_H_
#define FILE_H_

#include "defines.h"

struct InterLeaver {
    INT8U buffer[204];
    struct InterLeaver *next;
};

void interleave_init(void);
bool do_interleaver(INT8U *ts_buf_204);
#endif
