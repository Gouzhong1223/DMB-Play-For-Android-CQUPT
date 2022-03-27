#ifndef _MPEG_DEC_H_
#define _MPEG_DEC_H_

typedef struct {
    unsigned int pos;
    unsigned int num;
    unsigned int bits;
    unsigned int data;
} StreamBuffer;

typedef struct {
    unsigned int ID;
    unsigned int layer;
    unsigned int protection;
    unsigned int bitrate_index;
    unsigned int sampling_freq;
    unsigned int mode;
    unsigned int mode_extension;
    unsigned int copyright;
    unsigned int original_copy;
    unsigned int emphasis;
} AudioHeader;

typedef struct {
    unsigned char crc_word[4];
    unsigned int flag;
} CRC_WORDS;

typedef struct {
    unsigned int F_PAD_type;
    unsigned int F_PAD_type_ext;
    unsigned int X_PAD_Ind;
    unsigned int ByteL_Ind;
    unsigned int IH_Cmd_field;
    unsigned int M_S_flags;
    unsigned int Origin;
    unsigned int Serial_Cmd_field;
    unsigned int ByteL_data_field;
    unsigned int CI;
} F_PAD;

typedef struct {
    unsigned char Str[256];
    unsigned int num;
} TEXT;

typedef struct {
    unsigned int Toggle;
    unsigned int First_last;
    unsigned int Cmd_flag;
    unsigned int Length;
    unsigned int Cmd;
    unsigned int Charset;
    unsigned int SegNum;
} DynamicLabel;


typedef struct {
    uint8_t pXpadBuf[64];           // points to XPAD data buffer
    uint8_t pSegDataGroup[24];      // points to segment data group
    uint8_t pSegBuf[136];           // points to segment buffer (each segment has 17 bytes)
    // the b[6:0] of 1st byte is the segment length
    // the b[7] of 1st byte is CRC error flag
    uint8_t pDispBuf0[144];         // display buffer 0,
    uint8_t pDispBuf1[144];         // display buffer 1,
    uint8_t CINum;                  // 1 byte, the total CI number of the Variable XPAD field
    uint8_t VXpadDLabelStartByteNo; // 1 byte, the start byte No of D Label in a Variable XPAD
    uint8_t VXpadSubFieldLength;    // 1 byte, the num of bytes of the D Label in a variable XPAD
    uint8_t ToggleFlag;             // 1 byte, Toggle flag of a new Label
    uint8_t ToggleChangeFlag;       // 1 byte, Toggle change flag
    uint8_t FastenDispFlag;         // 1 byte, Fasten Display flag
    uint8_t Language;               // 1 byte, the language
    uint8_t SegNum;                 // 1 byte, the segment number
    uint8_t DataGroupByteNo;        // 1 byte, the byte No of a data group
    uint8_t DispBufId;              // 1 byte, Indicates the current display buffer
    uint8_t DispBufAddr;            // 1 byte, the current display address
    uint8_t DispBuf0ByteNum;        // 1 byte, the number of bytes of display buffer 0
    uint8_t DispBuf1ByteNum;        // 1 byte, the number of bytes of display buffer 1
} XPADS_SETTING, *XPADPS_SETTING;

#define FRACBITS        28
#define MUL(x, y)        ((x) >> 12) * ((y) >> 16)

void mpeg_dec_init(void);

int mpeg_dec(int &nch, int &sampling, int &PCM_Length, short *PCM_Frame, int Frame_Length,
             unsigned char *Audio_Frame, int &Label_flag, unsigned int &Str_Length, char *Str);

void DAB_XpadInit(XPADPS_SETTING psXpad);

uint8_t DAB_DlabelCrc(uint8_t *pStr, uint8_t ByteNum);

void DAB_DlabelSegDec(XPADPS_SETTING psXpad);

void DAB_UpdateXpadDispBuf(XPADPS_SETTING psXpad);

void DAB_ReadXpad(XPADPS_SETTING psXpad, int32_t nFrameLen, int32_t nTableId);

uint8_t DAB_GetXpadSubFieldLength(uint8_t ContentIndicator);

#endif//_MP2G_DEC_H_
