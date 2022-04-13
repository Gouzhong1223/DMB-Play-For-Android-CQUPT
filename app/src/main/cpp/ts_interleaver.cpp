#include <cstdio>
#include "ts_interleaver.h"

struct InterLeaver inter_leaver[12];
struct InterLeaver *header, *p;


int frame_cnt;

void interleave_init() {
    int i;
    frame_cnt = 1;
    for (i = 0; i < 12 - 1; i++) {
        inter_leaver[i].next = &inter_leaver[i + 1];
    }
    inter_leaver[11].next = &inter_leaver[0];
    header = &inter_leaver[0];
}


bool do_interleaver(INT8U *ts_buf_204) {
    if (frame_cnt < 12) {
        frame_cnt++;
        return false;
    }
    for (int j = 0; j < 17; j++) {
        p = header;
        for (int i = 0; i < 12; i++) {
            ts_buf_204[i + j * 12] = p->buffer[i + j * 12];
            p = p->next;
        }
    }
    return true;
}
